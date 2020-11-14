package com.target.loader

object ErrorHandler extends LazyLogging{

  def error(exception:Throwable):Unit={
    logger.fatal(s"FATAL EXCEPTION. Exiting..."+
      s"Message: ${exception.toString}")
    sys.exit(1)
  }

}
