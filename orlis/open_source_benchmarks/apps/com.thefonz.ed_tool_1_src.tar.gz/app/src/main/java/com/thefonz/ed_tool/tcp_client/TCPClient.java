package com.thefonz.ed_tool.tcp_client;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.thefonz.ed_tool.MainActivity;
import com.thefonz.ed_tool.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by thefonz on 06/04/15.
 */
public class TCPClient extends Application {

    private String serverMessage;
    public static final int SERVERPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param key text entered by client
     */
    public void sendMessage(String key){
        if (out != null && !out.checkError()) {
            out.println(key);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
//           Get saved IP address from preferences and use that to connect to .
            Context applicationContext = MainActivity.getContextOfApplication();
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            String SERVERIP = SP.getString("ipAddress", "");
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);

            try {

                //send the key to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Client", "C: Sent");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "E: Error", e);
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
