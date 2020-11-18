package com.target.saver

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StructType

object DataFrameSchemaChecker extends LazyLogging {

  def validateSchema(df: DataFrame, requiredSchema: StructType) = {
    val missingStructFields = requiredSchema.diff(df.schema)
    if (missingStructFields.nonEmpty) {
      logger.info(s"The [${missingStructFields.mkString(", ")}] StructFields are not included in the DataFrame with " +
        s"the following StructFields [${df.schema.toString()}]")
      ErrorHandler.error(new InvalidDataFrameSchemaException(missingStructFields.toString()))
    }
    logger.info("Schema successfully validated")
  }
  private case class InvalidDataFrameSchemaException(smth: String) extends Exception(smth)
}

