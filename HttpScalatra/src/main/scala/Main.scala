object Main extends App {
  val server = HttpServer.buildWebService(8080, classOf[WebService])
  server.start()
}
