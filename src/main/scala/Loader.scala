import io.circe.ParsingFailure
import javax.annotation.Nonnegative.Checker
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.parquet.ParquetUtils.FileTypes
import org.apache.spark.sql.types.{DataType, StructType}

import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.util.parsing.json.JSONArray

object Loader {

  var facts: String = ""
  var events: String = ""
  var validate: String = ""
  var api: String = ""
  var retry: Int = -1
  var timeout: Int = -1

  def parseArgs (args: Array[String]) = {
    var pair = scala.collection.mutable.Map("" -> "")
    val lst = args.map(_.split("="))
    for (i <- lst) {
      if (i.length == 2) {
        i(0) match {
          case "facts" => facts = i(1)
          case "events" => events = i(1)
          case "validate" => validate = i(1)
          case "API" => api = i(1)
          case "retry" => retry = i(1).toInt
          case "timeout" => timeout = i(1).toInt
          case _ => println("Incorrect option: " + i(0)); sys.exit(1)
        }
        println(i(0).toString + "===" + i(1).toString)
      }
      else
        println("Incorrect option: " + i(0).toString)
    }
  }

  def main(args: Array[String]): Unit = {

    val usage = """
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
      parseArgs(args)
    }
    else {
      print(usage)
      sys.exit(1)
    }
    val valid_map = ConfigParser.parseFile(validate)
    
    val schemaSource = Source.fromFile("schema.json").getLines().mkString
    val spark = SparkSession
      .builder()
      .appName("Java Spark SQL basic example")
      .config("spark.master", "local")
      .getOrCreate();
    val df_reader = spark.read.format("csv")
      .option("sep", ";")
      .option("inferSchema", "true")
      .option("header", "true")
    Try(df_reader.load(events)) match {
      case Success(value) => DataframeValidator.validate(value, Globals.FileTypesEnum.e_events)
      case Failure(exception) => ErrorHandler.error(exception)
    }
    Try(df_reader.load(facts)) match {
      case Success(value) => DataframeValidator.validate(value, Globals.FileTypesEnum.e_facts)
      case Failure(exception) => ErrorHandler.error(exception)
    }
  }
}
