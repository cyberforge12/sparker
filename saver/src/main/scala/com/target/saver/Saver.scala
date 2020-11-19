package com.target.saver

import java.io.{BufferedWriter, File, FileWriter}

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.avro.Schema
import org.apache.avro.data.Json
import org.apache.spark.sql.types.StructType
import net.liftweb.json._
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
  def getDFList(df: DataFrame): ListBuffer[DataFrame] = {
    val tmpJsonFile = "tmp.json"
    val listDF = ListBuffer[DataFrame]()
    val iter = df.toLocalIterator()
    while (iter.hasNext) {
      val file = new File(tmpJsonFile)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(iter.next().toString())
      bw.close()
      val df = Try(spark.read.json(tmpJsonFile))
      df match {
        case Success(value) => listDF += value
        case Failure(exception) => logger.error(exception.getMessage)
      }
    }
    listDF
  }

  def fixDfTypes(df: DataFrame): DataFrame = {
      df.withColumn("event_time", col("event_time").cast(LongType))
        .withColumn("transaction_amount", col("transaction_amount").cast(LongType))
  }

  //TODO:

  def saveDfToParquet(validDF: DataFrame) = {
    logger.info("Preparing parquet-file...")

  }

  //TODO:

  def sendErrorToDatabse(validDF: DataFrame) = {
    logger.info("Updating the record in the database with error code 2...")
  }

  def validateRecords(df: DataFrame, schema: StructType) = {

    val listDF = getDFList(df)
    for (i <- listDF) {
      val validDF = fixDfTypes(i)
      Try(DataFrameSchemaChecker.validateSchema(validDF, schema)) match {
        case Success(value) =>
          saveDfToParquet(validDF)
        case Failure(exception) =>
          sendErrorToDatabse(validDF)
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
      .option("dbtable", s"(SELECT req_body FROM $table WHERE status=0) tmp")

    Try(jdbc_reader.load()) match {
      case Success(value) =>
        logger.info("Successfully fetched " + value.count() + " records from database")
        value
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
