package com.target.loader

import org.apache.log4j.{LogManager, Logger}


trait LazyLogging {

  lazy val logger: Logger = LogManager.getLogger(getClass.getName)

}
