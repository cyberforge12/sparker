package com.target.http

import java.sql.DriverManager

import com.target.http.Main.conn_st
import com.target.util.LazyLogging
import net.liftweb.json._
import org.scalatra.ScalatraServlet

import scala.util.{Failure, Success, Try}



class WebService extends ScalatraServlet with LazyLogging {
  get("/") {
    "Ez"
  }
  post("/handleJson") {
    logger.info("Incoming request...")
    val jsonString = request.body
    //parse Json Value
    val jValue = parse(jsonString)
    //Create common class for queries
    val queries = new Queries

    /*
Ladder structure: Get Connection - Success - Try to Insert ReqBody - Success - LogIt
                                                                   - Fail - Try to insert Error - Success - LogIt
                                                                                                - Fail - LogIt
                                    Close Connection
                                 - Fail - LogIt
     */
    val conn = Try(DriverManager.getConnection(conn_st))
    conn match {
      case Success(conn) => val res = Try(queries.insertReqBody(compactRender(jValue), conn))
      res match {
        case Success(v) => logger.info("Added success record to the database")
        case Failure(e) => val res = Try(queries.insertingErrorToDb(e, conn))
          res match {
            case Success(v) => logger.info("Added error record to the database. Error: " + e.toString);
            case Failure(e) => logger.info("Something went wrong: " + e.toString);
          }
      }
        conn.close()

      case Failure(e) => logger.info("Wrong connection to db argument " + e.toString)
    }
  }
  def writeDB(json: String) = {
    {
      logger.info("Incoming request...")
      //parse Json Value
      val jValue = parse(json)
      //Create common class for queries
      val queries = new Queries

      /*
  Ladder structure: Get Connection - Success - Try to Insert ReqBody - Success - LogIt
                                                                     - Fail - Try to insert Error - Success - LogIt
                                                                                                  - Fail - LogIt
                                      Close Connection
                                   - Fail - LogIt
       */
      val conn = Try(DriverManager.getConnection(conn_st))
      conn match {
        case Success(conn) => val res = Try(queries.insertReqBody(compactRender(jValue), conn))
          res match {
            case Success(v) => logger.info("Added success record to the database")
            case Failure(e) => val res = Try(queries.insertingErrorToDb(e, conn))
              res match {
                case Success(v) => logger.info("Added error record to the database. Error: " + e.toString);
                case Failure(e) => logger.info("Something went wrong: " + e.toString);
              }
          }
          conn.close()

        case Failure(e) => logger.info("Wrong connection to db argument " + e.toString)
      }
    }
  }
}




/*
CREATE TABLE task (
id BIGSERIAL PRIMARY KEY,
date TIMESTAMP DEFAULT NOW(),
status INT NOT NULL,
err_msg VARCHAR ( 50 ) DEFAULT NULL,
req_body TEXT DEFAULT NULL);
)
 */