/*
jdbc:postgresql://localhost:5432/task?user=postgres   OR any suitable jdbc
^ argument to pass for the main class
 */


package http

object Main extends App with LazyLogging {
  val conn_st = args(0)
  val server = HttpServer.buildWebService(8080, classOf[WebService])
  logger.info("Starting server")
  server.start()
  logger.info("Server started at " + server.getURI)
}
