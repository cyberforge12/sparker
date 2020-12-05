package com.target.util

import org.apache.logging.log4j.{LogManager, Logger}

trait LazyLogging {

  lazy val logger: Logger = LogManager.getLogger(getClass.getName)

}
