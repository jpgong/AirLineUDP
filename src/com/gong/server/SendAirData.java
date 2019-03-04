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
	// jdk���ṩ���̳߳�
	private static ExecutorService executorService;
	// ����CPUʱ�̳߳��й����̵߳���Ŀ
	private final int POOL_SIZE = 4;
	private RandomAccessFile raf;
	
	public SendAirData(){
		try {
			socket = new DatagramSocket(port);
			// �����̳߳�
			// Runtime��availableProcessors()���ص�ǰϵͳ��CPU����Ŀ
			// ϵͳCPUԽ�࣬�̳߳��й����߳���ĿҲԽ��
			executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
			raf = new RandomAccessFile("fds_data.txt", "r");
			System.out.println("��ϵͳ��CPU����Ŀ��" + Runtime.getRuntime().availableProcessors());
			System.out.println("����������");
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
	 * ����һ����Ӧ��Ϣ�����࣬�������ж��������
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
				// Ϊ�����Ŀͻ��˴���һ���ļ�������
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fds_data.txt")));
				int count = 0;
				while (br.readLine() != null) {
					count++;
				}
				
				String line = null;
				if ( position == -1) {
					System.out.println("new connection accepted��" + packet.getAddress() + ":" + packet.getPort() + "������������");
					line = "����" + count + "���������ݴ�����\r\n";
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
					System.out.println("�Կͻ���" + packet.getAddress() + "�����������Ѿ��������");
					br.close();
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
