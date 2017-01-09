package pitcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;
import java.nio.ByteBuffer;

public class Pitcher {
	// could use this time out duration when server doesn't respond
	//public static final int TIME_OUT_DURATION = 1000;
	public static final long PERIOD_OF_STATS_PRINTING = 1000L;
	
	Random rng;
	int port, mps, size;
	String host_name;
	Thread thisThread;
	
	byte[] randData;
	
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
	private byte[] generateRandomBytes(int size){
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
		byte[] data = new byte[size];
		ByteBuffer bb;
		
		PitcherData pd = new PitcherData();
		long minTimePerLoop = 1000L / mps;
		long packetNum = 0;
		
		PitcherStats ps = new PitcherStats(pd);
		
		Timer timer = new Timer();
		timer.schedule(ps, PERIOD_OF_STATS_PRINTING, PERIOD_OF_STATS_PRINTING);
		
		while(true){
			long startTimeThisIter = System.currentTimeMillis();
			
			// Send
			bb = ByteBuffer.allocate(size);
			bb.putInt(size);
			bb.putLong(packetNum++);
			bb.put(generateRandomBytes(size - Integer.BYTES - Long.BYTES));
			
			toServer.write(bb.array());
			toServer.flush();
			
			long timeOfSending = System.currentTimeMillis();
			
			synchronized (pd) {
				pd.messagesThisSecond++;
				pd.totalSentMessages++;
			}
			
			// Receive
			s.setSoTimeout((int) minTimePerLoop);
			long timeOfRecvC;
			try{
				long recvPacketNum;
				do{
					fromServer.readFully(data);
					bb = ByteBuffer.wrap(data);
					recvPacketNum = bb.getLong();
					timeOfRecvC = bb.getLong();
					// no need to read rest of the data
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
				pd.totalSuccessfulMessages++;
				pd.successfulMessagesThisSecond++;
				
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
