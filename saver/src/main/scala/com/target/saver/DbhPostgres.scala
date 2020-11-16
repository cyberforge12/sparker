package com.target.saver

import java.sql.DriverManager

import scala.util.{Failure, Success, Try}

class DbhPostgres(con_st: String) extends LazyLogging {

  logger.info("Connecting to PostgreSQL database")
  val conn = Try(DriverManager.getConnection(con_st)) match {
    case Success(value) => value
    case Failure(exception) => ErrorHandler.error(exception); sys.exit(1)
  }

  def requestMessages(table: String) = {

    logger.info("Requesting data from table " + table)
    Try({
      val sth = conn.prepareStatement("SELECT req_body FROM ? WHERE status=0")
      sth.setString(1, table)
      sth.executeQuery()
    }) match {
      case Success(value) => value
      case Failure(exception) =>
        logger.error("Incorrect database request")
        conn.close()
        ErrorHandler.error(exception)
        sys.exit(1)
    }
  }
}
