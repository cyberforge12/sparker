import cats.syntax.functor._
import io.circe._
import io.circe.generic.auto._
import io.circe.yaml.parser
import org.yaml.snakeyaml.Yaml

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

object ConfigParser {

  // IntelliJ Says this import isn't needed, but it won't compile without it.

  private def bufferContentsAsString(buffer: BufferedSource): String = {
    val contents = buffer.mkString
    buffer.close()
    contents
  }

  private def loadFromFile(filename: String): String = {
    val buffer = Source.fromFile(filename)
    bufferContentsAsString(buffer)
  }

  def parseFile(filename: String) = {

    Try {
        loadFromFile(filename)
    } match {
      case Success(contents) => parse(contents)
      case Failure(thr) => println("File Error"); sys.exit(1)
    }
  }

  def parse(conf: String) = {
    var str = conf
    val yaml = new Yaml()
    val pattern = "(.+match: )(\")(.+)(\")"
    yaml.load(conf.replaceAll(pattern, "$1\'$3\'")).asInstanceOf[java.util.LinkedHashMap[String, String]]
  }

}
