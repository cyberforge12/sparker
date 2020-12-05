package com.target.saver

import com.target.util.{ArgsParser, LazyLogging}
import org.apache.spark.sql.functions.{col, date_format, from_json}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}

object Saver extends App with LazyLogging {

  logger.info("Running Saver")

  val usage =
    """
    Usage: Saver.jar args

        args (key=value ...):
          conn_str  database connection string, including login/passwd
          schema    path to AVRO-scheme file
          table     POSTGRES table name containing messages
  """

  val argsMap: Map[String, String] = {
    if (args.length == 3) {
      ArgsParser.parse(args)
    }
    else {
      print(usage)
      sys.exit(0)
    }
  }

  val spark = SparkSession.builder()
    .appName("Saver")
    .config("spark.master", "local")
    .getOrCreate()

  def checkSchema(sparkSession: SparkSession, dataFrame: DataFrame, schema: StructType): Unit = {

    import sparkSession.implicits._
    val res: DataFrame = dataFrame
      .withColumn("value", from_json($"req_body", schema))
      .withColumn("short_date", date_format(col("date"), "yyyyMMdd"))
      .select("id", "short_date", "req_body", "value.*")

    val fail: Array[AnyRef] = res.filter(col("event_id").isNull)
      .select("id")
      .collect()
      .map(_.getInt(0).asInstanceOf[AnyRef])
    DBHandler.updateDatabase(fail, 2)

    val success: DataFrame = res.filter(col("event_id").isNotNull)
    DBHandler.saveDfToParquet(success)
    val successIDs: Array[AnyRef] = success.select("id")
      .collect()
      .map(_.getInt(0).asInstanceOf[AnyRef])
    DBHandler.updateDatabase(successIDs, 1)
  }

  val schema: StructType = SchemaParser.toStructType(argsMap.getOrElse("schema", ""))
  val df: DataFrame = DBHandler.getData(spark)
  checkSchema(spark, df, schema)
}
