package com.zml.bio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: maylor
 * @date: 2021/4/21 11:27
 * @description:
 */
public class BIOServer {
	public static void main(String[] args) throws Exception {
		//首先创建了一个serverSocket
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("127.0.0.1", 8081));
		while (true) {
			Socket socket = serverSocket.accept();
			//同步阻塞
			new Thread(() -> {
				try {
					byte[] bytes = new byte[1024];
					int len = socket.getInputStream().read(bytes); //同步阻塞
					System.out.println(new String(bytes, 0, len));
					socket.getOutputStream().write(bytes, 0, len);
					socket.getOutputStream().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
}
