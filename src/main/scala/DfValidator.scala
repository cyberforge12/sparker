import org.apache.spark.sql.DataFrame

object DfValidator {

  def validate(df: DataFrame): Unit = {

    df.show()
  }

}
