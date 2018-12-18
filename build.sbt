name := "kafkabank-balance-exactlry-once"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions in Compile ++= Seq(
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlog-reflective-calls",
  "-Xlint")

javacOptions in Compile ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:unchecked",
  "-Xlint:deprecation")


// https://mvnrepository.com/artifact/com.lightbend/kafka-streams-scala
libraryDependencies += "com.lightbend" %% "kafka-streams-scala" % "0.2.1"

//libraryDependencies += "com.lightbend" %% "kafka-clients-scala" % "0.2.1"

// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams
//libraryDependencies += "org.apache.kafka" % "kafka-streams" % "0.10.0.0"


// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25"

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"

