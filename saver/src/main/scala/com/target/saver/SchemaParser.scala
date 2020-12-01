package com.target.saver

import com.target.util.{ErrorHandler, LazyLogging}
import org.apache.avro.Schema
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

object SchemaParser extends LazyLogging {

  def toAvro(filename: String): Schema = {
    logger.info("Sucessfully parsed schema from " + filename + " to avro.Schema")
    val str = loadFromFile(filename)
    createSchema(str)
  }

  def toStructType(filename: String): StructType = {
    val avroSchema = SchemaParser.loadFromFile(filename)
    SparkSession.builder().getOrCreate()
      .read
      .format("avro")
      .option("avroSchema", avroSchema)
      .load()
      .schema
  }

  private def bufferContentsAsString(buffer: BufferedSource): String = {
    val str = buffer.mkString
    buffer.close()
    str
  }

  private def loadFromFile(filename: String): String = {
    logger.info(s"Opening file: $filename")
    Try {
      Source.fromFile(filename)
    } match {
      case Failure(exception) =>
        ErrorHandler.fatal(exception);
        sys.exit(1)
      case Success(value) => bufferContentsAsString(value)
    }
  }

  private def createSchema(schemaString: String) = {
    new Schema.Parser().parse(schemaString)
  }

}
