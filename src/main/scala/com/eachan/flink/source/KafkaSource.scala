package com.eachan.flink.source

import org.apache.flink.api.common.functions.{MapFunction, RichMapFunction, RuntimeContext}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object KafkaSource {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setAutoWatermarkInterval(1000)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setCheckpointTimeout(1000)

    import java.util.Properties
    import org.apache.flink.api.common.serialization.SimpleStringSchema
    import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "hadoop102:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("auto.offset.reset", "latest")

    val kafkaStream: DataStream[String] = env.addSource(new FlinkKafkaConsumer011[String]("order_detail", new SimpleStringSchema(), properties))

    //    import org.apache.flink.streaming.api.TimeCharacteristic
    //    import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //    env.getConfig.setAutoWatermarkInterval(300)
    //    //BoundedOutOfOrdernessTimestampExtractor构造函数中的参数为maxOutOfOrderness，watermark=maxTs - maxOutOfOrderness
    //    //extractTimestamp为从数据中提取eventTime的方法
    //    val eventTimeWaterMarkDStream = valueTimeDataStream.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[(String, Long)](Time.milliseconds(30)) {
    //      override def extractTimestamp(t: (String, Long)): Long = {
    //        t._2
    //      }
    //    })
    val valueTimeDataStream: DataStream[(String, Long)] = kafkaStream.map(str => {
      val arr: Array[String] = str.split(",")
      (arr(0), arr(1).toLong)
    })
    val value1: DataStream[(String, Long)] = valueTimeDataStream.keyBy(_._1).sum(1)


    valueTimeDataStream.keyBy(_._1).map(new RichMapFunction[(String, Long), String] {
      lazy val lastNumber: ValueState[Long] = getRuntimeContext.getState[Long](new ValueStateDescriptor[Long]("lastNum",classOf[Long]))
      lazy val testTime: ValueState[Int] = getRuntimeContext.getState[Int](new ValueStateDescriptor[Int]("times",classOf[Int]))
      override def map(value: (String, Long)): String = {
        val testTimeRes: Int = testTime.value()
        testTime.update(testTimeRes+1)
        val lastNumberRes: Long = lastNumber.value()
        lastNumber.update(value._2)
        val diff: Long = (value._2-lastNumberRes).abs
        if(diff>3 && testTimeRes!=0){
          "连续两次温度差值大于3"
        }else{
          "正常"
        }
      }
    }).print()


    env.execute()

  }


}




