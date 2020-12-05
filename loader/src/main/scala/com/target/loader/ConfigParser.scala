package com.target.loader

import com.target.util.{ErrorHandler, LazyLogging}
import io.circe.Json
import io.circe.yaml._
import org.yaml.snakeyaml.Yaml

import java.util
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.Set
import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

class ConfigParser (filename: String) extends LazyLogging {
  val configMap: java.util.LinkedHashMap[String, Object] = parseToLinkedHashMap(filename)
  val eventKeyColumns: Set[String] = getKeyColumns(Globals.eventTable)
  val factsKeyColumns: Set[String] = getKeyColumns(Globals.factsTable)
  val configLinkedHashMap: util.LinkedHashMap[String, Object] = parseToLinkedHashMap(filename)
  val configClasses: Map[String, Any] = parseToClass(filename)

  private def getKeyColumns(table: String): Set[String] = {
    configMap("validate")
      .asInstanceOf[java.util.LinkedHashMap[String, String]]
      .get(table)
      .asInstanceOf[java.util.LinkedHashMap[String, String]]
      .keySet().asScala
  }

  private def bufferContentsAsString(buffer: BufferedSource): String = {
    val contents = buffer.mkString
    buffer.close()
    contents
  }

  private def loadFromFile(filename: String) = {
    logger.info(s"Opening config file: $filename")
    Try {
      Source.fromFile(filename)
    } match {
      case Failure(exception) => ErrorHandler.fatal(exception); sys.exit(0)
      case Success(value) => bufferContentsAsString(value)
    }
  }

  private def fixYaml(yaml: String): String = {
    val pattern = "(.+match: )(\")(.+)(\")"
    yaml.replaceAll(pattern, "$1\'$3\'")
  }

  private def parseToLinkedHashMap(filename: String): java.util.LinkedHashMap[String, Object] = {
    val yaml = fixYaml(loadFromFile(filename))
    val reader = new Yaml()
    reader.load(yaml)
  }

  private def parseToClass(filename: String): Map[String, Any] = {
    new CirceParser(fixYaml(loadFromFile(filename)), this).value.tables
  }

  def parseToJson(filename: String): Json = {
    val yaml = loadFromFile(filename)
    parser.parse(fixYaml(yaml)) match {
      case Right(b) => b
      case Left(a) => ErrorHandler.fatal(new Exception(a)); sys.exit(1)
    }
  }
}
