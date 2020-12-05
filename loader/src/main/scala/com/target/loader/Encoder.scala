package com.target.loader
import org.apache.spark.sql.DataFrame

class Encoder(df: DataFrame) {

  val dfColumns: Set[String] = df.columns.toSet
  val columns: Seq[String] = Globals.columnsForJson.filter(dfColumns)
  private val df1: DataFrame = df.select(columns.head, columns.tail: _*)

  toJson(df1)

  private def toJson(df: DataFrame): Unit = {
    df
      .repartition(1)
      .write.format("json")
      .option("header", "true")
      .mode("overwrite")
      .json("encoder_send.json")
  }

}
