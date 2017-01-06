package pitcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import com.sun.org.apache.bcel.internal.classfile.Constant;

public class Pitcher {
	Random rng;
	int port, mps, size;
	String host_name;
	
	/**
	 * 
	 * @param port number of "Catcher" this "Pitcher" will try to connect to
	 * @param mps messages sent per second to "Catcher"
	 * @param size of messages sent to "Catcher"
	 * @param host name of "Catcher"
	 */
	public Pitcher(int port, int mps, int size, String host){
		this.host_name = host;
		this.port = port;
		this.mps = mps;
		this.size = size;
		rng = new Random();
	}
	
	/**
	 * 
	 * @return a randomly generated array of bytes
	 */
	private byte[] generateRandomData(int size){
		byte[] data = new byte[size];
		for(int i = 0; i < size; i++)
			data[i] = 'a';
		return data;
		//byte[] data = new byte[size];
		//rng.nextBytes(data);
		//return data;
	}
	
	/**
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void run() throws Exception{
		Socket s = new Socket(host_name, port);
		DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
		DataInputStream fromServer = new DataInputStream(s.getInputStream());
		
		long sentMessages = 0L;
		long waitTime = 1000L / mps;
		while(true){
			double avgPCtime = 0., avgCPtime = 0., avgPCPtime = 0.;
			long  maxPCtime = 0L, maxCPtime = 0L, maxPCPtime = 0L;
			
			for(int i = 0; i < mps; i++){
				// Send
				toServer.writeInt(size);
				toServer.writeLong(0L);
				byte[] data = generateRandomData(size - (Integer.SIZE + Long.SIZE) / Byte.SIZE);
				toServer.write(data);
				
				long timeOfSending = System.currentTimeMillis();
				
				sentMessages++;
				
				// Receive
				int recvInt = fromServer.readInt();
				long timeOfRecvC = fromServer.readLong();
				fromServer.readFully(data);
				
				long timeOfRecvP = System.currentTimeMillis();
				
				long PCtime = timeOfRecvC - timeOfSending;
				long CPtime = timeOfRecvP - timeOfRecvC;
				long PCPtime = timeOfRecvP - timeOfSending;
				
				avgPCtime += PCtime;
				avgCPtime += CPtime;
				avgPCPtime += PCPtime;
				
				maxPCtime = Math.max(maxPCtime, PCtime);
				maxCPtime = Math.max(maxCPtime, CPtime);
				maxPCPtime = Math.max(maxPCPtime, PCPtime);
				
				Thread.sleep(waitTime - PCPtime);
			}
			
			avgPCtime /= mps;
			avgCPtime /= mps;
			avgPCPtime /= mps;
			
			Calendar calendar = Calendar.getInstance();
			System.out.println("Time: " + calendar.get(Calendar.HOUR_OF_DAY) + ":"
											+ calendar.get(Calendar.MINUTE) + ":"
											+ calendar.get(Calendar.SECOND));
			System.out.println("Total messages sent: " + sentMessages);
			System.out.println("Messages per second: " + mps);
			
			System.out.println("A->B average time: " + avgPCtime);
			System.out.println("B->A average time: " + avgCPtime);
			System.out.println("A->B->A average time: " + avgPCPtime);
			
			System.out.println("A->B max time: " + maxPCtime);
			System.out.println("B->A max time: " + maxCPtime);
			System.out.println("A->B->A max time: " + maxPCPtime);
			
			System.out.println("-----------------------------------");
		}
	}
}
