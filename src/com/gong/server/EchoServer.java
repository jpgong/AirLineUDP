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
	// jdk���ṩ���̳߳�
	private ExecutorService executorService;
	// ����CPUʱ�̳߳��й����̵߳���Ŀ
	private final int POOL_SIZE = 4;

	public EchoServer() throws IOException {
		serverSocket = new ServerSocket(port);
		// �����̳߳�
		// Runtime��availableProcessors()���ص�ǰϵͳ��CPU����Ŀ
		// ϵͳCPUԽ�࣬�̳߳��й����߳���ĿҲԽ��
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		System.out.println("����������");
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
	 * ����һ����Ӧ��Ϣ�����࣬�������ж��������
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
				// �����������������������������Ϣ�ͶԿͷ��˻�Ӧ
				Scanner scanner = new Scanner(socket.getInputStream());
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				System.out.println("new connection accepted��" + socket.getInetAddress() + ":" + socket.getPort());
				while (scanner.hasNext()) {
					String msg = scanner.nextLine();
					System.out.println("�յ���Ϣ��" + msg);
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
