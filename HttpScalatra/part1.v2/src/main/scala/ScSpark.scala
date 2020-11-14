
import org.apache.ivy.Main
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql._
import org.apache.spark._

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.break

object ScSpark extends App {

    val validator: Validate = new Validate
    val filePathEvent = "/Users/vasilijgoremykin/Downloads/event_synth.csv"
    val filePathExt = "/Users/vasilijgoremykin/Downloads/ext_fact_synth.csv"
    val spark = SparkSession.builder.appName("Simple Application").config("spark.master", "local").getOrCreate()
    val dfEvent = spark.read.option("header", true).options(Map("delimiter"->";")).csv(filePathEvent)
    val EventSchema = dfEvent.schema
    val dfExt = spark.read.option("header", true).options(Map("delimiter"->";")).csv(filePathExt)
    val ExtSchema = dfExt.schema

    var validEventList = ListBuffer[Row]()
    var validExtList = ListBuffer[Row]()
    var errorListEvent = ListBuffer[Row]()
    var errorListExt = ListBuffer[Row]()
    dfEvent.rdd.collect().foreach(parseRow(_, validEventList, errorListEvent, validator.event_vals))
    dfExt.rdd.collect().foreach(parseRow(_, validExtList, errorListExt, validator.ext_vals))

    val errorEventDf = spark.createDataFrame(spark.sparkContext.parallelize(errorListEvent.toSeq), EventSchema)
    val errorExtDf = spark.createDataFrame(spark.sparkContext.parallelize(errorListExt.toSeq), ExtSchema)

    errorExtDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("overwrite")
      .csv("/Users/vasilijgoremykin/Desktop/error")
    errorEventDf.coalesce(1)
      .write
      .option("header","true")
      .option("sep",",")
      .mode("append")
      .csv("/Users/vasilijgoremykin/Desktop/error")

    val validEventDf = spark.createDataFrame(spark.sparkContext.parallelize(validEventList.toSeq), EventSchema)
    val validExtDf = spark.createDataFrame(spark.sparkContext.parallelize(validExtList.toSeq), ExtSchema)

    val result = validExtDf.join(validEventDf, validExtDf("event_id") === validEventDf("event_id"), "inner")
    println(result.count())
    //^ результат пустой потому что пересечение 5 и 0 = 0. Таблица ext_fact - полностью не валидная.

    def parseRow(row: Row, validDf: ListBuffer[Row], errorList: ListBuffer[Row], mapVals: Map[String, validator.ValidateConfig]): Unit = {
        var error = 0
        val rowMap = row.getValuesMap[String](row.schema.fieldNames)
        for (field <- rowMap.keySet) { if (mapVals.contains(field)) {
            if (!validator.validateField(rowMap(field), mapVals(field))) {error+= 1}
        }
        }
        if (error == 0) validDf += row
        else errorList += row
    }

}