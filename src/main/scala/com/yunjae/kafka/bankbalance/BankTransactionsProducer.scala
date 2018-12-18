package com.yunjae.kafka.bankbalance

import java.time.Instant
import java.util.Properties
import java.util.concurrent.ThreadLocalRandom

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{Serdes, StringSerializer}

object BankTransactionsProducer {
  def main(args: Array[String]): Unit = {
    val config = {
      val properties = new Properties()
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
      properties.put(ProducerConfig.ACKS_CONFIG, "all")
      properties.put(ProducerConfig.RETRIES_CONFIG, "3")
      properties.put(ProducerConfig.LINGER_MS_CONFIG, "1")
      // ensure we don't push duplicates
      properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
      properties
    }
    val producer: Producer[String, String] = new KafkaProducer[String, String](config)

    var i = 0;
    var isRun = true
    while(isRun) {
      println(s"Producing batch : $i" )
      try {
        producer.send(newRandomTransaction("john"))
        Thread.sleep(100)
        producer.send(newRandomTransaction("stephane"))
        Thread.sleep(100)
        producer.send(newRandomTransaction("alice"))
        Thread.sleep(100)
        i += 1
      } catch {
        case _: InterruptedException =>  isRun = false
      }
    }

    producer.close()
  }

  def newRandomTransaction(name: String): ProducerRecord[String, String] = {
    // create an empty json {}
    val transaction = JsonNodeFactory.instance.objectNode()
    val amount = ThreadLocalRandom.current().nextInt(0, 1000);
    // Instant.now() is get the current time using java 8
    val now = Instant.now()

    transaction.put("name", name)
    transaction.put("amount", amount)
    transaction.put("time", now.toString)
    new ProducerRecord[String, String]("bank-transactions", name, transaction.toString)
  }

}
