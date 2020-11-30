package com.target.loader

import com.target.util.LazyLogging
import scalaj.http._

object Sender extends LazyLogging {

  def send(str: String) = {

    val result = Http("http://" + Loader.argsMap.getOrElse("API", "")).postData(str)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    logger.info("Reply from http API: \n" +
      result.headers.map(data => s"${data._1}: ${data._2.toString()}").mkString("\n"))
  }

}
