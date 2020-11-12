package com.target.loader
import io.circe.Error
import io.circe.generic.JsonCodec
import io.circe._
import io.circe.generic.auto._
import cats.syntax.either._

class CirceParserADT(yamlString: String, config: ConfigParser) extends CirceParser(yamlString) {


  implicit val decodeVal: Decoder[Val] = new Decoder[Val] {
    final def apply(c: HCursor): Decoder.Result[Val] = {
      for {
        v <- c.downField("validate").as[Map[String, Tab]]
      } yield new Val(v)
    }
  }

  implicit val decodeTab: Decoder[Tab] = new Decoder[Tab] {
    final def apply(c: HCursor): Decoder.Result[Tab] = {
      for {
        // TODO: Как указывать значение для downField динамически?
        v <- c.downField("event").as[Map[String, Col]]
      } yield new Tab(v)
    }
  }

  val validator3 = json
    .leftMap(err => err: Error)
    .flatMap(_.as[Val])
    .valueOr(throw _)

  @JsonCodec
  case class Val(validate: Map[String, Tab]) {
    println(this.getClass.toString)
  }

  @JsonCodec
  case class Tab(event: Map[String, Col]) {
    println(this.getClass.toString)
  }

  @JsonCodec
  case class Col(a: String, b: String, c: String) {
    println(this.getClass.toString)
  }

}
