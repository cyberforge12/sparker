package com.target.loader
import io.circe.Json
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList
import scalaj.http._

import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

import scala.util.{Failure, Success, Try}

object Sender extends LazyLogging {

  def send(str: String) = {

    val result = Http(Loader.api).postData(str)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    result.headers.foreach(println(_))
    }

}
