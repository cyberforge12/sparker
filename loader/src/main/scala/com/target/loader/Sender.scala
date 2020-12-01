package com.target.loader

import com.target.util.LazyLogging
import scalaj.http._

object Sender extends LazyLogging {

  def send(str: String) = {

    val result: HttpResponse[String] = Http("http://" + Loader.argsMap.getOrElse("API", "")).postData(str)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(Loader.argsMap.getOrElse("timeout", "10000").toInt)).asString
    logger.info("Reply from http API: \n" +
      result.headers.map(data => s"${data._1}: ${data._2.toString()}").mkString("\n"))
  }

}
