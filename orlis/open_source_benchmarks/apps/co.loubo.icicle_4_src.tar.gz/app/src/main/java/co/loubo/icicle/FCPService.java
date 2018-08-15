package co.loubo.icicle;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.Message;

public class FCPService extends Service {
    private ServiceHandler mServiceHandler;
	private FreenetUtil freenet;
	private RefreshThread refreshThread;
	public BlockingQueue<Message> queue;
	public GlobalState gs;
	protected ConnectivityManager cm;
	
	private class RefreshThread extends Thread{
		
		public void run(){
			while (true) {
				synchronized (this) {
					try {
						//check status of connection, reconnect if able
						if(freenet.isAlive()){
							//if we are connected, but the network type changed and we are wifi only, disconnect
							if(gs.isWifiOnly() && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() != ConnectivityManager.TYPE_WIFI){
								freenet.tearDown();
							}
						}else{
							if(!gs.isWifiOnly() || (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)){
								switch(freenet.getState()) {
									case TERMINATED:
										freenet = new FreenetUtil(getApplicationContext(),queue, gs);
									default: //NEW
										freenet.start();
										updateStatus();
										updatePeers();
								}
								
							}else{
								//wait for the network to become available
                                Thread.sleep(5000);
								continue;
							}
						}
                        if(gs.getRefresh_rate() == 0){
                            Thread.sleep(10000);
                            continue;
                        }
                        Thread.sleep(gs.getRefresh_rate()*1000);
						if(gs.isMainActivityVisible()){
							updateStatus();
						}
                        if(gs.serviceShouldStop()){
                            stopSelf();
                        }
					} catch (Exception e) {
						e.printStackTrace();
                        break;
					}
				}
			}
		}
		
	}
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
            switch(refreshThread.getState()){
                case TERMINATED:
                    refreshThread = new RefreshThread();
                    refreshThread.start();
            }
		}
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		queue = new ArrayBlockingQueue<>(1024);
		this.gs = (GlobalState) getApplication();
		this.gs.setQueue(queue);
		
		cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		freenet = new FreenetUtil(this,queue, this.gs);
		//only connect if we allow connection on non-wifi or we are on wifi
		if(!this.gs.isWifiOnly() || (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI)){
			freenet.start();
			updateStatus();
			updatePeers();
		}
        refreshThread = new RefreshThread();
        refreshThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy(){
		freenet.tearDown();
		refreshThread.interrupt();
		int timeout = 5;
		while(timeout > 0 && this.gs.isConnected()){
			synchronized (this) {
				try {
					Thread.sleep(1000);
					timeout--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		super.onDestroy();
	}

	public void updateStatus(){
		try {
			queue.put(Message.obtain(null, 0, Constants.MsgGetNode, 0));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* This function is no longer used - persistent requests are automatically refreshed on connect, and updated while connected
	public void updatePersistentRequests(){
		try {
			queue.put(Message.obtain(null, 0, Constants.MsgGetPersistentRequests, 0));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/

	public void updatePeers(){
		try {
			queue.put(Message.obtain(null, 0, Constants.MsgGetPeers, 0));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}