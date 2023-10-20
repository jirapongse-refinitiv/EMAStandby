package example;

import com.refinitiv.ema.access.OmmConsumerEvent;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.ReqMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;

public class ConsumerGroup {
	
	Consumer consumer1;
	Consumer consumer2;
	private ConsumerGroupClient client;
	private long seqNum=0;

	public void onRefreshMsg(RefreshMsg refreshMsg)
	{
		//System.out.println(refreshMsg);
		seqNum = refreshMsg.seqNum();
		client.onRefreshMsg(refreshMsg);
	}
	
	public void onUpdateMsg(UpdateMsg updateMsg) 
	{
		//System.out.println(updateMsg);
		seqNum = updateMsg.seqNum();
		client.onUpdateMsg(updateMsg);
	}

	public void onStatusMsg(StatusMsg statusMsg) 
	{
		//System.out.println(statusMsg);
		client.onStatusMsg(statusMsg);
	}
	
	public void onDisconnect(Consumer consumer) {
		checkNewActive(consumer);
		
	}
	public void onConnect(Consumer consumer) {
		if(consumer == consumer1) {			
			if(consumer2.IsConnected()==false)
				setConsumer1Active();
		
		}else if(consumer==consumer2) {			
			if(consumer1.IsConnected()==false)
				setConsumer2Active();	
		}
		
	}
	
	private void checkNewActive(Consumer consumer) {
		if(consumer == consumer1) {			
				setConsumer2Active();
			
		}else if(consumer==consumer2) {			
				setConsumer1Active();			
		}
	}
	public ConsumerGroup(ConsumerGroupClient cl) {
		consumer1 = new Consumer("Consumer_1", this, true);
		consumer2 = new Consumer("Consumer_2", this, false);
		client = cl;
		
		
	}
	public void registerClient(ReqMsg reqMsg) 
	{
		consumer1.registerClient(reqMsg);
		consumer2.registerClient(reqMsg);
	}
	private void setConsumer1Active() {
		consumer2.SetStandby();			
		consumer1.flushCache(seqNum, true);
		
	}
	
	private void setConsumer2Active() {
		consumer1.SetStandby();
		consumer2.flushCache(seqNum, true);
		}
	}
