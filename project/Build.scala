import sbt._
import sbt.Keys._
import com.twitter.sbt._
import com.github.retronym.SbtOneJar.oneJarSettings

object FactorieServer extends Build {
  val finagleVersion = "5.0.0"

  lazy val thrifting1Build = Project(
    id = "factorie-server",
    base = file("."),
    settings = Project.defaultSettings ++ 
      StandardProject.newSettings ++
      oneJarSettings ++ 
      Seq(
        organization := "com.github.brianmartin",
        version := "0.1-SNAPSHOT",
        scalaVersion := "2.9.2",
        resolvers ++= Seq(
          "twitter-repo" at "http://maven.twttr.com"
        ),
        libraryDependencies ++= Seq(
          // Logging
          "ch.qos.logback" % "logback-classic" % "0.9.29",
          // Thrift
          "com.twitter" % "finagle-http" % "1.9.1",
          // JSON
          "net.liftweb" %% "lift-json" % "2.5-M1",
          "net.liftweb" %% "lift-common" % "2.5-M1",
          // Guava
          "com.google.guava" % "guava" % "13.0",
          // Testing
          "org.scalatest" %% "scalatest" % "1.8" % "test"
        ),
        publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
      )
  )
}

