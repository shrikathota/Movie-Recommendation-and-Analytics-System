package org.example

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType, TimestampType, DoubleType}

object StreamingAnalytics {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("MovieStreamingAnalytics")
      .master("local[*]")
      .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/movie_db.movie_stats")
      .getOrCreate()

    import spark.implicits._

    // Define the schema for the incoming rating data
    val schema = StructType(Array(
      StructField("userId", IntegerType),
      StructField("movieId", IntegerType),
      StructField("rating", DoubleType),
      StructField("timestamp", TimestampType)
    ))

    // Read from Kafka
    val kafkaStreamDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "movie-ratings")
      .load()

    val ratingsDF = kafkaStreamDF
      .selectExpr("CAST(value AS STRING) as value_string") // Add an alias here
      .select(from_csv($"value_string", schema, Map[String, String]()).as("data"))
      // Use the alias here
      .select("data.*")

    // --- Task 1: Store all raw ratings data permanently in HDFS ---
    val hdfsQuery = ratingsDF.writeStream
      .format("csv")
      .option("path", "hdfs://localhost:9000/movie-data/raw_ratings") // Your HDFS path
      .option("checkpointLocation", "/tmp/hdfs_checkpoint")
      .start()

    println("Streaming raw data to HDFS...")

    // --- Task 2: Calculate aggregates and store in MongoDB ---
    val movieStatsDF = ratingsDF
      .groupBy($"movieId")
      .agg(
        count("*").as("num_ratings"),
        avg("rating").as("avg_rating")
      )
      .withColumn("avg_rating", round($"avg_rating", 2))

    val mongoQuery = movieStatsDF.writeStream
      .outputMode("complete")
      .foreachBatch { (batchDF: org.apache.spark.sql.DataFrame, batchId: Long) =>
        println(s"Writing batch $batchId to MongoDB...")
        batchDF.write
          .format("mongo")
          .mode("append")
          .save()
      }

      .option("checkpointLocation", "/tmp/mongo_checkpoint")
      .start()

    println("Streaming aggregated data to MongoDB...")

    // Wait for the streams to terminate
    spark.streams.awaitAnyTermination()
  }
}
