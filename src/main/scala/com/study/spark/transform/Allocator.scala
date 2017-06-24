package com.study.spark.transform

import scalaz._, Scalaz._

import com.study.spark.util.Constants

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.linalg.{Matrix, SingularValueDecomposition, Vectors}
import org.apache.spark.mllib.linalg.distributed.RowMatrix

import com.study.spark.conf.HasConfig
import com.study.spark.data.{Result, CorpusInfo}

trait Allocator extends HasConfig {
  /**
    * Triggers the following Actions : -
    *
    *  i. Computes SVD
    * ii. Logs the final Insights based on ranking.
    *
    * @param corpusInfo - Contains the Preprocessing results (Term Document Matrix).
    */
  def allocate(corpusInfo: CorpusInfo) = {
    corpusInfo.docTermMatrix.cache()

    val vecRdd = corpusInfo
      .docTermMatrix.select(Constants.COLUMN_TFIDF)
      .rdd.map { row => Vectors.fromML(row.getAs[MLVector](Constants.COLUMN_TFIDF)) }

    vecRdd.cache()

    val mat = new RowMatrix(vecRdd)
    val svd = mat.computeSVD(config.k, computeU = true)

    val topTopicTerms = topTermsInTopTopic(svd, config.noTopics, config.numDocs, corpusInfo.termIds)
    val topTopicDocs = topDocsInTopTopic(svd, config.noTopics, config.numDocs, corpusInfo.docIds)

    logResults(topTopicTerms, topTopicDocs)

    config.sc.stop()
  }

  /** Logs the final Insights */
  def logResults(topConceptTerms: Seq[Seq[Result]], topConceptDocs: Seq[Seq[Result]]) = {
    val result = for ((terms, docs) <- topConceptTerms.zip(topConceptDocs)) yield {
      s"""
         |   Rank        : ${terms.headOption.cata(_.rank.toString, Constants.EMPTY_STR)}
         |   Topic terms : ${terms.map(_.content).mkString(Constants.SEPERATOR_COMMA)}
         |   Term Scores : ${terms.map(_.score).mkString(Constants.SEPERATOR_COMMA)}
         |   Topic docs  : ${docs.map(_.content).mkString(Constants.SEPERATOR_COMMA)}
         |
        """.stripMargin
    }

    config.log.info(result.mkString("\n"))

    persist(result)
  }

  def persist(result: Seq[String]) = {
    val hdfs = org.apache.hadoop.fs.FileSystem.get(config.sc.hadoopConfiguration)
    hdfs.delete(new org.apache.hadoop.fs.Path(config.outputFile), true)

    config.sc.parallelize(result).saveAsTextFile(config.outputFile)

    config.log.info(
      s"""
        | ************** Completed writing the Insights *****************
        |
        | Location : ${config.outputFile}
        |
      """.stripMargin
    )
  }

  /**
    * Get Top Terms in Top Topics
    *
    * @param svd - Singular Value Decomposition
    * @param noTopics - No of Topics
    * @param numTerms - No Terms
    * @param termIds - Termids
    * @return - Top Terms in Top Topics
    */
  def topTermsInTopTopic(
    svd: SingularValueDecomposition[RowMatrix, Matrix],
    noTopics: Int,
    numTerms: Int,
    termIds: Array[String]
  ): Seq[Seq[Result]] = {
    val v = svd.V
    val topTerms = new ArrayBuffer[Seq[Result]]()
    val arr = v.toArray
    for (i <- 0 until noTopics) {
      val offs = i * v.numRows
      val termWeights = arr.slice(offs, offs + v.numRows).zipWithIndex
      val sorted = termWeights.sortBy(-_._1)
      topTerms += sorted.take(numTerms).map {case (score, id) => Result(i+1, termIds(id), score)}
    }
    topTerms
  }

  /**
    * Get Top Docs in Top Topics
    *
    * @param svd - Singular Value Decomposition
    * @param noTopics - No of Topics
    * @param numDocs - No Terms
    * @param docIds - Termids
    * @return - Top Docs in Top Topics
    */
  def topDocsInTopTopic(
    svd: SingularValueDecomposition[RowMatrix, Matrix],
    noTopics: Int,
    numDocs: Int,
    docIds: Map[Long, String]
  ): Seq[Seq[Result]] = {
    val u  = svd.U
    val topDocs = new ArrayBuffer[Seq[Result]]()
    for (i <- 0 until noTopics) {
      val docWeights = u.rows.map(_.toArray(i)).zipWithUniqueId
      topDocs += docWeights.top(numDocs).map { case (score, id) => Result(i+1, docIds(id), score)}
    }
    topDocs
  }
}