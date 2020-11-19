package com.target.saver

object ErrorHandler extends LazyLogging{

  def error(exception:Throwable):Unit={
    logger.fatal(s"FATAL EXCEPTION. Exiting...\n" +
      s"Message: ${exception.toString}\n" +
      s"Backtrace:\n" +
      exception.getStackTrace.mkString("\n"))
    sys.exit(0)
  }

  def info(exception:Throwable):Unit={
    logger.info(s"EXCEPTION:\n" +
      s"Message: ${exception.toString}\n")
  }
}
