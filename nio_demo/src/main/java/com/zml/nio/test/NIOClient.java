package com.zml.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * @author: maylor
 * @date: 2021/4/21 11:27
 * @description:
 */
public class NIOClient {

	public static void main(String[] args) throws IOException {
		//创建远程地址
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8888);
		SocketChannel channel = null;
		//定义缓存
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			//开启通道
			channel = SocketChannel.open();
			//连接远程远程服务器
			channel.connect(address);
			Scanner sc = new Scanner(System.in);
			while (true) {
				System.out.println("客户端即将给 服务器发送数据..");
				String line = sc.nextLine();
				if (line.equals("exit")) {
					break;
				}
				//控制台输入数据写到缓存
				buffer.put(line.getBytes("UTF-8"));
				//重置buffer 游标
				buffer.flip();
				//数据发送到数据
				channel.write(buffer);
				//清空缓存数据
				buffer.clear();
				//读取服务器返回的数据
				int readLen = channel.read(buffer);
				if (readLen == -1) {
					break;
				}
				//重置buffer游标
				buffer.flip();
				byte[] bytes = new byte[buffer.remaining()];
				//读取数据到字节数组
				buffer.get(bytes);
				System.out.println("收到了服务器发送的数据 : " + new String(bytes, "UTF-8"));
				buffer.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != channel) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
