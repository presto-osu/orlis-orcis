/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Base64;

import fr.mobdev.goblim.listener.NetworkListener;
import fr.mobdev.goblim.objects.Img;

public class NetworkManager {
	
	private NetworkListener listener;
	private static NetworkManager instance;
	private Context context;

	private NetworkManager(NetworkListener listener,Context context)
	{
		this.listener = listener;
		this.context = context;
	}
	
	public static NetworkManager getInstance(NetworkListener listener, Context context){
		if (instance == null)
			instance = new NetworkManager(listener,context);
		if(listener != null && listener != instance.getListener())
			instance.setListener(listener);
		return instance;
	}
	
	private NetworkListener getListener()
	{
		return listener;
	}
	
	private void setListener(NetworkListener listener)
	{
		this.listener = listener;
	}
	
	public void upload(final String siteUrl, final int nbDays, final String fileName, final byte[] bytearray)
	{
		if(!isConnectedToInternet(context)) {
			listener.fileUploadError(context.getString(R.string.no_network));
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				Img img = uploadImage(siteUrl,nbDays, fileName,bytearray);
				if(img != null)
					listener.fileUploaded(img);
			}
		}).start();
	}

	public Img uploadImage(String siteUrl, int nbDays, String fileName, byte[] byteArray) {

		URL url = null;
		Img imgOutput = null;
		try {
			url = new URL(siteUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		HttpURLConnection conn;
		InputStream stream = null;
		DataOutputStream request = null;
		try {
			if(isConnectedToInternet(context))
			{
				String crlf = "\r\n";
				String hyphens = "--";
				String boundary =  "------------------------dd8a045fcc22b35c";
                //check if there is a HTTP 301 Error
                if(url != null) {
                    conn = (HttpURLConnection) url.openConnection();
                }
                else {
                    listener.fileUploadError(context.getString(R.string.connection_failed));
                    return null;
                }
				String location = conn.getHeaderField("Location");
				if(location != null) {
                    //if there is follow the new destination
					siteUrl = location;
					url = new URL(location);
				}
				conn = (HttpURLConnection) url.openConnection();
                //prepare the connection for upload
				conn.setRequestMethod("POST");
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				conn.setRequestProperty("User-Agent", "Goblim");

				conn.setRequestProperty("Expect", "100-continue");
				conn.setRequestProperty("Accept", "*/*");
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

				request = new DataOutputStream(conn.getOutputStream());

                //ask for JSON answer
				request.writeBytes(hyphens + boundary + crlf);
				request.writeBytes("Content-Disposition: form-data; name=\"format\"" + crlf);
				request.writeBytes(crlf);
				request.writeBytes("json" + crlf);

                //ask for storage duration
				request.writeBytes(hyphens + boundary + crlf);
				request.writeBytes("Content-Disposition: form-data; name=\"delete-day\"" + crlf);
				request.writeBytes(crlf);
				request.writeBytes(nbDays + crlf);

                //setup filename and say that octets follow
				request.writeBytes(hyphens + boundary + crlf);
				request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + crlf);
				request.writeBytes("Content-Type: application/octet-stream" + crlf);
				request.writeBytes(crlf);
				request.flush();

                //write image data
				request.write(byteArray);

                //finish the format http post packet
				request.writeBytes(crlf);
				request.writeBytes(hyphens + boundary + hyphens + crlf);
				request.flush();

                //get answer
				stream = conn.getInputStream();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			listener.fileUploadError(context.getString(R.string.network_error));
		}

		if(stream != null) {
            //prepare JSON reading
			InputStreamReader isr = new InputStreamReader(stream);
			BufferedReader br = new BufferedReader(isr);
			boolean isReading = true;
			String data;
			String jsonStr = "";
            //get all data in a String
			do {
				try {
					data = br.readLine();
					if (data != null)
						jsonStr += data;
					else
						isReading = false;
				} catch (IOException e) {
					e.printStackTrace();
					isReading = false;
				}
			} while (isReading);

			//parse JSON answer
			try {
				request.close();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
			    // Parse the JSON to a JSONObject
				JSONObject rootObject = new JSONObject(jsonStr);
				// Get msg (root) element
				JSONObject msg = rootObject.getJSONObject("msg");
                // is there an error?
				if(msg.has("msg")) {
					String error = msg.getString("msg");
					listener.fileUploadError(error);
					return null;
				}
				else if(msg.has("short")) {
                    //retrieve useful data
					String hashOutput = msg.getString("short");
					String realHashOutput = msg.getString("real_short");
					String token = msg.getString("token");
					imgOutput = new Img(0, siteUrl, hashOutput, realHashOutput, Calendar.getInstance(), nbDays, null,token);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				listener.fileUploadError(context.getString(R.string.unreadable_json));
			}
		}
		return imgOutput;
	}

    public void deleteImage(final String deleteUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (!isConnectedToInternet(context))
                    listener.deleteError(context.getString(R.string.no_network));
                URL url = null;
                try {
                    url = new URL(deleteUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (url != null) {
                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        if(urlConnection != null) {
                            InputStream stream = urlConnection.getInputStream();
                            stream.close();
                        }
                        else {
                            listener.deleteError(context.getString(R.string.network_error));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    listener.deleteSucceed();
                }
            }
        }).start();
    }

	private boolean isConnectedToInternet(Context context) 
	{
        //verify the connectivity
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null) 
		{
			State networkState = networkInfo.getState();
			if (networkState.equals(State.CONNECTED)) 
			{
				return true;
			}
		}
		return false;
	}

}