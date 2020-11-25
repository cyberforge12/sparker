package com.target.loader

import java.util
import java.util.Properties

import org.apache.log4j.PropertyConfigurator
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConversions.mapAsScalaMap
import scala.util.{Failure, Success, Try}

object Loader extends LazyLogging {

  var facts: String = ""
  var events: String = ""
  var validate: String = ""
  var api: String = ""
  var retry: Int = -1
  var timeout: Int = -1

  def parseArgs(args: Array[String]): Unit = {

    logger.info("Parsing CLI arguments")
    val lst = args.map(_.split("="))
    for (i <- lst) {
      if (i.length == 2) {
        i(0) match {
          case "facts" => facts = i(1)
          case "events" => events = i(1)
          case "validate" => validate = i(1)
          case "API" => api = "http://" + i(1)
          case "retry" => retry = i(1).toInt
          case "timeout" => timeout = i(1).toInt
          case _ => ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
        }
      }
      else
        ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
    }
  }

  def main(args: Array[String]): Unit = {

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
  """

    if (args.length == 6) {
      logger.info("Running Loader")
      parseArgs(args)
    }
    else {
      print(usage)
      sys.exit(0)
    }
    val configMap = new ConfigParser(validate).configLinkedHashMap
    val validateMap = configMap.getOrDefault("validate", "").asInstanceOf[util.LinkedHashMap[String, Object]]
    val eventsMap = validateMap.getOrDefault("event", "").asInstanceOf[util.LinkedHashMap[String, Object]]
    val factsMap = validateMap.getOrDefault("ext_fact", "").asInstanceOf[util.LinkedHashMap[String, Object]]
    val eventDt_map = eventsMap.getOrDefault("event_dt", "").asInstanceOf[util.LinkedHashMap[String, Object]]
    println(eventDt_map)

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
    var df1: DataFrame = spark.emptyDataFrame
    var df2: DataFrame = spark.emptyDataFrame
    var resFrame: DataFrame = spark.emptyDataFrame
    try {
      df1 = DataframeValidator.validateEvents(df_reader.load(events))
    }
    catch {
      case e: Exception => ErrorHandler.error(e)
    }

    try {
      df2 = DataframeValidator.validateFacts(df_reader.load(facts))
    }
    catch {
      case e: Exception => ErrorHandler.error(e)
    }
    if (!df1.isEmpty && !df2.isEmpty) resFrame = DataframeValidator.validate(df1, df2)
    resFrame.toJSON.foreach(Sender.send(_))
  }
}
