/*
jdbc:postgresql://localhost:5432/task?user=postgres   OR any suitable jdbc
^ argument to pass for the main class
keystore/server.jks
keystore/server_truststore.jks
 */


package com.target.http

import com.target.util.LazyLogging

object Main extends App with LazyLogging {
  val conn_st = args(0)
  val certLocation = args(1)
  val servTrustLocation = args(2)
  val server = HttpServer.server(443)
//  val server = HttpServer.buildWebService(443, classOf[WebService])
  logger.info("Starting server")
//  server.start()
//  logger.info("Server started at " + server.getURI)
}
