package com.target.loader
import io.circe.Error
import io.circe.generic.JsonCodec
import io.circe._
import cats.syntax.either._

class CirceParser(yamlString: String, config: ConfigParser) {

  implicit val decodeMapVal: Decoder[Map[String, Val]] = new Decoder[Map[String, Val]] {
    final def apply(c: HCursor): Decoder.Result[Map[String, Val]] = {
      c.downField("validate").as[Map[String, Val]]
    }
  }

  implicit val decodeVal: Decoder[Val] = new Decoder[Val] {
    final def apply(c: HCursor): Decoder.Result[Val] = {
      for {
        v <- c.downField("validate").as[Map[String, Tab]]
      } yield new Val(v)
    }
  }

  implicit val decodeTab: Decoder[Tab] = new Decoder[Tab] {
    final def apply(c: HCursor): Decoder.Result[Tab] = {
      c.focus match {
        case Some(x) => {
          for {
            v <- x.as[Map[String, Col]]
          } yield new Tab(v)
        }
        case None => Left(DecodingFailure("Some decoder error", List()))
      }
    }
  }

  implicit val decodeCol: Decoder[Col] = new Decoder[Col] {
    final def apply(c: HCursor): Decoder.Result[Col] = {
      for {
        a <- c.downField("match").as[Option[String]]
        b <- c.downField("nullable").as[Option[Boolean]]
        e <- c.downField("valueset").as[Option[List[String]]]
      } yield new Col(a, b, e)
    }
  }

  val json: Either[ParsingFailure, Json] = yaml.parser.parse(yamlString)

  val value: Val = json
    .leftMap(err => err: Error)
    .flatMap(_.as[Val])
    .valueOr(throw _)

  val configLink: ConfigParser = config

  @JsonCodec
  case class Val(tables: Map[String, Tab]) {
  }

  @JsonCodec
  case class Tab(columns: Map[String, Col]) {
  }

  @JsonCodec
  case class Col(a: Option[String], b: Option[Boolean], c: Option[List[String]]) {
  }

}
