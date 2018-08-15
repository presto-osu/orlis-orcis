package com.linuxcounter.lico_update_003;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class getSysInfo extends Activity implements OnClickListener {

	public String sAppVersion = "0.0.8";
	
	static String aSendData[] = {};

	final String TAG = "MyDebugOutput";

	@SuppressWarnings("deprecation")
	@SuppressLint({ "NewApi", "SdCardPath" })
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "getSysInfo: onCreate...");

		String toks[] = null;
		String MemTotalt = null;
		int MemTotal = 0;
		String MemFreet = null;
		int MemFree = 0;
		String SwapTotalt = null;
		int SwapTotal = 0;
		String SwapFreet = null;
		int SwapFree = 0;
		String cpumodel = null;
		String scpunum = null;
		int cpunum = 0;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_sys_info);
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(this);
		
	    TextView myText = (TextView)findViewById(R.id.textView5);

	    System.getProperty("user.region");

	    String androidversion;
	    androidversion = System.getProperty("http.agent").replaceAll(".*Android *([0-9.]+).*", "$1");

		Log.i(TAG, "getSysInfo: androidversion: "+androidversion);

	    System.getProperty("os.version");
	    
	    Point size = new Point();
	    WindowManager w = getWindowManager();
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
          w.getDefaultDisplay().getSize(size); 
        }else{
          Display d = w.getDefaultDisplay(); 
          d.getWidth(); 
          d.getHeight(); 
        }
	    
        String loadavg = "";
	    try {
	    	BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/loadavg" ) ), 1000 );
	        String[] temp = reader.readLine().split(" ");
	        loadavg = temp[0]+" "+temp[1]+" "+temp[2];
	        reader.close();
	    } catch(IOException ex) {
	    	ex.printStackTrace();
	    }
		Log.i(TAG, "getSysInfo: loadavg: "+loadavg);

		String cpuinfo = "";
		try {
			cpuinfo = getStringFromFile("/proc/cpuinfo").replace("\r", "").replace("\n\n", "\n");
			toks = cpuinfo.split("\n");
			for (int a = 0; a<toks.length; a++) {
			   String k = (String) toks[a];
			   String[] toks2 = k.split(":");
			   if (toks2[0].trim().matches("^Processor.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
				   cpumodel = toks2[1].trim();
			   } else if (toks2[0].trim().matches("^processor.*") && toks2[1].trim().matches("^[0-9]+")) {
				   scpunum = toks2[1].trim();
			   }
			}
			try {
			    cpunum = Integer.parseInt(scpunum);
			    cpunum++;
			} catch(NumberFormatException nfe) {
			   System.out.println("Could not parse " + nfe);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.i(TAG, "getSysInfo: cpumodel: "+cpumodel);
		Log.i(TAG, "getSysInfo: cpunum: "+cpunum);

	    String flags = "";
		try {
			String flagst = getStringFromFile("/proc/cpuinfo").replace("\r", "").replace("\n\n", "\n");
			toks = flagst.split("\n");
			for (int a = 0; a<toks.length; a++) {
			   String k = (String) toks[a];
			   String[] toks2 = k.split(":");
				if (toks2[0].trim().matches("^Features.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
					flags = toks2[1].trim();
				}
				if (toks2[0].trim().matches("^flags.*") && toks2[1].trim().matches("^[a-zA-Z]+.*")) {
					flags = toks2[1].trim();
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.i(TAG, "getSysInfo: flags: "+flags);
	    
	    String meminfo = "";
		try {
			meminfo = getStringFromFile("/proc/meminfo");
			toks = meminfo.split("\n");
			for (int a = 0; a<toks.length; a++) {
			   String k = (String) toks[a];
			   String[] toks2 = k.split(":");
			   if (toks2[0].trim().matches(".*MemTotal.*")) {
				   MemTotalt = toks2[1].replace(" kB", "").trim();
				   MemTotal = Integer.parseInt(MemTotalt.toString());
//				   MemTotal = (MemTotal * 1024);
			   } else if (toks2[0].trim().matches(".*MemFree.*")) {
				   MemFreet = toks2[1].replace(" kB", "").trim();
				   MemFree = Integer.parseInt(MemFreet.toString());
//				   MemFree = (MemFree * 1024);
			   } else if (toks2[0].trim().matches(".*SwapTotal.*")) {
				   SwapTotalt = toks2[1].replace(" kB", "").trim();
				   SwapTotal = Integer.parseInt(SwapTotalt.toString());
//				   SwapTotal = (SwapTotal * 1024);
			   } else if (toks2[0].trim().matches(".*SwapFree.*")) {
				   SwapFreet = toks2[1].replace(" kB", "").trim();
				   SwapFree = Integer.parseInt(SwapFreet.toString());
//				   SwapFree = (SwapFree * 1024);
			   }
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.i(TAG, "getSysInfo: MemTotal: "+MemTotal);
		Log.i(TAG, "getSysInfo: MemFree: "+MemFree);
		Log.i(TAG, "getSysInfo: SwapTotal: "+SwapTotal);
		Log.i(TAG, "getSysInfo: SwapFree: "+SwapFree);

		long total = 0;
		long avail = 0;
		File dir = null;
		boolean extexists = false;
		
		dir = new File("/mnt/sdcard/external_sd/");
		if(dir.exists() && (dir.isDirectory() || dir.isFile())) {
			total = TotalMemoryOfDir("/mnt/sdcard/external_sd/");
			avail = FreeMemoryOfDir("/mnt/sdcard/external_sd/");
			extexists = true;
		}
		dir = new File("/mnt/extSdCard/");
		if(dir.exists() && (dir.isDirectory() || dir.isFile())) {
			total = TotalMemoryOfDir("/mnt/extSdCard/");
			avail = FreeMemoryOfDir("/mnt/extSdCard/");
			extexists = true;
		}
		String sdpath = Environment.getExternalStorageDirectory().getPath();
		dir = new File(sdpath);
		if(dir.exists() && (dir.isDirectory() || dir.isFile())) {
			total = TotalMemoryOfDir(sdpath);
			avail = FreeMemoryOfDir(sdpath);
			extexists = true;
		}

		long disktotal = 0;
		if (extexists == true) {
			disktotal = TotalMemory() + total;
		} else {
			disktotal = TotalMemory();
		}
		Log.i(TAG, "getSysInfo: disktotal: "+disktotal);

		long freedisk = 0;
		if (extexists == true) {
			freedisk = FreeMemory() + avail;
		} else {
			freedisk = FreeMemory();
		}
		Log.i(TAG, "getSysInfo: freedisk: "+freedisk);

		String hostname = "localhost";
		String filename = ".linuxcounter";
		String filepath = Environment.getExternalStorageDirectory()+ "/data/com.linuxcounter.lico_update_003";
		File readFile = new File(filepath, filename);
        String machine_id = "";
        String machine_updatekey = "";
		String load = "";
		try {
	    	BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(readFile) ), 1000 );
	        load = reader.readLine();
	        reader.close();
	        String[] toks1 = load.split(" ");
			machine_id = (String)toks1[0];
			machine_updatekey = (String)toks1[1];
		} catch(Exception e1) {
			// Do nothing
		}
		
		String machine = System.getProperty("os.arch");
		String version = "";
		try {
			version = Command("uname -r").trim();
		} catch (Exception e1) {
			try {
				version = getStringFromFile("/proc/sys/kernel/osrelease");
			} catch (Exception e2) {
				version = "unknown";
			}
		}
		Log.i(TAG, "getSysInfo: machine: "+machine);
		Log.i(TAG, "getSysInfo: version: "+version);

		String uptime = Command("uptime").trim();
		String[] toks2 = uptime.split(", ");
		uptime = toks2[0].replace("up time: ", "").trim();
		Log.i(TAG, "getSysInfo: uptime: "+uptime);

		String url = "http://api.linuxcounter.net/v1/machines/" + machine_id;

		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		String deviceclass = "Smartphone";
		if (tabletSize) {
			deviceclass = "Tablet";
		}
		Log.i(TAG, "getSysInfo: deviceclass: "+deviceclass);

		TelephonyManager tm = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
		String countryCode = tm.getNetworkCountryIso();
		Log.i(TAG, "getSysInfo: countryCode: "+countryCode.toUpperCase());

		aSendData = new String[] {
				"url#" + url,
				"machine_id#" + machine_id,
				"machine_updatekey#" + machine_updatekey,
				"appversion#" + sAppVersion,
				"hostname#" + hostname,
				"cores#" + cpunum,
				"flags#" + flags,
				"diskspace#" + disktotal,
				"diskspaceFree#" + freedisk,
				"memory#" + MemTotal,
				"memoryFree#" + MemFree,
				"swap#" + SwapTotal,
				"swapFree#" + SwapFree,
				"distversion#" + androidversion,
				"online#1",
				"uptime#" + uptime,
				"loadavg#" + loadavg,
				"distribution#Android",
				"kernel#" + version,
				"cpu#" + cpumodel,
				"country#" + countryCode.toUpperCase(),
				"architecture#" + machine,
				"class#" + deviceclass
		};

	    String sysinformation = 
	    	    "hostname : " + hostname + "\n" +
	    		"machine_id : " + machine_id + "\n" +
	    		"machine_updatekey : " + machine_updatekey + "\n" +
				"appversion : " + sAppVersion + "\n" +
				"processor : " + cpumodel + "\n" +
	    		"cpunum : " + cpunum + "\n" +
	    		"totaldisk : " + disktotal + "\n" +
	    		"totalram : " + MemTotal + "\n" +
	    		"freedisk : " + freedisk + "\n" +
	    		"freeram : " + MemFree + "\n" +
	    		"totalswap : " + SwapTotal + "\n" +
	    		"freeswap : " + SwapFree + "\n" +
	    		"flags : " + flags + "\n" +
	    		"machine : " + machine + "\n" +
	    		"version : " + version + "\n" +
	    		"uptime : " + uptime + "\n" +
	    		"load : " + loadavg + "\n" +
	    		"distribution : Android\n" +
				"distribversion : " + androidversion + "\n" +
				"class : " + deviceclass + "\n" +
				"country : " + countryCode.toUpperCase() + "\n";

		// Log.i(TAG, "getSysInfo: sending to LiCo...");
		// Log.i(TAG, "getSysInfo: " + sysinformation);

	    myText.setText(sysinformation);
	    
	}
	
    public static String convertStreamToString(InputStream is) throws Exception {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
    	File fl = new File(filePath);
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}

    public long TotalMemory() {
    	StatFs statFs = null;
    	statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
    	long totalBlocks = (long) statFs.getBlockCount();
    	long blockSize = (long) statFs.getBlockSize();
    	long Total = ((((long)totalBlocks * (long)blockSize)) / 1024);
        return Total;
    }

    public long FreeMemory() {
    	StatFs statFs = null;
    	statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
    	long availBlocks = (long) statFs.getAvailableBlocks();
    	long blockSize = (long) statFs.getBlockSize();
    	long free_memory = ((((long)availBlocks * (long)blockSize)) / 1024);
        return free_memory;
    }
    
    public long TotalMemoryOfDir(String dir) {
    	StatFs statFs = null;
    	statFs = new StatFs(dir);
    	long totalBlocks = (long) statFs.getBlockCount();
    	long blockSize = (long) statFs.getBlockSize();
    	long Total = ((((long)totalBlocks * (long)blockSize)) / 1024);
        return Total;
    }

    public long FreeMemoryOfDir(String dir) {
    	StatFs statFs = null;
    	statFs = new StatFs(dir);
    	long availBlocks = (long) statFs.getAvailableBlocks();
    	long blockSize = (long) statFs.getBlockSize();
    	long free_memory = ((((long)availBlocks * (long)blockSize)) / 1024);
        return free_memory;
    }
    
    public String Command(String command) {
    	try {
    	    Process process = Runtime.getRuntime().exec(command);
    	    BufferedReader reader = new BufferedReader(
    	    new InputStreamReader(process.getInputStream()));
    	    int read;
    	    char[] buffer = new char[4096];
    	    StringBuffer output = new StringBuffer();
    	    while ((read = reader.read(buffer)) > 0) {
    	        output.append(buffer, 0, read);
    	    }
    	    reader.close();
    	    process.waitFor();
    	    return output.toString();
    	} catch (IOException e) {
    	    throw new RuntimeException(e);
    	} catch (InterruptedException e) {
    	    throw new RuntimeException(e);
    	}
    }
    
	public void onClick(View v) {
		Intent msgIntent = new Intent(this, UpdateInBackgroundService.class);
		startService(msgIntent);
		
		startActivity(new Intent(this,sendSysInfo.class));
		finish();
	}
	
}
