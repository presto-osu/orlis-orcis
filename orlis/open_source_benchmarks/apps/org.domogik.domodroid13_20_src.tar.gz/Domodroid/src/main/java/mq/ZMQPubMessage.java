package mq;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.lang.reflect.Method;

/**
 * Created by mpunie on 13/05/2015.
 */
//TODO add Tracer engine to log message

class ZMQPubMessage extends AsyncTask<String, Void, Integer> {
    private ZMQ.Socket pub = null;
    private final String mytag = this.getClass().getName();

    private static String getHostName() {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "unknown";
        }
    }

    public ZMQPubMessage() {
        com.orhanobut.logger.Logger.init("ZMQPubMessage").methodCount(0);
        try {
            ZMQ.Context context = ZMQ.context(1);
            this.pub = context.socket(ZMQ.PUB);
        } catch (Exception e) {
            Logger.d("error:" + e);
        }
    }

    protected Integer doInBackground(String... params) {
        com.orhanobut.logger.Logger.init("ZMQPubMessage").methodCount(0);
        String url = params[0];
        String cat = params[1];
        try {
            Logger.d("Start sending");
            JSONObject jo = new JSONObject();
            jo.put("text", params[2]);
            jo.put("media", "speech");
            jo.put("identity", "domodroid");
            jo.put("source", "terminal-android." + getHostName());
            String msg = jo.toString();
            Logger.json(msg);
            this.pub.connect(url);
            // we need this timeout to let zeromq connect to the publisher
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String msgId = cat + "." + String.valueOf(System.currentTimeMillis() * 1000) + "." + "0_1";
            if (!this.pub.sendMore(msgId)) {
                Logger.e("Send of msg id not ok: " + msgId);
            }
            if (!this.pub.send(msg)) {
                Logger.e("Send of msg not ok: " + msg);
            }
            Logger.d("End sending");
        } catch (JSONException e) {
            Logger.d("json error:" + e);
        } catch (Exception e) {
            Logger.d("error:" + e);
        }
        this.pub.close();
        return 1;
    }
}
