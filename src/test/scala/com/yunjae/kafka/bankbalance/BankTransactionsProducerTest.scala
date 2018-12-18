package com.yunjae.kafka.bankbalance

import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest._

class BankTransactionsProducerTest extends FlatSpec with Matchers {
  "New Random Transaction" should "create test" in {
    val record = BankTransactionsProducer.newRandomTransaction("john")

    val key = record.key()
    val value = record.value()
    assert(key == "john")

    val mapper = new ObjectMapper()
    try {
      val node = mapper.readTree(value)
      assert(node.get("name").asText == "john")
      assert(node.get("amount").asInt() < 1000)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    println(value)
  }
}
