package net.bitconomy.ckpoolwatcher;

/**
 * Created by Ali on 2/3/2015.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity {

    private Button getButton;
    private Button btnSave;
    private Button btnCancel;
    private EditText usernameField;
    private EditText apiKeyField;

    TextView IsConnectedTxt;
    TextView minhash;
    TextView hrhash;
    TextView poolh;
    TextView worknm;
    TextView poollb;
    TextView donate;
    //TextView kano;
    LinearLayout mainbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SharedPreferences preferences = getSharedPreferences("prefName", MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();
        edit.putBoolean("isFirstRun", false);
        edit.commit();
        getButton = (Button) findViewById(R.id.getButton);
        getButton.setOnClickListener(new Button_Clicker());

        usernameField   = (EditText)findViewById(R.id.etUser);
        apiKeyField = (EditText)findViewById(R.id.etApi);

        // get reference to the views
        minhash = (TextView) findViewById(R.id.minhash);
        hrhash = (TextView) findViewById(R.id.hrhash);
        poolh = (TextView) findViewById(R.id.poolh);
        worknm = (TextView) findViewById(R.id.worknm);
        IsConnectedTxt = (TextView) findViewById(R.id.IsConnectedTxt);
        poollb = (TextView) findViewById(R.id.poollb);
        donate = (TextView) findViewById(R.id.donate);
        mainbox = (LinearLayout) findViewById(R.id.mainbox);
        //kano = (TextView) findViewById(R.id.kano);

        SharedPreferences settings = getSharedPreferences("prefName", MODE_PRIVATE);
        boolean isFirstRun = settings.getBoolean("isFirstRun", true);

        //dialog




        // check if it's the first run

        if (!isFirstRun){
            String user = settings.getString("Username", "Username");
            usernameField.setText(user);
            String api = settings.getString("Api", "Api Key");
            apiKeyField.setText(api);
        }

        // check if you are connected or not
        if(isConnected()){
            IsConnectedTxt.setBackgroundColor(0xFF00CC00);
            IsConnectedTxt.setText("You are connected");
        }
        else{
            IsConnectedTxt.setText("You are NOT connected");
        }
        //donate setup
        donate.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href=\"http://aliw.tip.me\">Donate</a>";
        donate.setText(Html.fromHtml(text));


        //check if they have an api key set
        if (apiKeyField.getText().toString().length() < 12) {
            showDialog();
        }


        // call AsynTask to perform network operation on separate thread
    }



    class Button_Clicker implements Button.OnClickListener {
        @Override
        public void onClick(View v) {

            if (v == getButton) {

                //check if they have an api key set
                if (apiKeyField.getText().toString().length() < 12) {
                    showDialog();
                }else {

                    SharedPreferences preferences = getSharedPreferences("prefName", MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("Username", usernameField.getText().toString());
                    edit.putString("Api", apiKeyField.getText().toString());
                    edit.commit();
                    new HttpAsyncTask().execute("https://www.kano.is/index.php?k=api&username=" + usernameField.getText().toString() + "&api=" + apiKeyField.getText().toString() + "&json=y&work=y");
                    new HttpAsyncTask().execute("https://www.kano.is/index.php?k=api&username=" + usernameField.getText().toString() + "&api=" + apiKeyField.getText().toString() + "&json=y");
                    mainbox.setVisibility(View.VISIBLE);

                }

            }
        }
    }

    private void showDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("API KEY");

        final TextView textView =(TextView)dialog.findViewById(R.id.kano);
        Linkify.addLinks(textView, Linkify.ALL);

        Button btnSave=(Button)dialog.findViewById(R.id.save);
        btnSave.setOnClickListener(new Button_Clicker());
        Button btnCancel=(Button)dialog.findViewById(R.id.cancel);
        btnCancel.setOnClickListener(new Button_Clicker());

        dialog.show();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();

            }
        });
    }



    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            try {
                JSONObject json = new JSONObject(result);
                if(result.contains("u_hashrate5m"))
                {

                    String str, minh, hr, phash, lb;
                    str = minh = hr = phash = lb = "";
                    Log.d("YOGIRL", "Json value :" + json);
                    Log.d("YOGIRL", "Json value :" + json.getString("lastbc"));

                    double hashrate5m = Double.parseDouble(json.getString("u_hashrate5m"));
                    double hashrate5m_t = (hashrate5m / 1000000000000.00);
                    double hashrate1hr = Double.parseDouble(json.getString("u_hashrate1hr"));
                    double hashrate1hr_t = (hashrate1hr / 1000000000000.00);
                    double p_hashrate5m = Double.parseDouble(json.getString("p_hashrate5m"));
                    double p_hashrate5m_t = (p_hashrate5m / 1000000000000.00);
                    double p_hashrate1hr = Double.parseDouble(json.getString("p_hashrate1hr"));
                    double p_hashrate1hr_t = (p_hashrate1hr / 1000000000000.00);

                    BigDecimal a = new BigDecimal(hashrate5m_t);
                    BigDecimal floored_5m = a.setScale(2, BigDecimal.ROUND_DOWN);
                    BigDecimal b = new BigDecimal(hashrate1hr_t);
                    BigDecimal floored_1hr = a.setScale(2, BigDecimal.ROUND_DOWN);
                    BigDecimal c = new BigDecimal(p_hashrate5m_t);
                    BigDecimal p_floored_5m = c.setScale(2, BigDecimal.ROUND_DOWN);
                    BigDecimal d = new BigDecimal(p_hashrate1hr_t);
                    BigDecimal p_floored_1hr = d.setScale(2, BigDecimal.ROUND_DOWN);



                    minh += "5min User HashRate: " + (floored_5m) + " TH/S";
                    hr += "1Hr User HashRate: " + (floored_1hr) + " TH/S";
                    phash += "1Hr Pool HashRate: " + (p_floored_1hr) + " TH/S";
                    lb += "Last Block: "+ json.getString("lastbc");

                    //  str += "5min Pool HashRate: "+ (p_floored_5m) + " TH/S";
                    //str += "Last Block Height: "+ json.getString("lastheight");
                    //  str += "Current Difficulty: "+ json.getString("currndiff");



                    minhash.setText(minh);
                    hrhash.setText(hr);
                    poolh.setText(phash);
                    poollb.setText(lb);

                } else if(result.contains("workername"))
                {
                    String workn = "";
                    String workername = (json.getString("workername:0"));

                    workn += "Workername: " + (workername) + " ";
                    worknm.setText(workn);

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}
