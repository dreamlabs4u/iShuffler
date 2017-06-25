name := "iShuffler"

version := "1.0.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.0.0" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "2.0.0" % "provided",
  "org.apache.spark" % "spark-mllib_2.11" % "2.0.0" % "provided",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.6.2",
  "org.scalaz" %% "scalaz-core" % "7.2.13",
  "org.scalatest" %% "scalatest" % "3.0.1" % "provided"
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "hadoop", xs @ _*)        => MergeStrategy.first
  case PathList("org", "apache", "spark", xs @ _*)         => MergeStrategy.first
  case PathList("com", "google", xs @ _*)                  => MergeStrategy.first
  case PathList("org", "apache", xs @ _*)                  => MergeStrategy.first
  case PathList("org", "aopalliance", xs @ _*)             => MergeStrategy.first
  case PathList("javax", "xml", xs @ _*)                   => MergeStrategy.first
  case PathList("javax", "inject", xs @ _*)                   => MergeStrategy.first
  case PathList("com", "esotericsoftware", xs @ _*)        => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html"       => MergeStrategy.first
  case "application.conf"                                  => MergeStrategy.concat
  case "unwanted.txt"                                      => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)