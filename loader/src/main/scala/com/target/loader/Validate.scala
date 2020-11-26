package com.target.loader

import java.util

import scala.collection.JavaConverters._
import com.target.loader.Loader.validate

import scala.collection.JavaConversions.iterableAsScalaIterable

class Validate {

  val configMap = new ConfigParser(validate).configLinkedHashMap

  //val columnParams = getColumnParams(configMap, "event", "event_dt")


  val event_vals = Map(
    "event_id" -> ValidateConfig(getColumnParams(configMap, "event", "event_id")),
    "event_time" -> ValidateConfig(getColumnParams(configMap, "event", "event_time")),
    "event_channel" -> ValidateConfig(getColumnParams(configMap, "event", "event_channel")),
    "sub_channel" -> ValidateConfig(getColumnParams(configMap, "event", "sub_channel")),
    "event_type" -> ValidateConfig(getColumnParams(configMap, "event", "event_type")),
    "sub_type" -> ValidateConfig(getColumnParams(configMap, "event", "sub_type")),
    "transaction_amount" -> ValidateConfig(getColumnParams(configMap, "event", "transaction_amount")),
    "transaction_beneficiar_account_number" -> ValidateConfig(getColumnParams(configMap, "event", "transaction_beneficiar_account_number")),
    "ccaf_dt_load" -> ValidateConfig(getColumnParams(configMap, "event", "ccaf_dt_load")),
    "event_dt" -> ValidateConfig(getColumnParams(configMap, "event", "event_dt")))

  val ext_vals = Map("number_card_recepient" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "number_card_recepient")),
    "payer_card_number" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "payer_card_number")),
    "recepient_bik" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "recepient_bik")),
    "recepient_inn" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "recepient_inn")),
    "recepient_fio" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "recepient_fio")),
    "client_phone_number" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "client_phone_number")),
    "ccaf_dt_load" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "ccaf_dt_load")),
    "event_dt" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "event_dt")),
    "issue_date_card_owner" -> ValidateConfig(getColumnParams(configMap, "ext_fact", "issue_date_card_owner")))

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

  def getColumnParams(config: util.LinkedHashMap[String, Object], table: String, column: String): util
  .LinkedHashMap[String, String] = {
    val validateMap = configMap.getOrDefault("validate", "").asInstanceOf[util.LinkedHashMap[String, Object]]
    val tableMap = validateMap.getOrDefault(table, "").asInstanceOf[util.LinkedHashMap[String, Object]]
    val columnMap = tableMap.getOrDefault(column, "").asInstanceOf[util.LinkedHashMap[String, String]]
    columnMap
  }

  case class ValidateConfig(map: util.LinkedHashMap[String, String])  {
    var m = ""
    var nullable = true
    var valueset: Set[String] = Set()
    if (map.containsKey("match")) m = map.get("match")
    if (map.containsKey("nullable")) nullable = false
    if (map.containsKey("valueset")) {
      val array = map.get("valueset").asInstanceOf[util.ArrayList[String]]
      array.foreach(valueset += _)
    }
  }

}


/*
  case class Ext_fact(number_card_recepient: String, payer_card_number: String, recepient_bik: String,
                      recepient_fio: String, recepeint_inn: String, client_phone_number: String,
                      ccaf_dt_load: String, event_dt: String, issue_date_card_owner: String)



   */


