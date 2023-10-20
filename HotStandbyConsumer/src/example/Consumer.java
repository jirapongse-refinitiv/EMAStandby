package example;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.refinitiv.ema.access.AckMsg;
import com.refinitiv.ema.access.EmaFactory;
import com.refinitiv.ema.access.GenericMsg;
import com.refinitiv.ema.access.Msg;
import com.refinitiv.ema.access.OmmConsumer;
import com.refinitiv.ema.access.OmmConsumerClient;
import com.refinitiv.ema.access.OmmConsumerConfig;
import com.refinitiv.ema.access.OmmConsumerEvent;
import com.refinitiv.ema.access.OmmState;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.ReqMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;
import com.refinitiv.ema.access.DataType.DataTypes;
import com.refinitiv.ema.rdm.EmaRdm;

public class Consumer implements OmmConsumerClient {
	
	private boolean isActive;
	private  LinkedList<Msg> cache = new LinkedList<Msg>();
	private ConsumerGroup proxy;
	private OmmConsumer consumer;
	private String name;
	private boolean isConnected;
	
	public Consumer(String consumername,  ConsumerGroup group, boolean active) {
		proxy = group;
		name = consumername;
		isConnected = false;
		isActive = active;
		OmmConsumerConfig config = EmaFactory.createOmmConsumerConfig();
		consumer =  EmaFactory.createOmmConsumer(config.consumerName(consumername), this);
		
	}

	synchronized private void processRefreshMessage(RefreshMsg refreshMsg) {
		if(IsActive()) {
			System.out.println(name);
			proxy.onRefreshMsg(refreshMsg);
		}else {			
			PutMsgToCache(EmaFactory.createRefreshMsg(refreshMsg));
		}
	 }
	
	synchronized private void processUpdateMessage(UpdateMsg updateMsg) {
		if(IsActive()) {
			System.out.println(name);
			proxy.onUpdateMsg(updateMsg);			
		}else {
			PutMsgToCache(EmaFactory.createUpdateMsg(updateMsg));
		}
	 }

	synchronized public void flushCache(long seqNum, boolean active) 
	{
		
			boolean founded = false;
		
			for (Msg msg : cache) { 
	            if(msg.dataType() == DataTypes.REFRESH_MSG) {
	            	RefreshMsg refreshMsg = (RefreshMsg) msg;
	            	if(founded == true) {
	            		proxy.onRefreshMsg(refreshMsg);
	            	}
	            	else if(refreshMsg.seqNum() == seqNum) {
	            		founded=true;
	            	}
	            	
	            	
	            }else if (msg.dataType() == DataTypes.UPDATE_MSG) {
	            	UpdateMsg updateMsg = (UpdateMsg) msg;
	            	if(founded == true) {
	            		proxy.onUpdateMsg(updateMsg);
	            	}else if(updateMsg.seqNum() == seqNum) {
	            		founded=true;
	            	}
	            }
				
	        } 
			
			cache.clear();
			if(active) {
				SetActive();
			}else {
				SetStandby();
			}
		
	}
	public long registerClient(ReqMsg reqMsg) 
	{
		return consumer.registerClient(reqMsg, this);
	}
	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)
	{
		
		//System.out.println(refreshMsg);
		if(refreshMsg.domainType() == EmaRdm.MMT_MARKET_PRICE) {	
			processRefreshMessage(refreshMsg);
		}else if(refreshMsg.domainType() == EmaRdm.MMT_LOGIN) {
			checkLoginStatus(refreshMsg);
		}
	}
	private void checkLoginStatus(Msg msg) {
		
		if(msg.dataType() == DataTypes.REFRESH_MSG) {
			RefreshMsg refreshMsg = (RefreshMsg)msg;
			OmmState state = refreshMsg.state();
			if(state.streamState() == OmmState.StreamState.CLOSED || state.streamState() == OmmState.StreamState.CLOSED_RECOVER) {
				isConnected = false;
				if(IsActive()) {
					proxy.onDisconnect(this);
				}				
			}else if (state.streamState() == OmmState.StreamState.OPEN && state.dataState() == OmmState.DataState.SUSPECT) {
				isConnected = false;
				if(IsActive()) {
					proxy.onDisconnect(this);
				}
			}else if (state.streamState() == OmmState.StreamState.OPEN && state.dataState() == OmmState.DataState.OK) {
				isConnected = true;
				cache.clear();
				if(IsActive()==false)					
					proxy.onConnect(this);
			}
		}else if(msg.dataType() == DataTypes.STATUS_MSG) {
			StatusMsg statusMsg = (StatusMsg)msg;
			if(statusMsg.hasState()) {
			OmmState state = statusMsg.state();
				if(state.streamState() == OmmState.StreamState.CLOSED || state.streamState() == OmmState.StreamState.CLOSED_RECOVER) {
					isConnected = false;
					if(IsActive()) {
						proxy.onDisconnect(this);
					}
				}else if (state.streamState() == OmmState.StreamState.OPEN && state.dataState() == OmmState.DataState.SUSPECT) {
					isConnected = false;
					if(IsActive()) {
						proxy.onDisconnect(this);
					}
				}else if (state.streamState() == OmmState.StreamState.OPEN && state.dataState() == OmmState.DataState.OK) {
					isConnected = true;
					cache.clear();
					if(IsActive()==false)						
						proxy.onConnect(this);
				}
			}
		}
	}
	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event) 
	{
		
		//System.out.println(updateMsg);
		if(updateMsg.domainType() == EmaRdm.MMT_MARKET_PRICE) {	
			processUpdateMessage(updateMsg);
		}
	}
	

	public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event) 
	{
		
		//System.out.println(statusMsg);
		if(statusMsg.domainType() == EmaRdm.MMT_MARKET_PRICE) {	
//			if(IsActive()) {
//				System.out.println(name);
//				proxy.onStatusMsg(statusMsg);			
//			}else {
//				PutMsgToCache(EmaFactory.createStatusMsg(statusMsg));
//			}
		}else if(statusMsg.domainType() == EmaRdm.MMT_LOGIN) {
			checkLoginStatus(statusMsg);
		}
		
	}

	public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
	public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
	public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent){}
	
	public void SetActive() {isActive = true;}
	public void SetStandby() {isActive = false;}
	
	public boolean IsActive() {return isActive;}
	public boolean IsConnected() {return isConnected;}
	
	private void PutMsgToCache(Msg msg) {
		synchronized(cache) {
			cache.add(msg);
		}
	}
}
