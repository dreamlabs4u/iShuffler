package com.study.spark.transform

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import java.util.Properties

import org.apache.spark.ml.feature.{CountVectorizer, IDF}
import org.apache.spark.sql.{Column, ColumnName, Dataset}
import org.apache.spark.sql.functions._

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

import com.study.spark.conf.HasConfig
import com.study.spark.data.CorpusInfo
import com.study.spark.util.{Constants, Util}

/**
  * Controls the Preprocessing Phase
  */
trait PreProcessor extends HasConfig {

  /**
    * Read in the JSON Source data and convert it to Dataset[(document Title, Document Content)]
    *
    * @return Dataset[(document Title, Document Content)]
    */
  def readDocumentData: Dataset[(String, String)] = {
    import config.spark.implicits._

    config.log.info(
      s"""
        |**********Loading the Source Data from : ${config.srcPath}
      """.stripMargin
    )

    config
      .spark
      .read
      .json(config.srcPath)
      .select(config.documentTitle, config.documentContent)
      .as[(Array[String], Array[String])]
      .flatMap {case (a, b) => a.zip(b) }
  }

  /**
    * Read in the Stop File Words
    *
    * @param path - Stop File Location
    *
    * @return Set of Stop Words
    */
  def readStopWords(path: String = config.stopwordFile): Set[String] = Util.readFile(path).toSet

  def generateTerms(
    docs: Dataset[(String, String)] = readDocumentData,
    stopWords: Set[String] = readStopWords()
  ): Dataset[(String, Seq[String])] = {

    import config.spark.implicits._

    val broadcast = config.spark.sparkContext.broadcast(stopWords)

    docs.mapPartitions { iter =>
      val pipeline = getNLPPipeline()
      iter.map { case (title, contents) => (title, textToLemmas(contents, broadcast.value, pipeline)) }
    }
  }

  /**
    * Creates the Natural Language Processor Pipeline.
    *
    * @return NLPPipeline Object
    */
  def getNLPPipeline(): StanfordCoreNLP = {

    config.log.info("**********Initiates the NLPPipeline**********")

    val props = new Properties()
    props.put(Constants.NLP_ATTRIBUTE_ANNOTATOR, Constants.NLP_ATTRIBUTE_ANNOTATOR_VALUES)
    new StanfordCoreNLP(props)
  }

  /** Lemmitize the input string using the stop words */
  def textToLemmas(text: String, stopWords: Set[String], pipeline: StanfordCoreNLP): Seq[String] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences.asScala;
         token <- sentence.get(classOf[TokensAnnotation]).asScala) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma) && Util.isOnlyLetters(lemma)) {
        lemmas += lemma.toLowerCase
      }
    }
    lemmas
  }

  /**
    * Triggers the following processes : -
    *
    *    i. Read raw data
    *   ii. Cleanse Raw data.
    *  iii. Lemmitize cleansed data
    *   iv. Calculate term frequency
    *    v. Calculate Inverse Document Frequency
    *   vi. Document Term Matrix.
    **/
  def doPreProcess(
    terms: Dataset[(String, Seq[String])] = generateTerms()
  ): CorpusInfo = {

    import config.spark.implicits._

    val filteredTermsDF = terms
      .toDF(Constants.COLUMN_TITLE, Constants.COLUMN_TERMS)
      .where(size(new Column(Constants.COLUMN_TERMS)) > 1)

    val countVectorizer = new CountVectorizer()
      .setInputCol(Constants.COLUMN_TERMS)
      .setOutputCol(Constants.COLUMN_TERM_FREQUENCIES)
      .setVocabSize(config.numTerms)

    val vocabModel = countVectorizer.fit(filteredTermsDF)

    val docTermFreqs = vocabModel.transform(filteredTermsDF)

    val termIds = vocabModel.vocabulary

    docTermFreqs.cache()

    val docIds = docTermFreqs.rdd.map(_.getString(0)).zipWithUniqueId().map(_.swap).collect().toMap

    val idf = new IDF().setInputCol(Constants.COLUMN_TERM_FREQUENCIES).setOutputCol(Constants.COLUMN_TFIDF)
    val idfModel = idf.fit(docTermFreqs)
    val docTermMatrix = idfModel.transform(docTermFreqs).select(Constants.COLUMN_TITLE, Constants.COLUMN_TFIDF)

    CorpusInfo(docTermMatrix, termIds, docIds, idfModel.idf.toArray)
  }
}