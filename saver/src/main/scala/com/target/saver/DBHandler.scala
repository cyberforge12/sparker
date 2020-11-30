package com.target.saver

import java.sql.DriverManager

import com.target.saver.Saver.{argsMap, logger}
import io.circe.Json
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, date_format, expr, schema_of_json, udf}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import io.circe.parser._

import scala.util.{Failure, Success, Try}

object DBHandler extends LazyLogging {

  private def checkUDF(s: String, schema: StructType): Boolean = {
    val json = parse(s) match {
      case Right(b) => b
    }
    val keys = json.asObject match {
      case Some(x) => x.keys.toList
    }
    for (col <- schema) {
      if (!keys.contains(col.name)) {
        return false
      }
    }
    true
  }

  def checkSchema(sparkSession: SparkSession, dataFrame: DataFrame, schema: StructType): DataFrame = {
    val check: UserDefinedFunction = udf(checkUDF(_: String, schema))
    dataFrame
      .withColumn("is_valid", check(col("req_body")))
  }

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
        value.createOrReplaceTempView("df")
        value.withColumn("short_date", date_format(col("date"), "yyyyMMdd"))
      }
      case Failure(exception) => {
        ErrorHandler.error(exception)
        sys.exit(1)
      }
    }
  }

  def updateDatabase(dataFrame: DataFrame) = {
    val res = dataFrame.filter(col("is_valid").contains(false))
      .select("id")
      .collect()
      .map(_.getInt(0).asInstanceOf[AnyRef])
    sendErrorToDatabse(res)
  }

  private def sendErrorToDatabse(records: Array[AnyRef]) = {
    logger.info("Updating " + records.length + " records in the database with error code 2...")
    val conn = DriverManager.getConnection(argsMap.getOrElse("conn_str", ""))
    try {
      val prep = conn.prepareStatement("UPDATE ? SET status = 2, err_msg = ? WHERE id = ANY (?)")
      val array = conn.createArrayOf("int4", records)
      prep.setString(1, "Invalid JSON")
      prep.setArray(2, array)
      prep.execute()
      logger.info(prep.getFetchSize + " records updated")
    }
    catch {
      case e: Exception => {
        logger.info("Failed updating records in the database. Error: " + e.toString)
      }
    }
    finally {conn.close()}
  }

}
