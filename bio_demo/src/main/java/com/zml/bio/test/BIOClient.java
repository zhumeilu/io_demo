package com.zml.bio.test;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: maylor
 * @date: 2021/4/21 11:27
 * @description:
 */
public class BIOClient {

	public static void main(String[] args) throws IOException {
		Socket socket = new Socket("127.0.0.1", 8081);
		socket.getOutputStream().write("hello".getBytes());
		socket.getOutputStream().flush();
		System.out.println("server send back data =====");
		byte[] bytes = new byte[1024];
		int len = socket.getInputStream().read(bytes);
		System.out.println(new String(bytes, 0, len));
		socket.close();
	}
}
