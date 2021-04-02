package com.uv;

import com.uv.client.NettyClient;
import com.uv.protocol.RpcRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import io.netty.channel.Channel;

/**
 * <uv> [2018/10/13 19:51]
 */
public class Main {

  public static void main(String[] args) throws Exception {
    NettyClient client = new NettyClient("127.0.0.1", 8080);
    //启动client服务
    client.start();

    Channel channel = client.getChannel();
    //消息体
    RpcRequest request = new RpcRequest();
    request.setId(UUID.randomUUID().toString());
    request.setData("client.message");
    //channel对象可保存在map中，供其它地方发送消息
    channel.writeAndFlush(request);

    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        String str = bufferedReader.readLine();
        RpcRequest request1 = new RpcRequest();
        request1.setId(UUID.randomUUID().toString());
        request1.setData(str);
        channel.writeAndFlush(request1);
        System.out.println("已发送数据：" + request1);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
