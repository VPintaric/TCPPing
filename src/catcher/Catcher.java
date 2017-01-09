package catcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

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
			try{
				DataInputStream fromClient = new DataInputStream(connSocket.getInputStream());
				DataOutputStream toClient = new DataOutputStream(connSocket.getOutputStream());
				ByteBuffer bb;
				byte[] data;
				
				while(true){
					data = new byte[Integer.BYTES + Long.BYTES];
					
					fromClient.readFully(data);
					bb = ByteBuffer.wrap(data);
					
					// Read what will be the total "packet" size
					int totalPacketSize = bb.getInt();
					// Read packet number
					long packetNum = bb.getLong();
					// Read random data
					byte[] randData = new byte[totalPacketSize - Integer.BYTES - Long.BYTES];
					fromClient.readFully(randData);
					
					long timeOfRecv = System.currentTimeMillis();
					
					bb = ByteBuffer.allocate(totalPacketSize);
					// send received packet number
					bb.putLong(packetNum);
					// Send time when packet was received to the client
					bb.putLong(timeOfRecv);
					// Fill rest of the packet space with rubbish
					bb.put(randData, 0, totalPacketSize - 2 * Long.BYTES);
					
					toClient.write(bb.array());
					toClient.flush();
				}
			} catch(IOException exc){
				System.out.println("Connection to client dropped");
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
		ServerSocket s = null;
		
		try{
			if(ip_address == null)
				s = new ServerSocket(port, getLuckyBacklogNumber(), null);
			else
				s = new ServerSocket(port, getLuckyBacklogNumber()
									, InetAddress.getByName(ip_address));
		} catch(BindException exc){
			if(port < 1024)
				System.err.println("Binding failed: maybe you've given port number <1024 with no root privileges?");
			throw exc;
		}
		
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
