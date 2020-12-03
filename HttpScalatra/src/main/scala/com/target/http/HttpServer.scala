package com.target.http
import com.target.http.Main.certLocation
import javax.servlet.Servlet
import org.eclipse.jetty.server.{Connector, HttpConfiguration, HttpConnectionFactory, SecureRequestCustomizer, Server, ServerConnector, SslConnectionFactory}
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.webapp.WebAppContext

object HttpServer {
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
    sslContextFactory.setKeyStorePassword("serverpass")
    sslContextFactory.setKeyManagerPassword("serverpass")
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