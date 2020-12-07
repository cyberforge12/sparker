package com.target.loader
import com.target.util.{ArgsParser, LazyLogging}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Try

object Loader extends App with LazyLogging {

  val usage =
    """
    Usage: Logger.jar args

        args (key=value ...):
          facts     path to csv file with facts
          events    path to csv file with events
          validate  path to yml file with fields validation rules
          API       REST API ipv4 address
          retry     maximum number of retries
          timeout   timout between retries
          cacert    path to a keystore with trusted certificates
  """

  Sender.send(usage)

  val argsMap: Map[String, String] = {
    if (args.length == 7) {
      logger.info("Running Loader")
      ArgsParser.parse(args)
    }
    else {
      print(usage)
      sys.exit(0)
    }
  }

  val spark = SparkSession
    .builder()
    .appName("Loader")
    .config("spark.master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val df_reader = spark.read
    .format("com.databricks.spark.csv")
    .option("sep", ";")
    .option("header", "true")

  val df1: DataFrame = Try(DataframeValidator.validateEvents(df_reader
    .load(argsMap.getOrElse("events", "")), spark))
    .getOrElse(spark.emptyDataFrame)
  val df2: DataFrame = Try(DataframeValidator.validateFacts(df_reader
    .load(argsMap.getOrElse("facts", "")), spark))
    .getOrElse(spark.emptyDataFrame)
  if (!df1.isEmpty && !df2.isEmpty) DataframeValidator.validate(df1, df2).toJSON.foreach(Sender.send(_))
  else {logger.info("Validation is Empty")}

}
