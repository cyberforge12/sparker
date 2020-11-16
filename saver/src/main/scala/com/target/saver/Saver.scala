package com.target.saver

import scala.util.{Failure, Success, Try}

object Saver extends App with LazyLogging {

  var conn_str: String = ""
  var scheme: String = ""
  var table: String = ""
  val usage =
    """
    Usage: Saver.jar args

        args (key=value ...):
          dbh       database connection string, including login/passwd
          scheme    path to AVRO-scheme file
          table     POSTGRES table name containing messages
  """

  if (args.length == 3) {
    logger.info("Running Saver")
    parseArgs(args)
    val dbh = new DbhPostgres(conn_str)
    val results = dbh.requestMessages(table)
  }
  else {
    print(usage)
    sys.exit(1)
  }

  def parseArgs(args: Array[String]): Unit = {

    logger.info("Parsing CLI arguments")
    val lst = args.map(_.split("=", 2))
    for (i <- lst) {
      if (i.length == 2) {
        i(0) match {
          case "dbh" => conn_str = i(1)
          case "scheme" => scheme = i(1)
          case "table" => table = i(1)
          case _ => ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
        }
      }
      else
        ErrorHandler.error(new IllegalArgumentException("Incorrect option: " + i(0)))
    }
    logger.info("Parsed arguments as dbh=" + conn_str + ", scheme=" + scheme + ", table=" + table)
  }
}
