package com.target.saver

import com.target.saver.Saver.argsMap
import com.target.util.{ErrorHandler, LazyLogging}
import org.apache.spark.sql.functions.{col, date_format}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.io.File
import java.sql.{Connection, DriverManager}
import scala.reflect.io.Directory
import scala.util.{Failure, Success, Try}

object DBHandler extends LazyLogging {

  def getData(sparkSession: SparkSession): DataFrame = {

    val table: String = argsMap.getOrElse("table", "")
    val conn_str: String = Saver.argsMap.getOrElse("conn_str", "")

    val jdbc_reader = sparkSession.read
      .format("jdbc")
      .option("url", conn_str)
      .option("dbtable", s"(SELECT id, date, req_body FROM $table WHERE status=0) tmp")

    Try(jdbc_reader.load()) match {
      case Success(value) => {
        logger.info("Successfully fetched records from database")
        value.createOrReplaceTempView("df")
        value.withColumn("short_date", date_format(col("date"), "yyyyMMdd"))
      }
      case Failure(exception) => ErrorHandler.fatal(exception)
    }
  }

  def updateDatabase(records: Array[AnyRef], status: Int): Unit = {
    if (records.length > 0) {
      val message: String = status match {
        case 1 => ""
        case 2 => "Invalid JSON"
      }
      logger.info("Updating " + records.length + " records in the database with error code 2...")
      val conn: Connection = DriverManager.getConnection(argsMap.getOrElse("conn_str", ""))
      Try {
        val prep = conn.prepareStatement("UPDATE task SET status = ?, err_msg = ? WHERE id = ANY (?)")
        val array = conn.createArrayOf("int4", records)
        prep.setInt(1, status)
        prep.setString(2, message)
        prep.setArray(3, array)
        prep.execute()
        logger.info(prep.getUpdateCount + " records updated")
      } match {
        case Success(value) => {}
        case Failure(e) => logger.info("Failed updating records in the database. Error: " + e.toString)
      }
      conn.close()
    }
  }

  def saveDfToParquet(df: DataFrame): Unit = {
    val filename = "saver.parquet"
    new Directory(new File(filename)).deleteRecursively()

    logger.info(s"Writing to $filename...")

    // SRC-layer
    df.coalesce(1)
      .select("id", "short_date", "req_body")
      .write
      .partitionBy("short_date")
      .parquet(filename)

    // INC-Layer
    df.coalesce(1)
      .drop("id", "req_body", "short_date")
      .write
      .mode("append")
      .option("encoding", "UTF-16")
      .option("charset", "UTF-16")
      .parquet(filename)

    logger.info(s"Done writing to $filename")
  }

}
