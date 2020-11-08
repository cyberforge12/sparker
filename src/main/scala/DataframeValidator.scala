import org.apache.spark.sql.DataFrame

object DataframeValidator {

  def validate(df: DataFrame, df_type: Globals.FileTypesEnum.FileTypesEnum): Unit = {
    df_type match {
      case facts => {}
      case events => {}
    }
    df.show()
  }

}
