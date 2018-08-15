package ru.subprogram.paranoidsmsblocker.smsreceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.activities.CAMainActivity;
import ru.subprogram.paranoidsmsblocker.database.CADbEngine;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.database.entities.CASms;
import ru.subprogram.paranoidsmsblocker.utils.CAUtils;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

public class CADefaultSmsReceiver extends BroadcastReceiver{

	private static final String TAG = "CADefaultSmsReceiver";

	public final static int NOTIFICATION_ID = 100601;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.d(TAG, "Start receiving sms...");
			
			Map<String, String> msg = RetrieveMessages(intent);
			
			if (msg != null) {
	    		CADbEngine dbEngine = createDbEngine(context);

	    		try {
		    		ArrayList<CAContact> oldWhiteList = new ArrayList<CAContact>();
		    		dbEngine.getContactsTable().getWhiteList(oldWhiteList);
		    		
					for (String sender : msg.keySet()) {
		    			Log.d(TAG, "Check sender "+sender);
			    		
		            	if(isNeedToBlock(context, sender, oldWhiteList)){
			    			Log.d(TAG, "Sender is in black list!");
		                	String text = msg.get(sender);
		                	insertSmsToDatabase(dbEngine, sender, text);
		                	showNotification(context, sender, text);
		            		abortBroadcast();
		            		break;
		            	}
		            }
	    		}
	    		finally {
	    			dbEngine.close();
	    		}
	        }
		}
		catch (Exception e) {
			Log.e(TAG, "Error on receive sms: "+e);
		}
    }
	
	private CADbEngine createDbEngine(Context context) throws Exception {
		CADbEngine dbEngine = new CADbEngine(context);
		dbEngine.open();
		return dbEngine;
	}

	private void insertSmsToDatabase(CADbEngine dbEngine, String sender, String text) {
		long now = System.currentTimeMillis();
		CASms sms = new CASms(sender, text, now);
		dbEngine.getSmsTable().insert(sms);
	}

	private boolean isNeedToBlock(Context context, String sender, List<CAContact> whiteList) {
		Log.d(TAG, "White list contains "+whiteList.size()+" items");
		for(CAContact contact: whiteList) {
			//Log.d(TAG, "Compare with "+contact.getAddress());
			if(contact.getAddress().equals(sender))
				return false;
		}
		
		boolean isInPhoneBook = CAUtils.isPhoneBookContains(context, sender);
		Log.d(TAG, "isInPhoneBook = "+isInPhoneBook);

		return !isInPhoneBook;
	}
	
	private void showNotification(Context context, String title, String text) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);

		Intent intent = new Intent(context, CAMainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.putExtra(CAMainActivity.KEY_TAB_POSITION, 2);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
		NotificationCompat.Builder builder= new NotificationCompat.Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(text);
		//builder.setLargeIcon(getIcon(context));
		builder.setSmallIcon(R.drawable.icon);
		builder.setContentIntent(contentIntent);
		builder.setTicker(context.getString(R.string.notification_ticker, title));
		builder.setAutoCancel(true);
		builder.setOnlyAlertOnce(false);
		/*
		if(notification.detailsAreaTextLines.size() > 0) {
			InboxStyle inboxStyle = new InboxStyle();
			inboxStyle.setBigContentTitle(notification.contectTitle);
			inboxStyle.setSummaryText(notification.contentText);
			for(Spanned line : notification.detailsAreaTextLines) {
				inboxStyle.addLine(line);
			}
			builder.setStyle(inboxStyle);
		}*/
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

    private static Map<String, String> RetrieveMessages(Intent intent) {
        
    	Map<String, String> msg = null; 
        Bundle bundle = intent.getExtras();
        
        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                int pdusCount = pdus.length;
                msg = new HashMap<String, String>(pdusCount);
				SmsMessage[] msgs = new SmsMessage[pdusCount];
                
                for (int i = 0; i < pdusCount; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    
                    String origAddress = msgs[i].getOriginatingAddress();
                    
                    if (!msg.containsKey(origAddress)) {
                        msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody()); 
                        
                    } else {    
                        String prevParts = msg.get(origAddress);
                        String msgString = prevParts + msgs[i].getMessageBody();
                        msg.put(origAddress, msgString);
                    }
                }
            }
        }

        return msg;
    }
}
