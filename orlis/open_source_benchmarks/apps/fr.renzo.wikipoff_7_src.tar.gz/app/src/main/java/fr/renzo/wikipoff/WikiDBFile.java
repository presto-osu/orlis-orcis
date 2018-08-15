package fr.renzo.wikipoff;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WikiDBFile implements Serializable {

	private static final long serialVersionUID = -4809830901675667519L;
	@SuppressWarnings("unused")
	private static final String TAG = "WikiDBFile";
	private String date="";
	private String filename;
	
	public WikiDBFile() {}
	
	public WikiDBFile(File sqlitefile) {
		this.filename = sqlitefile.getName();
		setSize(sqlitefile.length());	
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	private long size;
	private String url;
	public String getUrl() {
		return url;
	}
	public String toString(){
		return this.filename;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGendate() {
		return this.date;
	}


	public Date getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse(this.date);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
		
	}
	public String getDateAsString() {
		return this.date.toString();
	}
	
	public void setGendate(String date) {
		this.date = date;
		
	}

	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

}
