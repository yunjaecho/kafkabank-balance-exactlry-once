package com.yunjae.kafka.bankbalance

import java.time.Instant
import java.util.Properties

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{JsonNodeFactory, ObjectNode}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import org.apache.kafka.connect.json.{JsonDeserializer, JsonSerializer}
import org.apache.kafka.streams.kstream.internals.TimeWindow
import org.apache.kafka.streams.kstream.{KStream, KTable, Materialized, Serialized}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}

object BankBalanceExactlyOnceApp {

  def main(args: Array[String]): Unit = {
    val config = {
      val properties = new Properties()
      properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-balance-application")
      properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

      // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in product
      properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

      // Exactly once processing!!
      properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
      properties
    }

    // json Serde
    val jsonSerializer: Serializer[JsonNode] = new JsonSerializer()
    val jsonDeserializer: Deserializer[JsonNode] = new JsonDeserializer()
    val jsonSerde: Serde[JsonNode] = Serdes.serdeFrom(jsonSerializer, jsonDeserializer)

    val builder = new StreamsBuilder()

    val bankTransactions: KStream[String, JsonNode] = builder.stream("bank-transactions")

    val initialBalance: ObjectNode = JsonNodeFactory.instance.objectNode()
    initialBalance.put("count", 0)
    initialBalance.put("balance", 0)
    initialBalance.put("time", Instant.ofEpochMilli(0L).toString)

    val bankBalance: KTable[String, JsonNode] = bankTransactions
      .groupByKey(Serialized.`with`(Serdes.String(), jsonSerde))
      .aggregate(
        () => initialBalance,
        (_, transaction: JsonNode, balance: JsonNode) => newBalance(transaction, balance),
        jsonSerde,
        "bank-balance-agg"
      )

    bankBalance.to(Serdes.String(), jsonSerde, "bank-balance-exactly-once")
    val streams = new KafkaStreams(builder.build(), config)
    // only do this in dev - not in prod
    streams.cleanUp
    streams.start

    println(streams)

    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = streams.close()
    }   )

  }

  def newBalance(transaction: JsonNode, balance: JsonNode): JsonNode = {
    val newBalance = JsonNodeFactory.instance.objectNode()
    newBalance.put("count", balance.get("count").asInt() + 1)
    newBalance.put("balance", balance.get("balance").asInt() + transaction.get("amount").asInt())

    val balanceEpoch = Instant.parse(balance.get("time").asText()).toEpochMilli
    val transactionEpoch = Instant.parse(transaction.get("time").asText()).toEpochMilli
    val newBalanceInstant = Instant.ofEpochMilli(Math.max(balanceEpoch, transactionEpoch))
    newBalance.put("time", newBalanceInstant.toString)
    return newBalance
  }


}
