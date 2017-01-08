package pitcher;

public class PitcherData {
	public long totalSentMessages;
	public long messagesThisSecond;
	public long maxPCtime, maxCPtime, maxPCPtime;
	public long sumPCtime, sumCPtime, sumPCPtime;
	
	
	public PitcherData(){
		totalSentMessages = messagesThisSecond = 0L;
		maxPCtime = maxCPtime = maxPCPtime = 0L;
		sumPCtime = sumCPtime = sumPCPtime = 0L;
	}
	
	public void reset(){
		messagesThisSecond = 0L;
		maxPCtime = maxCPtime = maxPCPtime = 0L;
		sumPCtime = sumCPtime = sumPCPtime = 0L;
	}
}
