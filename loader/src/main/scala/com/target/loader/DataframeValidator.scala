package com.target.loader

import com.target.loader.Globals.columnsForJson
import com.target.util.LazyLogging
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable.ListBuffer

object DataframeValidator extends LazyLogging {

  val validator = new Validate()


//Global lists for Spark's executor to build valid DFs
  val validExtList = ListBuffer[Row]()
  val errorExtList = ListBuffer[Row]()
  val validEventList = ListBuffer[Row]()
  val errorEventList = ListBuffer[Row]()


  def validateFacts(df: DataFrame, spark: SparkSession): DataFrame = {
    logger.info(s"Validating facts dataframe")
    val schema = df.schema

    df.foreach{row => parseRow(row, validExtList, errorExtList, validator.ext_vals)}
    val errorExtDf: DataFrame = spark.createDataFrame(spark.sparkContext.parallelize(errorExtList.toSeq), schema)
    errorExtDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("overwrite")
      .csv("error")
    spark.createDataFrame(spark.sparkContext.parallelize(validExtList.toSeq), schema)
  }

  def validateEvents(df: DataFrame, spark: SparkSession): DataFrame = {
    logger.info(s"Validating events dataframe")
    val schema = df.schema

    df.foreach{row => parseRow(row, validEventList, errorEventList, validator.event_vals)}
    val errorEventDf: DataFrame = spark.createDataFrame(spark.sparkContext.parallelize(errorEventList.toSeq), schema)
    errorEventDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("append")
      .csv("error")
    spark.createDataFrame(spark.sparkContext.parallelize(validEventList.toSeq), schema)
  }

  //Final validation for the task
  def validate(df1: DataFrame, df2: DataFrame): DataFrame = {
    val result: DataFrame = df1.join(df2, "event_id")
      .drop(df2.col("event_dt"))
      .drop(df2.col("ccaf_dt_load"))
    val removeHeaders: Seq[String] = result.schema.fieldNames.diff(columnsForJson).toSeq
    result.drop(removeHeaders:_*).filter(result("type_operation") === "RurPayment").filter(result("event_channel") === "MOBILE")
  }


  //Parsin row to fullfil one of 2 lists - Valid/NonValid
  def parseRow(row: Row, validDf: ListBuffer[Row], errorList: ListBuffer[Row], mapVals: Map[String, validator.ValidateConfig]): Unit = {

    val rowMap: Map[String, String] = row.getValuesMap[String](row.schema.fieldNames)
    for (field <- rowMap.keys) {
      if (mapVals.contains(field)) {
        if (!validator.validateField(rowMap(field), mapVals(field))) {
          logger.info("ROW PARSING ERROR: " + field + " = " + rowMap(field))
          return
        }
      }
    }
    validDf += row
  }

}
