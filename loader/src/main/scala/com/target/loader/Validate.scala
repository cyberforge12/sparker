package com.target.loader

class Validate {

  case class ValidateConfig(m: String, nullable: Boolean, valueset: Set[String])

  val event_vals = Map("event_id" -> ValidateConfig("[a-z0-9]{24,26}", nullable = false, Set()), "event_time" -> ValidateConfig("\\d{13}", nullable = false, Set()),
    "event_channel" -> ValidateConfig("[A-Z]+", nullable = false, Set("WEB", "MOBILE", "BRANCH", "SMS", "WEBKIOSK")), "sub_channel" -> ValidateConfig("[A-Z]+", nullable = false,
      Set("WEBAPI", "MOBILEAPI", "ASFS.BRANCHAPI", "MBK", "ATMAPI")), "event_type" -> ValidateConfig("[A-Z]+", nullable = false, Set("PAYMENT", "WITHDRAW")),
    "sub_type" -> ValidateConfig("[A-Z]+", nullable = false, Set("RURPAYMENT", "RURPAYJURSB", "CASH", "MB_CARD_TO_CARD")), "transaction_amount" -> ValidateConfig("\\d+", nullable =
      false, Set()), "transaction_sender_account_number" -> ValidateConfig("[X0-9]{16,20}", nullable = false, Set()), "transaction_beneficiar_account_number" ->
      ValidateConfig("[X0-9]{16,20}", nullable = false, Set()), "ccaf_dt_load" -> ValidateConfig("\\d{8}", nullable = false, Set()), "event_dt" -> ValidateConfig("\\d{8}", nullable = false, Set()))

  val ext_vals = Map("number_card_recepient" -> ValidateConfig("[X0-9]{16,20}", nullable = true, Set()), "payer_card_number" -> ValidateConfig("[X0-9]{16,20}", nullable = true, Set()),
    "recepient_bik" -> ValidateConfig("\\d{8}", nullable = true, Set()), "recepient_inn" -> ValidateConfig("\\d{11}", nullable = true, Set()), "recepient_fio" -> ValidateConfig("[а-яА-Я\\s]+", nullable =
      true, Set()), "client_phone_number" -> ValidateConfig("\\d+", nullable = true, Set()), "ccaf_dt_load" -> ValidateConfig("\\d{8}", nullable = false, Set()), "event_dt" -> ValidateConfig("\\d{8}", nullable = false, Set()),
    "issue_date_card_owner" -> ValidateConfig("\\d{2}.\\d{2}.\\d{4}\\s+\\d{1,2}:\\d{2}", nullable = true, Set()))

  def validateField(value: String, vConf: ValidateConfig): Boolean = {
    if (!value.toUpperCase.equals("NULL")) {
      if (vConf.valueset.isEmpty || vConf.valueset.contains(value)) {
        if (value matches (vConf.m)) return true
        return false
      }
      return false
    }
    if (vConf.nullable)  true
    else false
  }

  def isNull(s: String) = {
    if (s.toUpperCase().equals("NULL")) true
    else false
  }

}


/*
  case class Ext_fact(number_card_recepient: String, payer_card_number: String, recepient_bik: String,
                      recepient_fio: String, recepeint_inn: String, client_phone_number: String,
                      ccaf_dt_load: String, event_dt: String, issue_date_card_owner: String)



   */


