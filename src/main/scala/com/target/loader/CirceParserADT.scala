package com.target.loader
import io.circe.Error
import io.circe.generic.JsonCodec
import io.circe._
import io.circe.generic.auto._
import cats.syntax.either._

class CirceParserADT(yamlString: String, config: ConfigParser) extends CirceParser(yamlString) {


  implicit val decodeMapVal: Decoder[Map[String, Val]] = new Decoder[Map[String, Val]] {
    final def apply(c: HCursor): Decoder.Result[Map[String, Val]] = {
      c.downField("validate").as[Map[String, Val]]
      //      for {
      //        v <- c.downField("validate").as[(String, Val)].
      //      } yield Map[String, Val](v)
    }
  }

  implicit val decodeVal: Decoder[Val] = new Decoder[Val] {
    final def apply(c: HCursor): Decoder.Result[Val] = {
      for {
        v <- c.downField("validate").as[Map[String, Tab]]
      } yield new Val(v)
      /*
            c.focus match {
              case Some(x) => x.as[Map[String, Tab]] match {
                case Right(b) => {
                  for ((k, v) <- b) yield new Val(k, v)
                }
              }
            }
      */
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
      }
      //      println("TAB: " + jsonMap)
/*
      for {
        v <- jsonMap
        //         TODO: Как указывать значение для downField динамически?
        //        v <- c.downField("event").as[Map[String, Col]]
      } yield new Tab("2", v)
*/
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


  /*
  val validator3 = json
    .leftMap(err => err: Error)
    .flatMap(_.as[Map[String, Tab]])
//    .flatMap(_.as[Val])
    .valueOr(throw _)
  println("val3 " + validator3.toString())
*/

  val validator5 = json
    .leftMap(err => err: Error)
    //    .flatMap(_.as[Map[String, Val]])
    .flatMap(_.as[Val])
    .valueOr(throw _)
  println("val5 " + validator5.toString())

  /*
  val validator4 = json
    .leftMap(err => err: Error)
    .flatMap(_.as[Map[String, Tab]])
    //    .flatMap(_.as[Val])
    .valueOr(throw _)
  println("val4 " + validator4.toString())
*/

  @JsonCodec
  case class Val(tables: Map[String, Tab]) {
  }

  @JsonCodec
  case class Tab(columns: Map[String, Col]) {
  }

  @JsonCodec
  case class Col(a: Option[String], b: Option[Boolean], c: Option[List[String]]) {

  }

  @JsonCodec
  case class Opt(name: String, value: String) extends Debug(name) {
    override def printDebug: Unit = {
      println(this.getClass + ": " + this.name + " = " + value)
    }
  }

  class Debug(name: String) {

    printDebug

    def printDebug: Unit = {
      println(this.getClass + ": " + name + " = " + this.getClass.toString)
    }
  }

}