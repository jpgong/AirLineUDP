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
		//与本地的任意一个UDP端口绑定
		socket = new DatagramSocket();
		System.out.println("客户端启动！");
	}
	
	public void talk() {
		InetSocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
		
		String msg = null;
		
		try {
			msg = "发送数据";
			//将字符串转化为字节数组
			byte[] sendData = msg.getBytes();
			
			//建立一个发送数据的UDP数据报
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socketAddress);
			socket.send(sendPacket);
			
			while(true) {
				//在许多UDP协议中，规定数据报的长度不超过512
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
