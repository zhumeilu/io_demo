package com.zml.nio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author: maylor
 * @date: 2021/4/21 11:27
 * @description:
 */
public class NIOServer extends Thread {

	//1.声明多路复用器
	private Selector   selector;
	//2.定义读写缓冲区
	private ByteBuffer readBuffer  = ByteBuffer.allocate(1024);
	private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

	//3.定义构造方法初始化端口
	public NIOServer(int port) {
		init(port);
	}

	//4.main方法启动线程
	public static void main(String[] args) {
		new Thread(new NIOServer(8888)).start();
	}

	//5.初始化
	private void init(int port) {
		try {
			System.out.println("服务器正在启动......");
			//1)开启多路复用器
			this.selector = Selector.open();
			//2) 开启服务通道
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			//3)设置为非阻塞
			serverSocketChannel.configureBlocking(false);
			//4)绑定端口
			serverSocketChannel.bind(new InetSocketAddress(port));
			//5)注册,标记服务通标状态
			serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
			System.out.println("服务器启动完毕");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override public void run() {
		while (true) {
			try {
				//1.当有至少一个通道被选中,执行此方法
				this.selector.select();
				//2.获取选中的通道编号集合
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
				//3.遍历keys
				while (keys.hasNext()) {
					SelectionKey key = keys.next(); //4.当前key需要从动刀集合中移出,如果不移出,下次循环会执行对应的逻辑,造成业务错乱
					keys.remove(); //5.判断通道是否有效
					if (key.isValid()) {
						try {//6.判断是否可读
							if (key.isAcceptable()) {
								accept(key);
							}
						} catch (CancelledKeyException e) {
							//出现异常断开连接
							key.cancel();
						}
						try {
							//7.判断是否可读
							if (key.isReadable()) {
								read(key);
							}
						} catch (CancelledKeyException e) {
							//出现异常断开连接
							key.cancel();
						}
						try {
							//8.判断是否可写
							if (key.isWritable()) {
								write(key);
							}
						} catch (CancelledKeyException e) {
							//出现异常断开连接
							key.cancel();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void accept(SelectionKey key) {
		try {
			//1.当前通道在init方法中注册到了selector中的ServerSocketChannel
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			//2.阻塞方法, 客户端发起后请求返回.
			SocketChannel channel = serverSocketChannel.accept();
			///3.serverSocketChannel设置为非阻塞
			channel.configureBlocking(false);
			//4.设置对应客户端的通道标记,设置次通道为可读时使用
			channel.register(this.selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//使用通道读取数据
	private void read(SelectionKey key) {
		try {
			//清空缓存
			this.readBuffer.clear();
			//获取当前通道对象
			SocketChannel channel = (SocketChannel) key.channel();
			//将通道的数据(客户发送的data)读到缓存中.
			int readLen = channel.read(readBuffer);
			//如果通道中没有数据
			if (readLen == -1) {
				//关闭通道
				key.channel().close();
				//关闭连接
				key.cancel();
				return;
			}
			//Buffer中有游标,游标不会重置,需要我们调用flip重置. 否则读取不一致
			this.readBuffer.flip();
			//创建有效字节长度数组
			byte[] bytes = new byte[readBuffer.remaining()];
			//读取buffer中数据保存在字节数组
			readBuffer.get(bytes);
			System.out.println("收到了从客户端 " + channel.getRemoteAddress() + " : " + new String(bytes, "UTF-8"));
			//注册通道,标记为写操作
			channel.register(this.selector, SelectionKey.OP_WRITE);
		} catch (Exception e) {
		}
	}

	//给通道中写操作
	private void write(SelectionKey key) {
		//清空缓存
		this.readBuffer.clear();
		//获取当前通道对象
		SocketChannel channel = (SocketChannel) key
				.channel();
		//录入数据
		Scanner scanner = new Scanner(System.in);
		try {
			System.out.println("即将发送数据到客户端..");
			String line = scanner.nextLine();
			//把录入的数据写到Buffer中
			writeBuffer.put(line.getBytes("UTF-8"));
			//重置缓存游标
			writeBuffer.flip();
			channel.write(writeBuffer);
			channel.register(this.selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
