package com.target.util

object ArgsParser extends ArgsParser {

  def parse(args: Array[String]): Map[String, String] = {

    logger.info("Parsing arguments...")

    val argsList = args.map(_.split("=", 2))
    val res = (for (i <- argsList) yield i(0) -> i(1)).toMap
    logger.info("Parsed arguments as Map: \n" + res.toString)
    res
  }
}

abstract class ArgsParser extends LazyLogging {
  def parse(args: Array[String]): Map[String, String]
}
