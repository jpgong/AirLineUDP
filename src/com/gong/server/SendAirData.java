package com.gong.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendAirData {
	private int port = 9999;
	private DatagramSocket socket;
	// jdk中提供的线程池
	private static ExecutorService executorService;
	// 单个CPU时线程池中工作线程的数目
	private final int POOL_SIZE = 4;
	private RandomAccessFile raf;
	
	public SendAirData(){
		try {
			socket = new DatagramSocket(port);
			// 创建线程池
			// Runtime的availableProcessors()返回当前系统中CPU的数目
			// 系统CPU越多，线程池中工作线程数目也越多
			executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
			raf = new RandomAccessFile("fds_data.txt", "r");
			System.out.println("该系统中CPU的数目：" + Runtime.getRuntime().availableProcessors());
			System.out.println("服务器启动");
		} catch (SocketException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void service(){
		while (true) {
			try {
				DatagramPacket packet = null;
				packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				executorService.execute(new Handle(packet));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new SendAirData().service();
	}

	/**
	 * 定义一个响应消息进程类，用来进行多进程任务
	 * 
	 * @author jpgong
	 *
	 */
	class Handle implements Runnable {
		private DatagramPacket packet;
		private long position;
		
		
		public Handle(DatagramPacket packet) {
			this.packet = packet;
			position = -1;
		}

		@Override
		public void run() {
			try {
				// 为新来的客户端创建一个文件输入流
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data.txt")));
				int count = 0;
				while (br.readLine() != null) {
					count++;
				}
				
				String line = null;
				if ( position == -1) {
					System.out.println("new connection accepted：" + packet.getAddress() + ":" + packet.getPort() + "发来数据请求。");
					line = "共有" + count + "条航班数据待处理\r\n";
					position = 0;
				}else {
					raf.seek(position);
					line = raf.readLine() + "\r\n";
					position = raf.getFilePointer();
				}
				packet.setData(line.getBytes());
				Thread.sleep(10);
				socket.send(packet);
				
				if (position < raf.length()) {
					SendAirData.executorService.execute(this);
				}else {
					line = "no data!\r\n";
					packet.setData(line.getBytes());
					Thread.sleep(10);
					socket.send(packet);
					System.out.println("对客户端" + packet.getAddress() + "的所有数据已经发送完毕");
					br.close();
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
