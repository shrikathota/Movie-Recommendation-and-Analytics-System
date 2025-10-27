package org.example

import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.types.{IntegerType, DoubleType}

object MovieRecommender {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("MovieRecommender")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // --- 1. Load Training Data from HDFS ---
    val ratingsDF = spark.read
      .option("header", "false")
      .csv("hdfs://localhost:9000/movie-data/raw_ratings")
      .toDF("userId", "movieId", "rating", "timestamp")
      .withColumn("userId", $"userId".cast(IntegerType))
      .withColumn("movieId", $"movieId".cast(IntegerType))
      .withColumn("rating", $"rating".cast(DoubleType))
      .select("userId", "movieId", "rating")

    println("A few valid user IDs from the training data:")
    ratingsDF.select("userId").distinct().limit(5).show()
    println(s"Total ratings loaded from HDFS: ${ratingsDF.count()}")

    // --- 2. Train the Recommendation Model using ALS ---
    val als = new ALS()
      .setMaxIter(5)
      .setRegParam(0.01)
      .setUserCol("userId")
      .setItemCol("movieId")
      .setRatingCol("rating")

    println("Training the ALS model...")
    val model = als.fit(ratingsDF)
    model.setColdStartStrategy("drop") // Don't recommend to new users or for new items

    // --- 3. Save the Model to HDFS ---
    val modelPath = "hdfs://localhost:9000/movie-data/als_model"
    model.write.overwrite().save(modelPath)
    println(s"Model saved to $modelPath")

    // --- 4. Generate and Show Recommendations for 3 Sample Users ---
    println("\nTop 10 recommendations for 3 sample users:")

    // Get 3 distinct user IDs from the training data
    val sampleUsers = ratingsDF.select("userId").distinct().limit(3)

    // Generate recommendations for that subset of users
    val recommendations = model.recommendForUserSubset(sampleUsers, 10)

    // Load movie titles to make recommendations readable
    val moviesDF = spark.read
      .option("header", "true")
      .csv("file:///home/shrika/IdeaProjects/MovieAnalyticsPipeline/data/ml-latest-small/movies.csv")
      .withColumn("movieId", $"movieId".cast(IntegerType))

    recommendations
      .withColumn("recommendation", functions.explode($"recommendations"))
      .select($"userId", $"recommendation.movieId", $"recommendation.rating")
      .join(moviesDF, "movieId")
      .select($"userId", $"title", $"rating", $"genres")
      .sort($"userId", functions.desc("rating"))
      .show(false)

    spark.stop()
  }
}