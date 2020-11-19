package com.target.loader

import java.util

import org.apache.spark
import org.apache.spark.sql.SparkSession
import org.apache.spark._
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.{DataFrame, Row}

import scala.collection.mutable.ListBuffer

object DataframeValidator extends LazyLogging {

  val validator = new Validate()
  val spark = SparkSession
    .builder()
    .appName("Loader")
    .config("spark.master", "local")
    .getOrCreate()

  def validateFacts(df: DataFrame): DataFrame = {
    logger.info(s"Validating facts dataframe")
    val schema = df.schema
    var validExtList = ListBuffer[Row]()
    var errorExtList = ListBuffer[Row]()

    df.rdd.collect().foreach(parseRow(_, validExtList, errorExtList, validator.ext_vals))
    val errorExtDf = spark.createDataFrame(spark.sparkContext.parallelize(errorExtList.toSeq), schema)
    errorExtDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("overwrite")
      .csv("error")
    spark.createDataFrame(spark.sparkContext.parallelize(validExtList.toSeq), schema)
  }

  def validateEvents(df: DataFrame): DataFrame = {
    logger.info(s"Validating events dataframe")
    val schema = df.schema
    var validEventList = ListBuffer[Row]()
    var errorEventList = ListBuffer[Row]()

    df.rdd.collect().foreach(parseRow(_, validEventList, errorEventList, validator.event_vals))
    val errorEventDf = spark.createDataFrame(spark.sparkContext.parallelize(errorEventList.toSeq), schema)
    errorEventDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("append")
      .csv("error")
    spark.createDataFrame(spark.sparkContext.parallelize(validEventList.toSeq), schema)
  }

  def validate(df1: DataFrame, df2: DataFrame)
  : DataFrame = {
    val result = df1.join(df2, "event_id")
      .drop(df2.col("event_dt"))
      .drop(df2.col("ccaf_dt_load"))
    result.filter(result("type_operation") === "RurPayment").filter(result("event_channel") === "MOBILE")
  }

  //после валидации дает уже отфильтрованный датафрейм.


  def parseRow(row: Row, validDf: ListBuffer[Row], errorList: ListBuffer[Row], mapVals: Map[String, validator.ValidateConfig]): Unit = {
    var error = 0
    val rowMap = row.getValuesMap[String](row.schema.fieldNames)
    for (field <- rowMap.keys) {
      logger.info("Validated field " + field)
      if (mapVals.contains(field)) {
        //TODO: поле event_dt при загрузке распарсилось как Int, поэтому невозможно запустить сравнение с regex.
        // Возможно другие поля тоже кастанулись в типы, отличные от String
        if (!validator.validateField(rowMap(field), mapVals(field))) {
          error+= 1
          println(field + " point " + rowMap(field))
        }
      }
    }
    if (error == 0) validDf += row
    else errorList += row
  }

}
