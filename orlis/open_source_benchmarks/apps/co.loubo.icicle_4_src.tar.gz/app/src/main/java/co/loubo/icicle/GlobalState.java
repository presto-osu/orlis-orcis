package co.loubo.icicle;
 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import net.pterodactylus.fcp.ARK;
import net.pterodactylus.fcp.AddPeer;
import net.pterodactylus.fcp.DSAGroup;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.FcpUtils;
import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.PersistentRequestModified;
import net.pterodactylus.fcp.PersistentRequestRemoved;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.Version;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;
 

public class GlobalState extends Application{
	
	public static interface StateListener {
        public void onStateChanged(Bundle data);
	}

	private StateListener statusListener;
	private StateListener downloadListener;
	private StateListener uploadListener;
	private StateListener peersListener;
	
	private CopyOnWriteArrayList<LocalNode> localNodes;
    private CopyOnWriteArrayList<FriendNode> friendNodes;
	private int activeLocalNode;
	private String deviceID;
	private int refresh_rate;
	private boolean wifiOnly;
	private String identity;
	
	private boolean isConnected = false;
	private boolean isMainActivityVisible = false;
	private BlockingQueue<Message> queue;
	private final Handler mFreenetHandler = new Handler();
	final Runnable updateStatus = new Runnable() {
		public void run() {
			Intent intent = new Intent(Constants.BROADCAST_UPDATE_STATUS);
			sendBroadcast(intent);
		}
	};

	final Runnable updateDownloads = new Runnable() {
		public void run() {
			Intent intent = new Intent(Constants.BROADCAST_UPDATE_DOWNLOADS);
			sendBroadcast(intent);
		}
	};
	
	final Runnable updateUploads = new Runnable() {
		public void run() {
			Intent intent = new Intent(Constants.BROADCAST_UPDATE_UPLOADS);
			sendBroadcast(intent);
		}
	};

	final Runnable updatePeers = new Runnable() {
		public void run() {
			Intent intent = new Intent(Constants.BROADCAST_UPDATE_PEERS);
			sendBroadcast(intent);
		}
	};
	private Debouncer debounceBroadcasts = new Debouncer(mFreenetHandler, Constants.debounceInterval);
	private NodeStatus nodeStatus;
	private CopyOnWriteArrayList<Peer> peers;
	private CopyOnWriteArrayList<Download> DownloadsList;
	private CopyOnWriteArrayList<Upload> UploadsList;
	private CopyOnWriteArrayList<UploadDir> UploadDirsList;
    private CopyOnWriteArrayList<FreenetMessage> messageList;
	SharedPreferences sharedPref;
	private Intent serviceIntent;
	private SSKKeypair anSSKeypair;
    private Activity activeActivity;
	@SuppressLint("HandlerLeak")
	private final Handler toastHandler = new Handler() {
		public void handleMessage(Message msg) {
            if(msg.arg1 == 0){
                Toast.makeText(getApplicationContext(),msg.getData().getString(Constants.ToastMessage), msg.arg2).show();
            }else {
                Toast.makeText(getApplicationContext(), getResources().getString(msg.arg1), msg.arg2).show();
            }
		}
	};
	
	public void onCreate() {
        super.onCreate();
        initializeState();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        loadPreferences();
        
        
	}
	
	private void initializeState(){
        serviceIntent = new Intent(this, FCPService.class);
		this.peers = new CopyOnWriteArrayList<>();
		this.DownloadsList = new CopyOnWriteArrayList<>();
		this.UploadsList = new CopyOnWriteArrayList<>();
		this.UploadDirsList = new CopyOnWriteArrayList<>();
        this.messageList = new CopyOnWriteArrayList<>();
		this.nodeStatus = null;
		this.setConnected(false);
	}
	
	public void savePreferences() {
		Editor editor = sharedPref.edit();
		String encodedLocal;
        String encodedFriends;
		  
		  try {
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
              objectOutputStream.writeObject(this.localNodes);
              objectOutputStream.close();
              encodedLocal = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
              byteArrayOutputStream = new ByteArrayOutputStream();
              objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
              objectOutputStream.writeObject(this.friendNodes);
              objectOutputStream.close();
              encodedFriends = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));

		  } catch (IOException e) {
		   e.printStackTrace();
		   return;
		  }
		
		  editor.putString(Constants.PREF_LOCAL_NODES, encodedLocal);
          editor.putString(Constants.PREF_FRIEND_NODES, encodedFriends);
		  editor.putInt(Constants.PREF_ACTIVE_LOCAL_NODE, this.activeLocalNode);
		  editor.putInt(Constants.PREF_REFRESH_RATE, this.refresh_rate);
		  editor.putBoolean(Constants.PREF_WIFI_ONLY, this.wifiOnly);
		  editor.apply();
	}
	
	@SuppressWarnings("unchecked")
	public void loadPreferences() {
		String strLocalNodes = sharedPref.getString(Constants.PREF_LOCAL_NODES, "");
		if(strLocalNodes.equals("")){
			this.localNodes = new CopyOnWriteArrayList<>();
		}else{
			byte[] bytes = Base64.decode(strLocalNodes.getBytes(),Base64.DEFAULT);
			  try {
			   ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream(bytes) );
			   this.localNodes = (CopyOnWriteArrayList<LocalNode>)objectInputStream.readObject();
			  } catch (IOException | ClassNotFoundException | ClassCastException e) {
				  e.printStackTrace();
				  this.localNodes = new CopyOnWriteArrayList<>();
			  }
		}
        String strFriendNodes = sharedPref.getString(Constants.PREF_FRIEND_NODES, "");
        if(strFriendNodes.equals("")){
            this.friendNodes = new CopyOnWriteArrayList<>();
        }else{
            byte[] bytes = Base64.decode(strFriendNodes.getBytes(),Base64.DEFAULT);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream(bytes) );
                this.friendNodes = (CopyOnWriteArrayList<FriendNode>)objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                e.printStackTrace();
                this.friendNodes = new CopyOnWriteArrayList<>();
            }
        }
		this.activeLocalNode = sharedPref.getInt(Constants.PREF_ACTIVE_LOCAL_NODE, 0);
		this.refresh_rate = sharedPref.getInt(Constants.PREF_REFRESH_RATE, 0);
		this.deviceID = sharedPref.getString(Constants.PREF_DEVICE_ID,"");
		this.wifiOnly = sharedPref.getBoolean(Constants.PREF_WIFI_ONLY, true);
		if(this.deviceID.equals("")){
			Editor editor = sharedPref.edit();
			Random random = new Random();
			this.deviceID = new BigInteger(130, random).toString(32);
			editor.putString(Constants.PREF_DEVICE_ID,this.deviceID);
			editor.apply();
		}
	}
	
	public void setStatusStateListener(StateListener listener) {
		this.statusListener = listener;
	}
	public void setDownloadStateListener(StateListener listener) {
		this.downloadListener = listener;
	}
	public void setUploadStateListener(StateListener listener) {
		this.uploadListener = listener;
	}
	public void setPeersStateListener(StateListener listener) {
		this.peersListener = listener;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setNodeHello(NodeHello nodeHello) {
		this.nodeStatus = new NodeStatus(false,nodeHello.getVersion());
	}
	
	public void setNodeData(NodeData newNodeData){
		this.nodeStatus.setAdvanced(true);
		this.nodeStatus.setRecentInputRate(Double.parseDouble(newNodeData.getVolatile("recentInputRate"))/1000);
		this.nodeStatus.setRecentOutputRate(Double.parseDouble(newNodeData.getVolatile("recentOutputRate"))/1000);
		this.nodeStatus.setUptimeSeconds(Double.parseDouble(newNodeData.getVolatile("uptimeSeconds")));
		this.identity = newNodeData.getIdentity();
        extractNodeReference(newNodeData, this.getActiveLocalNode());
		sendRedrawStatus();
        savePreferences();
	}

	public String getIdentity() {
		return this.identity;
	}


	public void addToPeerList(Peer peer) {
		int existingPeer = getPeerIndex(peer.getIdentity());
		if(existingPeer < 0){
			this.peers.add(peer);
		}else{
			this.peers.set(existingPeer, peer);
		}
		sendRedrawPeersList();
	}

	public Peer getPeer(int index){
		return peers.get(index);
	}

	public CopyOnWriteArrayList<Peer> getDarknetPeerList() {
		CopyOnWriteArrayList<Peer> darknetPeers = new CopyOnWriteArrayList<Peer>();
		for (Peer p : peers) {
			if(!p.isOpennet()){
				darknetPeers.add(p);
			}
		}
		return darknetPeers;
	}
	
	public void addToDownloadsList(PersistentGet get){
		Download existingDownload = getDownload(get.getIdentifier());
		if(existingDownload == null){
			DownloadsList.add(new Download(get));
		}else{
			existingDownload.setPersistentGet(get);
		}
		sendRedrawDownloads();
	}

	public void addToUploadsList(PersistentPut put){
		Upload existingUpload = getUpload(put.getIdentifier());
		if(existingUpload == null){
			UploadsList.add(new Upload(put));
		}else{
			existingUpload.setPersistentPut(put);
		}
		sendRedrawUploads();
	}

	public void addToUploadsList(PersistentPutDir persistentPutDir) {
		UploadDir existingUpload = getUploadDir(persistentPutDir.getIdentifier());
		if(existingUpload == null){
			UploadDirsList.add(new UploadDir(persistentPutDir));
		}else{
			existingUpload.setPersistentPutDir(persistentPutDir);
		}
		sendRedrawUploads();
	}

    public void addToMessageList(FreenetMessage freenetMessage){
        this.messageList.add(freenetMessage);
    }

    public CopyOnWriteArrayList<FreenetMessage> getMessageList(){
        return this.messageList;
    }

	
	public void sendRedrawStatus(){
		debounceBroadcasts.call(this.updateStatus);
	}
	
	public void sendRedrawDownloads(){
		debounceBroadcasts.call(this.updateDownloads);
	}
	
	public void sendRedrawUploads(){
		debounceBroadcasts.call(this.updateUploads);
	}
	
	public void sendRedrawPeersList(){
		debounceBroadcasts.call(this.updatePeers);
	}
	
	public void sendRedrawAll(){
		sendRedrawStatus();
		sendRedrawDownloads();
		sendRedrawUploads();
		sendRedrawPeersList();
	}
	
	public void redrawStatus(){
		Bundle data = new Bundle();
		data.putSerializable(Constants.STATUS, 	this.nodeStatus);
		data.putBoolean(Constants.IS_CONNECTED, this.isConnected);
        data.putBoolean(Constants.HAS_LOCAL_NODES, this.localNodes.size() > 0);
		if (statusListener != null) {
			statusListener.onStateChanged(data);
        }
	}
	
	public void redrawDownloads() {
		Bundle data = new Bundle();
		data.putSerializable(Constants.DOWNLOADS, this.DownloadsList);
		data.putBoolean(Constants.IS_CONNECTED, this.isConnected);
		if (downloadListener != null) {
			downloadListener.onStateChanged(data);
        }
	}
	
	public void redrawUploads() {
		Bundle data = new Bundle();
		data.putSerializable(Constants.UPLOADS, this.UploadsList);
		data.putSerializable(Constants.UPLOAD_DIRS, this.UploadDirsList);
		data.putBoolean(Constants.IS_CONNECTED, this.isConnected);
		if (uploadListener != null) {
			uploadListener.onStateChanged(data);
        }
	}
	
	public void redrawPeerList(){
		Bundle data = new Bundle();
		data.putSerializable(Constants.PEERS, this.peers);
		data.putBoolean(Constants.IS_CONNECTED, this.isConnected);
		if (peersListener != null) {
			peersListener.onStateChanged(data);
        }
	}

	public Download getDownload(String identifier){
		for(int i=0;i<this.DownloadsList.size();i++){
	        if(this.DownloadsList.get(i).getPersistentGet().getIdentifier().equals(identifier)){
	            return this.DownloadsList.get(i);
		        }
		}
		return null;
	}
	
	public Upload getUpload(String identifier){
		for(int i=0;i<this.UploadsList.size();i++){
	        if(this.UploadsList.get(i).getPersistentPut().getIdentifier().equals(identifier)){
	            return this.UploadsList.get(i);
		        }
		}
		return null;
	}
	
	public UploadDir getUploadDir(String identifier){
		for(int i=0;i<this.UploadDirsList.size();i++){
	        if(this.UploadDirsList.get(i).getPersistentPutDir().getIdentifier().equals(identifier)){
	            return this.UploadDirsList.get(i);
		        }
		}
		return null;
	}

	public int getPeerIndex(String identifier){
		for(int i=0;i<this.peers.size();i++){
	        if(this.peers.get(i).getIdentity().equals(identifier)){
	            return i;
		        }
		}
		return -1;
	}
	
	public int getDownloadIndex(String identifier){
		for(int i=0;i<this.DownloadsList.size();i++){
	        if(this.DownloadsList.get(i).getPersistentGet().getIdentifier().equals(identifier)){
	            return i;
		        }
		}
		return -1;
	}
	
	public int getUploadIndex(String identifier){
		for(int i=0;i<this.UploadsList.size();i++){
	        if(this.UploadsList.get(i).getPersistentPut().getIdentifier().equals(identifier)){
	            return i;
		        }
		}
		return -1;
	}
	
	public int getUploadDirIndex(String identifier){
		for(int i=0;i<this.UploadDirsList.size();i++){
	        if(this.UploadDirsList.get(i).getPersistentPutDir().getIdentifier().equals(identifier)){
	            return i;
		        }
		}
		return -1;
	}
	
	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
		sendRedrawStatus();
	}

	public boolean isMainActivityVisible() {
		return isMainActivityVisible;
	}

	public void setMainActivityVisible(boolean isMainActivityVisible) {
		this.isMainActivityVisible = isMainActivityVisible;
	}

	public void updateTransferProgress(SimpleProgress simpleProgress) {
		Download existingDownload = getDownload(simpleProgress.getIdentifier());
		if(existingDownload == null){
			Upload existingUpload = getUpload(simpleProgress.getIdentifier());
			
			
			if(existingUpload == null){
				UploadDir existingUploadDir = getUploadDir(simpleProgress.getIdentifier());
				
				if(existingUploadDir == null){
					return;
				}
				existingUploadDir.updateProgress(simpleProgress);
				sendRedrawUploads();
			}else{
				existingUpload.updateProgress(simpleProgress);
				sendRedrawUploads();
			}
		}else{
			existingDownload.updateProgress(simpleProgress);
			sendRedrawDownloads();
		}
	}

	public void addDataLength(FcpMessage fcpMessage) {
		Download existingDownload = getDownload(fcpMessage.getField("Identifier"));
		if(existingDownload == null){
			Upload existingUpload = getUpload(fcpMessage.getField("Identifier"));
			
			if(existingUpload == null){
				UploadDir existingUploadDir = getUploadDir(fcpMessage.getField("Identifier"));
				if(existingUploadDir == null){
					return;
				}
				existingUploadDir.updateDataLength(fcpMessage.getField("DataLength"));
				sendRedrawUploads();
			}else{
				existingUpload.updateDataLength(fcpMessage.getField("DataLength"));
				sendRedrawUploads();
			}
		}else{
			existingDownload.updateDataLength(fcpMessage.getField("DataLength"));
			sendRedrawDownloads();
		}
		
	}

	public void updateDataFound(DataFound dataFound) {
		Download existingDownload = getDownload(dataFound.getIdentifier());
		if(existingDownload != null){
			existingDownload.setDataFound(dataFound);
		}
	}

	public void removePersistentRequest(PersistentRequestRemoved persistentRequestRemoved) {
		int existingDownloadIndex = getDownloadIndex(persistentRequestRemoved.getIdentifier());
		if(existingDownloadIndex < 0){
			int existingUploadIndex = getUploadIndex(persistentRequestRemoved.getIdentifier());
			
			
			if(existingUploadIndex< 0){
				int existingUploadDirIndex = getUploadDirIndex(persistentRequestRemoved.getIdentifier());
				
				if(existingUploadDirIndex < 0){
					return;
				}
				UploadDirsList.remove(existingUploadDirIndex);
				sendRedrawUploads();
			}else{
				UploadsList.remove(existingUploadIndex);
				sendRedrawUploads();
			}
		}else{
			DownloadsList.remove(existingDownloadIndex);
			sendRedrawDownloads();
		}
	}

	public void addPutSuccessful(PutSuccessful putSuccessful) {
		Upload existingUpload = getUpload(putSuccessful.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(putSuccessful.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setPutSuccessful(putSuccessful);
		}else{
			existingUpload.setPutSuccessful(putSuccessful);
		}
		sendRedrawUploads();
	}
	

	public void addPutFetchable(PutFetchable putFetchable) {
		Upload existingUpload = getUpload(putFetchable.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(putFetchable.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setPutFetchable(putFetchable);

		}else{
			existingUpload.setPutFetchable(putFetchable);
		}
		sendRedrawUploads();
	}

	public void addURIGenerated(URIGenerated uriGenerated) {
		Upload existingUpload = getUpload(uriGenerated.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(uriGenerated.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setUriGenerated(uriGenerated);

		}else{
			existingUpload.setUriGenerated(uriGenerated);
		}
	}

	public void addStartedCompression(StartedCompression startedCompression) {
		Upload existingUpload = getUpload(startedCompression.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(startedCompression.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setStartedCompression(startedCompression);

		}else{
			existingUpload.setStartedCompression(startedCompression);
		}
	}

	public void addFinishedCompression(FinishedCompression finishedCompression) {
		Upload existingUpload = getUpload(finishedCompression.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(finishedCompression.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setFinishedCompression(finishedCompression);

		}else{
			existingUpload.setFinishedCompression(finishedCompression);
		}
	}

	public void addGetFailed(GetFailed getFailed) {
		Download existingDownload = getDownload(getFailed.getIdentifier());
		if(existingDownload != null){
			existingDownload.setGetFailed(getFailed);
		}
		sendRedrawDownloads();
	}

	public void addPutFailed(PutFailed putFailed) {
		Upload existingUpload = getUpload(putFailed.getIdentifier());
		
		if(existingUpload == null){
			UploadDir existingUploadDir = getUploadDir(putFailed.getIdentifier());
			if(existingUploadDir == null){
				return;
			}
			existingUploadDir.setPutFailed(putFailed);

		}else{
			existingUpload.setPutFailed(putFailed);
		}
		sendRedrawUploads();
	}

	public void updatePeristentRequest(PersistentRequestModified persistentRequestModified) {
	
		Download existingDownload = getDownload(persistentRequestModified.getIdentifier());
		if(existingDownload == null){
			Upload existingUpload = getUpload(persistentRequestModified.getIdentifier());
			
			
			if(existingUpload == null){
				UploadDir existingUploadDir = getUploadDir(persistentRequestModified.getIdentifier());
				
				if(existingUploadDir == null){
					return;
				}
				if(existingUploadDir.getPriority() == persistentRequestModified.getPriority().ordinal()){
					return;
				}
				existingUploadDir.setPriority(persistentRequestModified.getPriority().ordinal());
				sendRedrawUploads();
				
			}else{
				if(existingUpload.getPriority() == persistentRequestModified.getPriority().ordinal()){
					return;
				}
				existingUpload.setPriority(persistentRequestModified.getPriority().ordinal());
				sendRedrawUploads();
			}
		}else{
			if(existingDownload.getPriority() == persistentRequestModified.getPriority().ordinal()){
				return;
			}
			existingDownload.setPriority(persistentRequestModified.getPriority().ordinal());
			sendRedrawDownloads();
		}
	}

	public void setQueue(BlockingQueue<Message> queue) {
		this.queue = queue;
	}

	public BlockingQueue<Message> getQueue() {
		return this.queue;
	}

	public LocalNode getActiveLocalNode() {
		return localNodes.get(this.activeLocalNode);
	}
	
	public int getActiveLocalNodeIndex(){
		return this.activeLocalNode;
	}
	
	public void setActiveLocalNodeIndex(int newIndex){
		this.activeLocalNode = newIndex;
		onActiveNodeChanged();
	}
	
	public CopyOnWriteArrayList<LocalNode> getLocalNodeList(){
		return this.localNodes;
	}

	public void onActiveNodeChanged() {
		Editor editor = sharedPref.edit();
		editor.putInt(Constants.PREF_ACTIVE_LOCAL_NODE, this.activeLocalNode);
		editor.apply();
		//Toast.makeText(this, R.string.node_change_active, Toast.LENGTH_SHORT).show();
		showToast(R.string.node_change_active,Toast.LENGTH_SHORT);
		restartFCPService();
	}
	
	public void showToast(int stringMessage,int length){
		Message msg = toastHandler.obtainMessage();
		msg.arg1 = stringMessage;
		msg.arg2 = length;
		toastHandler.sendMessage(msg);
	}

    public void showToast(String stringMessage,int length){
        Message msg = toastHandler.obtainMessage();
        msg.arg1 = 0;
        msg.arg2 = length;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ToastMessage,stringMessage);
        msg.setData(bundle);
        toastHandler.sendMessage(msg);
    }
	
	public void restartFCPService(){
		stopFCPService();
		initializeState();
		startFCPService();
	}
	
public void onRefreshRateChange(int integer, boolean need_to_reset_loop) {
		Editor editor = sharedPref.edit();
		editor.putInt(Constants.PREF_REFRESH_RATE, integer);
		editor.apply();
		if(need_to_reset_loop){
			restartFCPService();
		}
	}

	public int getRefresh_rate() {
		return refresh_rate;
	}

	public void setRefresh_rate(int refresh_rate) {
		//if the new refresh rate is shorter than the old one, restart the service. Otherwise may need to wait a while for new refresh rate to take effect.
		boolean need_to_reset_loop = refresh_rate < this.refresh_rate || this.refresh_rate == 0;
		this.refresh_rate = refresh_rate;
		onRefreshRateChange(refresh_rate,need_to_reset_loop);
	}



	public boolean isWifiOnly() {
		return wifiOnly;
	}

	public void setWifiOnly(boolean wifiOnly) {
		this.wifiOnly = wifiOnly;
		Editor editor = sharedPref.edit();
		editor.putBoolean(Constants.PREF_WIFI_ONLY, wifiOnly);
		editor.apply();
	}

	public void startFCPService() {
		//System.out.println(">>>GlobalState.startFCPService()");
		startService(serviceIntent);
	}
	
	public void stopFCPService() {
		//System.out.println(">>>GlobalState.stopFCPService()");
		if(serviceIntent == null){
			return;
		}
	    stopService(serviceIntent);
	}

    public boolean serviceShouldStop() {
        return this.activeActivity == null;
    }

    public void registerActivity(Activity act){
        //System.out.println(">>>GlobalState.registerActivity("+act.toString()+")");
        this.activeActivity = act;
        startFCPService();
    }

    public void unregisterActivity(Activity act){
        //System.out.println(">>>GlobalState.unregisterActivity("+act.toString()+")");
        if(this.activeActivity == act)
        this.activeActivity = null;
    }

	public SSKKeypair getSSKKeypair() {
		try {
			this.setSSKeypair(null);
			this.queue.put(Message.obtain(null, 0, Constants.MsgGetSSKeypair, 0));
			int limit = 15;
			while(this.anSSKeypair == null && limit > 0){
				limit--;
				Thread.sleep(1000);
			}
			return anSSKeypair;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setSSKeypair(SSKKeypair sskKeypair) {
		this.anSSKeypair = sskKeypair;
		
	}

	public void handleIdentifierCollision(IdentifierCollision identifierCollision) {

		//check if the Identifier Collision was caused by the user trying to upload the same file
		for(Upload u:UploadsList){
			if(u.getPersistentPut().getIdentifier().equals(identifierCollision.getIdentifier())){
				Message msg = toastHandler.obtainMessage();
				msg.arg1 = R.string.identifierCollision_upload;
				msg.arg2 = Toast.LENGTH_LONG;
				toastHandler.sendMessage(msg);
			}		
		}

		//Identifier Collision was not caused by an upload
	}

    private void extractNodeReference(NodeData myNode, LocalNode activeLocalNode) {
        String refStr = "";
        String temp;
        String EncodedStr = "";
        refStr+="identity="+myNode.getIdentity()+"\n";
        EncodedStr+="identity="+myNode.getIdentity()+"\n";
        refStr+="lastGoodVersion="+myNode.getLastGoodVersion()+"\n";
        temp = new String(Base64.encode(myNode.getLastGoodVersion().toString().getBytes(), Base64.NO_PADDING|Base64.NO_WRAP));
        EncodedStr+="lastGoodVersion=="+temp+"\n";
        if(myNode.getNodeRef().getLocation() > 0) {
            refStr += "location=" + myNode.getNodeRef().getLocation() + "\n";
            temp = new String(Base64.encode(String.valueOf(myNode.getNodeRef().getLocation()).getBytes(), Base64.NO_PADDING | Base64.NO_WRAP));
            EncodedStr += "location==" + temp + "\n";
        }
        refStr+="myName="+myNode.getMyName()+"\n";
        temp = new String(Base64.encode(myNode.getMyName().getBytes(), Base64.NO_PADDING|Base64.NO_WRAP));
        EncodedStr+="myName=="+temp+"\n";
        refStr+="opennet="+myNode.isOpennet()+"\n";
        EncodedStr+="opennet="+myNode.isOpennet()+"\n";
        refStr+="sig="+myNode.getSignature()+"\n";
        EncodedStr+="sig="+myNode.getSignature()+"\n";
        refStr+="sigP256="+myNode.getField("sigP256")+"\n";
        EncodedStr+="sigP256="+myNode.getField("sigP256")+"\n";
        refStr+="version="+myNode.getVersion()+"\n";
        temp = new String(Base64.encode(myNode.getVersion().toString().getBytes(), Base64.NO_PADDING|Base64.NO_WRAP));
        EncodedStr+="version=="+temp+"\n";
        refStr+="ark.number="+myNode.getARK().getNumber()+"\n";
        EncodedStr+="ark.number="+myNode.getARK().getNumber()+"\n";
        refStr+="ark.pubURI="+myNode.getARK().getPublicURI()+"\n";
        EncodedStr+="ark.pubURI="+myNode.getARK().getPublicURI()+"\n";
        refStr+="auth.negTypes="+myNode.getField("auth.negTypes")+"\n";
        temp = new String(Base64.encode(myNode.getField("auth.negTypes").getBytes(), Base64.NO_PADDING|Base64.NO_WRAP));
        EncodedStr+="auth.negTypes=="+temp+"\n";
        refStr+="dsaGroup.g="+myNode.getDSAGroup().getBase()+"\n";
        EncodedStr+="dsaGroup.g="+myNode.getDSAGroup().getBase()+"\n";
        refStr+="dsaGroup.p="+myNode.getDSAGroup().getPrime()+"\n";
        EncodedStr+="dsaGroup.p="+myNode.getDSAGroup().getPrime()+"\n";
        refStr+="dsaGroup.q="+myNode.getDSAGroup().getSubprime()+"\n";
        EncodedStr+="dsaGroup.q="+myNode.getDSAGroup().getSubprime()+"\n";
        refStr+="dsaPubKey.y="+myNode.getDSAPublicKey()+"\n";
        EncodedStr+="dsaPubKey.y="+myNode.getDSAPublicKey()+"\n";
        refStr+="ecdsa.P256.pub="+myNode.getField("ecdsa.P256.pub")+"\n";
        EncodedStr+="ecdsa.P256.pub="+myNode.getField("ecdsa.P256.pub")+"\n";
        refStr+="physical.udp="+myNode.getPhysicalUDP()+"\n";
        temp = new String(Base64.encode(myNode.getPhysicalUDP().getBytes(), Base64.NO_PADDING|Base64.NO_WRAP));
        EncodedStr+="physical.udp=="+temp+"\n";
        refStr+="End\n";
        EncodedStr+="End\n";
        activeLocalNode.setNodeReference(refStr);
        activeLocalNode.setEncodedNodeReference(EncodedStr);
    }

    public void handleProtocolError(ProtocolError protocolError) {
        String msg = getResources().getString(R.string.protocolError)+": "+ protocolError.getCodeDescription();
        if(!protocolError.getCodeDescription().equals(protocolError.getExtraDescription())) msg += " ("+ protocolError.getExtraDescription() + ")";
        showToast(msg,Toast.LENGTH_LONG);
    }

    public void addFriendNode(FriendNode ref) {
        if(!this.friendNodes.contains(ref)) {
            this.friendNodes.add(ref);
        }else{
            this.friendNodes.set(this.friendNodes.lastIndexOf(ref), ref);
        }
        this.savePreferences();
    }

    public CopyOnWriteArrayList<FriendNode> getFriendNodes(){
        return this.friendNodes;
    }

    public AddPeer processStringIntoNode(String in){
        String arkPubURI = null;
        String arkPrivURI = null;
        String arkNumber = null;
        String dsaGroupG = null;
        String dsaGroupP = null;
        String dsaGroupQ = null;
        String ecdsaP256pub = null;
        String sigP256 = null;

        NodeRef aNode = new NodeRef();
        //hack for case when Location is not set in NodeRef
        aNode.setLocation(-1);
        String str2;
        String[] array = in.split("\\r?\\n");

        for (String anArray : array) {
            if (anArray.startsWith("identity=")) {
                if (anArray.charAt(9) == '=')
                    str2 = new String(Base64.decode(anArray.substring(10), Base64.DEFAULT));
                else
                    str2 = anArray.substring(9);
                aNode.setIdentity(str2);
            } else if (anArray.startsWith("opennet=")) {
                if (anArray.charAt(8) == '=')
                    str2 = new String(Base64.decode(anArray.substring(9), Base64.DEFAULT));
                else
                    str2 = anArray.substring(8);
                aNode.setOpennet(Boolean.valueOf(str2));
            } else if (anArray.startsWith("myName=")) {
                if (anArray.charAt(7) == '=')
                    str2 = new String(Base64.decode(anArray.substring(8), Base64.DEFAULT));
                else
                    str2 = anArray.substring(7);
                aNode.setName(str2);
            } else if (anArray.startsWith("location=")) {
                if (anArray.charAt(9) == '=')
                    str2 = new String(Base64.decode(anArray.substring(10), Base64.DEFAULT));
                else
                    str2 = anArray.substring(9);
                aNode.setLocation(Double.valueOf(str2));
            } else if (anArray.startsWith("physical.udp=")) {
                if (anArray.charAt(13) == '=')
                    str2 = new String(Base64.decode(anArray.substring(14), Base64.DEFAULT));
                else
                    str2 = anArray.substring(13);
                aNode.setPhysicalUDP(str2);
            } else if (anArray.startsWith("ark.pubURI=")) {

                arkPubURI = anArray.substring(11);
            } else if (anArray.startsWith("ark.privURI=")) {
                if (anArray.charAt(12) == '=')
                    str2 = new String(Base64.decode(anArray.substring(13), Base64.DEFAULT));
                else
                    str2 = anArray.substring(12);
                arkPrivURI = str2;
            } else if (anArray.startsWith("ark.number=")) {
                if (anArray.charAt(11) == '=')
                    str2 = new String(Base64.decode(anArray.substring(12), Base64.DEFAULT));
                else
                    str2 = anArray.substring(11);
                arkNumber = str2;
            } else if (anArray.startsWith("dsaPubKey.y=")) {
                if (anArray.charAt(12) == '=')
                    str2 = new String(Base64.decode(anArray.substring(13), Base64.DEFAULT));
                else
                    str2 = anArray.substring(12);
                aNode.setDSAPublicKey(str2);
            } else if (anArray.startsWith("dsaGroup.g=")) {
                if (anArray.charAt(11) == '=')
                    str2 = new String(Base64.decode(anArray.substring(12), Base64.DEFAULT));
                else
                    str2 = anArray.substring(11);
                dsaGroupG = str2;
            } else if (anArray.startsWith("dsaGroup.p=")) {
                if (anArray.charAt(11) == '=')
                    str2 = new String(Base64.decode(anArray.substring(12), Base64.DEFAULT));
                else
                    str2 = anArray.substring(11);
                dsaGroupP = str2;
            } else if (anArray.startsWith("dsaGroup.q=")) {
                if (anArray.charAt(11) == '=')
                    str2 = new String(Base64.decode(anArray.substring(12), Base64.DEFAULT));
                else
                    str2 = anArray.substring(11);
                dsaGroupQ = str2;
            } else if (anArray.startsWith("auth.negTypes=")) {
                if (anArray.charAt(14) == '=')
                    str2 = new String(Base64.decode(anArray.substring(15), Base64.DEFAULT));
                else
                    str2 = anArray.substring(14);
                aNode.setNegotiationTypes(FcpUtils.decodeMultiIntegerField(str2));
            } else if (anArray.startsWith("version=")) {
                if (anArray.charAt(8) == '=')
                    str2 = new String(Base64.decode(anArray.substring(9), Base64.DEFAULT));
                else
                    str2 = anArray.substring(8);
                aNode.setVersion(new Version(str2));
            } else if (anArray.startsWith("lastGoodVersion=")) {
                if (anArray.charAt(16) == '=')
                    str2 = new String(Base64.decode(anArray.substring(17), Base64.DEFAULT));
                else
                    str2 = anArray.substring(16);
                aNode.setLastGoodVersion(new Version(str2));
            } else if (anArray.startsWith("testnet=")) {
                if (anArray.charAt(8) == '=')
                    str2 = new String(Base64.decode(anArray.substring(9), Base64.DEFAULT));
                else
                    str2 = anArray.substring(8);
                aNode.setTestnet(Boolean.valueOf(str2));
            } else if (anArray.startsWith("sig=")) {
                if (anArray.charAt(4) == '=')
                    str2 = new String(Base64.decode(anArray.substring(5), Base64.DEFAULT));
                else
                    str2 = anArray.substring(4);
                aNode.setSignature(str2);
            } else if (anArray.startsWith("ecdsa.P256.pub=")) {
                if (anArray.charAt(15) == '=')
                    str2 = new String(Base64.decode(anArray.substring(16), Base64.DEFAULT));
                else
                    str2 = anArray.substring(15);
                ecdsaP256pub = str2;
            } else if (anArray.startsWith("sigP256=")) {
                if (anArray.charAt(8) == '=')
                    str2 = new String(Base64.decode(anArray.substring(9), Base64.DEFAULT));
                else
                    str2 = anArray.substring(8);
                sigP256 = str2;
            }

        }
        aNode.setARK(new ARK(arkPubURI,arkPrivURI,arkNumber));
        aNode.setDSAGroup(new DSAGroup(dsaGroupG,dsaGroupP,dsaGroupQ));
        AddPeer aPeer = new AddPeer(aNode);
        if(ecdsaP256pub != null){
            aPeer.setField("ecdsa.P256.pub", ecdsaP256pub);
        }
        if(sigP256 != null){
            aPeer.setField("sigP256", sigP256);
        }
        aPeer.setField("Trust",Constants.DEFAULT_TRUST);
        aPeer.setField("Visibility",Constants.DEFAULT_VISIBILITY);
        return aPeer;
    }

	public String getPrettyDate(Date timestamp) {
		Date now = new Date();
		long difference = now.getTime()-timestamp.getTime();
		if(difference < 60000l){ // less than a minute
			return String.valueOf((int)Math.floor(difference/1000l))+"s";
		}
		if(difference < 3600000l){ //less than an hour
			return String.valueOf((int)Math.floor(difference/60000l))+"m";
		}
		if(difference < 86400000l){ //less than a day
			return String.valueOf((int)Math.floor(difference/3600000l))+"h";
		}
		if(difference < 2592000000l){ //less than a month
			return String.valueOf((int)Math.floor(difference/86400000l))+"d";
		}
		if(difference < 31536000000l){ //less than a year
			DateFormat format = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
			return format.format(timestamp);
		}
		DateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
		return format.format(timestamp);//more than a year ago
	}
}