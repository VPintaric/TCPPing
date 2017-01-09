package pitcher;

public class PitcherData {
	public long totalSentMessages, totalSuccessfulMessages;
	public long messagesThisSecond, successfulMessagesThisSecond;
	public long maxPCtime, maxCPtime, maxPCPtime;
	public long sumPCtime, sumCPtime, sumPCPtime;
	
	
	public PitcherData(){
		totalSentMessages = messagesThisSecond = 0L;
		maxPCtime = maxCPtime = maxPCPtime = 0L;
		sumPCtime = sumCPtime = sumPCPtime = 0L;
	}
	
	public void reset(){
		messagesThisSecond = successfulMessagesThisSecond = 0L;
		sumPCtime = sumCPtime = sumPCPtime = 0L;
	}
}
