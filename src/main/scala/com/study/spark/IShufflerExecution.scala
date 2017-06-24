package com.study.spark

import com.study.spark.conf.IShufflerConfig
import com.study.spark.transform.IShuffler

/**
  * The iShuffler is a Simple Document Clustering application classify the documents into different topics.
  * The whole execution goes through the following processes.
  *
  *    1. Parse the Raw Data Source (JSON Format)
  *    2. Clean the Dataset.
  *    3. Generate Term Document Matrix
  *    4. Apply the Clustering Algorithm (TF -IDF)
  *    5. Evaluate the Results.
  *
  *  and the implementation divides the process into the following divisions
  *    i. Preprocessing Step :  #1, #2, #3
  *   ii. Allocation Step : #4, #5
  */
object IShufflerExecution extends App {
  val iShuffler = IShuffler(IShufflerConfig(args))

  iShuffler.trigger
}