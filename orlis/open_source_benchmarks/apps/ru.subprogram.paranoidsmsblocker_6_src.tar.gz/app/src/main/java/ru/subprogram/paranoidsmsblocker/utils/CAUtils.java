package ru.subprogram.paranoidsmsblocker.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.database.entities.TAContactStatus;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class CAUtils {

	private static final String TAG = "CAUtils";

	public static interface GetMissingBlackListItemsObserver {
		boolean isCancelled();
	}

	public static List<CAContact> getMissingBlackListItems(Context context,
			Set<String> addresses,
			ArrayList<CAContact> oldBlackList, ArrayList<CAContact> oldWhiteList, 
			GetMissingBlackListItemsObserver observer) {

		List<CAContact> newBlackList = new ArrayList<CAContact>();
		
		for(String address: addresses) {
			if(observer!=null && observer.isCancelled())
				return null;
			if(address.startsWith("z"))
				Log.i(TAG, address);
			boolean isAlreadyExist = isListContains(oldBlackList, address)
											|| isListContains(oldWhiteList, address);
			if(isAlreadyExist) continue;
			
			if(!isPhoneBookContains(context, address))
				newBlackList.add(new CAContact(TAContactStatus.EBlackList, address));
		}
		return newBlackList;
	}

	public static boolean isPhoneBookContains(Context context, String address) {
		Cursor phoneCursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null,ContactsContract.CommonDataKinds.Phone.NUMBER +" = ?", new String[] {address}, null); 
		if(phoneCursor!=null) {
			try {
				if(phoneCursor.moveToNext()) 
					return true;
				else {
					if(address.length()>5 && PhoneNumberUtils.isWellFormedSmsAddress(address)) {
						phoneCursor.close();
						
					    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
					    phoneCursor = context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME},null,null,null);
					    	
						return phoneCursor.moveToNext();
					}
					else 
						return false;
				}
			}
			finally {
				phoneCursor.close();
			}
		}
		else
			return false;
	}

	public static Set<String> getContactsByExistentSms(Context context, GetMissingBlackListItemsObserver observer) {
		Set<String> addresses = new HashSet<String>();
		
		
		Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		if(cursor!=null) {
			try {
				while(cursor.moveToNext()) {
			       String address = getString(cursor, "address");
		    	   addresses.add(address);
		    	   if(observer!=null && observer.isCancelled())
		    		   return null;
				}
			}
			finally {
				cursor.close();
			}
		
		}
		return addresses;
	}

	private static boolean isListContains(ArrayList<CAContact> list, String address) {
		for(CAContact contact: list) {
			if(address.equals(contact.getAddress())) {
				return true;
			}
		}
		return false;
	}

	private static String getString(Cursor cursor, String colName) {
		return cursor.getString(cursor.getColumnIndex(colName));
	}

}
