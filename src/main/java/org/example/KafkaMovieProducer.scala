package org.example

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.io.Source

object KafkaMovieProducer {
  def main(args: Array[String]): Unit = {
    // Kafka Producer Configuration
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic = "movie-ratings"
    val ratingsFile = "data/ml-latest-small/ratings.csv"

    println(s"Starting to stream data from $ratingsFile to Kafka topic '$topic'...")

    try {
      // Read the CSV file line by line
      val source = Source.fromFile(ratingsFile)
      for (line <- source.getLines().drop(1)) {
        val key = line.split(",")(0)
        val record = new ProducerRecord[String, String](topic, key, line)
        producer.send(record)

        Thread.sleep(100)
      }
      source.close()
      println("Finished streaming data.")
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      producer.close()
    }
  }
}