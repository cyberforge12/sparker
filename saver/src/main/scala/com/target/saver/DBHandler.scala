package com.target.saver

import com.target.saver.Saver.argsMap
import org.apache.spark.sql.functions.{col, date_format}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

object DBHandler extends LazyLogging {

  def getData(sparkSession: SparkSession): DataFrame = {

    val table = argsMap.getOrElse("table", "")
    val conn_str = Saver.argsMap.getOrElse("conn_str", "")

    val jdbc_reader = sparkSession.read
      .format("jdbc")
      .option("url", conn_str)
      .option("dbtable", s"(SELECT id, date, req_body FROM $table WHERE status=0) tmp")

    Try(jdbc_reader.load()) match {
      case Success(value) => {
        logger.info("Successfully fetched records from database")
        value.withColumn("short_date", date_format(col("date"), "yyyyMMdd"))
      }
      case Failure(exception) => {
        ErrorHandler.error(exception)
        sys.exit(1)
      }
    }
  }

}
