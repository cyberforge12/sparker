package http
import javax.servlet.Servlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object HttpServer {
  def buildWebService(port: Integer, webServiceClass: Class[_ <: Servlet]) = {
    val server: Server = new Server(port)
    val context: WebAppContext = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("/tmp")
    context.addServlet(webServiceClass, "/*")
    server.setHandler(context)
    server
  }
}