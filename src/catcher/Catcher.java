package catcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Implementation of server-side program for TCPPing protocol.
 * @author V. Pintaric
 *
 */
public class Catcher {
	
	/**
	 * Thread implementation for multithreaded server.
	 * @author V. Pintaric
	 *
	 */
	private class ServingThread implements Runnable{
		Socket connSocket;
		
		/**
		 * @param connSocket - active connection socket to client
		 */
		public ServingThread(Socket connSocket){
			this.connSocket = connSocket;
		}
	
		@Override
		public void run() {
			while(true){
				try{
					DataInputStream fromClient = new DataInputStream(connSocket.getInputStream());
					DataOutputStream toClient = new DataOutputStream(connSocket.getOutputStream());
					
					// Read what will be the total "packet" size
					int totalPacketSize = fromClient.readInt();
					// Read rest of the data which is actually irrelevant
					byte[] data = new byte[totalPacketSize - Integer.SIZE / Byte.SIZE];
					fromClient.readFully(data);
					
					long timeOfRecv = System.currentTimeMillis();
					
					// Send time when packet was received to the client
					toClient.writeLong(timeOfRecv);
					// Fill rest of the packet space with rubbish
					toClient.write(data, 0, totalPacketSize - Long.SIZE / Byte.SIZE);
				}
				catch(IOException exc){ // in case connection gets dropped end this thread
					break;
				}
			}
		}
		
	};
	
	String ip_address;
	int port;
	
	public Catcher(int port, String ip_address){
		this.port = port;
		this.ip_address = ip_address;
	}
	
	/**
	 * 
	 * @return a perfect backlog number :)
	 */
	private int getLuckyBacklogNumber(){
		return 42;
	}
	
	public void run() throws IOException{
		ServerSocket s;
		if(ip_address == null)
			s = new ServerSocket(port, getLuckyBacklogNumber(), null);
		else
			s = new ServerSocket(port, getLuckyBacklogNumber()
								, InetAddress.getByName(ip_address));
		
		while(true){
			Socket connSocket;
			try{
				connSocket = s.accept();
			} catch(Exception exc){
				break;
			}
			new Thread(new ServingThread(connSocket)).start();
		}
		
		s.close();
	}
}
