package com.example

import java.io.{DataOutputStream, FileOutputStream, PrintStream}

import breeze.linalg.{DenseVector => BreezeVector, argmax => brzArgmax}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.linalg.{Vectors, Vector => SparkVector}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions


object Train {
  private val irisSchema = StructType(
    Array(
      StructField("c1", DoubleType, nullable = false),
      StructField("c2", DoubleType, nullable = false),
      StructField("c3", DoubleType, nullable = false),
      StructField("c4", DoubleType, nullable = false),
      StructField("label", IntegerType, nullable = false)
    )
  )

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("categorizer-train")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val rootLogger = Logger.getRootLogger
    rootLogger.setLevel(Level.INFO)

    val Array(inputArg, outputArg) = args

    val csvOptions = Map(
      "header" -> "false",
      "inferSchema" -> "false",
      "delimiter" -> ",",
      "escape" -> null,
      "parserLib" -> "univocity",
      "mode" -> "DROPMALFORMED"
    )
    
    val irisData = sqlContext.load(
      source = "com.databricks.spark.csv",
      schema = irisSchema,
      options = csvOptions + ("path" -> inputArg)
    ).cache()

    irisData.registerTempTable("iris")


    val irisPoints = sqlContext.sql(
      """
        |SELECT
        | c1,
        | c2,
        | c3,
        | c4,
        | label
        |FROM iris
      """.stripMargin
    ).map(row => {
      LabeledPoint(row.getInt(4), Vectors.dense(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)))
    }).cache()

    val Array(train, test) = irisPoints.randomSplit(Array(0.7, 0.3), 42)

    val irisNet = new MultiLayerNetwork(neuralNetConf())
    irisNet.init()
    irisNet.setListeners(new ScoreIterationListener())

    irisNet.setUpdater(null) // 0.4-rc3.8 workaround.

    var model = new SparkDl4jMultiLayer(sc, irisNet)

    for (i <- 0 until 10) {
      model = new SparkDl4jMultiLayer(sc, model.fit(train.toJavaRDD(), 32))
      println(s"Finished epoch $i.")
    }

    val dos = new DataOutputStream(new FileOutputStream(s"$outputArg.model.bin"))
    Nd4j.write(model.getNetwork.params(), dos)
    dos.close()

    val pos = new PrintStream(new FileOutputStream(s"$outputArg.model.conf"))
    pos.print(model.getNetwork.getLayerWiseConfigurations.toJson)
    pos.close()
  }

  private def neuralNetConf() = new NeuralNetConfiguration.Builder()
    .seed(42)
    .iterations(1)
    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    .learningRate(1e-1)
    .l1(0.01).regularization(true).l2(1e-3)
    .list(3)
    .layer(0, new DenseLayer.Builder().nIn(4).nOut(3)
      .activation("tanh")
      .weightInit(WeightInit.XAVIER)
      .build())
    .layer(1, new DenseLayer.Builder().nIn(3).nOut(2)
      .activation("tanh")
      .weightInit(WeightInit.XAVIER)
      .build())
    .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
      .weightInit(WeightInit.XAVIER)
      .activation("softmax")
      .nIn(2).nOut(3).build())
    .backprop(true).pretrain(false)
    .build()
}
