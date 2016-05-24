package controllers

import java.io.{DataInputStream, File, FileInputStream}
import java.nio.file.Paths
import javax.inject._

import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.factory.Nd4j
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

@Singleton
class HomeController @Inject() extends Controller {
  val irisNet = loadNeuralNetwork("classifier.model")

  def index = Action {
    Ok("OK")
  }

  def cwd = Action {
    Ok(Paths.get("").toAbsolutePath.toString)
  }

  def iris = Action(parse.json) { request =>
    val json = request.body.as[JsObject]
    val input = for {
      c1 <- (json \ "c1").asOpt[Double]
      c2 <- (json \ "c2").asOpt[Double]
      c3 <- (json \ "c3").asOpt[Double]
      c4 <- (json \ "c4").asOpt[Double]
    } yield Array(c1, c2, c3, c4)

    input.map { data =>
      val inputVector = Nd4j.create(data)
      val outputVector = irisNet.output(inputVector)
      val label = Nd4j.argMax(outputVector, 1).getDouble(0)
      val output = (0 until outputVector.shape()(1)).map(outputVector.getDouble).toArray

      Ok(
        json +
        ("prediction" -> Json.toJson(label)) +
        ("probabilities" -> Json.toJson(output))
      )
    }.getOrElse {
      BadRequest("Invalid Input")
    }
  }

  private def loadNeuralNetwork(name: String): MultiLayerNetwork = {
    val params = {
      val path = s"../IrisService.Data.1.0.0.0/$name.bin"
      val dis = new DataInputStream(new FileInputStream(path))
      val ret = Nd4j.read(dis)
      dis.close()
      ret
    }

    val jsonConf = {
      val path = s"../TaskDetector.Data.1.0.0.0/$name.conf"
      val json = FileUtils.readFileToString(new File(path))
      MultiLayerConfiguration.fromJson(json)
    }

    new MultiLayerNetwork(jsonConf, params)
  }

}
