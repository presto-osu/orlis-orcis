package com.thefonz.ed_tool;

/**
 * Created by thefonz on 18/03/15.
 */

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.thefonz.ed_tool.tcp_client.CustomKeyMap;
import com.thefonz.ed_tool.tcp_client.TCPClient;
import com.thefonz.ed_tool.utils.Utils;

import java.util.Arrays;
import java.util.Objects;

public class Tab_ButtonBox extends Fragment {
    private TCPClient mTcpClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.tab_buttonbox, container, false);

        // initialize all buttons and set onclicklisteners for all
        final Button buttonB_1 = (Button)myFragmentView.findViewById(R.id.buttonB_1);
            buttonB_1.setOnClickListener(onClickListener);
        final Button buttonB_2 = (Button)myFragmentView.findViewById(R.id.buttonB_2);
            buttonB_2.setOnClickListener(onClickListener);
        final Button buttonB_3 = (Button)myFragmentView.findViewById(R.id.buttonB_3);
            buttonB_3.setOnClickListener(onClickListener);
        final Button buttonB_4 = (Button)myFragmentView.findViewById(R.id.buttonB_4);
            buttonB_4.setOnClickListener(onClickListener);
        final Button buttonB_5 = (Button)myFragmentView.findViewById(R.id.buttonB_5);
            buttonB_5.setOnClickListener(onClickListener);
        final Button buttonB_6 = (Button)myFragmentView.findViewById(R.id.buttonB_6);
            buttonB_6.setOnClickListener(onClickListener);
        final Button buttonB_7 = (Button)myFragmentView.findViewById(R.id.buttonB_7);
            buttonB_7.setOnClickListener(onClickListener);
        final Button buttonB_8 = (Button)myFragmentView.findViewById(R.id.buttonB_8);
            buttonB_8.setOnClickListener(onClickListener);
        final Button buttonB_9 = (Button)myFragmentView.findViewById(R.id.buttonB_9);
            buttonB_9.setOnClickListener(onClickListener);
        final Button buttonB_10 = (Button)myFragmentView.findViewById(R.id.buttonB_10);
            buttonB_10.setOnClickListener(onClickListener);
        final Button buttonB_11 = (Button)myFragmentView.findViewById(R.id.buttonB_11);
            buttonB_11.setOnClickListener(onClickListener);
        final Button buttonB_12 = (Button)myFragmentView.findViewById(R.id.buttonB_12);
            buttonB_12.setOnClickListener(onClickListener);
        final Button buttonB_13 = (Button)myFragmentView.findViewById(R.id.buttonB_13);
            buttonB_13.setOnClickListener(onClickListener);
        final Button buttonB_14 = (Button)myFragmentView.findViewById(R.id.buttonB_14);
            buttonB_14.setOnClickListener(onClickListener);
        final Button buttonB_15 = (Button)myFragmentView.findViewById(R.id.buttonB_15);
            buttonB_15.setOnClickListener(onClickListener);
        final Button buttonB_16 = (Button)myFragmentView.findViewById(R.id.buttonB_16);
            buttonB_16.setOnClickListener(onClickListener);
        final Button buttonB_17 = (Button)myFragmentView.findViewById(R.id.buttonB_17);
            buttonB_17.setOnClickListener(onClickListener);
        final Button buttonB_18 = (Button)myFragmentView.findViewById(R.id.buttonB_18);
            buttonB_18.setOnClickListener(onClickListener);
        final Button buttonB_19 = (Button)myFragmentView.findViewById(R.id.buttonB_19);
            buttonB_19.setOnClickListener(onClickListener);
        final Button buttonB_20 = (Button)myFragmentView.findViewById(R.id.buttonB_20);
            buttonB_20.setOnClickListener(onClickListener);
        final Button buttonB_21 = (Button)myFragmentView.findViewById(R.id.buttonB_21);
            buttonB_21.setOnClickListener(onClickListener);
        final Button buttonB_22 = (Button)myFragmentView.findViewById(R.id.buttonB_22);
            buttonB_22.setOnClickListener(onClickListener);
        final Button buttonB_23 = (Button)myFragmentView.findViewById(R.id.buttonB_23);
            buttonB_23.setOnClickListener(onClickListener);
        final Button buttonB_24 = (Button)myFragmentView.findViewById(R.id.buttonB_24);
            buttonB_24.setOnClickListener(onClickListener);

        // Check for saved IP address
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String storedIPAddress = SP.getString("ipAddress", " ");
        if (Objects.equals(storedIPAddress, " ")) {
            String msg = "   No saved Server IP address found,\n\nPlease enter the Server IP in the Button-Box settings page";
            Utils.showToast_Long(getActivity(), msg);
            Utils.m("" + msg);
        }
        else
        {
            new connectTask().execute("");
            String msg = "Server IP Found, Connecting";
            Utils.m("" + msg);
        }

        return myFragmentView;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.buttonB_1:
                    // TODO - check for preferences, as user may have changed button commands
                    // String key = getpreferences.... etc etc ;
                    String key = CustomKeyMap.h;
                    //send the key to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(key);
                    }
                    break;
                case R.id.buttonB_2:
                    //DO something
                    break;
                case R.id.buttonB_3:
                    //DO something
                    break;
                case R.id.buttonB_4:
                    //DO something
                    break;
                case R.id.buttonB_5:
                    //DO something
                    break;
                case R.id.buttonB_6:
                    //DO something
                    break;
                case R.id.buttonB_7:
                    //DO something
                    break;
                case R.id.buttonB_8:
                    //DO something
                    break;
                case R.id.buttonB_9:
                    //DO something
                    break;
                case R.id.buttonB_10:
                    //DO something
                    break;
                case R.id.buttonB_11:
                    //DO something
                    break;
                case R.id.buttonB_12:
                    //DO something
                    break;
                case R.id.buttonB_13:
                    //DO something
                    break;
                case R.id.buttonB_14:
                    //DO something
                    break;
                case R.id.buttonB_15:
                    //DO something
                    break;
                case R.id.buttonB_16:
                    //DO something
                    break;
                case R.id.buttonB_17:
                    //DO something
                    break;
                case R.id.buttonB_18:
                    //DO something
                    break;
                case R.id.buttonB_19:
                    //DO something
                    break;
                case R.id.buttonB_20:
                    //DO something
                    break;
                case R.id.buttonB_21:
                    //DO something
                    break;
                case R.id.buttonB_22:
                    //DO something
                    break;
                case R.id.buttonB_23:
                    //DO something
                    break;
                case R.id.buttonB_24:
                    //DO something
                    break;
            }
        }
    };

    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... key) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            super.onProgressUpdate(message);
            if (Objects.equals(Arrays.toString(message), "[handshakeAccepted]")) {
                Utils.m("Result from server = " + Arrays.toString(message));
                Utils.m(" Connection Successful ");
                Utils.showToast_Short(getActivity(), " Connection Successful ");
            }
       }
    }
}