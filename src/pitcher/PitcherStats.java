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
			boolean timedOut = pd.messagesThisSecond == 0;
			double mps = (double) pd.messagesThisSecond / Pitcher.PERIOD_OF_STATS_PRINTING * 1000.;
			
			double avgPCtime = 0., avgCPtime = 0., avgPCPtime = 0.;
			if(!timedOut){
				avgPCtime = (double) pd.sumPCtime / pd.messagesThisSecond;
				avgCPtime = (double) pd.sumCPtime / pd.messagesThisSecond;
				avgPCPtime = (double) pd.sumPCPtime / pd.messagesThisSecond;
			}
			
			Date date = new Date();
			SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
			System.out.printf("[%s] [total_successful_packets %d] [successful_packets_per_second %.2f]\n", 
								f.format(date), pd.totalSentMessages, mps);
			System.out.printf("[avg_times [P->C %s] [C->P %s] [P->C->P %s]] [max_times [P->C %s] [C->P %s] [P->C->P %s]]\n",
								getTimeString(avgPCtime, timedOut), getTimeString(avgCPtime, timedOut), getTimeString(avgPCPtime, timedOut),
								getTimeString(pd.maxPCtime, timedOut), getTimeString(pd.maxCPtime, timedOut), getTimeString(pd.maxPCPtime, timedOut));
			
			pd.reset();
		}
	}

}
