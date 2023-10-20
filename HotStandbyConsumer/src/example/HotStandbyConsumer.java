package example;

import java.util.Iterator;

import com.refinitiv.ema.access.ElementList;
import com.refinitiv.ema.access.EmaFactory;
import com.refinitiv.ema.access.FieldEntry;
import com.refinitiv.ema.access.FieldList;
import com.refinitiv.ema.access.OmmArray;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.ReqMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;
import com.refinitiv.ema.rdm.EmaRdm;

class AppClient implements ConsumerGroupClient
{

	@Override
	public void onRefreshMsg(RefreshMsg refreshMsg) {
		// TODO Auto-generated method stub
		FieldList fieldList = refreshMsg.payload().fieldList();
		System.out.print("Refersh: "+refreshMsg.seqNum()+", ");
		decode(fieldList);

	}

	@Override
	public void onUpdateMsg(UpdateMsg updateMsg) {
		// TODO Auto-generated method stub
		FieldList fieldList = updateMsg.payload().fieldList();
		System.out.print("Update: "+updateMsg.seqNum()+", ");
		decode(fieldList);
	}
	

	@Override
	public void onStatusMsg(StatusMsg statusMsg) {
		// TODO Auto-generated method stub
		
	}
	
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
public class HotStandbyConsumer {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World");
		AppClient client = new AppClient();
		ConsumerGroup consumer = new ConsumerGroup(client);
		ReqMsg reqMsg = EmaFactory.createReqMsg();
		ElementList view = EmaFactory.createElementList();
		OmmArray array = EmaFactory.createOmmArray();
		
		array.fixedWidth(2);
		array.add(EmaFactory.createOmmArrayEntry().intValue(3));
		array.add(EmaFactory.createOmmArrayEntry().intValue(22));
		array.add(EmaFactory.createOmmArrayEntry().intValue(25));

		view.add(EmaFactory.createElementEntry().uintValue(EmaRdm.ENAME_VIEW_TYPE, 1));
		view.add(EmaFactory.createElementEntry().array(EmaRdm.ENAME_VIEW_DATA, array));
		
		consumer.registerClient(reqMsg.serviceName("ELEKTRON_DD").name("JPY=").payload(view));
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
