package com.target.saver

import java.io.{BufferedWriter, File, FileWriter}
import java.sql.{Connection, DriverManager, ResultSet}


import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.avro.Schema
import org.apache.avro.data.Json
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import scala.collection.mutable.ListBuffer

object Saver extends App with LazyLogging {

  var conn_str: String = ""
  var schema: String = ""
  var table: String = ""
  val usage =
    """
    Usage: Saver.jar args

        args (key=value ...):
          dbh       database connection string, including login/passwd
          schema    path to AVRO-scheme file
          table     POSTGRES table name containing messages
  """
  val spark = SparkSession
    .builder()
    .appName("Saver")
    .config("spark.master", "local")
    .getOrCreate()


  //TODO: removed saving and reading from file
  def getDFList(df: DataFrame): ListBuffer[(String, String, String, DataFrame, Int)] = {
    val tmpJsonFile = "tmp.json"
    val listDF = ListBuffer[(String, String, String, DataFrame, Int)]()
    val iter = df.toLocalIterator()
    while (iter.hasNext) {
      val file = new File(tmpJsonFile)
      val bw = new BufferedWriter(new FileWriter(file))
      val row = iter.next()
      val req_body = row.getAs[String]("req_body")
      bw.write(req_body)
      bw.close()
      val date = row.getAs[String]("short_date")
      val timestamp = row.getAs[java.sql.Timestamp]("date").toString
      val id = row.getAs[Int]("id")
      val df = Try(spark.read.json(tmpJsonFile))
      df match {
        case Success(value) => listDF += ((timestamp, date, req_body, value, id))
        case Failure(exception) => logger.error(exception.getMessage)
      }
    }
    listDF
  }

  def fixDfTypes(df: DataFrame): DataFrame = {
      df.withColumn("event_time", col("event_time").cast(LongType))
        .withColumn("transaction_amount", col("transaction_amount").cast(LongType))
  }

  //TODO: Уточнить у Игоря про разделение на слои и нужно ли создавать отдельные файлы под каждую запись

  def saveDfToParquet(date: String, srcString: String, incDF: DataFrame) = {
    logger.info("Preparing parquet-file...")
    val uuid = java.util.UUID.randomUUID.toString
    import spark.implicits._
    val srcDF = List((uuid, date, srcString)).toDF("uuid", "date", "req_body")

    srcDF.write
      .partitionBy("date")
      .parquet("saver-" + uuid + ".parquet")

    incDF.write
      .mode("append")
      .option("encoding", "UTF-16")
      .option("charset", "UTF-16")
      .parquet("saver-" + uuid + ".parquet")
  }

  //TODO:

  def sendErrorToDatabse(id: Int) = {
    logger.info("Updating the record in the database with error code 2...")
    val conn = DriverManager.getConnection(conn_str)
    try {
      val prep = conn.prepareStatement("UPDATE task SET status = 2, err_msg = ? WHERE id = ?")
      prep.setString(1, "Invalid JSON")
      prep.setInt(2, id)
      prep.execute()
      logger.info("Changed error string")
      println("Success!")
    }
    catch {
    case e: Exception => {
      val prep = conn.prepareStatement("INSERT INTO task (status, err_msg) VALUES (?, ?) ")
      prep.setInt(1, 2)
      prep.setString(2, e.toString)
      logger.info("Added error record to the database. Error: " + e.toString)
    }
    }
    finally {conn.close()}
  }

  def validateRecords(df: DataFrame, schema: StructType) = {

    val listDF = getDFList(df)
    for (i <- listDF) {
//      val validDF = fixDfTypes(i._3)
      val validDF = i._4
      Try(DataFrameSchemaChecker.validateSchema(validDF, schema)) match {
        case Success(value) =>
          saveDfToParquet(i._2, i._3, validDF)
        case Failure(exception) =>
          sendErrorToDatabse(i._5)
      }
    }
  }

  def getDfFromJsonString(json: String) = {
    logger.info("Getting dataframe from json string")
    import spark.implicits._
    val ds = Seq(json)
    spark.read
      .json(ds.toDS())
  }

  def getDfFromJsonFile(jsonFilename: String): DataFrame = {
    logger.info("Getting dataframe from json file")
    spark.read
      .json(jsonFilename)
  }

  def getDfFromJsonStringAvro(json: String) = {
    logger.info("Getting dataframe from json string")
    import spark.implicits._
    val avro = Json.parseJson(json)
    val ds = Seq(json)
    spark.read
      .json(ds.toDS())
  }

  private def parseArgs(args: Array[String]): Unit = {

    logger.info("Parsing CLI arguments")
    val lst = args.map(_.split("=", 2))
    for (i <- lst) {
      if (i.length == 2) {
        i(0) match {
          case "dbh" => conn_str = i(1)
          case "schema" => schema = i(1)
          case "table" => table = i(1)
          case _ => ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
        }
      }
      else
        ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
    }
    logger.info("Parsed arguments as dbh=" + conn_str + ", scheme=" + schema + ", table=" + table)
  }

  private def getDataframeFromDatabase: DataFrame = {

    val jdbc_reader = spark.read
      .format("jdbc")
      .option("url", conn_str)
      .option("dbtable", s"(SELECT id, date, req_body FROM $table WHERE status=0) tmp")

    Try(jdbc_reader.load()) match {
      case Success(value) =>
        logger.info("Successfully fetched " + value.count() + " records from database")
        value.withColumn("short_date", date_format(col("date"), "yyyyMMdd"))
      case Failure(exception) =>
        ErrorHandler.error(exception)
        sys.exit(1)
    }
  }

  spark.sparkContext.setLogLevel("ERROR")

  if (args.length == 3) {
    logger.info("Running Saver")
    parseArgs(args)

    val df = getDataframeFromDatabase
    validateRecords(df, SchemaParser.getSparkSchemaFromFile(schema))
  }
  else {
    print(usage)
    sys.exit(0)
  }
}
