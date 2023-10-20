package example;

import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;

interface  ConsumerGroupClient {
	public void onRefreshMsg(RefreshMsg refreshMsg);
	
	public void onUpdateMsg(UpdateMsg updateMsg);

	public void onStatusMsg(StatusMsg statusMsg);
}
