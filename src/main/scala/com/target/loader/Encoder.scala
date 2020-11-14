package com.target.loader
import org.apache.spark.sql.DataFrame

class Encoder(df: DataFrame) {

  val dfColumns = df.columns.toSet
  val columns: Seq[String] = Globals.columnsForJson.filter(dfColumns)
  private val df1 = df.select(columns.head, columns.tail: _*)

  toJson(df1)

  private def toJson(df: DataFrame) = {
    df
      .repartition(1)
      .write.format("json")
      .option("header", "true")
      .mode("overwrite")
      .json("encoder_send.json")
  }

}
