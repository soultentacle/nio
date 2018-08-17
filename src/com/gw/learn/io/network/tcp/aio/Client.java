package com.gw.learn.io.network.tcp.aio;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by dell on 2018/8/16.
 */
public class Client {

    private boolean connected;
    private AsynchronousSocketChannel socketChannel;

    public void start() throws Exception {
        if (connected) {
            throw new Exception("已启动");
        }

        synchronized (this) {
            socketChannel = AsynchronousSocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 9001), null, new CompletionHandler<Void, Void>() {


                @Override
                public void completed(Void result, Void attachment) {
                    connected = true;
                    read();
                }

                @Override
                public void failed(Throwable exc, Void attachment) {

                }
            });
        }
        while (!connected) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            write("hello,I'm GW!");
        }


    }


    public void read() {
        System.out.println("读取消息");
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        socketChannel.read(readBuffer, null, new CompletionHandler<Integer, CompletionHandler<AsynchronousSocketChannel, Object>>() {
            @Override
            public void completed(Integer result, CompletionHandler<AsynchronousSocketChannel, Object> attachment) {
                if (result > 0) {
                    readBuffer.flip();
                    byte[] data = new byte[1024];
                    int i = 0;
                    while (readBuffer.hasRemaining()) {
                        byte b = readBuffer.get();
                        data[i] = b;
                        System.out.print(b);
                        i++;
                    }
                    System.out.println(i);
                    System.out.println(data);
                    try {
                        System.out.println(new String(data, 0, i, "UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //继续读取
                read();
            }

            @Override
            public void failed(Throwable exc, CompletionHandler<AsynchronousSocketChannel, Object> attachment) {
                System.out.println("连接失败");
                exc.printStackTrace();
            }
        });
    }

    public void write(String msg) {
        System.out.println("发送消息");
        try {
            byte[] data = msg.getBytes("UTF-8");

            for (int i = 0; i < data.length; i += 1024) {
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
                        System.out.println("发送成功");
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.out.println("发送失败");
                        exc.printStackTrace();
                    }
                });
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }

}
