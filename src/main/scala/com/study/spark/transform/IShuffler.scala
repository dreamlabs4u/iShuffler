package com.study.spark.transform

import com.study.spark.conf.{IShufflerConfig, HasConfig}

case class IShuffler(config: IShufflerConfig) extends HasConfig with PreProcessor with Allocator {
  /**
    * Controls the Execution Flow with two Pluggable Phases
    *
    * 1. Pre-Processor : -
    *   Parses the input data, performs cleansing, evaluates Term Document Matrix.
    * 2. Allocator
    *   Evaluates SVD and logs the insights
    */
  def trigger = {
    val corpusInfo = doPreProcess()

    println(corpusInfo)

    allocate(corpusInfo)
  }
}
