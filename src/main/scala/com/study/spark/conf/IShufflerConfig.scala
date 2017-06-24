package com.study.spark.conf

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Try

/**
  * The Configuration Object which holds all the Job Params
  *
  * @param args - Program arguments, if any
  *
  * Argument 1 : Spark Master
  * Argument 2 : Stopwords file Location
  * Argument 3 : Source File Location
  * Argument 4 : K
  * Argument 5 : Number of Terms
  * Argument 6 : Executor Memory
  * Argument 7 : Entity Corresponding to Document Title.
  * Argument 8 : Entity Corresponding to Document Content
  */
case class IShufflerConfig(args: Array[String]) {

  lazy val master = Try{args(0)}.toOption.getOrElse("local[*]")

  lazy val stopwordFile: String = Try{args(1)}.toOption.getOrElse("src/main/resources/stopwords.txt")

  lazy val srcPath: String = Try{args(2)}.toOption.getOrElse("./src/main/resources/json/NLP-test.json")

  lazy val k: Int = Try{args(3)}.toOption.getOrElse("100").toInt

  lazy val numTerms = Try{args(4)}.toOption.getOrElse("20000").toInt

  lazy val executerMemory = Try{args(5)}.toOption.getOrElse("2g")

  lazy val documentTitle =  Try{args(6)}.toOption.getOrElse("hits.hits._source.programname")

  lazy val documentContent = Try{args(7)}.toOption.getOrElse("hits.hits._source.abstract")

  @transient
  lazy val conf = new SparkConf().setAppName("iShuffler").setMaster(master).set("spark.executor.memory", executerMemory)

  @transient
  lazy val spark = SparkSession.builder().config(conf).getOrCreate()

  @transient
  lazy val sc = spark.sparkContext
}

/**
  *  Config Trait to Mix-in at different levels of Operation.
  */
trait HasConfig {
  val config: IShufflerConfig
}