package pitcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Pitcher {
	private static final long MAX_ITERATION_DURATION = 1000L;
	
	Random rng;
	int port, mps, size;
	String host_name;
	Thread thisThread;
	
	/**
	 * NOTE: In case of a slow connection real messages per second rate could be
	 * lower than the "mps" parameter.
	 * 
	 * @param port number of "Catcher" this "Pitcher" will try to connect to
	 * @param mps messages sent per second to "Catcher"
	 * @param size of messages sent to "Catcher"
	 * @param host name of "Catcher"
	 */
	public Pitcher(int port, int mps, int size, String host){
		thisThread = Thread.currentThread();
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
		rng.nextBytes(data);
		return data;
	}

	private String getTimeString(double time, boolean timedOut) {
		if(timedOut)
			return "timed out";
		else
			return String.format("%.4f ms", time);
	}
	
	/**
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void run() throws Exception{
		@SuppressWarnings("resource")
		Socket s = new Socket(host_name, port);
		DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
		DataInputStream fromServer = new DataInputStream(s.getInputStream());
		
		long sentMessages = 0L;
		double realMps = mps;
		while(true){
			double avgPCtime = 0., avgCPtime = 0., avgPCPtime = 0.;
			long  maxPCtime = 0L, maxCPtime = 0L, maxPCPtime = 0L;
			
			long startTimeThisIter = System.currentTimeMillis();
			long messagesThisIter = 0L;
			
			boolean serverTimeOut = false;
			while(messagesThisIter < mps && 
					System.currentTimeMillis() - startTimeThisIter < MAX_ITERATION_DURATION){
				
				// Send
				long timeOfSending = System.currentTimeMillis();
				long packetNum = sentMessages;
				
				toServer.writeInt(size);
				toServer.writeLong(packetNum);
				byte[] data = generateRandomData(size - 2 * Integer.BYTES);
				toServer.write(data);
				
				messagesThisIter++;
				sentMessages++;
				
				// Receive
				s.setSoTimeout((int) (MAX_ITERATION_DURATION - (System.currentTimeMillis() - startTimeThisIter)));
				long timeOfRecvC;
				try{
					long recvPacketNum;
					do{
						recvPacketNum = fromServer.readLong();
						timeOfRecvC = fromServer.readLong();
						fromServer.readFully(data, 0, size - 2 * Long.BYTES);
					}while(recvPacketNum != packetNum);
				} catch(SocketTimeoutException exc){
					if(messagesThisIter == 1){ // this means that server didn't answer at all this second
						serverTimeOut = true;
						System.err.println("No answer from server for packet number #" + packetNum);
					}
					break;
				}
				
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
			}
			// in case the iteration finished before MAX_ITERATION_DURATION ms passed
			Thread.sleep(Math.max(MAX_ITERATION_DURATION - (System.currentTimeMillis() - startTimeThisIter), 0L));
		
			long thisIterDuration = System.currentTimeMillis() - startTimeThisIter;
			realMps = (double) messagesThisIter / thisIterDuration * 1000;
			
			if(!serverTimeOut){
				avgPCtime /= messagesThisIter;
				avgCPtime /= messagesThisIter;
				avgPCPtime /= messagesThisIter;
			}
			
			// Printing out the statistics
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			System.out.println("Time: " + format.format(date));

			System.out.println("Total messages sent: " + sentMessages);
			System.out.printf("Messages per second: %.2f\n", realMps);
			
			System.out.println("Pitcher->Catcher average time: " + getTimeString(avgPCtime, serverTimeOut));
			System.out.println("Catcher->Pitcher average time: " + getTimeString(avgCPtime, serverTimeOut));
			System.out.println("Pitcher->Catcher->Pitcher average time: " + getTimeString(avgPCPtime, serverTimeOut));
			
			System.out.println("Pitcher->Catcher max time: " + getTimeString(maxPCtime, serverTimeOut));
			System.out.println("Catcher->Pitcher max time: " + getTimeString(maxCPtime, serverTimeOut));
			System.out.println("Pitcher->Catcher->Pitcher max time: " + getTimeString(maxPCPtime, serverTimeOut));
			
			System.out.println("-----------------------------------");
		}
	}
}
