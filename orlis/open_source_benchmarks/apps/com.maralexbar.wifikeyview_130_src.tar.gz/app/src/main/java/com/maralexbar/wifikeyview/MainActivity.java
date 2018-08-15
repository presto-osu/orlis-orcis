/*
 * Copyright (C) 2014
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.maralexbar.wifikeyview;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.renderscript.Int2;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	String pwd="";
	String type="";
	String open="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pwd=getResources().getString(R.string.pwd);
		type=getResources().getString(R.string.type);
		open=getResources().getString(R.string.open);

		EditText et = (EditText) this.findViewById(R.id.data_text);
		et.setText(RunAsRoot("cat /data/misc/wifi/wpa_supplicant.conf | busybox grep -E \"key_mgmt=|ssid=|psk=\""));
	}

	private String RunAsRoot(String cmd) {
        String ret;
        int id=1;
		try {
		    Process process = Runtime.getRuntime().exec("su");
			DataOutputStream dos = new DataOutputStream(process.getOutputStream());
			InputStream is = process.getInputStream();

			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();

            String tmp="";
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int read = 0;
			while((read = is.read(buffer)) > 0){
				baos.write(buffer, 0, read);
			}
			tmp = baos.toString("UTF-8");
			tmp = tmp.replace("\t", "");
			tmp = tmp.replace("\"", "");
			tmp = tmp.replace("ssid=", "SSID: ");
			tmp = tmp.replace("psk=", pwd+": ");
			tmp = tmp.replace("key_mgmt=NONE", type+": "+open+"\n");
			tmp = tmp.replace("key_mgmt=WPA-PSK", type+": "+"WPA-PSK"+"\n");
			String[] datas = tmp.split("\n");

		    process.waitFor();
		    
		    ret = id == 0 ? "NULL" : tmp;
		} catch (IOException e) {
		    throw new RuntimeException(e);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
		return ret;
	}
}
