import org.apache.log4j._

trait LazyLogging {

  lazy val logger: Logger = LogManager.getLogger(getClass.getName)

}
