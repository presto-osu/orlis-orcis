package ru.subprogram.paranoidsmsblocker.activities.utils;

import android.webkit.MimeTypeMap;

public class CAMime {

	public static String getMimeType(String url) {
	    String type = null;
	    String extension = getFileExtensionFromUrl(url);
	    if (extension != null) {
	        MimeTypeMap mime = MimeTypeMap.getSingleton();
	        type = mime.getMimeTypeFromExtension(extension);
	    }
	    
	    if(type == null)
	    	type = getMimeFromLocalMap(extension);
	    
	    return type;
	}
	
	private static String getFileExtensionFromUrl(String url) {
		int slashPos = url.lastIndexOf('/');
		if(slashPos<0) 
			return "";
		String filename = url.substring(slashPos+1);
		int dotPos = filename.lastIndexOf('.');
		if(dotPos<0) 
			return "";
		return filename.substring(dotPos+1);
	}

	private static String getMimeFromLocalMap(String extension) {

		if(extension.equals("bin"))
			return "file/bin";
		else if(extension.equals("db"))
			return "file/database";
		return null;
	}

}
