package com.target.loader
import java.io.{File, FileInputStream}
import java.net.http.HttpClient
import java.security.{KeyStore, SecureRandom}

import javax.net.ssl.{HttpsURLConnection, KeyManagerFactory, SSLContext, SSLSocketFactory, TrustManagerFactory}
import com.target.util.LazyLogging
import javax.net.SocketFactory
import scalaj.http._
import nl.altindag.sslcontext.SSLFactory
import org.eclipse.jetty.util.ssl.SslContextFactory
import scalaj.http.Http

import scala.tools.nsc.interactive.REPL.using
object Sender extends LazyLogging {

  def send(str: String) = {

    val sslContextFactory = new SslContextFactory
    sslContextFactory.setKeyStorePath("keystore/client.jks")
    sslContextFactory.setKeyStorePassword("password")
    sslContextFactory.setTrustAll(true)

    sslContextFactory.setTrustStorePath("keystore/client_truststore.jks")
    sslContextFactory.setTrustStorePassword("password")

    //^ оно вроде как должно работать но собирает null в итоге


    def getFactory(): SSLSocketFactory = {
      val ks = KeyStore.getInstance("JKS")

      ks.load(new FileInputStream(new File("keystore/client_truststore.jks")), "password".toCharArray)

      val kmf = KeyManagerFactory.getInstance("SunX509")
      kmf.init(ks, "password".toCharArray)
      val tmf = TrustManagerFactory.getInstance("SunX509")
      tmf.init(ks)
      //tmf.init(kss, null)
      val context = SSLContext.getInstance("TLS")
      context.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
      context.getSocketFactory
    }

    //^ а как сюда серт с доступными именами добавить - хз

    val result: HttpResponse[String] = Http("https://" + Loader.argsMap.getOrElse("API", "")).postData(str)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.sslSocketFactory(getFactory()))
      .option(HttpOptions.readTimeout(Loader.argsMap.getOrElse("timeout", "10000").toInt)).asString

    logger.info("Reply from http API: \n" +
      result.headers.map(data => s"${data._1}: ${data._2.toString()}").mkString("\n"))


  }



}
