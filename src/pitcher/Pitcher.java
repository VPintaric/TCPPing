package pitcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;

public class Pitcher {
	// could use this time out duration when server doesn't respond
	//public static final int TIME_OUT_DURATION = 1000;
	public static final long PERIOD_OF_STATS_PRINTING = 1000L;
	
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
		
		PitcherData pd = new PitcherData();
		long minTimePerLoop = 1000L / mps;
		long packetNum = 0L;
		
		PitcherStats ps = new PitcherStats(pd);
		
		Timer timer = new Timer();
		timer.schedule(ps, PERIOD_OF_STATS_PRINTING, PERIOD_OF_STATS_PRINTING);
		
		while(true){
			long startTimeThisIter = System.currentTimeMillis();
			
			// Send
			toServer.writeInt(size);
			toServer.writeLong(packetNum++);
			byte[] data = generateRandomData(size - 2 * Integer.BYTES);
			toServer.write(data);
			long timeOfSending = System.currentTimeMillis();
			
			// Receive
			s.setSoTimeout((int) minTimePerLoop);
			long timeOfRecvC;
			try{
				long recvPacketNum;
				do{
					recvPacketNum = fromServer.readLong();
					timeOfRecvC = fromServer.readLong();
					fromServer.readFully(data, 0, size - 2 * Long.BYTES);
				}while(recvPacketNum != packetNum - 1);
			} catch(SocketTimeoutException exc){
				System.err.println("No answer from server for packet number #" + packetNum);
				continue;
			}
			long timeOfRecvP = System.currentTimeMillis();
			
			long PCtime = timeOfRecvC - timeOfSending;
			long CPtime = timeOfRecvP - timeOfRecvC;
			long PCPtime = timeOfRecvP - timeOfSending;
			
			synchronized (pd) {
				pd.totalSentMessages++;
				pd.messagesThisSecond++;
				
				pd.maxPCtime = Math.max(pd.maxPCtime, PCtime);
				pd.maxCPtime = Math.max(pd.maxCPtime, CPtime);
				pd.maxPCPtime = Math.max(pd.maxPCPtime, PCPtime);
				
				pd.sumPCtime += PCtime;
				pd.sumCPtime += CPtime;
				pd.sumPCPtime += PCPtime;
			}
			
			long excessTime = Math.max(minTimePerLoop - (System.currentTimeMillis() - startTimeThisIter), 0L);
			Thread.sleep(excessTime);
		}
	}
}
