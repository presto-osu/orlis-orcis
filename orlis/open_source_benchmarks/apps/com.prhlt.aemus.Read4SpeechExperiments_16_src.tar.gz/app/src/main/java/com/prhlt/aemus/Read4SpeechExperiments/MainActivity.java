/*
 *    Read 4 Speech Experiments
 *
 *   Copyright (C) 2015, egranell
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Created on: 01/01/2015
 *      Author: egranell
 *
 *
 */


package com.prhlt.aemus.Read4SpeechExperiments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends ActionBarActivity {
    private static final int BUFFER_SIZE = 256;
    private static final int RESULT_SETTINGS = 0;
    private static String tag = "Read4SpeechExperiments";
    private static CustomViewPager mViewPager;
    private static ArrayList<String> sentences;
    private static ArrayList<String> sentencesID;

    private static int recorderSampleRate = 16000;
    private static int recorderChannels = AudioFormat.CHANNEL_IN_MONO;
    private static int recorderEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private static int recorderMinBufferSize = AudioRecord.getMinBufferSize(recorderSampleRate, recorderChannels, recorderEncoding);
    private static int playerChannels = AudioFormat.CHANNEL_OUT_MONO;
    private static AudioRecord recorder;
    private static AudioTrack player;
    private static AudioManager am;
    private static TextToSpeech tts;

    private static Boolean record = false;
    private static File rootDir;
    private static File linesDir;
    private static Context context;
    AlertDialog.Builder cleanDialog;
    AlertDialog.Builder thanksDialog;
    AlertDialog.Builder sendDialog;
    AlertDialog.Builder instructionsDialog;
    AlertDialog.Builder notSpaceDialog;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private String name = "Combination";
    private String ID;

    protected static float getPercentDone() {
        float percent = 0;
        for (int i = 0; i < sentences.size(); i++) {
            if (new File(rootDir + "/" + sentencesID.get(i) + ".raw").exists()) {
                percent = percent + 1 / (float) sentences.size();
            }
        }
        return (float) (Math.round(percent * Math.pow(10, 3)) / Math.pow(10, 3)) * 100;
    }

    protected static long getAvailableSpaceInKB() {
        return Environment.getExternalStorageDirectory().getFreeSpace() / 1024L;
    }

    public static void startRecording(final int position) {
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Bluetooth", false)) {
            Log.d(tag, "Connecting to the available Bluetooth microphone");
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                    Log.d(tag, "Audio SCO state: " + state);
                    if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                        Log.d(tag, "Bluetooth connected");
                        context.unregisterReceiver(this);
                    }

                }
            }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));

            Log.d(tag, "Starting bluetooth");
            am.startBluetoothSco();
        }

        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[recorderMinBufferSize];
                BufferedOutputStream bos;
                try {
                    bos = new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(rootDir + "/" + sentencesID.get(position - 1) + ".raw")));
                    while (record) {
                        int recorderBufferSize = recorder.read(buffer, 0, buffer.length);
                        if (recorderBufferSize < 0)
                            break;
                        try {
                            bos.write(buffer, 0, recorderBufferSize);
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        streamThread.start();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Si quito la barra no se muestra el menú
        setContentView(R.layout.activity_main);
        context = getApplicationContext();


        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        // Añadir en la configuración la posibilidad de habilitar el sintetizador y elegir el idioma
        tts.setLanguage(new Locale("es", "ES"));

        sendDialog = new AlertDialog.Builder(this);
        sendDialog.setTitle(R.string.sendTitle);
        sendDialog.setIcon(R.drawable.ic_launcher);
        sendDialog.setMessage(R.string.sendMessage);
        sendDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        sendDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendData(true);
            }
        });

        cleanDialog = new AlertDialog.Builder(this);
        cleanDialog.setTitle(R.string.cleanTitle);
        cleanDialog.setIcon(R.drawable.ic_launcher);
        cleanDialog.setMessage(R.string.cleanMessage);
        cleanDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("name", "");
                editor.commit();

                if (rootDir.exists()) {
                    for (File f : rootDir.listFiles()) {
                        if (!f.delete()) {
                            Log.e(tag, "Cannot delete file: " + f);
                        }
                    }
                    if (!rootDir.delete()) {
                        Log.e(tag, "Cannot delete file: " + rootDir);
                    }
                }
                thanksDialog.show();
            }
        });
        cleanDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        thanksDialog = new AlertDialog.Builder(this);
        thanksDialog.setTitle(R.string.thanksTitle);
        thanksDialog.setIcon(R.drawable.ic_launcher);
        thanksDialog.setMessage(R.string.thanksMessage);
        thanksDialog.setNegativeButton(R.string.newRecord, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                init();
                mSectionsPagerAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(0);
                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, 0)).updateContent();
                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, 1)).updateContent();
            }
        });

        thanksDialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        thanksDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                }
                return true;
            }
        });

        notSpaceDialog = new AlertDialog.Builder(this);
        notSpaceDialog.setTitle(R.string.notSpaceTitle);
        notSpaceDialog.setIcon(R.drawable.ic_launcher);
        notSpaceDialog.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        notSpaceDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                }
                return true;
            }
        });

        init();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        SharedPreferences settings = getSharedPreferences("Preferences", 0);
        boolean show = settings.getBoolean("show", true);
        showInstructions(show);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                cleanRecords();
                return true;
            case R.id.send:
                sendData(false);
                return true;
            case R.id.settings:
                Intent i = new Intent(this, settings.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            case R.id.instructions:
                showInstructions(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void init() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        name = settings.getString("name", "user");
        ID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        rootDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Read4SpeechExperiments/");

        if (!rootDir.exists()) {
            if (!rootDir.mkdirs())
                Log.e(tag, "Cannot create directory: " + rootDir);
        }

        linesDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Read4SpeechExperiments/Lines");

        if (!linesDir.exists()) {
            if (!linesDir.mkdirs())
                Log.e(tag, "Cannot create directory: " + linesDir);
        }

        File transcriptions = new File(String.format("%s/%s_%s_transcriptions.txt", rootDir, name, ID));

        loadLines(settings.getString("mandatoryFile", ""), settings.getString("optionalFile", ""), Integer.valueOf(settings.getString("OptionalFileNumber", "0")),transcriptions);

        if (!transcriptions.exists())
            if (!writeToFile(sentencesID, sentences))
                Log.e(tag, String.format("Cannot create the file of transcription: %s", transcriptions.getAbsoluteFile()));

        //Check if there is enough space on the SD (about 300KB per phrase)
        Long spaceAvailable = getAvailableSpaceInKB();
        Long spaceNecessary = (long) sentences.size() * 200;
        if (spaceAvailable < spaceNecessary) {
            Log.e(tag, String.format("%s %s %s", getResources().getString(R.string.spaceNecessary1), String.valueOf(spaceNecessary / 1024 + 1), getResources().getString(R.string.spaceNecessary2)));
            notSpaceDialog.setMessage(String.format("%s %s %s", getResources().getString(R.string.spaceNecessary1), String.valueOf(spaceNecessary / 1024 + 1), getResources().getString(R.string.spaceNecessary2)));
            notSpaceDialog.show();
        }


    }

    protected void loadLines(String mandatoryFile, String optionalFile, int optionalSentences, File file) {
        sentences = new ArrayList<String>();
        sentencesID = new ArrayList<String>();
        //File file = new File(audioDir + "/" + name + "_transcriptions_" + ID + ".txt");
        //Log.i(tag,"File length: " + file.length());
        if (!file.exists() || file.length() == 0) {
            try {
                BufferedReader br;
                Log.e(tag,"Loading "+file+" file.");
                Log.e(tag,"Loading "+mandatoryFile+" file.");
                if (!mandatoryFile.matches("/") && new File(mandatoryFile).exists()){ // Text list
                    String ext = mandatoryFile.split("\\.")[mandatoryFile.split("\\.").length-1];
                    Log.e(tag,"Loading "+ext+" file.");
                 if(ext.equals("zip")) {
                     unzip(new ZipInputStream(new BufferedInputStream(new FileInputStream(new File(mandatoryFile)))), linesDir);
                     br = new BufferedReader(new FileReader(new File(linesDir + "/lines.txt")));
                 }else
                     br = new BufferedReader(new FileReader(mandatoryFile));
                }else{ // Example
                    Toast.makeText(getApplicationContext(), R.string.senteceFileNeeded, Toast.LENGTH_LONG).show();
                    unzip(new ZipInputStream(getResources().openRawResource(R.raw.test)),linesDir);
                    br = new BufferedReader(new FileReader(new File(linesDir+"/lines.txt")));
                    //br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.test)));
                }

                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sentencesID.add(String.format("%s_%s_%s",name,ID,line.split("\t")[0]));
                        if(line.split("\t").length>1)
                            sentences.add(line.substring(line.indexOf("\t")));
                        else
                            sentences.add("");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    br = new BufferedReader(new FileReader(optionalFile));
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

                ArrayList<Integer> numbers = new ArrayList<Integer>();
                while (numbers.size() < optionalSentences) {
                    int r = (int) Math.floor(1 + Math.random() * 100);
                    if (!numbers.contains(r)) {
                        numbers.add(r);
                    }
                }
                try {
                    String line;
                    int l = 0;
                    while ((line = br.readLine()) != null) {
                        l++;
                        if (numbers.contains(l)) {
                            sentences.add(line);
                            sentencesID.add(String.format("%s_test_%s_%s", name, String.format("%02d", l), Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID)));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                InputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sentencesID.add(line.split(" ")[0]);
                        sentences.add(line.substring(line.indexOf(" ")));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private boolean writeToFile(ArrayList<String> fileName, ArrayList<String> line) {
        try {
            File file = new File(String.format("%s/%s_%s_transcriptions.txt", rootDir, name, ID));
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            // if file doesn't exists, then create it
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(tag, "Cannot create file: " + file);
                    return false;
                }
            }
            for (int i = 0; i < line.size(); i++) {
                ps.println(fileName.get(i) + " " + line.get(i));
            }
            ps.close();
            return true;

        } catch (IOException e) {
            Log.e(tag, "File write failed: " + e.toString());
            return false;
        }
    }

    @SuppressLint({"NewApi", "InflateParams"})
    void showInstructions(Boolean show) {
        SharedPreferences settings = getSharedPreferences("Preferences", 0);
        View checkboxView = getLayoutInflater().inflate(R.layout.checkbox, null);
        CheckBox checkBox = (CheckBox) checkboxView.findViewById(R.id.checkbox);
        checkBox.setChecked(settings.getBoolean("show", true));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences("Preferences", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("show", isChecked);
                editor.apply();
            }
        });


        try {
            instructionsDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        } catch (NoSuchMethodError e) {
            instructionsDialog = new AlertDialog.Builder(this);
        }

        instructionsDialog.setTitle(R.string.intructionTitle);
        instructionsDialog.setIcon(R.drawable.ic_launcher);
        instructionsDialog.setMessage(Html.fromHtml(getString(R.string.instructions)));
        instructionsDialog.setView(checkboxView);
        if (show) {
            instructionsDialog.show();
        }
    }

    @SuppressLint("ShowToast")
    void cleanRecords() {
        if (getPercentDone() > 0) {
            cleanDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.noRecordToDelete, Toast.LENGTH_LONG).show();
        }
    }

    public static void unzip(ZipInputStream zipFile, File targetDirectory) throws IOException {
        Log.i(tag, "Unzipping: "+zipFile+" in "+targetDirectory);
        ZipInputStream zis = zipFile;
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    public static void zip(ArrayList<String> files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    @SuppressLint("ShowToast")
    void sendData(Boolean confirmation) {
        float done = getPercentDone();
        if (done == 0) {
            Toast.makeText(getApplicationContext(), R.string.noRecords, Toast.LENGTH_LONG).show();
        } else {
            if (done < 100 && !confirmation) {
                sendDialog.show();
            } else {
                Intent itSend = new Intent();
                //itSend.setType("*/*");
                itSend.setType("plain/text");
                itSend.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);

                //itSend.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"speech4experiments@gmail.com"});
                itSend.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Read4SpeechExperiments] Acquisition from " + name +
                        " with " + android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE);
                itSend.putExtra(android.content.Intent.EXTRA_TEXT, "These are the records!\n\n" + name);

                ArrayList<Uri> uris = new ArrayList<Uri>();
                //uris.add(Uri.parse("mailto:speech4experiments@gmail.com"));
                ArrayList<String> files = new ArrayList<String>();
                if (new File(rootDir + "/" + name +"_"+ ID+ "_transcriptions"  + ".txt").exists()) {
                    files.add(rootDir + "/" + name +"_"+ ID+ "_transcriptions" + ".txt");
                    //uris.add(Uri.fromFile(new File(rootDir + "/" + name + "_transcripts_" + ID + ".txt")));
                }
                for (int i = 0; i < sentences.size(); i++) {
                    if (new File(rootDir + "/" + sentencesID.get(i) + ".raw").exists()) {
                        files.add(rootDir + "/" + sentencesID.get(i) + ".raw");
                        //  uris.add(Uri.fromFile(new File(rootDir + "/" + sentencesID.get(i) + ".raw")));
                    }
                }
                try {
                    zip(files, rootDir + "/" + name + "_" + ID + ".zip");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uris.add(Uri.fromFile(new File(rootDir + "/" + name + "_" + ID + ".zip")));
                itSend.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(itSend);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                if (settings.getString("name", null) == null || settings.getString("name", "").matches("")) {
                    Toast.makeText(getApplicationContext(), R.string.setSpeakerID, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(this, settings.class);
                    startActivityForResult(i, RESULT_SETTINGS);
                } else {
                    name = settings.getString("name", null);
                }

                if (!new File(settings.getString("mandatoryFile", "NULL")).exists())
                    Toast.makeText(getApplicationContext(), R.string.senteceFileNeeded, Toast.LENGTH_LONG).show();

                /*File file = new File(String.format("%s/%s_transcripts_%s.txt", rootDir, name, ID));
                if (file.exists()) {
                    if (!file.delete()) {
                        Log.e(tag, "Cannot delete file: " + file);
                    }
                }*/

                init();

                mSectionsPagerAdapter.notifyDataSetChanged();
                int index = mViewPager.getCurrentItem();
                if (index > 0)
                    ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index - 1)).updateContent();
                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).updateContent();
                if (index < sentences.size() - 1)
                    ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index + 1)).updateContent();
                break;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SENTENCE_NUMBER = "sentence_number";
        private static final String ARG_SENTENCE = "sentence";
        private static final String ARG_RECORDING = "recording";
        private static final String ARG_HANDFREE_RECORDING = "handFreeRecording";
        private static final String ARG_PLAYING = "playing";
        private static Handler handler = null;
        TextView sentenceNumber;
        TextView progress;
        TextView sentence;
        ImageView imageSentence;
        ImageButton recButton;
        ImageButton playButton;
        ProgressBar progressBar;

        public PlaceholderFragment() {
            handler = new Handler() {

                // Create handleMessage function
                public void handleMessage(Message msg) {

                    int index = mViewPager.getCurrentItem();
                    Log.i(tag, index + " " + ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).getArguments().getBoolean(ARG_HANDFREE_RECORDING) + " " + msg.getData().getString("message") + " " + msg.getData().getInt("sentence") + " " + msg.getData().getInt("move"));
                    String aResponse = null;
                    int s = 0;
                    if (msg.getData().containsKey("message"))
                        aResponse = msg.getData().getString("message");
                    if (msg.getData().containsKey("sentence")) {
                        s = msg.getData().getInt("sentence");
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager,index)).getArguments().putInt(ARG_SENTENCE_NUMBER, s + 1);
                    }
                    if (msg.getData().containsKey("move")) {
                        s = msg.getData().getInt("move");
                    }

                    if (aResponse != null) {
                        if (aResponse.matches("start recording") || aResponse.matches("stop recording")) {
                            ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).getArguments().putBoolean(ARG_HANDFREE_RECORDING, true);
                            recButton.performClick();
                        } else if (aResponse.matches("next sentence") || aResponse.matches("finish")) {
                            if (aResponse.matches("finish")) {
                                //    mViewPager.setCurrentItem(m);
                                //    mViewPager.getAdapter().startUpdate(mViewPager);
                                //     mViewPager.getAdapter().finishUpdate(mViewPager);
                                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).getArguments().putBoolean(ARG_HANDFREE_RECORDING, false);
                            }
                        }/* else if (aResponse.matches("init")) {
                                mViewPager.setCurrentItem(m);
                                mViewPager.getAdapter().startUpdate(mViewPager);
                                mViewPager.getAdapter().finishUpdate(mViewPager);
                        }*/


                    }



                    /*if (index > 1 && (mViewPager.getAdapter().instantiateItem(mViewPager, index - 1)) != null) {
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index - 1)).getArguments().putInt(ARG_SENTENCE_NUMBER, s);
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index - 1)).updateContent();
                    }*/
                    if ((mViewPager.getAdapter().instantiateItem(mViewPager, index)) != null) {
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).getArguments().putInt(ARG_SENTENCE_NUMBER, s + 1);
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).updateContent();
                    }
                    /*if (index + 1 < sentences.size() && (mViewPager.getAdapter().instantiateItem(mViewPager, index + 1)) != null) {
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index + 1)).getArguments().putInt(ARG_SENTENCE_NUMBER, s + 2);
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index + 1)).updateContent();
                    }*/

                    /// ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).updateContent();
                }
            };
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SENTENCE_NUMBER, sectionNumber);
            args.putString(ARG_SENTENCE, sentences.get(sectionNumber - 1));
            args.putBoolean(ARG_RECORDING, false);
            args.putBoolean(ARG_HANDFREE_RECORDING, false);
            args.putBoolean(ARG_PLAYING, false);
            fragment.setArguments(args);
            return fragment;
        }

        @SuppressLint({"NewApi", "InlinedApi"})
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.fragmentLayout);
            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.v(null, "TOUCH EVENT");
                    return false;
                }
            });

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            sentenceNumber = (TextView) rootView.findViewById(R.id.sentenceNumber);
            sentence = (TextView) rootView.findViewById(R.id.sentence);
            imageSentence = (ImageView) rootView.findViewById(R.id.imageSentence);
            recButton = (ImageButton) rootView.findViewById(R.id.rec_button);
            playButton = (ImageButton) rootView.findViewById(R.id.play_button);
            progress = (TextView) rootView.findViewById(R.id.progress);
            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
            progressBar.setMax(100);
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));
            progressBar.setProgress((int) getPercentDone());
            progress.setText(String.format("%s%%", String.format("%.2f", getPercentDone())));
            sentenceNumber.setText(String.format("%s %s/%d", getResources().getString(R.string.sentence), Integer.toString(getArguments().getInt(ARG_SENTENCE_NUMBER)), sentences.size()));
            sentence.setText(sentences.get(getArguments().getInt(ARG_SENTENCE_NUMBER) - 1));
            sentence.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_HANDFREE_RECORDING, false);
                    if (tts.isSpeaking()) tts.stop();

                    if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("handsFree", false)) {
                        if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_RECORDING)) {
                            recButton.performClick();
                        }
                    }
                    if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_PLAYING)) {
                        playButton.performClick();
                    }
                }
            });

            if (settings.getBoolean("images", false)) {
                String mDrawableName = linesDir + "/" + (sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1)).split("_")[2]+".png";
                Log.i(tag,mDrawableName);
                if(new File(mDrawableName).exists())
                    imageSentence.setImageDrawable(Drawable.createFromPath(mDrawableName));
            }

            final AlertDialog.Builder recDialog = new AlertDialog.Builder(getActivity());
            recDialog.setTitle(R.string.recordTitle);
            recDialog.setIcon(R.drawable.ic_launcher);
            recDialog.setMessage(R.string.recordMessage);
            recDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );

            recDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mViewPager.setPagingEnabled(false);
                    record = true;
                    playButton.setEnabled(false);
                    ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_RECORDING, true);
                    sentence.setBackgroundResource(R.color.recording);
                    recButton.setBackgroundResource(R.drawable.record_stop);
                    recorder = new AudioRecord(AudioSource.MIC, recorderSampleRate, recorderChannels, recorderEncoding, recorderMinBufferSize * 10);
                    recorder.startRecording();
                    startRecording(((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER));
                }
            });

            recButton.setOnClickListener(new OnClickListener() {
                @SuppressLint("ResourceAsColor")
                public void onClick(View arg0) {
                    if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_RECORDING)) {
                        if (recorder != null) {
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                recorder.stop();
                                recorder.release();
                                am.stopBluetoothSco();
                            }
                        }
                        if (new File(String.format("%s/%s.raw", rootDir, sentencesID.get(((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER) - 1))).exists()) {
                            sentence.setBackgroundResource(R.color.recorded);
                            playButton.setEnabled(true);
                        } else {
                            sentence.setBackgroundResource(R.color.notRecorded);
                            playButton.setEnabled(false);
                        }
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_RECORDING, false);
                        //((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_HANDFREE_RECORDING, false);
                        recButton.setBackgroundResource(R.drawable.record);
                        record = false;
                        mViewPager.setPagingEnabled(true);

                        int index = mViewPager.getCurrentItem();
                        if (index > 1)
                            ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index - 1)).updateContent();
                        if ((mViewPager.getAdapter().instantiateItem(mViewPager, index)) != null)
                            ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index)).updateContent();
                        if (index < sentences.size() - 1)
                            ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, index + 1)).updateContent();

                    } else {
                        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("name", "").matches("")) {
                            Toast.makeText(context, R.string.setSpeakerID, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(context, settings.class);
                            startActivityForResult(i, RESULT_SETTINGS);
                        } else {
                            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("handsFree", false)
                                    && !((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_HANDFREE_RECORDING, true);
                                Log.i(tag, "handsFreeModerecord " + ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING) + " " + ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER));
                                handsFreeMode();
                            } else {
                                Log.i(tag, "number " + (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER) - 1));
                                if (new File(String.format("%s/%s.raw", rootDir, sentencesID.get(((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER) - 1))).exists()
                                        && !((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                                    recDialog.show();
                                } else {
                                    Log.i(tag, "recording");
                                    record = true;
                                    mViewPager.setPagingEnabled(false);
                                    playButton.setEnabled(false);
                                    ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_RECORDING, true);
                                    sentence.setBackgroundResource(R.color.recording);
                                    recButton.setBackgroundResource(R.drawable.record_stop);
                                    recorder = new AudioRecord(AudioSource.MIC, recorderSampleRate, recorderChannels, recorderEncoding, recorderMinBufferSize * 10);
                                    recorder.startRecording();
                                    startRecording(((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER));
                                }
                            }
                        }
                    }
                }
            });

            playButton.setOnClickListener(new OnClickListener() {
                @SuppressWarnings("resource")
                @SuppressLint("ResourceAsColor")
                public void onClick(View arg0) {
                    File file = new File(String.format("%s/%s.raw", rootDir, sentencesID.get(((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getInt(ARG_SENTENCE_NUMBER) - 1)));
                    int bufferSizeInBytes = (int) file.length();
                    if (!((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_PLAYING) && bufferSizeInBytes > 0) {
                        mViewPager.setPagingEnabled(false);
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_PLAYING, true);
                        playButton.setBackgroundResource(R.drawable.play_stop);
                        recButton.setEnabled(false);

                        byte[] audioData = new byte[bufferSizeInBytes];

                        player = new AudioTrack(AudioManager.STREAM_MUSIC, recorderSampleRate, playerChannels, recorderEncoding, bufferSizeInBytes, AudioTrack.MODE_STREAM);
                        player.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                            @Override
                            public void onPeriodicNotification(AudioTrack track) {
                            }

                            @Override
                            public void onMarkerReached(AudioTrack track) {
                                Log.e(tag, "Audio track end of file reached...");
                                mViewPager.setPagingEnabled(true);
                                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_PLAYING, false);
                                playButton.setBackgroundResource(R.drawable.play);
                                if (player != null) {
                                    player.release();
                                    player = null;
                                }
                                recButton.setEnabled(true);
                            }
                        });

                        try {
                            InputStream inputStream = new FileInputStream(file);
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

                            int i = 0;
                            while (dataInputStream.available() > 0) {
                                audioData[i] = dataInputStream.readByte();
                                i++;
                            }
                            dataInputStream.close();

                            player.write(audioData, 0, bufferSizeInBytes);
                            player.setNotificationMarkerPosition(bufferSizeInBytes / 2);
                            player.play();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mViewPager.setPagingEnabled(true);
                        ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putBoolean(ARG_PLAYING, false);
                        playButton.setBackgroundResource(R.drawable.play);
                        recButton.setEnabled(true);
                        if (player != null) {
                            player.release();
                            player = null;
                        }
                    }
                }
            });

            //rootDir + "/" + sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1) + ".raw"
            if (new File(String.format("%s/%s.raw", rootDir, sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1))).exists()) {
                sentence.setBackgroundResource(R.color.recorded);
                playButton.setEnabled(true);
            } else {
                sentence.setBackgroundResource(R.color.notRecorded);
                playButton.setEnabled(false);
            }
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            /*if (isVisibleToUser) {
                // load data here
            }else{
                // fragment is no longer visible
            }*/
        }

        public void updateContent() {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
  //          Log.i(tag, "UPDATE CONTENT " + this.getArguments().getBoolean(ARG_HANDFREE_RECORDING) + " " + this.getArguments().getBoolean(ARG_RECORDING));
//            Log.i(tag, rootDir + "/" + sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1) + ".raw");
            if (this.getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                sentence.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                if (this.getArguments().getBoolean(ARG_RECORDING)) {
    //                Log.i(tag, "UPDATE CONTENT ROJOOO");
                    sentence.setBackgroundColor(getResources().getColor(R.color.recording));
                }
                playButton.setEnabled(false);
                recButton.setEnabled(false);
            } else if (new File(rootDir + "/" + sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1) + ".raw").exists()) {
                sentence.setBackgroundResource(R.color.recorded);
                playButton.setEnabled(true);
                recButton.setEnabled(true);
            } else {
                sentence.setBackgroundResource(R.color.notRecorded);
                playButton.setEnabled(false);
                recButton.setEnabled(true);
            }
            sentence.setText(sentences.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1));
            imageSentence.setImageDrawable(null);
            if (settings.getBoolean("images", false)) {
                String mDrawableName = linesDir + "/" + (sentencesID.get(this.getArguments().getInt(ARG_SENTENCE_NUMBER) - 1)).split("_")[2]+".png";
                if(new File(mDrawableName).exists())
                    imageSentence.setImageDrawable(Drawable.createFromPath(mDrawableName));
            }
            progressBar.setProgress((int) getPercentDone());
            progress.setText(String.format("%s%%", String.format("%.2f", getPercentDone())));
            sentenceNumber.setText(String.format("%s %s/%d", getResources().getString(R.string.sentence), Integer.toString(this.getArguments().getInt(ARG_SENTENCE_NUMBER)), sentences.size()));
        }

        public void handsFreeMode() {
            playButton.setEnabled(false);
            mViewPager.setPagingEnabled(false);

            //sentence.setBackgroundColor(getResources().getColor(R.color.black_overlay));
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    int init = mViewPager.getCurrentItem();
                    int end = 0;


                    for (int i = init; i < sentences.size(); i++) {
                        Message msgObj = new Message();
                        Bundle b = new Bundle();
                        b.putString("message", "init");
                        b.putInt("move", i);
                        msgObj.setData(b);
                        handler.sendMessage(msgObj);

                        if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                            if (i + 1 < sentences.size())
                                ((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putInt(ARG_SENTENCE_NUMBER, i + 1);
                            tts.speak(sentences.get(i), TextToSpeech.QUEUE_FLUSH, null);
                            while (tts.isSpeaking()) {
                                //  Log.i(tag,"speaking "+i+" "+((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING));
                            }
                            Log.i(tag, "End tts");
                        }
                        if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            if (((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                                msgObj = new Message();
                                b = new Bundle();
                                b.putString("message", "start recording");
                                b.putInt("sentence", i);
                                msgObj.setData(b);
                                handler.sendMessage(msgObj);

                                Thread.sleep(10000);

                                msgObj = new Message();
                                b = new Bundle();
                                b.putString("message", "stop recording");
                                // b.putInt("move", i + 1);
                                msgObj.setData(b);
                                handler.sendMessage(msgObj);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        end = i + 1;
                        if (!((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().getBoolean(ARG_HANDFREE_RECORDING)) {
                            break;
                        }
                    }
                    Message msgObj = new Message();
                    Bundle b = new Bundle();
                    b.putString("message", "finish");
                    //b.putInt("sentence", init-1);
                    b.putInt("move", init);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                    mViewPager.setPagingEnabled(true);
                    //((PlaceholderFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).getArguments().putInt(ARG_SENTENCE_NUMBER, init);
                }
            }).start();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(tag, "New Fragment: " + (position + 1));
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return sentences.size();
        }
    }
}
