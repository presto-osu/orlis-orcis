package mq;

/**
 * Created by mpunie on 12/05/2015.
 */

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import misc.tracerengine;

public class ZMQService extends Service {
    private final String mytag = this.getClass().getName();

    private ZMQTask task;
    private tracerengine Tracer = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(this);
        Tracer = tracerengine.getInstance(SP_params, this);
        Tracer.d(this.getClass().getSimpleName(), "Service oncreate ...");
    }

    @Override
    public void onDestroy() {
        task.cancel(true);
        super.onDestroy();
        Toast.makeText(this, R.string.service_destroyed, Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Tracer.d(this.getClass().getSimpleName(), "Service onStart ...");
        super.onStartCommand(intent, flags, startId);
        doWork();
        return START_STICKY;
    }

    private void doWork() {
        Tracer.d(this.getClass().getSimpleName(), "Service Dowork Started ...");
        try {
            //task = new ZMQTask();
            task = new ZMQTask(this);
            task.execute(this);
        } catch (Exception e) {
            Tracer.e(this.getClass().getSimpleName(), e.toString());
        }
        Tracer.d(this.getClass().getSimpleName(), "Service Dowork finished ...");
    }

    public void handleMessage(String msgId, String json) {
        JSONObject jsonMessage = null;
        try {
            jsonMessage = new JSONObject(json);
        } catch (JSONException e) {
            Tracer.d(mytag, "Unable to parse message JSON" + e.toString());
        }
        if (jsonMessage == null) {
            Tracer.e(mytag, "msg was not properly parsed");
            return; // return early to bail out of processing
        }
        ZMQMessage msg = new ZMQMessage();
        msg.setId(msgId);
        msg.setMessage(json);

        Intent i = new Intent("domogik.domodroid.MESSAGE_RECV");
        i.putExtra("message", msg);
        sendBroadcast(i);
    }
}
