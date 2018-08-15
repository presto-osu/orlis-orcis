package com.linuxcounter.lico_update_003;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sendSysInfo extends Activity {

	final String TAG = "MyDebugOutput";
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_sys_info);
	    TextView myText = (TextView)findViewById(R.id.textView6);
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		postData(getApplicationContext(), getSysInfo.aSendData);
		String response = "Thanks! Your machine data got saved!\n\nYou now may close this app or you may put it into the background to do automatic updates every 8 hours.";
	    myText.setText(response);
	}



	public String postData(Context context, final String postdata[]) {
		Log.i(TAG, "sendSysInfo: start postData()...");
		String responseBody = "";
		String[] firstseparated = postdata[0].split("#");
		String url = firstseparated[1];
		String[] secseparated = postdata[1].split("#");
		String machine_id = secseparated[1];
		String[] thirdseparated = postdata[2].split("#");
		final String machine_updatekey = thirdseparated[1];

		String data = null;
		String contentType;
		contentType = "application/x-www-form-urlencoded";
		Log.i(TAG, "sendSysInfo: start Volley Send POST()...");

		RequestQueue queue = Volley.newRequestQueue(context);
		StringRequest sr = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.i(TAG, "sendSysInfo: response: " + response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i(TAG, "sendSysInfo: error: " + error.toString());
			}
		}){
			@Override
			protected Map<String,String> getParams(){
				Map<String,String> params = new HashMap<String, String>();
				for (int i = 0; i < postdata.length; i++) {
					String[] separated = postdata[i].split("#");
					Log.i(TAG, "sendSysInfo: PATCH data:  " + separated[0] + "=" + separated[1]);
					params.put(separated[0], separated[1]);
				}
				return params;
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String,String> params = new HashMap<String, String>();
				params.put("Accept", "application/json");
				params.put("Content-Type", "application/x-www-form-urlencoded");
				params.put("x-lico-machine-updatekey", machine_updatekey);
				return params;
			}
		};
		queue.add(sr);

		return responseBody;
	}
}
