name := """iris-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test

libraryDependencies += "org.nd4j" % "nd4j-x86" % "0.4-rc3.8"

libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "0.4-rc3.8"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
