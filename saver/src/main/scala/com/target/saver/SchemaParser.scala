package com.target.saver

import java.io

import com.target.saver.Saver.{schema, spark}

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}
import org.apache.avro.Schema
import org.apache.avro._
import org.apache.spark.sql.types.{DataType, StructType}

import scala.reflect.io.File

object SchemaParser extends LazyLogging {

//  val file = new io.File(schemaString).toString
//  val schemaSource = Source.fromFile(file).getLines.mkString
//  val schemaFromJson = DataType.fromJson("{"data": "type"}").asInstanceOf[StructType]
//  val schemaStructType = createStructType(schemaString)
  logger.info("Sucessfully parsed schema file")


  private def createStructType(schemaString: String) = {
    DataType.fromJson(schemaString)
  }

  private def bufferContentsAsString(buffer: BufferedSource): String = {
    val contents = buffer.mkString
    buffer.close()
    contents
  }

  def getJsonStringFromFile(filename: String): String = {
    logger.info("Sucessfully parsed schema from " + filename + " to string")
    loadFromFile(filename)
  }

  def getAvroSchemaFromFile(filename: String): Schema = {
    logger.info("Sucessfully parsed schema from " + filename + " to avro.Schema")
    val str = loadFromFile(filename)
    createSchema(str)
  }

  def getSparkSchemaFromFile(filename: String): StructType = {
    val avroSchema = SchemaParser.getJsonStringFromFile(filename)
    spark.read
      .format("avro")
      .option("avroSchema", avroSchema)
      .load()
      .schema
  }

  private def loadFromFile(filename: String): String = {
    logger.info(s"Opening file: $filename")
    Try {
      Source.fromFile(filename)
    } match {
      case Failure(exception) =>
        ErrorHandler.error(exception);
        sys.exit(1)
      case Success(value) => bufferContentsAsString(value)
    }
  }

  private def createSchema(schemaString: String) = {
    new Schema.Parser().parse(schemaString)
  }

}
