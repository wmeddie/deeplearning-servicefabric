name := """iris-model"""

version := "1.0"

scalaVersion := "2.10.6"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.3.1" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.3.1" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.3.1" % "provided"

libraryDependencies += "org.nd4j" % "nd4j-x86" % "0.4-rc3.8"

libraryDependencies += "org.deeplearning4j" % "dl4j-spark" % "0.4-rc3.8" intransitive()

libraryDependencies += "org.deeplearning4j" % "deeplearning4j-scaleout-api" % "0.4-rc3.8"

libraryDependencies += "com.databricks" %% "spark-csv" % "1.3.0"

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

