package com.target.loader

object Globals {

  val eventTable = "event"
  val factsTable = "ext_fact"
  val columnsForJson: Seq[String] = Seq("event_id", "event_time", "event_channel", "sub_channel", "event_type",
    "sub_type",
    "event_description", "transaction_amount", "transaction_sender_account_number",
    "transaction_beneficiar_account_number", "ccaf_dt_load", "event_dt", "issue_date_card_owner", "number_dul",
    "number_dul", "account_number_of_recipient", "number_card_recepient", "payer_card_number", "recepient_bik",
    "recepient_inn", "recepient_fio", "client_phone_number")

  object FileTypesEnum extends Enumeration {

    type FileTypesEnum = Value
    val e_facts: Value = Value("ext_facts")
    val e_events: Value = Value("events")
  }

}
