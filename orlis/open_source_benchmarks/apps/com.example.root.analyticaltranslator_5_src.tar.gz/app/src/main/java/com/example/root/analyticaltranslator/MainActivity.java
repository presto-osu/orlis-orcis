package com.example.root.analyticaltranslator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private TextView txtDisplay, txtDisplayResult,txtDisplayAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDisplay = (TextView) findViewById(R.id.edittext_german);
        txtDisplayResult = (TextView) findViewById(R.id.text_spanish);
        txtDisplayAlerts = (TextView) findViewById(R.id.text_alerts);

    }


    public void onclickactivity(View myFavoriteView) {

        String url = "http://lingoworld.eu/translator/website/php/translatorfunction.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        try {
                            txtDisplayResult.setText("");
                            JSONArray ar = new JSONArray(response);
                            for (int i = 0; i < ar.length(); i++) {
                                txtDisplayResult.append(ar.getString(i) + " ");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e("response", error.getMessage() + " ");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("text", txtDisplay.getText().toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        url = "http://lingoworld.eu/translator/website/php/messagefunction.php";
         stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        try {
                            txtDisplayAlerts.setText("");
                            JSONArray ar = new JSONArray(response);
                            for (int i = 0; i < ar.length(); i++) {
                                txtDisplayAlerts.append(ar.getString(i) + " ");
                                Log.e("alerts",ar.getString(i) + " ");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                        Log.e("response", error.getMessage() + " ");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("text", txtDisplay.getText().toString());
                return params;
            }
        };
        requestQueue.add(stringRequest);


    }

}