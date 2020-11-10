package com.target.loader

import java.util

import org.yaml.snakeyaml.Yaml

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

object ConfigParser extends LazyLogging {

  // IntelliJ Says this import isn't needed, but it won't compile without it.

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

  def parse(conf: String): util.LinkedHashMap[String, String] = {
    val yaml = new Yaml()
    val pattern = "(.+match: )(\")(.+)(\")"
    yaml.load(conf.replaceAll(pattern, "$1\'$3\'")).asInstanceOf[java.util.LinkedHashMap[String, String]]
  }

  def parseFile(filename: String): util.LinkedHashMap[String, String] = {
    logger.info(s"Parsing config file: $filename")
    parse(loadFromFile(filename))
  }
}
