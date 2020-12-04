package com.target.loader
import javax.net.ssl.{HttpsURLConnection, SSLSocketFactory}
import com.target.util.LazyLogging
import javax.net.SocketFactory
import scalaj.http._
import nl.altindag.sslcontext.SSLFactory
import scalaj.http.Http
object Sender extends LazyLogging {

  def send(str: String) = {
/*
    val sslContextFactory = new SslContextFactory
    sslContextFactory.setKeyStorePath("keystore/client.jks")
    sslContextFactory.setKeyStorePassword("password")
    sslContextFactory.setTrustAll(true)

    sslContextFactory.setTrustStorePath("keystore/client_truststore.jks")
    sslContextFactory.setTrustStorePassword("password")

    val httpClient = new HttpClient(sslContextFactory)
    httpClient

    ^ джава стайл
 */


    val sslFactory = SSLFactory.builder()
      .withIdentity("keystore/client.jks", "password".toCharArray)
      .withTrustStore("keystore/client_truststore.jks", "password".toCharArray)
      .build()
    //^ собака не забирает сертификаты




    val httpOption: HttpOptions.HttpOption = {
      case httpsURLConnection: HttpsURLConnection =>
        httpsURLConnection.setHostnameVerifier(sslFactory.getHostnameVerifier)
        httpsURLConnection.setSSLSocketFactory(sslFactory.getSslContext.getSocketFactory)
      case _ =>
    }

    val result: HttpResponse[String] = Http("https://" + Loader.argsMap.getOrElse("API", "")).postData(str)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.sslSocketFactory(sslFactory.getSslContext.getSocketFactory))
      .option(HttpOptions.readTimeout(Loader.argsMap.getOrElse("timeout", "10000").toInt)).asString

    logger.info("Reply from http API: \n" +
      result.headers.map(data => s"${data._1}: ${data._2.toString()}").mkString("\n"))


  }



}
