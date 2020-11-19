package com.target.saver

import java.io.{BufferedWriter, File, FileWriter}

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.avro._
import org.apache.avro.{Schema, ValidateAll}
import org.apache.avro.data.Json
import org.apache.spark.sql.functions.{from_json, schema_of_json}
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

  def validateUsingClass(df: DataFrame, schema: StructType) = {
    logger.info("Validating dataframe with selected scheme using Checker.class")
    DataFrameSchemaChecker.validateSchema(df, schema)
  }

  def getDfFromJsonRDD(json: String, schema: StructType = null): DataFrame = {
    //    val reader = spark.read
    //    Option(schema).foreach(reader.schema)
    //    reader.json(spark.sparkContext.parallelize(Array(json)))

    val json2 = JsonParser.parse(json)
    val json3 = prettyRender(json2)
    var strList = Seq.empty[String]
    strList = strList :+ json3
    val rddData = spark.sparkContext.parallelize(strList)
    import spark.implicits._
    val ds = rddData.toDS

    //    val resultDF = spark.createDataFrame(rddData, schema)
    val resultDF = spark.read.option("multiline", "true").json(rddData)
    resultDF.show()
    resultDF
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

  def validateUsingSQLFunctions(df: DataFrame) = {
    import spark.implicits._

    import org.apache.spark.sql.functions.{lit, schema_of_json, from_json}
    import collection.JavaConverters._

    val schemaNewDF = Try(schema_of_json(lit(df.select("req_body").as[String].first)))
    //    newDF.withColumn("jsonData", from_json($"jsonData", schema, Map[String, String]().asJava))


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

  private def createDataframeWithAvroSchema(schema: Schema): DataFrame = {

    val jdbc_reader = spark.read
      .format("json")
      .option("url", conn_str)
      .option("avroSchema", schema.toString)
      .option("dbtable", s"(SELECT req_body FROM $table WHERE status=0) tmp")

    Try(jdbc_reader.load()) match {
      case Success(value) =>
        logger.info("Successfully fetched records from database")
        value
      case Failure(exception) =>
        ErrorHandler.error(exception)
        sys.exit(1)
    }
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

    val sparkSchema = SchemaParser.getSparkSchemaFromFile(schema)
    val df = getDataframeFromDatabase
    validateRecords(df, SchemaParser.getSparkSchemaFromFile(schema))
  }
  else {
    print(usage)
    sys.exit(1)
  }
}
