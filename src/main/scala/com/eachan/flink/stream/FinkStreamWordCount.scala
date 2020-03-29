package com.eachan.flink.stream

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object FinkStreamWordCount {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    //flatMap和map需要隐式转换，导包
    import org.apache.flink.api.scala._
    //获取提交jar包时候配置的参数 --host hostname ...
    val tool: ParameterTool = ParameterTool.fromArgs(args)
    val host: String = tool.get("host")
    val port: Int = tool.getInt("port")
    val socketStream: DataStream[String] = env.socketTextStream(host, port)
    val resStream: DataStream[(String, Int)] = socketStream.flatMap(_.split(" ")).map((_, 1)).keyBy(0).sum(1)
    resStream.print().setParallelism(1)
    env.execute()

  }

}
