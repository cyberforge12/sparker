package com.target.loader

object Globals {

  val eventTable = "event"
  val factsTable = "ext_fact"

  object FileTypesEnum extends Enumeration {

    type FileTypesEnum = Value
    val e_facts: Value = Value("ext_facts")
    val e_events: Value = Value("events")
  }

}
