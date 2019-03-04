package com.gong.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ReceiveAirData {
	private String remoteHost = "10.8.37.246";
	private int remotePort = 9999;
	private DatagramSocket socket;
	
	public ReceiveAirData() throws SocketException {
		//�뱾�ص�����һ��UDP�˿ڰ�
		socket = new DatagramSocket();
		System.out.println("�ͻ���������");
	}
	
	public void talk() {
		InetSocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
		
		String msg = null;
		
		try {
			msg = "��������";
			//���ַ���ת��Ϊ�ֽ�����
			byte[] sendData = msg.getBytes();
			
			//����һ���������ݵ�UDP���ݱ�
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socketAddress);
			socket.send(sendPacket);
			
			while(true) {
				//�����UDPЭ���У��涨���ݱ��ĳ��Ȳ�����512
				DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
				socket.receive(receivePacket);
				System.out.println(new String(receivePacket.getData(), 0, receivePacket.getLength()));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally{
			socket.close();
		}
	}
	public static void main(String[] args) throws SocketException {
		new ReceiveAirData().talk();
	}
}
