package simpleexample;

import java.util.Iterator;

import com.refinitiv.ema.access.AckMsg;
import com.refinitiv.ema.access.ElementList;
import com.refinitiv.ema.access.EmaFactory;
import com.refinitiv.ema.access.FieldEntry;
import com.refinitiv.ema.access.FieldList;
import com.refinitiv.ema.access.GenericMsg;
import com.refinitiv.ema.access.Msg;
import com.refinitiv.ema.access.OmmArray;
import com.refinitiv.ema.access.OmmConsumer;
import com.refinitiv.ema.access.OmmConsumerClient;
import com.refinitiv.ema.access.OmmConsumerConfig;
import com.refinitiv.ema.access.OmmConsumerEvent;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.ReqMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;
import com.refinitiv.ema.rdm.EmaRdm;


import example.ConsumerGroup;

class AppClient implements OmmConsumerClient
{

	@Override
	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent arg1) {
		FieldList fieldList = refreshMsg.payload().fieldList();
		System.out.print("Refresh: "+refreshMsg.seqNum()+", ");
		decode(fieldList);
		
	}

	@Override
	public void onStatusMsg(StatusMsg arg0, OmmConsumerEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent arg1) {
		// TODO Auto-generated method stub
		FieldList fieldList = updateMsg.payload().fieldList();
		System.out.print("Update: "+updateMsg.seqNum()+", ");
		decode(fieldList);
		
	}
	
	@Override
	public void onAckMsg(AckMsg arg0, OmmConsumerEvent arg1) {}

	@Override
	public void onAllMsg(Msg arg0, OmmConsumerEvent arg1) {}

	@Override
	public void onGenericMsg(GenericMsg arg0, OmmConsumerEvent arg1) {}
	
	public void decode(FieldList fieldList) {
		Iterator<FieldEntry> iter = fieldList.iterator();
		FieldEntry fieldEntry;		
		String displayName = "";
		double bid =0, ask=0;
		while (iter.hasNext())
		{
			fieldEntry = iter.next();
			switch(fieldEntry.fieldId()) {
				case 22:
					bid = fieldEntry.real().asDouble();
					break;
				case 25:
					ask = fieldEntry.real().asDouble();
					break;
				case 3:
					displayName = fieldEntry.rmtes().toString();
					break;
			}
			
		}
		System.out.println(displayName+", "+bid+", "+ask);
	}
	
}
public class Consumer {

	public static void main(String[] args) {
		AppClient client = new AppClient();
		OmmConsumerConfig config = EmaFactory.createOmmConsumerConfig();
		
		OmmConsumer consumer = EmaFactory.createOmmConsumer(config.consumerName("Consumer_3"));
		ReqMsg reqMsg = EmaFactory.createReqMsg();
		ElementList view = EmaFactory.createElementList();
		OmmArray array = EmaFactory.createOmmArray();
		
		array.fixedWidth(2);
		array.add(EmaFactory.createOmmArrayEntry().intValue(3));
		array.add(EmaFactory.createOmmArrayEntry().intValue(22));
		array.add(EmaFactory.createOmmArrayEntry().intValue(25));

		view.add(EmaFactory.createElementEntry().uintValue(EmaRdm.ENAME_VIEW_TYPE, 1));
		view.add(EmaFactory.createElementEntry().array(EmaRdm.ENAME_VIEW_DATA, array));
		
		consumer.registerClient(reqMsg.serviceName("ELEKTRON_DD").name("JPY=").payload(view), client);
		while(true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}

	}

}

