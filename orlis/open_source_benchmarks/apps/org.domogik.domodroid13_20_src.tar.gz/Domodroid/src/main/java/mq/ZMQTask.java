package mq;

/**
 * Created by mpunie on 12/05/2015.
 */
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import misc.tracerengine;

import org.zeromq.ZMQ;

class ZMQTask extends AsyncTask<ZMQService, Void, Void> {
	private tracerengine Tracer = null;
	private final String mytag = this.getClass().getName();

	private ZMQService theContext = null;
	public ZMQTask(ZMQService context) {
		theContext = context;
		SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
		Tracer = tracerengine.getInstance(SP_params,context);
		Tracer.d(mytag, "contextutons!!!");
		
	}

	protected Void doInBackground(ZMQService... params) {
		Tracer.d(this.getClass().getSimpleName(), "Task started");
		ZMQService service = params[0];
		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket sub = context.socket(ZMQ.SUB);

		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(theContext);
		String ip = SP.getString("MQaddress", "");    // TODO : use a R. for the default value
		String port = SP.getString("MQsubport", "40412");    // TODO : use a R. for the default value
		String sub_url = "tcp://" + ip + ":" + port;
		
		sub.connect(sub_url);
		sub.subscribe("interface.output".getBytes());

		// TODO : recharger la conf quand elle a changé
		while (!isCancelled()) {
			// Read message contents
			try {
				String msgId = new String(sub.recv(0));
				String msgContent = "";
				if (sub.hasReceiveMore()) {
					msgContent = new String(sub.recv());
				}
				Tracer.d(mytag,msgId);
				// Do something with the message
				service.handleMessage(msgId, msgContent);
			} catch (Exception e) {
				Tracer.e(mytag, e.getMessage());
			}
			Tracer.d(mytag, "run");
		}
		Tracer.d(mytag, "Task ended");
		return null;
	}
}
