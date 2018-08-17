package com.gw.learn.io.network.tcp.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by dell on 2018/8/16.
 */
public class Server {

    private volatile boolean start;

    private AsynchronousServerSocketChannel serverSocketChannel ;

    private void log(String str) {
        System.out.println(str);
    }

    public void start() throws IOException {

        serverSocketChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(9001));
        start = true;
        //循环接收请求
        accept();
        synchronized (this){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void accept(){
        serverSocketChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                System.out.println("有客户连接进来！！！！");
                read(result);
                write(result,"hellow,I'm Server!");
                accept();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                log("接收客户端请求异常");
                accept();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public void read(AsynchronousSocketChannel socketChannel){
        System.out.println("读取消息");
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        socketChannel.read(readBuffer, socketChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel attachment) {
                readBuffer.flip();
                if(result > 0){
                    byte[] data = new byte[1024];
                    int i = 0;
                    while(readBuffer.hasRemaining()){
                        data[i] = readBuffer.get();
                        i++;
                    }
                    System.out.println(i);
                    System.out.println(data);
                    try {
                        System.out.println(new String(data, 0, i, "UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    write(attachment,"I'm server!");
                }


                //继续读取
                read(socketChannel);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                System.out.println("连接失败");
                exc.printStackTrace();
            }
        });
    }

    public void write(AsynchronousSocketChannel socketChannel,String msg) {
        System.out.println("写消息");
        try {
            byte[] data = msg.getBytes("UTF-8");

            for (int i = 0; i <= data.length; i += 1024) {
                ByteBuffer write = ByteBuffer.allocate(1024);
                if (data.length - i >= 1024) {
                    write.put(data, i, 1024);
                } else {
                    write.put(data, i, data.length - i);
                }
                write.flip();
                socketChannel.write(write, null, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer result, Object attachment) {

                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
