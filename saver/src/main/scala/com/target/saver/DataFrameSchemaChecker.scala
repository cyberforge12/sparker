package com.target.saver

import com.target.util.LazyLogging
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{StructField, StructType}

object DataFrameSchemaChecker extends LazyLogging {

  def validateSchema(df: DataFrame, requiredSchema: StructType): Unit = {

    val missingStructFields: Seq[StructField] = requiredSchema.diff(df.schema)

    if (missingStructFields.nonEmpty) {
      logger.info(s"The [${missingStructFields.mkString(", ")}] StructFields are not included in the DataFrame"
      )
      throw InvalidDataFrameSchemaException(missingStructFields.toString())
    }
    else {
      logger.info("Dataframe schema successfully validated")
    }
  }
  private case class InvalidDataFrameSchemaException(smth: String) extends Exception(smth)
}

