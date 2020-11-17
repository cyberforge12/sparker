package com.target.loader

object ErrorHandler extends LazyLogging{

  def error(exception:Throwable):Unit={
    logger.fatal(s"FATAL EXCEPTION. Exiting...\n" +
      s"Message: ${exception.toString}\n" +
      s"Backtrace:\n" +
      exception.getStackTrace.mkString("\n"))
    sys.exit(0)
  }
}
