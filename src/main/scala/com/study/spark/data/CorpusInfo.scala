package com.study.spark.data

import org.apache.spark.sql.DataFrame

/**
  * Holds the Document Term Matrix details generated as a result of Preprocessing.
  *
  * @param docTermMatrix
  * @param termIds
  * @param docIds
  * @param termIdfs
  */
case class CorpusInfo(
  docTermMatrix: DataFrame,
  termIds: Array[String],
  docIds: Map[Long, String],
  termIdfs: Array[Double]
) {

  override def toString = {
    s"""
      | ===============
      | Corpus summary:
      | ===============
      |
      |   docIds   : ${docIds.toList.sortBy(_._1)}
      |
      |
      |   termIdfs : ${termIdfs}
    """.stripMargin
  }
}

case class Result(rank: Int, content: String, score: Double)
