package com.study.spark.util

/**
  * Basic Utility Functions
  */
object Util {

  /** Read Contents of a file. */
  def readFile(path: String) = scala.io.Source.fromFile(path).getLines()

  /** Verify whether the String contains valid alphabets only*/
  def isOnlyLetters(str: String): Boolean = str.forall(c => Character.isLetter(c))

}
