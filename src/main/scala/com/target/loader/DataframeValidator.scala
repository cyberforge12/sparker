package com.target.loader

import java.util

import org.apache.spark.sql.DataFrame

object DataframeValidator extends LazyLogging {

  def validateFacts(df: DataFrame, map: util.LinkedHashMap[String, String]): Unit = {
    logger.info(s"Validating facts dataframe")
    df.foreach(println(_))
  }

  def validateEvents(df: DataFrame, map: util.LinkedHashMap[String, String]): Unit = {
    logger.info(s"Validating events dataframe")

  }

  def validate(df: DataFrame, df_type: Globals.FileTypesEnum.FileTypesEnum, map: java.util.LinkedHashMap[String, String])
  : Unit = {
    df_type match {
      case facts => validateFacts(df, map)
      case events => validateEvents(df, map)
    }
    df.show()
  }

}
