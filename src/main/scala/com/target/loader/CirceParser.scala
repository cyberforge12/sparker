package com.target.loader

import cats.syntax.either._
import io.circe._
import io.circe.generic.auto._
import io.circe.yaml

import scala.util.{Failure, Success, Try}

/*
class Wrapper[A](val underlying: Int) extends Decoder[A] {
  override def apply(c: HCursor): Result[A] = {
    println(underlying)
    c.downField("nullable").asRight match {
      case Right(v) => v.as[Boolean]
    }
  }
}
*/

/*
class DecoderNew[A](elems: A*) extends Decoder[A] {
  def withFilter(p: A => Boolean): Decoder[A] = {
    val tmpArrayBuffer = elems.filter(p)
    Decoder(tmpArrayBuffer: _*)
}
*/

class CirceParser (yamlString: String) {

  implicit val decodeColumn: Decoder[Column] = new Decoder[Column] {
    final def apply(c: HCursor): Decoder.Result[Column] = {
      for {
        a <- c.downField("match").as[Option[String]]
        b <- c.downField("nullable").as[Option[Boolean]]
        e <- c.downField("valueset").as[Option[List[String]]]
      } yield new Column(a, b, e)
    }
  }
//  implicit val housesDecoder: Decoder[HouseType] = (hcursor:HCursor) => for {
//    value <- hcursor.as[String]
//    result <- value match {
//      case "Helga_Hufflepuff" => HelgaHufflepuff.asRight
//      case "Rowena_Ravenclaw" => RowenaRavenclaw.asRight
//      case "Godric_Gryffindor" => GodricGryffindor.asRight
//      case "Salazar_Slyntherin" => SalazarSlyntherin.asRight
//      case s => DecodingFailure(s"Invalid house type ${s}", hcursor.history).asLeft
//    }
//  } yield result

  val json = yaml.parser.parse(yamlString)

  val json_test = yaml.parser.parse("""
validate:
  foo: Hello, World
  bar:
      one: One Third
      two: 33.333333
  baz:
      - Hello
      - World
""")

  def validate_test = {
    json_test
      .leftMap(err => err: Error)
      .flatMap(_.as[Data])
      .valueOr(throw _)
  }

  def validate = {
    json
      .leftMap(err => err: Error)
      .flatMap(_.as[Starter])
      .valueOr(throw _)
  }

  case class Nested(one: String, two: BigDecimal)
  case class Foo(foo: String, bar: Nested, baz: List[String])
  case class Data(validate: Foo)


  case class Column(matchField: Option[String], nullable: Option[Boolean], valueset: Option[List[String]])
  case class Table(event_id: Column)
  case class Validator(event: Table, ext_fact: Table)
  case class Starter(validate: Validator)
}


