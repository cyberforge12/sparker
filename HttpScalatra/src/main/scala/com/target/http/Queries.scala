package com.target.http

import java.sql.Connection

import com.target.util.LazyLogging

class Queries extends LazyLogging {
  def insertReqBody(body: String, conn: Connection) = {
    val prep = conn.prepareStatement("INSERT INTO task (status, req_body) VALUES (?, ?) ")
    prep.setInt(1, 0)
    prep.setString(2, body)
    prep.execute()
    //logger.info("Added success record to the database")
    "Success!"
  }
  def insertingErrorToDb(e: Throwable, conn: Connection) = {
    val prep = conn.prepareStatement("INSERT INTO task (status, err_msg) VALUES (?, ?) ")
    prep.setInt(1, 2)
    prep.setString(2, e.toString)
    prep.execute()
    "Success!"
    //logger.info("Added error record to the database. Error: " + e.toString)
  }
}
