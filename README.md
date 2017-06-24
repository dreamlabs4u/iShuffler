
# iShuffler - A Simple Document Clustering App

A Simple Document Clustering Application to cluster the Documents feed in as JSON Data. The aim is to come up with the following insights :-

  1. Clusters the input document data.
  2. Ranks on any criteria. 
  3. Top Documents in the cluster. 
  4. Top Terms defining the cluster. 

## Software Stack

* **Language:** Scala, Spark
* **Build Tool:** SBT
* **Logger:** Log4j 
* **Statistics Library:** Spark mllib

## Problem Statement

A data set of 300 documents is provided with this challenge in a JSON file format. Use headline, abstract, searchabletext, 
prospect keywords, created date time and any other attribute to cluster these documents. You can use any unsupervised or semi-supervised 
techniques to cluster these docuemnts. Use appropriate NLP techniques such as Word2Vec, Word2Word, Word2Document, TF/IDF as required. 
Provide some type of ranking or score for the cluster as well as individual documents in the cluster. If possible propose title/topic 
for the clustered document based on text content of the cluster.

### Input Schema

A sample input dataset is available [here](src/main/resources/json/NLP-test.json) and the schema for the data is given below : -

```
|-- _shards: struct (nullable = true)
|    |-- failed: long (nullable = true)
|    |-- successful: long (nullable = true)
|    |-- total: long (nullable = true)
|-- hits: struct (nullable = true)
|    |-- hits: array (nullable = true)
|    |    |-- element: struct (containsNull = true)
|    |    |    |-- _id: string (nullable = true)
|    |    |    |-- _index: string (nullable = true)
|    |    |    |-- _score: long (nullable = true)
|    |    |    |-- _source: struct (nullable = true)
|    |    |    |    |-- @timestamp: string (nullable = true)
|    |    |    |    |-- @version: string (nullable = true)
|    |    |    |    |-- abstract: string (nullable = true)
|    |    |    |    |-- airdate: string (nullable = true)
|    |    |    |    |-- articletype: string (nullable = true)
|    |    |    |    |-- briefname: string (nullable = true)
|    |    |    |    |-- buzzresultid: string (nullable = true)
|    |    |    |    |-- commentator: string (nullable = true)
|    |    |    |    |-- commentatorid: long (nullable = true)
|    |    |    |    |-- companyname: string (nullable = true)
|    |    |    |    |-- country: string (nullable = true)
|    |    |    |    |-- datecreated: string (nullable = true)
|    |    |    |    |-- deliverysetname: string (nullable = true)
|    |    |    |    |-- displayname: string (nullable = true)
|    |    |    |    |-- entityname: string (nullable = true)
|    |    |    |    |-- fulltext: string (nullable = true)
|    |    |    |    |-- headline: string (nullable = true)
|    |    |    |    |-- hyperlink: string (nullable = true)
|    |    |    |    |-- mediaitemid: long (nullable = true)
|    |    |    |    |-- mediaoutletlogo: string (nullable = true)
|    |    |    |    |-- mediatype: string (nullable = true)
|    |    |    |    |-- mediatypeid: long (nullable = true)
|    |    |    |    |-- picturecount: string (nullable = true)
|    |    |    |    |-- picturefolder: string (nullable = true)
|    |    |    |    |-- profileurl: string (nullable = true)
|    |    |    |    |-- programid: long (nullable = true)
|    |    |    |    |-- programname: string (nullable = true)
|    |    |    |    |-- prospectkeyword: string (nullable = true)
|    |    |    |    |-- searchabletext: string (nullable = true)
|    |    |    |    |-- socialuserid: long (nullable = true)
|    |    |    |    |-- sourcename: string (nullable = true)
|    |    |    |    |-- sourcesystem: string (nullable = true)
|    |    |    |    |-- state: string (nullable = true)
|    |    |    |    |-- summaryid: string (nullable = true)
|    |    |    |    |-- twitterfollowers: long (nullable = true)
|    |    |    |    |-- twitterfollowing: long (nullable = true)
|    |    |    |    |-- twitterposts: long (nullable = true)
|    |    |    |    |-- username: string (nullable = true)
|    |    |    |-- _type: string (nullable = true)
|    |-- max_score: long (nullable = true)
|    |-- total: long (nullable = true)
|-- timed_out: boolean (nullable = true)
|-- took: long (nullable = true)
```

## Solution Approach

* **Model Selection** 

* **Approach**
 
 The application goes through the following phases to bring the cluster insights : -
  
  1. Parse the Raw Data Source (JSON Format)
  2. Clean the Dataset.
  3. Generate Term Document Matrix
  4. Apply the Clustering Algorithm (TF -IDF)
  5. Evaluate the Results.
 
The App is designed to work as pluggable components (Pre-processor and Allocator) and bringing in a new Clustering algorithm will be quite easy.

## Instructions to Run

* Pre-requisite 
    * Scala and Spark should be configured and installed
    * SBT should be configured and installed

* Steps 
    * Clone the Project
    * Go to the cloned location. 
    * Build using sbt  
                       
            sbt clean compile

    * Trigger the application.
            
            sbt run

    * Sample Output
            
            Rank        : 1
            Topic terms : aurora, fleeting, platt, encounter, fascinating
            Topic docs  : Travel, General News, Other, Other, General News
                 
            Rank        : 2
            Topic terms : jetstar, qantas, officer, business, travel
            Topic docs  : General News, General News, General News, Aviation, Aviation
                 
            Rank        : 3
            Topic terms : jetstar, hrdlicka, closely, corporate, want
            Topic docs  : General News, General News, Real Estate, Business News, General News
                 
            Rank        : 4
            Topic terms : million, chief, joyce, officer, executive
            Topic docs  : Aviation, Aviation, Aviation, General News, General News
                 
            Rank        : 5
            Topic terms : officer, hassell, thomas, john, virgin
            Topic docs  : Aviation, Aviation, Aviation, General News, Real Estate
                      
### Scope for Improvement

1. We can play around the different entities in the JSON Data to see whether optimised results can be obtained.
2. The Data preprocessor can be designed to use a persistent layer.   
3. Test cases have to be brought in for code coverage and confidence. 
4. Try out other clustering algorithms and compare the results   
    - https://rstudio-pubs-static.s3.amazonaws.com/79360_850b2a69980c4488b1db95987a24867a.html

### References

* https://spark.apache.org/docs/latest/mllib-clustering.html  
* https://en.wikipedia.org/wiki/Latent_semantic_analysis

