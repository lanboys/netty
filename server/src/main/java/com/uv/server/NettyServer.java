package com.uv.server;
/*
 * @author uv
 * @date 2018/10/12 18:25
 * 服务端
 */

import com.uv.protocol.RpcDecoder;
import com.uv.protocol.RpcEncoder;
import com.uv.protocol.RpcRequest;
import com.uv.protocol.RpcResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

  public void bind(int port) throws Exception {

    EventLoopGroup bossGroup = new NioEventLoopGroup(); //bossGroup就是parentGroup，是负责处理TCP/IP连接的
    EventLoopGroup workerGroup = new NioEventLoopGroup(); //workerGroup就是childGroup,是负责处理Channel(通道)的I/O事件

    ServerBootstrap sb = new ServerBootstrap();
    sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 128) //初始化服务端可连接队列,指定了队列的大小128
        .childOption(ChannelOption.SO_KEEPALIVE, true) //保持长连接
        .childHandler(new ChannelInitializer<SocketChannel>() {  // 绑定客户端连接时候触发操作
          @Override
          protected void initChannel(SocketChannel sh) throws Exception {
            sh.pipeline()
                .addLast(new RpcDecoder(RpcRequest.class)) //解码request
                .addLast(new RpcEncoder(RpcResponse.class)) //编码response
                .addLast(new ServerHandler()); //使用ServerHandler类来处理接收到的消息
          }
        });
    //绑定监听端口，调用sync同步阻塞方法等待绑定操作完成，完成后返回ChannelFuture类似于JDK中Future
    ChannelFuture future = sb.bind(port).sync();

    if (future.isSuccess()) {
      System.out.println("服务端启动成功");
    } else {
      System.out.println("服务端启动失败");
      future.cause().printStackTrace();
      bossGroup.shutdownGracefully(); //关闭线程组
      workerGroup.shutdownGracefully();
    }

    //成功绑定到端口之后,给channel增加一个 管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程。
    future.channel().closeFuture().sync();
  }
}
