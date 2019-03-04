package com.gong.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	// jdk中提供的线程池
	private ExecutorService executorService;
	// 单个CPU时线程池中工作线程的数目
	private final int POOL_SIZE = 4;

	public EchoServer() throws IOException {
		serverSocket = new ServerSocket(port);
		// 创建线程池
		// Runtime的availableProcessors()返回当前系统中CPU的数目
		// 系统CPU越多，线程池中工作线程数目也越多
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		System.out.println("服务器启动");
	}

	public void service() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				executorService.execute(new Handle(socket));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}

	/**
	 * 定义一个响应消息进程类，用来进行多进程任务
	 * 
	 * @author jpgong
	 *
	 */
	class Handle implements Runnable {
		private Socket socket;

		public Handle(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				// 定义输入流和输出流，用来接收消息和对客服端回应
				Scanner scanner = new Scanner(socket.getInputStream());
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				System.out.println("new connection accepted：" + socket.getInetAddress() + ":" + socket.getPort());
				while (scanner.hasNext()) {
					String msg = scanner.nextLine();
					System.out.println("收到消息：" + msg);
					pw.println("Echo: " + msg);
					if (msg.equals("bye")) {
						break;
					}
				}
				scanner.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
