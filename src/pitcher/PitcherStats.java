package pitcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class PitcherStats extends TimerTask {
	PitcherData pd;

	public PitcherStats(PitcherData pd){
		this.pd = pd;
	}
	
	private String getTimeString(double time, boolean timedOut) {
		if(timedOut)
			return "t/o";
		else
			return String.format("%.2f ms", time);
	}
	
	@Override
	public void run() {
		synchronized (pd) {
			boolean timedOut = pd.successfulMessagesThisSecond == 0;
			double mps = (double) pd.messagesThisSecond / Pitcher.PERIOD_OF_STATS_PRINTING * 1000.;
			double totalPacketLoss = (1. - (double) pd.totalSuccessfulMessages / pd.totalSentMessages) * 100.;
			double packetLossLastSec = (1. - (double) pd.successfulMessagesThisSecond / pd.messagesThisSecond) * 100.;
			
			double avgPCtime = 0., avgCPtime = 0., avgPCPtime = 0.;
			if(!timedOut){
				avgPCtime = (double) pd.sumPCtime / pd.messagesThisSecond;
				avgCPtime = (double) pd.sumCPtime / pd.messagesThisSecond;
				avgPCPtime = (double) pd.sumPCPtime / pd.messagesThisSecond;
			}
			
			Date date = new Date();
			SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
			System.out.printf("[%s] [total_sent_packets %d] [packet_loss %d%%] [sent_packets_per_second %.2f] [packet_loss_last_sec %d%%]\n", 
								f.format(date), pd.totalSentMessages, (int) totalPacketLoss, mps, (int) packetLossLastSec);
			System.out.printf("[avg_times [P->C %s] [C->P %s] [P->C->P %s]] [max_times [P->C %s] [C->P %s] [P->C->P %s]]\n",
								getTimeString(avgPCtime, timedOut), getTimeString(avgCPtime, timedOut), getTimeString(avgPCPtime, timedOut),
								getTimeString(pd.maxPCtime, false), getTimeString(pd.maxCPtime, false), getTimeString(pd.maxPCPtime, false));
			
			pd.reset();
		}
	}

}
