package com.gw.test.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by dell on 2018/7/19.
 */
public class NIOClient {
    public static void main(String[] args) throws Exception {
        client();
    }

    public static void client() {
        SocketChannel channel = null;
        try {

            Selector selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(8080));
            channel.register(selector, SelectionKey.OP_CONNECT);

            System.out.println("客户端channel----->" + channel);

            while (true) {
                if (selector.select() > 0) {

                    Iterator<SelectionKey> set = selector.selectedKeys().iterator();
                    while (set.hasNext()) {
                        SelectionKey key = set.next();
                        //set.remove();

                        //SocketChannel ch=(SocketChannel) key.channel();
                        SocketChannel ch = channel;
                        System.out.println("通过可以获取的channel----->" + ch);

                        if (key.isConnectable()) {
                            ch.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Integer(1));
                            ch.finishConnect();
                        }

                        if (key.isReadable()) {
                            key.attach(new Integer(1));
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int len = 0;
                            while ((len = ch.read(buffer)) != 0) {
                                buffer.flip();
                                byte by[] = new byte[buffer.remaining()];
                                buffer.get(by);
                                output.write(by);
                                buffer.clear();
                            }
                            System.out.println(new String(output.toByteArray()));
                            output.close();
                        }

                        if (key.isWritable()) {
                            key.attach(new Integer(1));
                            ch.write(ByteBuffer.wrap((("client say:hi")).getBytes()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    static class ClientRunnable implements Runnable{
//
//        private SocketChannel ch;
//
//        private ClientRunnable(SocketChannel ch){
//            this.ch=ch;
//        }
//
//        @Override
//        public void run() {
//            try {
//                while(true){
//                    ch.write(ByteBuffer.wrap((("client say:hi")).getBytes()));
//                    Thread.sleep(5000);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                try {
//                    ch.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//    }

}
