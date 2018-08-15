package co.loubo.icicle;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import net.pterodactylus.fcp.*;

public class FreenetUtil extends Thread{
	
	/** The FCP connection. */
	private FcpConnection fcpConnection;
	private FreenetAdaptor fcpAdapter;
	private Context context;
	BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
	
	public FreenetUtil(Context context,BlockingQueue<Message> queue, GlobalState gs){
		this.context = context;
		this.fcpAdapter = new FreenetAdaptor();
		this.fcpAdapter.setGlobalState(gs);
		this.queue = queue;
	}
	
	protected void setUp() throws Exception {
		LocalNode activeLocalNode = this.fcpAdapter.getGlobalState().getActiveLocalNode();
		fcpConnection = new FcpConnection(activeLocalNode.getAddress(),activeLocalNode.getPort());
		try{
			fcpConnection.connect();
			this.fcpAdapter.getGlobalState().setConnected(true);
			fcpConnection.sendMessage(new ClientHello(context.getString(R.string.app_name) + this.fcpAdapter.getGlobalState().getDeviceID()));
			fcpConnection.sendMessage(new WatchGlobal(true));
            fcpConnection.sendMessage(new WatchFeeds(true));
			//fcpConnection.sendMessage(new SendTextFeed());
			fcpConnection.addFcpListener(fcpAdapter);
		}catch(ConnectException e){
			//failed to connect
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		}
		
	}

	
	protected void tearDown() {
		//System.out.println(">>>FreenetUtil.tearDown()");
		if(fcpConnection != null){
			fcpConnection.close();
		}
		if(this.fcpAdapter != null){
			this.fcpAdapter.getGlobalState().setConnected(false);
		}
	}
	
	public void uploadFile(FileUploadMessage msg){
		try {
			String identifier;
			if(msg.getKey().equals(Constants.KEY_TYPE_CHK)){
				identifier = Constants.KEY_TYPE_CHK+msg.getName();
			}else{
				identifier = Constants.KEY_TYPE_SSK+msg.getName();
			}
			ClientPut cp = new ClientPut(msg.getKey(), identifier, UploadFrom.direct);
			cp.setDataLength(msg.getSize());
            ContentResolver cR = context.getContentResolver();

			BufferedInputStream payloadInputStream = new BufferedInputStream(cR.openInputStream(msg.getUri()));
			cp.setPayloadInputStream(payloadInputStream);
			cp.setGlobal(true);
			cp.setPersistence(Persistence.forever);
			cp.setTargetFilename(msg.getName());
			cp.setMetadataContentType(msg.getMimeType());
			fcpConnection.sendMessage(cp);
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		}
	}
	
	public void generateSSK() {
		try {
			synchronized (fcpAdapter) {
				fcpConnection.sendMessage(new GenerateSSK());
				fcpAdapter.wait();
			}
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getNode() {
		try {
			synchronized (fcpAdapter) {
				fcpConnection.sendMessage(new GetNode(false, false, true));
				fcpAdapter.wait();
			}
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getPersistentRequests() {
		try {
			synchronized (fcpAdapter) {
				fcpConnection.sendMessage(new ListPersistentRequests());
				fcpAdapter.wait();
			}
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getPeers() {
		try {
			String identifier = String.valueOf(System.currentTimeMillis());
			synchronized (fcpAdapter) {
				fcpConnection.sendMessage(new ListPeers(identifier,false,true));
				fcpAdapter.wait();
			}
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addNoderef(AddPeer aPeer) {
		try {
			fcpConnection.sendMessage(aPeer);
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		}
	}

    private void updatePriority(Bundle obj) {
        try {
            synchronized (fcpAdapter) {
                ModifyPersistentRequest req = new ModifyPersistentRequest(obj.getString("identifier"),true);
				Priority p = Priority.values()[obj.getInt("priority")];
                req.setPriority(p);
                fcpConnection.sendMessage(req);
            }
        } catch (IOException e) {
            //failed to connect
            e.printStackTrace();
            fcpConnection = null;
            this.fcpAdapter.getGlobalState().setConnected(false);
        }
    }

	private void sendTextFeed(SendTextFeed sendTextFeed){
		try {


			String out = sendTextFeed.getField("Text");
			sendTextFeed.setField("Text","");
			sendTextFeed.setDataLength(out.getBytes().length);

			BufferedInputStream payloadInputStream = new BufferedInputStream(new ByteArrayInputStream(out.getBytes()));
			sendTextFeed.setPayloadInputStream(payloadInputStream);

			synchronized (fcpAdapter) {
				fcpConnection.sendMessage(sendTextFeed);
			}
		} catch (IOException e) {
			//failed to connect
			e.printStackTrace();
			fcpConnection = null;
			this.fcpAdapter.getGlobalState().setConnected(false);
		}
	}
	
	public void run(){
		//System.out.println(">>>FreenetUtil.run()");
		try {
			setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(fcpConnection != null){
			Message msg;
			while ((msg = queue.poll()) != null) {
				try {
					switch(msg.arg1){
					case Constants.MsgGetNode:
						getNode();
						break;
					case Constants.MsgGetPersistentRequests:
						getPersistentRequests();
						break;
					case Constants.MsgGetPeers:
						getPeers();
						break;
					case Constants.MsgEXIT:
						tearDown();
						return;
					case Constants.MsgFileUpload:
						uploadFile((FileUploadMessage)msg.obj);
						break;
					case Constants.MsgAddNoderef:
						addNoderef((AddPeer)msg.obj);
						break;
					case Constants.MsgGetSSKeypair:
						generateSSK();
						break;
                    case Constants.MsgUpdatePriority:
                        updatePriority((Bundle) msg.obj);
                        break;
					case Constants.MsgSendTextFeed:
						sendTextFeed((SendTextFeed) msg.obj);
						break;
					default:
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
}
