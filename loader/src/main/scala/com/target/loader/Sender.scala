package com.target.loader
import com.target.util.LazyLogging
import org.eclipse.jetty.util.ssl.SslContextFactory
import scalaj.http.{Http, _}

import java.io._
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl._

object Sender extends LazyLogging {

  def send(str: String) = {

    System.setProperty("javax.net.debug", "all")

    val sslContextFactory = new SslContextFactory
    sslContextFactory.setKeyStorePath("keystore/client.jks")
    sslContextFactory.setKeyStorePassword("password")

    sslContextFactory.setTrustStorePath("keystore/client_truststore.jks")
    sslContextFactory.setTrustStorePassword("password")

    //^ оно вроде как должно работать но собирает null в итоге

    def createSSLContext(): SSLContext = {
      val ks = KeyStore.getInstance("JKS")
      val ts = KeyStore.getInstance("JKS")

      ks.load(new FileInputStream(new File("keystore/client.jks")), "password".toCharArray)
      ts.load(new FileInputStream(new File("keystore/client_truststore.jks")), "password".toCharArray)

      val kmf = KeyManagerFactory.getInstance("SunX509")
      kmf.init(ks, "password".toCharArray)
      val tmf = TrustManagerFactory.getInstance("SunX509")
      tmf.init(ts)

      val context = SSLContext.getInstance("TLS")
      context.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
      context
    }

    def getFactory(): SSLSocketFactory = {
      val context = createSSLContext()
      context.getSocketFactory
    }
    val socket = getFactory().createSocket("127.0.0.1", 443).asInstanceOf[SSLSocket]
    socket.startHandshake()

    val out: PrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream)))
    out.println("test")
    out.println("test1")
    out.println("test2")
    out.flush()
    out.close()


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
