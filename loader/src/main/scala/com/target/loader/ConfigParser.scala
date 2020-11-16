package com.target.loader

import java.util
import scala.collection.JavaConverters._

import org.yaml.snakeyaml.Yaml
import io.circe.yaml._
import java.util.LinkedHashMap

import io.circe.{Json, ParsingFailure}

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.Set
import scala.collection.JavaConversions._

class ConfigParser (filename: String) extends LazyLogging {
  val configMap: java.util.LinkedHashMap[String, String] = parseToLinkedHashMap(filename)
  val eventKeyColumns: Set[String] = getKeyColumns(Globals.eventTable)
  val factsKeyColumns: Set[String] = getKeyColumns(Globals.factsTable)
  val configLinkedHashMap = parseToLinkedHashMap(filename)
  val configClasses = parseToClass(filename)

  def getNullable(table: String) = {
    val tableMap = configMap("validate")
      .asInstanceOf[java.util.LinkedHashMap[String, String]]
      .get(table).asInstanceOf[java.util.LinkedHashMap[String, String]]
  }


  // IntelliJ Says this import isn't needed, but it won't compile without it.

  private def getKeyColumns(table: String): Set[String] = {
    val tableMap = configMap("validate")
      .asInstanceOf[java.util.LinkedHashMap[String, String]]
      .get(table)
      .asInstanceOf[java.util.LinkedHashMap[String, String]]
      .keySet().asScala
    tableMap
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
      case Failure(exception) => ErrorHandler.error(exception); sys.exit(0)
      case Success(value) => bufferContentsAsString(value)
    }
  }

  def getNullable(config: Json, table: String): List[String] = {
    val cursor = config.hcursor
    val obj = config.asObject
    val x = cursor.downField("validate").downField(table)
    List[String]()
  }

  private def fixYaml(yaml: String): String = {
    val pattern = "(.+match: )(\")(.+)(\")"
    yaml.replaceAll(pattern, "$1\'$3\'")
  }

  private def parseToLinkedHashMap(filename: String): java.util.LinkedHashMap[String, String] = {
    val yaml = fixYaml(loadFromFile(filename))
    val reader = new Yaml()
    reader.load(yaml)
  }

  private def parseToClass(filename: String): Map[String, Any] = {
    val circeADT = new CirceParser(fixYaml(loadFromFile(filename)), this)
    circeADT.value.tables
  }

  def parseToJson(filename: String): Json = {
    val yaml = loadFromFile(filename)
    val json: Either[ParsingFailure, Json] = parser.parse(fixYaml(yaml))
    json match {
      case Right(b) => b
      case Left(a) => ErrorHandler.error(new Exception(a)); sys.exit(1)
    }
  }
}
