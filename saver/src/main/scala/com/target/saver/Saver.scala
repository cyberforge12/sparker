package com.target.saver

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._
import org.apache.avro.Schema
import org.apache.spark.sql.functions.schema_of_json
import org.apache.spark.sql.types.StructType

import net.liftweb.json._

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


  if (args.length == 3) {
    logger.info("Running Saver")
    parseArgs(args)
    val avroSchema = SchemaParser.getAvroSchemaFromFile(schema)

    val df = getDataframeFromDatabase
    validateUsingSQLFunctions(df)
    validateDataframe(df, SchemaParser.getSparkSchemaFromFile(schema))
  }
  else {
    print(usage)
    sys.exit(1)
  }

  def validateUsingClass(df: DataFrame, schema: StructType) = {
    logger.info("Validating dataframe with selected scheme using Checker.class")
    DataFrameSchemaChecker.validateSchema(df, schema)
  }

  def jsonToDataFrame(json: String, schema: StructType = null): DataFrame = {
    val reader = spark.read
    Option(schema).foreach(reader.schema)
    reader.json(spark.sparkContext.parallelize(Array(json)))
  }

  def getDfFromJsonString(json: String) = {
    logger.info("Getting dataframe from json string")
    import spark.implicits._
    spark.read.json(Seq(json).toDS)
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

  private def getDataframeFromDatabase: DataFrame = {

    val jdbc_reader = spark.read
      .format("jdbc")
      .option("url", conn_str)
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
}
