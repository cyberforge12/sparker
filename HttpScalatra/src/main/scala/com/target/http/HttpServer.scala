package com.target.http
import com.target.http.Main.{certLocation, conn_st, servTrustLocation}

import javax.servlet.Servlet
import javax.net.ssl
import org.eclipse.jetty.server.{Connector, HttpConfiguration, HttpConnectionFactory, SecureRequestCustomizer, Server, ServerConnector, SslConnectionFactory}
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.webapp.WebAppContext

import java.io.{FileInputStream, InputStreamReader}
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, SSLServerSocket, TrustManagerFactory}
import scala.io.Source.fromInputStream

object HttpServer {

  def server(port: Int) = {
//    System.setProperty("javax.net.debug", "all")
    val passphrase = "password".toCharArray
    val ctx = SSLContext.getInstance("TLS")

    val kmf = KeyManagerFactory.getInstance("SunX509")
    val ks = KeyStore.getInstance("JKS")
    ks.load(new FileInputStream(certLocation), passphrase)
    kmf.init(ks, passphrase)

    val tmf = TrustManagerFactory.getInstance("SunX509")
    val ts = KeyStore.getInstance("JKS")
    ts.load(new FileInputStream(servTrustLocation), passphrase)
    tmf.init(ts)

    ctx.init(kmf.getKeyManagers, tmf.getTrustManagers, null)
    val ssf = ctx.getServerSocketFactory
    val serverSocket = ssf.createServerSocket(port).asInstanceOf[SSLServerSocket]
    serverSocket.setNeedClientAuth(true)
    while (true) {
      val socket = serverSocket.accept()
      val bufferSource = fromInputStream(socket.getInputStream)
      println(bufferSource.mkString)
    }
  }



  def buildWebService(port: Integer, webServiceClass: Class[_ <: Servlet]) = {
    val server: Server = new Server(port)

    /*
        val connector = new ServerConnector(server)
        connector.setPort(9999)

     */
    val https = new HttpConfiguration()
    https.addCustomizer(new SecureRequestCustomizer())
    val sslContextFactory = new SslContextFactory()

    sslContextFactory.setKeyStorePath(certLocation)
    sslContextFactory.setKeyStorePassword("password")
    sslContextFactory.setKeyManagerPassword("password")

    sslContextFactory.setTrustStorePath(servTrustLocation)
    sslContextFactory.setTrustStorePassword("password")
    sslContextFactory.setNeedClientAuth(true)


    val sslConn = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"),
      new HttpConnectionFactory(https))
    sslConn.setPort(port)
    server.setConnectors(Array(sslConn))


    val context: WebAppContext = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("/tmp")
    context.addServlet(webServiceClass, "/*")
    server.setHandler(context)
    //server.setConnectors()
    server
  }
}