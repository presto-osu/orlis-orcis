package com.sevag.pitcha;

import android.app.Activity;
import android.graphics.Color;
import android.os.*;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;
import com.sevag.pitcha.recording.AudioRecorder;
import com.sevag.pitcha.uihelper.UIHelper;

public class MainActivity extends Activity implements UIHelper {

    private TextView noteTextView;
    private Thread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteTextView = (TextView) findViewById(R.id.noteOutputTextView);
    }

    @Override
    public void display(final String note, final double err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!note.isEmpty()) {
                    if ((99.5 < err) && (err < 100.5)) {
                        noteTextView.setText(note);
                        noteTextView.setTextColor(Color.GREEN);
                    } else {
                        noteTextView.setText(note);
                        noteTextView.setTextColor(Color.RED);
                    }
                } else {
                    noteTextView.setText("");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        endHook();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startHook();
        super.onResume();
    }

    private void endHook() {
        AudioRecorder.deinit();
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private void startHook() {
        AudioRecorder.init(this);
        launchPitcha();
    }

    private void launchPitcha() {
        audioThread = new Thread(new Runnable() {
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                AudioRecorder.run();
            }
        });

        audioThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
