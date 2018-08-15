package com.alexkang.loopboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
	
	private static final String PATH = Environment.getExternalStorageDirectory() + "/LoopBoard";
    private static final int SAMPLE_RATE = 44100;
    private static final int MAX_TRACKS = 16;

    private SampleAdapter mAdapter;
    private ListView mSampleList;

    protected ArrayList<Sample> mSamples;
	private AudioRecord mRecorder;
    protected int mMinBuffer;

    protected long lastKnownTime = System.nanoTime();

    private int numImported;
    private boolean isRecording = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mMinBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        mSamples = new ArrayList<>();

        mSampleList = (ListView) findViewById(R.id.sound_list);
        mAdapter = new SampleAdapter(this, mSamples);
        mSampleList.setAdapter(mAdapter);

        View footer = new View(this);
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 360));
        footer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mSampleList.addFooterView(footer, null, false);

		Button recButton = (Button) findViewById(R.id.rec_button); // Record button.
		recButton.setOnTouchListener(new OnTouchListener() {
			/*
			 * onTouch buttons are used to record sound by holding down the button to
			 * record, and letting go to save.
			 */
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int action = motionEvent.getAction();
				
				if (action == MotionEvent.ACTION_DOWN) {
                    view.setPressed(true);

                    if (((System.nanoTime() - lastKnownTime) / 1e6) < 250) {
                        lastKnownTime = System.nanoTime();
                        return false;
                    }

                    lastKnownTime = System.nanoTime();
                    startRecording(mSamples.size());
				}
				else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    view.setPressed(false);
                    stopRecording();
				}
				
				return true;
			}
		});

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstUse = sharedPref.getBoolean("first_use", true);
        if (firstUse) {
            Toast.makeText(this, "Press and hold the record button to create a sample!", Toast.LENGTH_SHORT).show();
            sharedPref.edit().putBoolean("first_use", false).apply();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete: // Deletes all local sounds on external storage.
				deleteAll();
				return true;
			case R.id.action_stop: // Stops all looped play backs.
                stopAll();
				return true;
			default:
				return true;
		}
	}

    @Override
    public void onResume() {
        super.onResume();

        new File(PATH + "/custom").mkdirs();
        File noMedia = new File(PATH + "/custom", ".nomedia");
        try {
            FileOutputStream output = new FileOutputStream(noMedia);
            output.write(0);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        refreshRecordings();
    }

    @Override
	public void onPause() {
		super.onPause();

        stopAll();
	}

    private void refreshRecordings() {
        mSamples.clear();
        numImported = 0;

        for (File file : new File(PATH + "/custom").listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".wav") || fileName.endsWith(".mp3") || fileName.endsWith(".mp4") || fileName.endsWith(".m4a")) {
                mSamples.add(new Sample(fileName.substring(0, fileName.length() - 4), file, this));
                numImported++;
            }
        }

        for (File file : new File(PATH).listFiles()) {
            String fileName = file.getName();

            if (fileName.endsWith(".pcm")) {
                try {
                    FileInputStream input = new FileInputStream(file);
                    byte[] output = new byte[(int) file.length()];

                    input.read(output);
                    input.close();
                    mSamples.add(new Sample(fileName.substring(0, fileName.length() - 4), output));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }
	
	protected void startRecording(int k) {
        try {
            if (k >= MAX_TRACKS + numImported) {
                Toast.makeText(this, "Cannot create any more sounds", Toast.LENGTH_SHORT).show();
                return;
            } else if (isRecording) {
                return;
            }

            isRecording = true;

            mRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    mMinBuffer
            );

            mRecorder.startRecording();
            new RecordingThread(k).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	protected void stopRecording() {
        try {
            new StopThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    private void nameRecording(final byte[] byteArray, final int index) {
        final String[] sampleName = new String[] {"Sample " + (index - numImported)};

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Name your recording");

        final EditText editText = new EditText(this);
        editText.setText(sampleName[0]);
        editText.selectAll();

        dialog.setView(editText);
        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SaveThread(byteArray, editText.getText().toString(), index).start();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void stopAll() {
        for (Sample sample : mSamples) {
            try {
                sample.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mAdapter.notifyDataSetChanged();
    }
	
	private void deleteAll() {
        try {
            stopAll();

            File[] files = new File(PATH).listFiles();
            for (File file : files) {
                if (!file.getName().equals(".nomedia") && !file.getName().equals("custom")) {
                    file.delete();
                }
            }

            refreshRecordings();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "All recordings deleted", Toast.LENGTH_SHORT).show();
	}

    private class RecordingThread extends Thread {

        private int index;

        public RecordingThread(int k) {
            index = k;
        }

        public void run() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[mMinBuffer];
            int b = 0;

            while (b < 14000) {
                try {
                    b += mRecorder.read(buffer, 0, mMinBuffer);
                } catch(NullPointerException e) {
                    return;
                }
            }

            while (isRecording) {
                mRecorder.read(buffer, 0, mMinBuffer);
                output.write(buffer, 0, mMinBuffer);
            }

            try {
                output.flush();
                final byte[] byteArray = output.toByteArray();

                if (byteArray.length < 22050) {
                    return;
                }

                if (index == mSamples.size()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nameRecording(byteArray, index);
                        }
                    });
                } else {
                    new SaveThread(byteArray, mSamples.get(index).getName(), index).start();
                }

                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class StopThread extends Thread {

        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mRecorder != null) {
                isRecording = false;

                try {
                    mRecorder.stop();
                    mRecorder.release();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                mRecorder = null;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    private class SaveThread extends Thread {

        private byte[] soundByte;
        private String name;
        private int index;

        public SaveThread(byte[] soundByte, String name, int index) {
            this.soundByte = soundByte;
            this.name = name;
            this.index = index;
        }

        public void run() {
            File savedPCM = new File(PATH, name + ".pcm");

            try {
                FileOutputStream output = new FileOutputStream(savedPCM);
                output.write(soundByte);
                output.close();

                if (index == mSamples.size()) {
                    mSamples.add(new Sample(name, soundByte));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                            mSampleList.smoothScrollByOffset(mSampleList.getMaxScrollAmount());
                        }
                    });
                } else {
                    mSamples.get(index).updateSample(soundByte);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
	
}
