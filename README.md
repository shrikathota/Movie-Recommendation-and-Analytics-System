# Movie Recommendation and Analytics System

A big data project that builds a real-time movie recommendation and analytics pipeline. This system provides personalized movie recommendations by processing and analyzing large-scale user review data with modern big data technologies.

---

## 📜 Table of Contents
* [Problem Statement](#-problem-statement)
* [Technologies Used](#-technologies-used)
* [Project Architecture](#-project-architecture)
* [Setup and Installation](#-setup-and-installation)
* [How to Run the Application](#-how-to-run-the-application)
* [Inference & Future Work](#-inference--future-work)

---

## 🎯 Problem Statement

The goal of this project is to build a Movie Recommendation System that can provide personalized movie recommendations based on user reviews and ratings. By leveraging big data technologies such as HDFS, Kafka, Spark, and MLlib, the system processes and stores large-scale movie review data and uses machine learning algorithms to generate accurate movie recommendations.

### Core Problems Addressed:
* Users often struggle to discover new movies they might enjoy, leading to reduced user engagement on streaming platforms.
* Existing systems may not scale effectively with large datasets (i.e., millions of user reviews and ratings).
* Real-time movie recommendations are needed, which can only be processed efficiently using big data frameworks.

---

## 🛠️ Technologies Used

* **Distributed Storage:** Hadoop HDFS
* **Real-Time Data Streaming:** Apache Kafka
* **Distributed Data Processing:** Apache Spark
* **Machine Learning:** Spark MLlib (for Collaborative Filtering)
* **Database:** MongoDB (for storing real-time analytics)
* **Programming Language:** Java
* **Build & Dependency Management:** Apache Maven

---

## 📁 Project Architecture

### File Structure
```
MovieAnalyticsPipeline/
├── .idea/
├── data/
│   └── ml-latest-small/
│       ├── links.csv
│       ├── movies.csv
│       ├── ratings.csv
│       ├── README.txt
│       └── tags.csv
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── example/
│   │   │           ├── KafkaMovieProducer.java
│   │   │           ├── MovieRecommender.java
│   │   │           └── StreamingAnalytics.java
│   │   └── resources/
│   └── test/
├── target/
├── .gitignore
└── pom.xml
```

### Source Code Overview
* `KafkaMovieProducer.java`: Reads the `ratings.csv` file and streams each user rating as a message to a Kafka topic.
* `StreamingAnalytics.java`: Consumes rating data from the Kafka topic in real-time, saves the raw data to HDFS, and calculates live aggregates (e.g., movie statistics) which are then stored in MongoDB.
* `MovieRecommender.java`: A batch processing job that reads the historical rating data from HDFS, trains a collaborative filtering model using MLlib's ALS algorithm, saves the trained model, and generates movie recommendations for a specified user.
* `pom.xml`: The Maven Project Object Model file. It defines project dependencies (Spark, Kafka, MLlib, MongoDB connector) and build configurations.

---

## ⚙️ Setup and Installation

These instructions are for setting up the environment on **Windows Subsystem for Linux (WSL)**.

### 1. Install Dependencies
Update your package lists and install Java, Maven, and other required tools.

```bash
# Update package lists
sudo apt update && sudo apt upgrade

# Install OpenJDK 11
sudo apt install openjdk-11-jdk

# Install Maven
sudo apt install maven
```

### 2. Install Big Data Tools

```bash
# Download and Extract Hadoop
wget [https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz](https://dlcdn.apache.org/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz)
tar -xvf hadoop-3.3.6.tar.gz

# Download and Extract Apache Spark
wget [https://dlcdn.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz](https://dlcdn.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz)
tar -xvf spark-3.5.1-bin-hadoop3.tgz

# Download and Extract Kafka
wget [https://downloads.apache.org/kafka/3.8.0/kafka_2.12-3.8.0.tgz](https://downloads.apache.org/kafka/3.8.0/kafka_2.12-3.8.0.tgz)
tar -xvf kafka_2.12-3.8.0.tgz

# Install MongoDB
curl -fsSL [https://www.mongodb.org/static/pgp/server-7.0.asc](https://www.mongodb.org/static/pgp/server-7.0.asc) | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] [https://repo.mongodb.org/apt/ubuntu](https://repo.mongodb.org/apt/ubuntu) jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt update
sudo apt install -y mongodb-org
```

### 3. Configure Environment Variables
Add the following lines to your `~/.bashrc` file to set the environment variables.

```bash
nano ~/.bashrc
```

Paste the following at the end of the file. **Remember to replace `/home/shrika/` with your actual home directory path.**

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export HADOOP_HOME=/home/shrika/hadoop-3.3.6
export SPARK_HOME=/home/shrika/spark-3.5.1-bin-hadoop3
export KAFKA_HOME=/home/shrika/kafka_2.12-3.8.0

export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export PATH=$PATH:$SPARK_HOME/bin
export PATH=$PATH:$KAFKA_HOME/bin

export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
```
Source the file to apply the changes: `source ~/.bashrc`.

### 4. Setup the Dataset
Download the [MovieLens Latest Small Dataset](https://grouplens.org/datasets/movielens/latest/) and place it in the project.

```bash
# Navigate to your project folder
cd ~/IdeaProjects/MovieAnalyticsPipeline 

# Create a data directory
mkdir data
cd data

# Copy the downloaded zip file from your Windows Downloads folder
# Replace 'shrik' with your Windows username
cp /mnt/c/Users/shrik/Downloads/ml-latest-small.zip .

# Unzip the dataset
unzip ml-latest-small.zip
```

---

## ▶️ How to Run the Application

Follow these steps in order to run the complete pipeline. Each command should be run in a **new terminal window**.

**1. Start Zookeeper Server**
```bash
zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
```

**2. Start Kafka Broker Server**
```bash
kafka-server-start.sh $KAFKA_HOME/config/server.properties
```

**3. Start Hadoop DFS**
```bash
start-dfs.sh
# You can verify that the NameNode and DataNode are running with the `jps` command.
```

**4. Create Kafka Topic**
Create a topic named `movie-ratings` where the producer will send data.
```bash
kafka-topics.sh --create --topic movie-ratings --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

**5. Start MongoDB**
```bash
sudo systemctl start mongod
```

**6. Run the Kafka Producer**
This will start reading `ratings.csv` and streaming the data to the Kafka topic.
```bash
# Navigate to the root of your project directory
cd ~/IdeaProjects/MovieAnalyticsPipeline

# Compile and run the Kafka producer
mvn compile exec:java -Dexec.mainClass="org.example.KafkaMovieProducer"
```

**7. Run the Spark Streaming Analytics Job**
While the producer is running, start the streaming consumer. This job will read from Kafka, write to HDFS, and update MongoDB.
```bash
# In a new terminal, navigate to the root of your project directory
cd ~/IdeaProjects/MovieAnalyticsPipeline

# Compile and run the Streaming Analytics job
mvn compile exec:java -Dexec.mainClass="org.example.StreamingAnalytics"
```

**8. Verify Data Ingestion**
You can check if the data is being saved correctly.

* **Check HDFS:**
    ```bash
    hdfs dfs -ls /movie_data/raw_ratings
    ```
* **Check MongoDB:**
    ```bash
    mongosh
    use movie_db
    db.movie_stats.find().pretty()
    ```

**9. Stop Streaming Jobs**
Once you have verified the data, you can stop the Kafka Producer and Spark Streaming jobs using `Ctrl+C` in their respective terminals.

**10. Run the ML Model Training Job**
This batch job reads all the data from HDFS to train the recommendation model.
```bash
# In a new terminal, navigate to the root of your project directory
cd ~/IdeaProjects/MovieAnalyticsPipeline

# Compile and run the Movie Recommender
mvn compile exec:java -Dexec.mainClass="org.example.MovieRecommender"
```
The terminal will display the final movie recommendations for the user specified in the code.

---

## 💡 Inference & Future Work

### Inference
* The system provides highly personalized recommendations using collaborative filtering, which can significantly improve user engagement.
* The architecture successfully demonstrates how big data technologies (Spark, Kafka, HDFS, MongoDB) can be integrated to build a scalable, real-time data processing pipeline that traditional systems cannot manage.

### Future Extensions
* **Hybrid Recommendation:** Combine collaborative filtering with content-based filtering (using movie genre, director, actors) for more accurate and diverse recommendations.
* **Real-Time Feedback Loop:** Incorporate user feedback on recommendations to dynamically update and improve the model in real-time.
* **Sentiment Analysis:** Use Natural Language Processing (NLP) on user reviews to factor sentiment into the recommendation logic.
* **Microservice Architecture:** Deploy the recommendation engine as a standalone microservice that can be integrated with various front-end applications or streaming platforms.

**Author:** Shrika Thota
