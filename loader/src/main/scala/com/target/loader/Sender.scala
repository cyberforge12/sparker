package com.target.loader
import io.circe.Json
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList

import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

import scala.util.{Failure, Success, Try}

object Sender extends LazyLogging {

  def send(str: String) = {
    val post = new HttpPost(Loader.api)
    val nameValuePairs = new ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", str))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val client = new DefaultHttpClient
    Try(client.execute(post)) match {
      case Success(value) => logger.info("POST Success. " +
        "--- HEADERS ---" +
        value.getAllHeaders.foreach(arg => arg.toString))
      case Failure(exception) => logger.error("POST Error: " + exception.getMessage)
    }
  }
}
