package com.target.saver

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}
import org.apache.avro.Schema

class SchemaParser(schemaFilename: String) extends LazyLogging {

  val schemaString = loadFromFile(schemaFilename)
  val schema = createSchema(schemaString)
  logger.info("Sucessfully parsed schema file")

  private def bufferContentsAsString(buffer: BufferedSource): String = {
    val contents = buffer.mkString
    buffer.close()
    contents
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
    val schema: Schema = new Schema.Parser().parse(schemaString)
  }

}
