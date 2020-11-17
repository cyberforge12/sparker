package com.target.saver

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.{DataFrame, SparkSession}

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

  if (args.length == 3) {
    logger.info("Running Saver")
    parseArgs(args)
    val avroSchema = new SchemaParser(schema).schema
    val df = getDataframe
    val dfSchema = new SchemaParser(df.schema.toString()).schema
    println(dfSchema)

  }
  else {
    print(usage)
    sys.exit(1)
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

  private def getDataframe: DataFrame = {
    val spark = SparkSession
      .builder()
      .appName("Saver")
      .config("spark.master", "local")
      .getOrCreate()
    val jdbc_reader = spark.read
      .format("jdbc")
      .option("url", conn_str)
      .option("dbtable", s"(SELECT * FROM $table WHERE status=0) tmp")

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
