package catcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Catcher {
	String ip_address;
	int port;
	
	public Catcher(int port, String ip_address){
		this.port = port;
		this.ip_address = ip_address;
	}
	
	public void run() throws IOException{
		ServerSocket s = new ServerSocket();
		s.bind(new InetSocketAddress(ip_address, port));
		
		while(true){
			Socket connSocket = s.accept(); // TODO: add multithreading
			while(true){
				try{
					DataInputStream fromClient = new DataInputStream(connSocket.getInputStream());
					DataOutputStream toClient = new DataOutputStream(connSocket.getOutputStream());
					
					int totalPacketSize = fromClient.readInt();
					fromClient.readLong();
					byte[] data = new byte[totalPacketSize - (Integer.SIZE + Long.SIZE) / Byte.SIZE];
					fromClient.readFully(data);
					
					long timeOfRecv = System.currentTimeMillis();
					
					toClient.writeInt(totalPacketSize);
					toClient.writeLong(timeOfRecv);
					toClient.write(data);
				}
				catch(IOException exc){ // in case connection gets dropped, wait for another client to connect
					break;
				}
			}
		}
	}
}
