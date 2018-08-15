package de.phoenixstudios.pc_dimmer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class Main extends FragmentActivity implements Setup.CallbackToMain, Scenes.CallbackToMain,
        Devicecontrol.CallbackToMain, Controlpanel.CallbackToMain, Channeloverview.CallbackToMain,
        Stageview.CallbackToMain, nodecontrol.CallbackToMain, stagesetup.CallbackToMain {
    public class PCD_Device {
        public String ID;
        public String Name;
    }
    public class PCD_Group {
        public String ID;
        public String Name;
    }
    public class PCD_Scene {
        public String ID;
        public String Name;
    }
    public class PCD_ControlpanelButton {
        public String ID;
        public String Name;
        public String Type;
        public int R;
        public int G;
        public int B;
        public int X;
        public int Y;
    }
    public class PCD_Node {
        public String ID;
        public String Name;
        public int X;
        public int Y;
        public int R;
        public int G;
        public int B;
        public int A;
        public int W;
        public int Dimmer;
        public boolean UseRGB;
        public boolean UseA;
        public boolean UseW;
        public boolean UseDimmer;
    }
    public class PCD_Nodeset {
        public String ID;
        public String Name;
        public int stretching;
        public int contrast;
        public int fadetime;
        public boolean ChangeRGB;
        public boolean ChangeA;
        public boolean ChangeW;
        public boolean ChangeDimmer;
        public PCD_Node Nodes[];
    }
    public class PCD {
        PCD_Device Devices[];
        PCD_Group Groups[];
        PCD_ControlpanelButton ControlpanelButtons[][];
        PCD_Scene Scenes[][];
        PCD_Nodeset Nodesets[];
    }
    public static PCD mPCD=null;

    static String DeviceNames[];
    static String GroupNames[];
    static String NodesetNames[];
    static String NodesNames[];
    public static int CurrentNodeset;
    public static int CurrentNode;
    public static String CurrentDeviceOrGroupID;

    public static class CurrentSetupDevice {
        public static String ID;
        public static int Startaddress;
        public static int color;
        public static int ChanCount;
    }
    public static boolean RefreshAddressEdit;

    boolean firstcall_presetbox=true;

    List<PCD_Scene> scenelist_grouplist;
    List<PCD_Scene> scenelist_childlist;
    Map<PCD_Scene, List<PCD_Scene>> scenelist_collection;
    ExpandableListView scenelistview;

    public static int Channelvalues[] = new int[8192];
    String CurrentPresetName="";
    String AvailablePresetNames[];
    static public int GlobalFadetime=150;
    String LastPresetName="";

    TabHost devicecontrol_tabHost;
    TabHost nodecontrol_tabHost;

    static boolean NetworkThreadAlive=false;
    static String NetworkIPAddress="192.168.0.1";
    static int NetworkPort=10160;
    static boolean NetworkConnectionOK=false;
    static String NetworkErrorMsg="";
    static String NetworkCommandString="";
    static String NetworkCommandStringQueue="";
    static String NetworkCommandReceivedString="";
    static int NetworkCommandStatus=0;
    static int DownloadStageview=0;
    static Bitmap stageviewdownload=null;
    public static boolean fragment_setup_isvisible=false;

    // Handler for TimerRunnable
    private Handler handler;

    CollectionPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // disable the screen-turn-off after some time
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCollectionPagerAdapter);
    }

    @Override
    protected void onStart(){
        super.onStart();
        handler = new Handler();
        handler.postDelayed(myTimerRunnable, 100);
    }

    @Override
    protected void onStop(){
        super.onStop();
        NetworkThreadAlive=false;
    }

    //Fires after the OnStop() state
    @Override
    protected void onDestroy() {
        NetworkThreadAlive=false;
        super.onDestroy();

        // allow the screen to turn off again
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

/*
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
*/
    }

    public void SetupCallbackToMain(int Cmd){
        switch (Cmd) {
            case R.layout.fragment_setup:
                firstcall_presetbox=true;
                LoadSettings();
                FindPresets();

                if (!CurrentPresetName.equals(LastPresetName)) {
                    CurrentPresetName=LastPresetName;
                    LoadPreset();
                }
                break;
            case R.id.connectBtn:
                // Connect
                NetworkIPAddress=((EditText) findViewById(R.id.ipAddressEdit)).getText().toString();
                NetworkPort=Integer.parseInt(((EditText) findViewById(R.id.portEdit)).getText().toString());
                GlobalFadetime=Integer.parseInt(((EditText) findViewById(R.id.fadetimeEdit)).getText().toString());

                // Netzwerkthread erstellen
                NetworkThreadAlive=true;
                new NetworkThread().execute();

                break;
            case R.id.disconnectBtn:
                // Disconnect
                NetworkThreadAlive=false;
                break;
            case R.id.syncBtn:
                // Synchronizing Data with PC_DIMMER
                if (NetworkConnectionOK) {
                    SynchronizeData();
                    ControlpanelCallbackToMain(R.layout.fragment_controlpanel);
                }
                break;
            case R.id.loadpresetbtn:
                CurrentPresetName = AvailablePresetNames[((Spinner) findViewById(R.id.presetbox)).getSelectedItemPosition()];
                LoadPreset();

                // Refresh controlpanel-fragment
                ControlpanelCallbackToMain(R.layout.fragment_controlpanel);
                break;
            case R.id.savepresetbtn:
                SavePreset();
                break;
            case R.id.deletepresetbtn:
                DeletePreset();
                break;
            case R.id.savesettingsbtn:
                SaveSettings();
                break;
            case R.id.fadetimeEdit:
                GlobalFadetime=Integer.parseInt(((EditText) findViewById(R.id.fadetimeEdit)).getText().toString());
                break;
        }
    }

    public void ScenesCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_scene:
                // Create Scenelists
                create_scenelist_grouplist();

                scenelist_collection = new LinkedHashMap<>();
                if (mPCD!=null) {
					if (mPCD.Scenes!=null) {
						for (PCD_Scene scene : scenelist_grouplist) {
							load_scene_child(mPCD.Scenes[Integer.parseInt(scene.ID)-1]);
							scenelist_collection.put(scene, scenelist_childlist);
						}
					}
                }
                scenelistview = (ExpandableListView) findViewById(R.id.scenes_listview);
                if (scenelistview!=null) {
                    final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(this, scenelist_grouplist, scenelist_collection);
                    scenelistview.setAdapter(expListAdapter);
                }


                //set_sceneGroupIndicatorToRight();
/*
                scenelistview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        final PCD_Scene selectedscene = (PCD_Scene) expListAdapter.getChild(groupPosition, childPosition);
                        start_scene(selectedscene.ID);
                        Toast.makeText(getBaseContext(), "Szene gestartet", Toast.LENGTH_LONG).show();

                        return true;
                    }
                });
*/
                break;
        }
    }

    public void DevicecontrolCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_devicecontrol:
                // Prepare Tabs and set Text
                devicecontrol_tabHost = (TabHost) findViewById(R.id.devicecontrol_tabHost);
                devicecontrol_tabHost.setup();

                TabHost.TabSpec devicecontrol_spec1 = devicecontrol_tabHost.newTabSpec("devicecontrol_tab1");
                devicecontrol_spec1.setContent(R.id.devicecontrol_tab1);
                devicecontrol_spec1.setIndicator(getString(R.string.str_dimmer));

                TabHost.TabSpec devicecontrol_spec2 = devicecontrol_tabHost.newTabSpec("devicecontrol_tab2");
                devicecontrol_spec2.setContent(R.id.devicecontrol_tab2);
                devicecontrol_spec2.setIndicator(getString(R.string.str_farbe));

                TabHost.TabSpec devicecontrol_spec3 = devicecontrol_tabHost.newTabSpec("devicecontrol_tab3");
                devicecontrol_spec3.setContent(R.id.devicecontrol_tab3);
                devicecontrol_spec3.setIndicator(getString(R.string.str_xy));

                TabHost.TabSpec devicecontrol_spec4 = devicecontrol_tabHost.newTabSpec("devicecontrol_tab4");
                devicecontrol_spec4.setContent(R.id.devicecontrol_tab4);
                devicecontrol_spec4.setIndicator(getString(R.string.str_more));

                devicecontrol_tabHost.addTab(devicecontrol_spec1);
                devicecontrol_tabHost.addTab(devicecontrol_spec2);
                devicecontrol_tabHost.addTab(devicecontrol_spec3);
                devicecontrol_tabHost.addTab(devicecontrol_spec4);

                // Fill Device- and Grouplists
                if (mPCD!=null) {
                    if (mPCD.Devices!=null) {
                        Spinner devicelistbox = (Spinner) findViewById(R.id.devicelistbox);
                        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(this, R.layout.devicelist_child_item, DeviceNames);
                        deviceAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                        devicelistbox.setAdapter(deviceAdapter);
                        devicelistbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                //Toast.makeText(parent.getContext(), "Gewähltes Gerät: " + mPCD.Devices[pos].Name, Toast.LENGTH_SHORT).show();
                                CurrentDeviceOrGroupID = mPCD.Devices[pos].ID;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }

                    if (mPCD.Groups!=null) {
                        try {
                            Spinner grouplistbox = (Spinner) findViewById(R.id.grouplistbox);
                            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, R.layout.devicelist_child_item, GroupNames);
                            groupAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                            grouplistbox.setAdapter(groupAdapter);
                            grouplistbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                    //Toast.makeText(parent.getContext(), "Gewählte Gruppe: " + mPCD.Groups[pos].Name, Toast.LENGTH_SHORT).show();
                                    CurrentDeviceOrGroupID = mPCD.Groups[pos].ID;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }catch(Exception e){
                            if (BuildConfig.DEBUG) {
                                System.out.println(e.toString());
                            }
                        }
                    }
                }

                // Prepare Dimmer-, Strobe- and Shutterslider and -buttons
                final de.phoenixstudios.pc_dimmer.VerticalSeekBar dimmerslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.dimmerslider);
                dimmerslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_dimmer(CurrentDeviceOrGroupID, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                dimmerslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar strobeslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.strobeslider);
                strobeslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_strobe(CurrentDeviceOrGroupID, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                strobeslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                Button dimmer100btn = (Button) findViewById(R.id.dimmer100btn);
                dimmer100btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dimmerslider.setProgress(255);
                    }
                });
                Button dimmer75btn = (Button) findViewById(R.id.dimmer75btn);
                dimmer75btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dimmerslider.setProgress(192);
                    }
                });
                Button dimmer50btn = (Button) findViewById(R.id.dimmer50btn);
                dimmer50btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dimmerslider.setProgress(128);
                    }
                });
                Button dimmer25btn = (Button) findViewById(R.id.dimmer25btn);
                dimmer25btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dimmerslider.setProgress(64);
                    }
                });
                Button dimmer0btn = (Button) findViewById(R.id.dimmer0btn);
                dimmer0btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dimmerslider.setProgress(0);
                    }
                });
                Button shutteraufbtn = (Button) findViewById(R.id.shutteraufbtn);
                shutteraufbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_shutter(CurrentDeviceOrGroupID, true, -1);
                    }
                });
                Button shutterzubtn = (Button) findViewById(R.id.shutterzubtn);
                shutterzubtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_shutter(CurrentDeviceOrGroupID, false, -1);
                    }
                });

                // Prepare Colorpickers and Colorcontrols
                ColorPicker colorpicker = (ColorPicker) findViewById(R.id.colorpicker);
                SaturationBar saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
                ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);

                colorpicker.addSaturationBar(saturationBar);
                colorpicker.addValueBar(valueBar);
                colorpicker.setShowOldCenterColor(false); // turn off showing the old color
                //colorpicker.setOldCenterColor(colorpicker.getColor()); // set the old selected color u can do it like this

                colorpicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                    public void onColorChanged(int selectedcolor) {
                        Main.set_color(CurrentDeviceOrGroupID, Color.red(selectedcolor), Color.green(selectedcolor), Color.blue(selectedcolor), GlobalFadetime, -1);
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar amberslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.amberslider);
                amberslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_channel(CurrentDeviceOrGroupID, "A", -1, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                amberslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar whiteslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.whiteslider);
                whiteslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_channel(CurrentDeviceOrGroupID, "W", -1, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                whiteslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });



                // Initialize PanTilt
                PanTiltCanvasView pantiltcanvas = (PanTiltCanvasView) findViewById(R.id.pantiltcanvas);
                pantiltcanvas.setOnTouchListener(new PanTiltCanvasView.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;

                            case MotionEvent.ACTION_MOVE:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_SCROLL:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle canvas touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });
                pantiltcanvas.clearCanvas();




                // Sonstiges
                // Prepare Dimmer-, Strobe- and Shutterslider and -buttons
                final de.phoenixstudios.pc_dimmer.VerticalSeekBar irisslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.irisslider);
                irisslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_iris(CurrentDeviceOrGroupID, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                irisslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar prismarotslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.prismarotslider);
                prismarotslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_prismarot(CurrentDeviceOrGroupID, i, GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                prismarotslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar fogslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.fogslider);
                fogslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_fog(i, GlobalFadetime, 0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                fogslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });
                Button prismaonbtn = (Button) findViewById(R.id.prismaonbtn);
                prismaonbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_prisma(CurrentDeviceOrGroupID, true, -1);
                    }
                });
                Button prismaoffbtn = (Button) findViewById(R.id.prismaoffbtn);
                prismaoffbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_prisma(CurrentDeviceOrGroupID, true, -1);
                    }
                });


                Button gobo1p = (Button) findViewById(R.id.gobo1p);
                prismaoffbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_gobo1plus(CurrentDeviceOrGroupID);
                    }
                });
                Button gobo1m = (Button) findViewById(R.id.gobo1m);
                prismaoffbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_gobo1minus(CurrentDeviceOrGroupID);
                    }
                });
                Button gobo2p = (Button) findViewById(R.id.gobo2p);
                prismaoffbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_gobo2plus(CurrentDeviceOrGroupID);
                    }
                });
                Button gobo2m = (Button) findViewById(R.id.gobo2m);
                prismaoffbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        set_gobo2minus(CurrentDeviceOrGroupID);
                    }
                });
        }
    }

    public void ControlpanelCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_controlpanel:
                GridView controlpanel = (GridView) findViewById(R.id.controlpanel);
                controlpanel.setAdapter(new ControlpanelButtonAdapter(this));

                if (Main.mPCD!=null) {
                    if (Main.mPCD.ControlpanelButtons != null) {
                        controlpanel.setNumColumns(mPCD.ControlpanelButtons[0].length); // [y][x]

                        ViewGroup.LayoutParams controlpanelparams = controlpanel.getLayoutParams();
                        controlpanelparams.height=mPCD.ControlpanelButtons.length*125;
                    }
                }else{
                    controlpanel.setNumColumns(4);

                    ViewGroup.LayoutParams controlpanelparams = controlpanel.getLayoutParams();
                    controlpanelparams.height=4*125;
                }

                break;
        }
    }

    public void ChanneloverviewCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_channeloverview:
                Button minus8chbtn = (Button) findViewById(R.id.minus8chbtn);
                minus8chbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((SeekBar) findViewById(R.id.channelslider)).setProgress(((SeekBar) findViewById(R.id.channelslider)).getProgress()-8);
                        UpdateChanneloverview();
                    }
                });
                Button updatechanneloverviewbtn = (Button) findViewById(R.id.updatechanneloverviewbtn);
                updatechanneloverviewbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpdateChanneloverview();
                    }
                });
                Button plus8chbtn = (Button) findViewById(R.id.plus8chbtn);
                plus8chbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((SeekBar) findViewById(R.id.channelslider)).setProgress(((SeekBar) findViewById(R.id.channelslider)).getProgress()+8);
                        UpdateChanneloverview();
                    }
                });

                SeekBar channelslider = (SeekBar) findViewById(R.id.channelslider);
                SeekBar ch1slider = (SeekBar) findViewById(R.id.ch1slider);
                SeekBar ch2slider = (SeekBar) findViewById(R.id.ch2slider);
                SeekBar ch3slider = (SeekBar) findViewById(R.id.ch3slider);
                SeekBar ch4slider = (SeekBar) findViewById(R.id.ch4slider);
                SeekBar ch5slider = (SeekBar) findViewById(R.id.ch5slider);
                SeekBar ch6slider = (SeekBar) findViewById(R.id.ch6slider);
                SeekBar ch7slider = (SeekBar) findViewById(R.id.ch7slider);
                SeekBar ch8slider = (SeekBar) findViewById(R.id.ch8slider);

                channelslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        UpdateChanneloverview();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                ch1slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress() + 1, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch2slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+2, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch3slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+3, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch4slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+4, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch5slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+5, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch6slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+6, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch7slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+7, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                ch8slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_absolutechannel(((SeekBar) findViewById(R.id.channelslider)).getProgress()+8, -1, seekBar.getProgress(), GlobalFadetime, -1);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                UpdateChanneloverview();
                break;
        }
    }

    public void StageviewCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_stageview:
                DownloadStageview=1;

                Button refreshstageviewbtn = (Button) findViewById(R.id.refreshstageviewbtn);
                refreshstageviewbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DownloadStageview = 1;
                    }
                });

                break;
        }
    }

    public void NodecontrolCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_nodecontrol:
                // Fill nodesetlistbox
                if (mPCD!=null) {
                    if (mPCD.Nodesets != null) {
                        try {
                            Spinner nodesetlistbox = (Spinner) findViewById(R.id.nodesetlistbox);
                            ArrayAdapter<String> nodesetAdapter = new ArrayAdapter<>(this, R.layout.devicelist_child_item, NodesetNames);
                            nodesetAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                            nodesetlistbox.setAdapter(nodesetAdapter);
                            nodesetlistbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                    CurrentNodeset = pos;

                                    ((VerticalSeekBar) findViewById(R.id.node_stretchslider)).setProgress(mPCD.Nodesets[CurrentNodeset].stretching);
                                    ((VerticalSeekBar) findViewById(R.id.node_contrastslider)).setProgress(mPCD.Nodesets[CurrentNodeset].contrast);
                                    ((EditText) findViewById(R.id.node_fadetimeedit)).setText(Integer.toString(mPCD.Nodesets[CurrentNodeset].fadetime));
                                    ((CheckBox) findViewById(R.id.nodeset_usergbcheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].ChangeRGB);
                                    ((CheckBox) findViewById(R.id.nodeset_useambercheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].ChangeA);
                                    ((CheckBox) findViewById(R.id.nodeset_usewhitecheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].ChangeW);
                                    ((CheckBox) findViewById(R.id.nodeset_usedimmercheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].ChangeDimmer);

                                    // cleanup nodelistbox
                                    NodesNames = new String[0];
                                    Spinner nodelistbox = (Spinner) findViewById(R.id.nodelistbox);
                                    ArrayAdapter<String> nodeAdapter = new ArrayAdapter<>(Main.this, R.layout.devicelist_child_item, NodesNames);
                                    nodeAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                                    nodelistbox.setAdapter(nodeAdapter);

                                    // fill nodelistbox
                                    if (mPCD != null) {
                                        if (mPCD.Nodesets != null) {
                                            if (CurrentNodeset < mPCD.Nodesets.length) {
                                                if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                                    NodesNames = new String[mPCD.Nodesets[CurrentNodeset].Nodes.length];
                                                    for (int i = 0; i < mPCD.Nodesets[CurrentNodeset].Nodes.length; i++) {
                                                        NodesNames[i] = mPCD.Nodesets[CurrentNodeset].Nodes[i].Name;
                                                    }

                                                    nodelistbox = (Spinner) findViewById(R.id.nodelistbox);
                                                    nodeAdapter = new ArrayAdapter<>(Main.this, R.layout.devicelist_child_item, NodesNames);
                                                    nodeAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                                                    nodelistbox.setAdapter(nodeAdapter);
                                                    nodelistbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                                            CurrentNode = pos;

                                                            ((VerticalSeekBar) findViewById(R.id.node_amberslider)).setProgress(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].A);
                                                            ((VerticalSeekBar) findViewById(R.id.node_whiteslider)).setProgress(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].W);
                                                            ((VerticalSeekBar) findViewById(R.id.node_dimmerslider)).setProgress(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].Dimmer);
                                                            ((CheckBox) findViewById(R.id.usergbcheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseRGB);
                                                            ((CheckBox) findViewById(R.id.useambercheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseA);
                                                            ((CheckBox) findViewById(R.id.usewhitecheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseW);
                                                            ((CheckBox) findViewById(R.id.usedimmercheckbox)).setChecked(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseDimmer);
                                                            ((com.larswerkman.holocolorpicker.ColorPicker) findViewById(R.id.node_colorpicker)).setColor(Color.rgb(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].R, mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].G, mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].B));

                                                            ((NodeXYCanvasView) findViewById(R.id.nodecanvas)).setPoint(mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].X, mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].Y);
                                                            findViewById(R.id.nodecanvas).invalidate();
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {

                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }catch(Exception e){
                            if (BuildConfig.DEBUG) {
                                System.out.println(e.toString());
                            }
                        }
                    }
                }



                // Prepare Tabs and set Text
                nodecontrol_tabHost = (TabHost) findViewById(R.id.nodecontrol_tabHost);
                nodecontrol_tabHost.setup();

                TabHost.TabSpec nodecontrol_spec1 = nodecontrol_tabHost.newTabSpec("nodecontrol_tab1");
                nodecontrol_spec1.setContent(R.id.nodecontrol_tab1);
                nodecontrol_spec1.setIndicator(getString(R.string.str_general));

                TabHost.TabSpec nodecontrol_spec2 = nodecontrol_tabHost.newTabSpec("nodecontrol_tab2");
                nodecontrol_spec2.setContent(R.id.nodecontrol_tab2);
                nodecontrol_spec2.setIndicator(getString(R.string.str_nodes));

                nodecontrol_tabHost.addTab(nodecontrol_spec1);
                nodecontrol_tabHost.addTab(nodecontrol_spec2);

                // Prepare Colorpickers and Colorcontrols
                ColorPicker node_colorpicker = (ColorPicker) findViewById(R.id.node_colorpicker);
                SaturationBar node_saturationBar = (SaturationBar) findViewById(R.id.node_saturationbar);
                ValueBar node_valueBar = (ValueBar) findViewById(R.id.node_valuebar);

                node_colorpicker.addSaturationBar(node_saturationBar);
                node_colorpicker.addValueBar(node_valueBar);
                node_colorpicker.setShowOldCenterColor(false); // turn off showing the old color
                //colorpicker.setOldCenterColor(colorpicker.getColor()); // set the old selected color u can do it like this

                node_colorpicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                    public void onColorChanged(int selectedcolor) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                        if ((mPCD.Nodesets[CurrentNodeset].Nodes.length > 0) && (CurrentNode < mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].R = Color.red(selectedcolor);
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].G = Color.green(selectedcolor);
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].B = Color.blue(selectedcolor);
                                            findViewById(R.id.nodecanvas).invalidate();
                                            set_node();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });


                // Prepare canvas
                NodeXYCanvasView nodecanvas = (NodeXYCanvasView) findViewById(R.id.nodecanvas);
                nodecanvas.setOnTouchListener(new NodeXYCanvasView.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;

                            case MotionEvent.ACTION_MOVE:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_SCROLL:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle canvas touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });
                nodecanvas.clearCanvas();




                // Prepare sliders
                final de.phoenixstudios.pc_dimmer.VerticalSeekBar node_amberslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_amberslider);
                node_amberslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                        if ((mPCD.Nodesets[CurrentNodeset].Nodes.length > 0) && (CurrentNode < mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].A = ((de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_amberslider)).getProgress();
                                            set_node();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                node_amberslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar node_whiteslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_whiteslider);
                node_whiteslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                        if ((mPCD.Nodesets[CurrentNodeset].Nodes.length > 0) && (CurrentNode < mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].W = ((de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_whiteslider)).getProgress();
                                            set_node();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                node_whiteslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar node_dimmerslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_dimmerslider);
                node_dimmerslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                        if ((mPCD.Nodesets[CurrentNodeset].Nodes.length > 0) && (CurrentNode < mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                            mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].Dimmer=((de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_dimmerslider)).getProgress();
                                            set_node();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                node_dimmerslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar node_stretchslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_stretchslider);
                node_stretchslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    mPCD.Nodesets[CurrentNodeset].stretching=((de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_stretchslider)).getProgress();
                                    set_nodeset();
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                node_stretchslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                final de.phoenixstudios.pc_dimmer.VerticalSeekBar node_contrastslider = (de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_contrastslider);
                node_contrastslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (mPCD != null) {
                            if (mPCD.Nodesets != null) {
                                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                                    mPCD.Nodesets[CurrentNodeset].contrast=((de.phoenixstudios.pc_dimmer.VerticalSeekBar) findViewById(R.id.node_contrastslider)).getProgress();
                                    set_nodeset();
                                }
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                node_contrastslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                break;
            case R.id.usergbcheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                if ((mPCD.Nodesets[CurrentNodeset].Nodes.length>0) && (CurrentNode<mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                    mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseRGB=((CheckBox) findViewById(R.id.usergbcheckbox)).isChecked();
                                    set_node();
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.useambercheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                if ((mPCD.Nodesets[CurrentNodeset].Nodes.length>0) && (CurrentNode<mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                    mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseA=((CheckBox) findViewById(R.id.useambercheckbox)).isChecked();
                                    set_node();
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.usewhitecheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                if ((mPCD.Nodesets[CurrentNodeset].Nodes.length>0) && (CurrentNode<mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                    mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseW=((CheckBox) findViewById(R.id.usewhitecheckbox)).isChecked();
                                    set_node();
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.usedimmercheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            if (mPCD.Nodesets[CurrentNodeset].Nodes != null) {
                                if ((mPCD.Nodesets[CurrentNodeset].Nodes.length>0) && (CurrentNode<mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                                    mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseDimmer=((CheckBox) findViewById(R.id.usedimmercheckbox)).isChecked();
                                    set_node();
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.nodeset_usergbcheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            mPCD.Nodesets[CurrentNodeset].ChangeRGB=((CheckBox) findViewById(R.id.nodeset_usergbcheckbox)).isChecked();
                            set_nodeset();
                        }
                    }
                }
                break;
            case R.id.nodeset_useambercheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            mPCD.Nodesets[CurrentNodeset].ChangeA=((CheckBox) findViewById(R.id.nodeset_useambercheckbox)).isChecked();
                            set_nodeset();
                        }
                    }
                }
                break;
            case R.id.nodeset_usewhitecheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            mPCD.Nodesets[CurrentNodeset].ChangeW=((CheckBox) findViewById(R.id.nodeset_usewhitecheckbox)).isChecked();
                            set_nodeset();
                        }
                    }
                }
                break;
            case R.id.nodeset_usedimmercheckbox:
                if (mPCD!=null) {
                    if (mPCD.Nodesets!=null) {
                        if ((mPCD.Nodesets.length>0) && (CurrentNodeset<mPCD.Nodesets.length)) {
                            mPCD.Nodesets[CurrentNodeset].ChangeDimmer=((CheckBox) findViewById(R.id.nodeset_usedimmercheckbox)).isChecked();
                            set_nodeset();
                        }
                    }
                }
                break;
        }
    }

    public void StageSetupCallbackToMain(int Cmd){
        switch(Cmd) {
            case R.layout.fragment_stagesetup:
                // Fill Device- and Grouplists
                if (mPCD!=null) {
                    if (mPCD.Devices != null) {
                        Spinner devicelistbox = (Spinner) findViewById(R.id.stagesetup_devicelistbox);
                        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(this, R.layout.devicelist_child_item, DeviceNames);
                        deviceAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
                        devicelistbox.setAdapter(deviceAdapter);
                        devicelistbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                //Toast.makeText(parent.getContext(), "Gewähltes Gerät: " + mPCD.Devices[pos].Name, Toast.LENGTH_SHORT).show();
                                CurrentSetupDevice.ID = mPCD.Devices[pos].ID;

                                //get device-information from PC_DIMMER
                                get_deviceinfo(CurrentSetupDevice.ID);

                                //update the GUI
                                ((EditText) findViewById(R.id.stagesetup_newaddressedit)).setText(Integer.toString(CurrentSetupDevice.Startaddress));
                                ((Button) findViewById(R.id.stagesetup_newcolorbtn)).setBackgroundColor(CurrentSetupDevice.color);
                                findViewById(R.id.stagesetup_dipswitchcanvas).invalidate();
                                ((TextView) findViewById(R.id.stagesetup_channelcountlbl)).setText(Integer.toString(CurrentSetupDevice.ChanCount));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }

                // Prepare sliders
                final SeekBar stagesetup_enlightslider = (SeekBar) findViewById(R.id.stagesetup_enlightseekbar);
                stagesetup_enlightslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        set_highlight(CurrentSetupDevice.ID, i, 0); // values above 0 will produce flicker. Reason is unclear
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                stagesetup_enlightslider.setOnTouchListener(new SeekBar.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                // Disallow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;

                            case MotionEvent.ACTION_UP:
                                // Allow ScrollView to intercept touch events.
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }

                        // Handle Seekbar touch events.
                        v.onTouchEvent(event);
                        return true;
                    }
                });

                break;
            case R.id.stagesetup_newaddressbtn:
                CurrentSetupDevice.Startaddress=Integer.parseInt(((EditText) findViewById(R.id.stagesetup_newaddressedit)).getText().toString());
                set_devaddress(CurrentSetupDevice.ID, CurrentSetupDevice.Startaddress);
                findViewById(R.id.stagesetup_dipswitchcanvas).invalidate();
                break;
            case R.id.stagesetup_newcolorbtn:
                AmbilWarnaDialog colordialog = new AmbilWarnaDialog(this, CurrentSetupDevice.color, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        CurrentSetupDevice.color=color;
                        ((Button) findViewById(R.id.stagesetup_newcolorbtn)).setBackgroundColor(CurrentSetupDevice.color);
                        set_devcolor(CurrentSetupDevice.ID, CurrentSetupDevice.color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });

                colordialog.show();
                break;
        }
    }


    public static void SendTCPCommand(String cmd){
        NetworkCommandStringQueue=cmd;
    }

    public static String SendReceiveTCPCommand(String cmd){
        String Answer = "";

        if (!NetworkThreadAlive) return Answer;

        NetworkCommandString = cmd;
        NetworkCommandStatus = 1;

        int WaitForTCPCommand = 500; // 5 Sekunden Timeout

        while (WaitForTCPCommand > 0) {
            WaitForTCPCommand--;

            if ((NetworkCommandStatus == 3) || (NetworkCommandStatus == -1)) {
                // Entweder Fehler oder Fertig -> Schleife beenden
                WaitForTCPCommand = 0;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                if (BuildConfig.DEBUG) {
                    System.out.println(e.toString());
                }
            }
        }

        switch (NetworkCommandStatus) {
            case -1:
                // Fehler bei Datenübertragung!
                Answer = "-1";
                break;
            case 1:
                // Kommando noch nicht gesendet!
                Answer = "-1";
                break;
            case 2:
                // Keine Antwort vom PC_DIMMER!
                Answer = "-1";
                break;
            case 3:
                Answer = NetworkCommandReceivedString;
                if (Answer==null) {
                    Answer="";
                }
                break;
            default:
                // Unbekannter Fehler!
                Answer = "-1";
                break;
        }

        return Answer;
    }

    private class NetworkThread extends AsyncTask<String, Integer, Long> {
        @Override
        protected Long doInBackground(String... args) {
            long result = 0;

            BufferedWriter outToServer=null;
            BufferedReader inFromServer=null;

            Socket TCPSocket=null;
            InetAddress serverAddr=null;

            while(NetworkThreadAlive) {
                try {
                    if (TCPSocket==null) {
                        // Verbindung herstellen, falls nicht (mehr) verbunden
                        try{
                            serverAddr = InetAddress.getByName(NetworkIPAddress);
                        }catch(UnknownHostException error){
                            if (BuildConfig.DEBUG) {
                                System.out.println(error.toString());
                            }
                        }
                        TCPSocket = new Socket(serverAddr, NetworkPort);
                        TCPSocket.setSoTimeout(10000);

                        NetworkConnectionOK=true;

                        outToServer = new BufferedWriter(new OutputStreamWriter(TCPSocket.getOutputStream()));
                        inFromServer = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
                    }

                    if (DownloadStageview==1) {
                        DownloadStageview=2;

                        try {
                            InputStream in = new java.net.URL("http://"+NetworkIPAddress+"/stageview.jpg").openStream();
                            stageviewdownload = BitmapFactory.decodeStream(in);
                            DownloadStageview=3;
                        } catch (Exception e) {
                            if (BuildConfig.DEBUG) {
                                System.out.println(e.toString());
                            }
                        }
                    }

                    // Wenn neues Kommando vorhanden, dann dieses senden
                    if (NetworkCommandStatus == 1) {
                        outToServer.write(NetworkCommandString);
						outToServer.newLine();
                        outToServer.flush();
                        NetworkCommandStatus = 2;

						NetworkCommandReceivedString = inFromServer.readLine(); // contains the original command - ignore this message (or use it as a error-check-option)
                        NetworkCommandReceivedString = inFromServer.readLine(); // contains the answer of the command
                        NetworkCommandStatus = 3;
						NetworkConnectionOK = true;
                    }
                } catch (IOException error) {
                    if (BuildConfig.DEBUG) {
                        System.out.println(error.toString());
                    }
                    NetworkConnectionOK=false;
                    NetworkCommandStatus = -1;
                    NetworkCommandReceivedString = "2"; // Keine Verbindung zum PC_DIMMER!
                    NetworkErrorMsg=error.toString();

                    // Verbindung beenden und neu etablieren
                    try {
                        if (TCPSocket!=null){
                            if (TCPSocket.isConnected()) {
                                inFromServer.close();
                                outToServer.close();
                                TCPSocket.close();
                            }
                        }
                        TCPSocket = null;
                    }catch(Exception error2){
                        if (BuildConfig.DEBUG) {
                            System.out.println(error2.toString());
                        }
                    }
                }

                // While-Schleife nur maximal alle 1ms durchlaufen
                try {
                    Thread.sleep(1);
                }catch(Exception error){
                    if (BuildConfig.DEBUG) {
                        System.out.println(error.toString());
                    }
                }
            }

            NetworkConnectionOK=false;
            NetworkCommandStatus = -1;
            NetworkCommandReceivedString = "2"; // Keine Verbindung zum PC_DIMMER!
            NetworkErrorMsg="Disconnected.";

            if (TCPSocket != null) {
                try {
                    inFromServer.close();
                    outToServer.close();
                    TCPSocket.close();
                } catch (IOException error) {
                    if (BuildConfig.DEBUG) {
                        System.out.println(error.toString());
                    }
                }
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            if (BuildConfig.DEBUG) {
                System.out.println(getString(R.string.str_commandthreadclosed));
            }
        }
    }

    static String mySubString(String myString, int start, int length) {
        return myString.substring(start, Math.min(start + length, myString.length()));
    }
    
    public void SynchronizeData() {
        // Download Projectsettings
        String s;
        int Maximum;
        int CountX;
        int CountY;
        int i;
        int j;
        int x;
        int y;
        mPCD = null;
        mPCD = new PCD();
        mPCD.Scenes = new PCD_Scene[12][];

        try {
            s = SendReceiveTCPCommand("get_devices");
            if (s.equals("-1")) return;
            //devices 56 1:NAME,{GUID} 2:NAME,{GUID} 3:....
            //devices 60 1:Blau Decke Rechts,{DF55AE31-ED33-491A-9756-ED9B6F076EF2} 2:Gelb Decke Rechts,{E084758A-0665-4AF7-9810-D0AEB3BEAA99} 3:Rot Decke Rechts,{C61B
            if (s.length() > 10) {
                Maximum = Integer.parseInt(mySubString(s, 8, s.indexOf(":") - 8 - 2));
                if (Maximum>0) {
                    mPCD.Devices = new PCD_Device[Maximum];
                    DeviceNames = new String[Maximum];

                    for (i = 0; i < (Maximum - 1); i++) {
                        mPCD.Devices[i] = new PCD_Device();
                        mPCD.Devices[i].ID = mySubString(s, s.indexOf(",") + 1, 38);
                        mPCD.Devices[i].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                        DeviceNames[i] = mPCD.Devices[i].Name;
                        s = mySubString(s, s.indexOf("}") + 2, s.length() - s.indexOf("}") - 2);
                        //((ProgressBar) findViewById(R.id.connectProgress)).setProgress(Math.round((i/Maximum)*25));
                    }
                    mPCD.Devices[Maximum - 1] = new PCD_Device();
                    mPCD.Devices[Maximum - 1].ID = mySubString(s, s.indexOf(",") + 1, 38);
                    mPCD.Devices[Maximum - 1].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                    DeviceNames[Maximum - 1] = mPCD.Devices[Maximum - 1].Name;
                }
            }

            s = SendReceiveTCPCommand("get_groups");
            if (s.equals("-1")) return;
            if (s.length() > 10) {
                Maximum = Integer.parseInt(mySubString(s, 7, s.indexOf(":") - 7 - 2));
                if (Maximum>0) {
                    mPCD.Groups = new PCD_Group[Maximum];
                    GroupNames = new String[Maximum];

                    for (i = 0; i < (Maximum - 1); i++) {
                        mPCD.Groups[i] = new PCD_Group();
                        mPCD.Groups[i].ID = mySubString(s, s.indexOf(",") + 1, 38);
                        mPCD.Groups[i].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                        GroupNames[i] = mPCD.Groups[i].Name;
                        s = mySubString(s, s.indexOf("}") + 2, s.length() - s.indexOf("}") - 2);
                        //((ProgressBar) findViewById(R.id.connectProgress)).setProgress(Math.round(25+(i/Maximum)*25));
                    }
                    mPCD.Groups[Maximum - 1] = new PCD_Group();
                    mPCD.Groups[Maximum - 1].ID = mySubString(s, s.indexOf(",") + 1, 38);
                    mPCD.Groups[Maximum - 1].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                    GroupNames[Maximum - 1] = mPCD.Groups[Maximum - 1].Name;
                }
            }

            for (i = 0; i <= 11; i++) {
                s = SendReceiveTCPCommand("get_scenes " + Integer.toString(i));
                if (s.equals("-1")) return;
                if (s.length() > 10) {
                    Maximum = Integer.parseInt(mySubString(s, 7, s.indexOf(":") - 7 - 2));
                    if (Maximum>0) {
                        mPCD.Scenes[i] = new PCD_Scene[Maximum];

                        for (j = 0; j < (Maximum - 1); j++) {
                            mPCD.Scenes[i][j] = new PCD_Scene();
                            mPCD.Scenes[i][j].ID = mySubString(s, s.indexOf(",") + 1, 38);
                            mPCD.Scenes[i][j].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                            s = mySubString(s, s.indexOf("}") + 2, s.length() - s.indexOf("}") - 2);
                            //((ProgressBar) findViewById(R.id.connectProgress)).setProgress(Math.round(50+(i/Maximum)*2));
                        }
                        mPCD.Scenes[i][Maximum - 1] = new PCD_Scene();
                        mPCD.Scenes[i][Maximum - 1].ID = mySubString(s, s.indexOf(",") + 1, 38);
                        mPCD.Scenes[i][Maximum - 1].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                    }
                }
            }

            s = SendReceiveTCPCommand("get_nodesets");
            if (s.equals("-1")) return;
            if (s.length() > 10) {
                Maximum = Integer.parseInt(mySubString(s, 9, s.indexOf(":") - 9 - 2));
                if (Maximum>0) {
                    mPCD.Nodesets = new PCD_Nodeset[Maximum];
                    NodesetNames = new String[Maximum];
                    for (i = 0; i < (Maximum - 1); i++) {
                        mPCD.Nodesets[i] = new PCD_Nodeset();
                        mPCD.Nodesets[i].ID = mySubString(s, s.indexOf(",") + 1, 38);
                        mPCD.Nodesets[i].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                        NodesetNames[i] = mPCD.Nodesets[i].Name;
                        mPCD.Nodesets[i].stretching = 128000;
                        mPCD.Nodesets[i].contrast = 20;
                        mPCD.Nodesets[i].fadetime = 75;
                        mPCD.Nodesets[i].ChangeRGB = true;
                        mPCD.Nodesets[i].ChangeA = false;
                        mPCD.Nodesets[i].ChangeW = false;
                        mPCD.Nodesets[i].ChangeDimmer = false;
                        s = mySubString(s, s.indexOf("}") + 2, s.length() - s.indexOf("}") - 2);
                    }
                    mPCD.Nodesets[Maximum - 1] = new PCD_Nodeset();
                    mPCD.Nodesets[Maximum - 1].ID = mySubString(s, s.indexOf(",") + 1, 38);
                    mPCD.Nodesets[Maximum - 1].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                    NodesetNames[Maximum - 1] = mPCD.Nodesets[Maximum - 1].Name;
                    mPCD.Nodesets[Maximum - 1].stretching = 128000;
                    mPCD.Nodesets[Maximum - 1].contrast = 20;
                    mPCD.Nodesets[Maximum - 1].fadetime = 75;
                    mPCD.Nodesets[Maximum - 1].ChangeRGB = true;
                    mPCD.Nodesets[Maximum - 1].ChangeA = false;
                    mPCD.Nodesets[Maximum - 1].ChangeW = false;
                    mPCD.Nodesets[Maximum - 1].ChangeDimmer = false;

                    for (i = 0; i < mPCD.Nodesets.length; i++) {
                        s = SendReceiveTCPCommand("get_nodes " + mPCD.Nodesets[i].ID);
                        if (s.equals("-1")) return;
                        if (s.length() > 10) {
                            Maximum = Integer.parseInt(mySubString(s, 6, s.indexOf(":") - 6 - 2));
                            if (Maximum>0) {
                                mPCD.Nodesets[i].Nodes = new PCD_Node[Maximum];

                                for (j = 0; j < (Maximum - 1); j++) {
                                    mPCD.Nodesets[i].Nodes[j] = new PCD_Node();
                                    mPCD.Nodesets[i].Nodes[j].ID = mySubString(s, s.indexOf(",") + 1, 38);
                                    mPCD.Nodesets[i].Nodes[j].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                                    mPCD.Nodesets[i].Nodes[j].X = 5000;
                                    mPCD.Nodesets[i].Nodes[j].Y = 5000;
                                    mPCD.Nodesets[i].Nodes[j].R = 255;
                                    mPCD.Nodesets[i].Nodes[j].G = 0;
                                    mPCD.Nodesets[i].Nodes[j].B = 0;
                                    mPCD.Nodesets[i].Nodes[j].A = 0;
                                    mPCD.Nodesets[i].Nodes[j].W = 0;
                                    mPCD.Nodesets[i].Nodes[j].Dimmer = 0;
                                    mPCD.Nodesets[i].Nodes[j].UseRGB = true;
                                    mPCD.Nodesets[i].Nodes[j].UseA = false;
                                    mPCD.Nodesets[i].Nodes[j].UseW = false;
                                    mPCD.Nodesets[i].Nodes[j].UseDimmer = false;
                                    s = mySubString(s, s.indexOf("}") + 2, s.length() - s.indexOf("}") - 2);
                                }
                                mPCD.Nodesets[i].Nodes[Maximum - 1] = new PCD_Node();
                                mPCD.Nodesets[i].Nodes[Maximum - 1].ID = mySubString(s, s.indexOf(",") + 1, 38);
                                mPCD.Nodesets[i].Nodes[Maximum - 1].Name = mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1);
                                mPCD.Nodesets[i].Nodes[Maximum - 1].X = 5000;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].Y = 5000;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].R = 255;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].G = 0;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].B = 0;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].A = 0;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].W = 0;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].Dimmer = 0;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].UseRGB = true;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].UseA = false;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].UseW = false;
                                mPCD.Nodesets[i].Nodes[Maximum - 1].UseDimmer = false;
                            }
                        }
                    }
                }
            }

            s = SendReceiveTCPCommand("get_controlpanel");
            if (s.equals("-1")) return;
            if (s.length()>14) {
                CountX = Integer.parseInt(mySubString(s, 7, s.indexOf(",") - 7));
                CountY = Integer.parseInt(mySubString(s, s.indexOf(",") + 8, s.length() - s.indexOf(",") - 8));
                if ((CountX > 0) && (CountY > 0)) {
                    s = SendReceiveTCPCommand("get_controlpanel 0 0");
                    //button: 1x1, name:bla, r:255, g:255, b:255, type:bla, id:{3A...}, button: 1x2, name:bla,....
                    mPCD.ControlpanelButtons = new PCD_ControlpanelButton[CountY][CountX];

                    for (y = 0; y < CountY; y++) {
                        for (x = 0; x < CountX; x++) {
                            mPCD.ControlpanelButtons[y][x] = new PCD_ControlpanelButton();
                            mPCD.ControlpanelButtons[y][x].ID = mySubString(s, s.indexOf("id:") + 3, 38);
                            mPCD.ControlpanelButtons[y][x].Name = mySubString(s, s.indexOf("name:") + 5, s.indexOf(", r:") - s.indexOf("name:") - 5);
                            mPCD.ControlpanelButtons[y][x].Type = mySubString(s, s.indexOf("type:") + 5, s.indexOf(", id:") - s.indexOf("type:") - 5);
                            mPCD.ControlpanelButtons[y][x].R = Integer.parseInt(mySubString(s, s.indexOf("r:") + 2, s.indexOf(", g:") - s.indexOf("r:") - 2));
                            mPCD.ControlpanelButtons[y][x].G = Integer.parseInt(mySubString(s, s.indexOf("g:") + 2, s.indexOf(", b:") - s.indexOf("g:") - 2));
                            mPCD.ControlpanelButtons[y][x].B = Integer.parseInt(mySubString(s, s.indexOf("b:") + 2, s.indexOf(", type:") - s.indexOf("b:") - 2));
                            mPCD.ControlpanelButtons[y][x].X = x;
                            mPCD.ControlpanelButtons[y][x].Y = y;

                            if ((y < (CountY - 1)) || (x < (CountX - 1))) {
                                s = mySubString(s, s.indexOf("id:") + 3 + 38 + 2, s.length() - s.indexOf("id:") - 3 - 38 - 2);
                            }
                            //((ProgressBar) findViewById(R.id.connectProgress)).setProgress(Math.round(75+(y/CountY)*20));
                        }
                    }
                }
            }

            // Kanalwerte abfragen
            QueryChannelvalues(1, 8);

            Toast.makeText(getBaseContext(), R.string.str_syncok, Toast.LENGTH_LONG).show();
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }

            Toast.makeText(getBaseContext(), R.string.str_synccancelled, Toast.LENGTH_LONG).show();
        }
    }

    public void QueryChannelvalues(int StartChannel, int ChannelCount) {
        String s;
        int Value;
        int Channel;

        try {
            s = SendReceiveTCPCommand("get_range " + Integer.toString(StartChannel) + " " + Integer.toString(StartChannel + ChannelCount));
            if (s.equals("-1")) return;
            //CVS 1:128,2:33,3:100,...

            if (s.contains("CVS")) {
                for (Channel = StartChannel; Channel <= (StartChannel + ChannelCount); Channel++) {
                    if (Channel < (StartChannel + ChannelCount)) {
                        Value = Integer.parseInt(mySubString(s, s.indexOf(":") + 1, s.indexOf(",") - s.indexOf(":") - 1));
                        s = mySubString(s, s.indexOf(",") + 1, s.length() - s.indexOf(",") - 1);
                    } else {
                        Value = Integer.parseInt(mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1));
                    }
                    Channelvalues[Channel - 1] = Value;
                }
            }
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }
    }

    public static int get_channel(int Channel) {
        String s;
        int Value=0;

        try {
            s = SendReceiveTCPCommand("get_ch " + Integer.toString(Channel));

            if (!s.contains("CV")) {
                Value = -1;
            } else {
                s = mySubString(s, 4, s.length() - 4); // "CV 42 42" -> "42 42"
                Value = Integer.parseInt(mySubString(s, s.indexOf(" ") + 1, s.length() - s.indexOf(" ") + 1));
            }
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }

        return Value;
    }

    public static int get_devchannel(String ID, String Channelname) {
        String s;
        int Value=0;

        try {
            s = SendReceiveTCPCommand("get_devchannel " + ID + " " + Channelname);

            if (!s.contains("DCV")) {
                Value = -1;
            } else {
                Value = Integer.parseInt(mySubString(s, s.indexOf(" ") + 1, s.length() - s.indexOf(" ") + 1));
            }
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }

        return Value;
    }

    public static int get_dimmer(String ID) {
        String s;
        int Value=-1;

        try {
            s = SendReceiveTCPCommand("get_dimmer " + ID);

            if (!s.contains("DDV")) {
                Value = -1;
            } else {
                Value = Integer.parseInt(mySubString(s, s.indexOf(" ") + 1, s.length() - s.indexOf(" ") + 1));
            }
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }

        return Value;
    }

    public void get_deviceinfo(String ID) {
        String s;
        int Value=0;
        int r=0;
        int g=0;
        int b=0;

        try {
            s = SendReceiveTCPCommand("get_deviceinfo " + ID);

            if (s.contains("deviceinfo")) {
                //s = mySubString(s, 12, s.length() - 12); // "name:NAME, address:15, r:255, g:255, b:255, chancount:2, ch1:DIMMER, ch2:SHUTTER"
                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // NAME, address:15, r:255, g:255, b:255, chancount:2, ch1:DIMMER, ch2:SHUTTER"

                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // 15, r:255, g:255, b:255, chancount:2, ch1:DIMMER, ch2:SHUTTER"
                CurrentSetupDevice.Startaddress=Integer.parseInt(mySubString(s, 0, s.indexOf(",")));

                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // 255, g:255, b:255, chancount:2, ch1:DIMMER, ch2:SHUTTER"
                r=Integer.parseInt(mySubString(s, 0, s.indexOf(",")));

                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // 255, b:255, chancount:2, ch1:DIMMER, ch2:SHUTTER"
                g=Integer.parseInt(mySubString(s, 0, s.indexOf(",")));

                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // 255, chancount:2, ch1:DIMMER, ch2:SHUTTER"
                b=Integer.parseInt(mySubString(s, 0, s.indexOf(",")));

                CurrentSetupDevice.color=Color.rgb(r, g, b);

                s = mySubString(s, s.indexOf(":") + 1, s.length() - s.indexOf(":") - 1); // 2, ch1:DIMMER, ch2:SHUTTER"
                CurrentSetupDevice.ChanCount=Integer.parseInt(mySubString(s, 0, s.indexOf(",")));
            }
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }
    }

    public static void set_channel(String ID, String Channel, int StartValue, int EndValue, int Fadetime, int Delay) {
        SendTCPCommand("set_ch " + ID + " " + Channel + " " + Integer.toString(StartValue) + " " + Integer.toString(EndValue) + " " + Integer.toString(Fadetime) + " " + Integer.toString(Delay));
    }

    public static void set_pantilt(String ID, int PanStartValue, int PanEndValue, int TiltStartValue, int TiltEndValue, int Fadetime, int Delay) {
        SendTCPCommand("set_pt " + ID + " " + Integer.toString(PanStartValue) + " " + Integer.toString(PanEndValue) + " " + Integer.toString(TiltStartValue) + " " + Integer.toString(TiltEndValue) + " " + Integer.toString(Fadetime) + " " + Integer.toString(Delay));
    }

    public static void set_color(String ID, int R, int G, int B, int Fadetime, int Delay) {
        SendTCPCommand("set_color "+ID+" "+Integer.toString(R)+" "+Integer.toString(G)+" "+Integer.toString(B)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_shutter(String ID, boolean Open, int Delay) {
        String s;
        if (Open) {
            s="255";
        }else{
            s="0";
        }

        SendTCPCommand("set_shutter "+ID+" "+s+" "+Integer.toString(Delay));
    }

    public static void set_strobe(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_strobe "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_dimmer(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_dimmer "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_fog(int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_fog "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_highlight(String ID, int Value, int Fadetime) {
        SendTCPCommand("set_highlight "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime));
    }

    public static void set_devaddress(String ID, int Startaddress) {
        SendTCPCommand("set_devaddress "+ID+" "+Integer.toString(Startaddress));
    }

    public static void set_devcolor(String ID, int color) {
        SendTCPCommand("set_devcolor "+ID+" "+Integer.toString(Color.red(color))+" "+Integer.toString(Color.green(color))+" "+Integer.toString(Color.blue(color)));
    }

    public static void set_gobo1rot(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_gobo1rot "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_gobo2rot(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_gobo2rot "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_gobo1plus(String ID) {
        SendTCPCommand("set_gobo1+ "+ID);
    }
    public static void set_gobo1minus(String ID) {
        SendTCPCommand("set_gobo1- "+ID);
    }
    public static void set_gobo2plus(String ID) {
        SendTCPCommand("set_gobo2+ "+ID);
    }
    public static void set_gobo2minus(String ID) {
        SendTCPCommand("set_gobo2- "+ID);
    }

    public static void set_iris(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_iris "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }
    public static void set_prisma(String ID, boolean Enabled, int Delay) {
        String s;
        if (Enabled) {
            s="255";
        }else{
            s="0";
        }

        SendTCPCommand("set_prisma "+ID+" "+s+" "+Integer.toString(Delay));
    }

    public static void set_prismarot(String ID, int Value, int Fadetime, int Delay) {
        SendTCPCommand("set_prismarot "+ID+" "+Integer.toString(Value)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }
    public static void set_absolutechannel(int Channel, int StartValue, int EndValue, int Fadetime, int Delay) {
        SendTCPCommand("set_ach "+Integer.toString(Channel)+" "+Integer.toString(StartValue)+" "+Integer.toString(EndValue)+" "+Integer.toString(Fadetime)+" "+Integer.toString(Delay));
    }

    public static void set_datainchannel(int Channel, int Value) {
        SendTCPCommand("set_dch "+Integer.toString(Channel)+" "+Integer.toString(Value));
    }

    public void set_nodeset() {
        if (mPCD!=null) {
            if (mPCD.Nodesets != null) {
                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                    int ChangeRGB=-1;
                    int ChangeA=-1;
                    int ChangeW=-1;
                    int ChangeD=-1;

                    mPCD.Nodesets[CurrentNodeset].fadetime=Integer.parseInt(((EditText) findViewById(R.id.node_fadetimeedit)).getText().toString());

                    if (mPCD.Nodesets[CurrentNodeset].ChangeRGB) ChangeRGB=1;
                    if (mPCD.Nodesets[CurrentNodeset].ChangeA) ChangeA=1;
                    if (mPCD.Nodesets[CurrentNodeset].ChangeW) ChangeW=1;
                    if (mPCD.Nodesets[CurrentNodeset].ChangeDimmer) ChangeD=1;

                    SendTCPCommand("set_nodeset " + mPCD.Nodesets[CurrentNodeset].ID + " " + Integer.toString(mPCD.Nodesets[CurrentNodeset].stretching) + " " +
                            Integer.toString(mPCD.Nodesets[CurrentNodeset].contrast) + " " + Integer.toString(mPCD.Nodesets[CurrentNodeset].fadetime) + " " + ChangeRGB + " " + ChangeA + " " + ChangeW + " " + ChangeD);
                }
            }
        }
    }

    public static void set_node() {
        if (mPCD!=null) {
            if (mPCD.Nodesets != null) {
                if ((mPCD.Nodesets.length > 0) && (CurrentNodeset < mPCD.Nodesets.length)) {
                    if (mPCD.Nodesets[Main.CurrentNodeset].Nodes != null) {
                        if ((mPCD.Nodesets[Main.CurrentNodeset].Nodes.length > 0) && (CurrentNode < mPCD.Nodesets[CurrentNodeset].Nodes.length)) {
                            int R, G, B, A, W, D;

                            if (mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseRGB) {
                                R = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].R;
                                G = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].G;
                                B = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].B;
                            } else {
                                R = -1;
                                G = -1;
                                B = -1;
                            }
                            if (mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseA) {
                                A = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].A;
                            } else {
                                A = -1;
                            }
                            if (mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseW) {
                                W = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].W;
                            } else {
                                W = -1;
                            }
                            if (mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].UseDimmer) {
                                D = mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].Dimmer;
                            } else {
                                D = -1;
                            }

                            SendTCPCommand("set_node " + mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].ID + " " +
                                    mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].X + " " + mPCD.Nodesets[CurrentNodeset].Nodes[CurrentNode].Y + " " +
                                    Integer.toString(R) + " " + Integer.toString(G) + " " + Integer.toString(B) + " " + Integer.toString(A) + " " + Integer.toString(W) + " " + Integer.toString(D));
                        }
                    }
                }
            }
        }
    }

    public static void start_scene(String ID) {
        SendTCPCommand("start_sc "+ID);
    }

    public static void stop_scene(String ID) {
        SendTCPCommand("stop_sc "+ID);
    }

    public static void click_button(int X, int Y) {
        SendTCPCommand("click_btn "+Integer.toString(X)+" "+Integer.toString(Y));
    }

    public static void run_command(String ID, int int1, int int2, String string1, String string2, String ID1, String ID2, int Value) {
        if (ID1.equals("")) {
            ID1="{00000000-0000-0000-0000-000000000000}";
        }
        if (ID2.equals("")) {
            ID2="{00000000-0000-0000-0000-000000000000}";
        }

        SendTCPCommand("run_cmd "+ID+" "+Integer.toString(int1)+" "+Integer.toString(int2)+" "+string1+" "+string2+" "+ID1+" "+ID2+" "+Integer.toString(Value));
    }

    private void create_scenelist_grouplist() {
        scenelist_grouplist = new ArrayList<>();

        PCD_Scene simplescene = new PCD_Scene();
        simplescene.ID = "1";
        simplescene.Name = getString(R.string.str_einfacheszenen);
        scenelist_grouplist.add(simplescene);

        PCD_Scene devicescenes = new PCD_Scene();
        devicescenes.ID = "2";
        devicescenes.Name = getString(R.string.str_geräteszenen);
        scenelist_grouplist.add(devicescenes);

        PCD_Scene audioscenes = new PCD_Scene();
        audioscenes.ID = "3";
        audioscenes.Name = getString(R.string.str_audioszenen);
        scenelist_grouplist.add(audioscenes);

        PCD_Scene movingscenes = new PCD_Scene();
        movingscenes.ID = "4";
        movingscenes.Name = getString(R.string.str_bewegungsszenen);
        scenelist_grouplist.add(movingscenes);

        PCD_Scene commands = new PCD_Scene();
        commands.ID = "5";
        commands.Name = getString(R.string.str_befehle);
        scenelist_grouplist.add(commands);

        PCD_Scene combinationscenes = new PCD_Scene();
        combinationscenes.ID = "6";
        combinationscenes.Name = getString(R.string.str_kombinationsszenen);
        scenelist_grouplist.add(combinationscenes);

        PCD_Scene presets = new PCD_Scene();
        presets.ID = "7";
        presets.Name = getString(R.string.str_presets);
        scenelist_grouplist.add(presets);

        PCD_Scene automaticscenes = new PCD_Scene();
        automaticscenes.ID = "8";
        automaticscenes.Name = getString(R.string.str_automatikszenen);
        scenelist_grouplist.add(automaticscenes);

        PCD_Scene effects = new PCD_Scene();
        effects.ID = "9";
        effects.Name = getString(R.string.str_effekte);
        scenelist_grouplist.add(effects);

        PCD_Scene mediacenterscenes = new PCD_Scene();
        mediacenterscenes.ID = "10";
        mediacenterscenes.Name = getString(R.string.str_mediacenterszenen);
        scenelist_grouplist.add(mediacenterscenes);

        PCD_Scene presetscenes = new PCD_Scene();
        presetscenes.ID = "11";
        presetscenes.Name = getString(R.string.str_presetszenen);
        scenelist_grouplist.add(presetscenes);

        PCD_Scene pluginscenes = new PCD_Scene();
        pluginscenes.ID = "12";
        pluginscenes.Name = getString(R.string.str_pluginszenen);
        scenelist_grouplist.add(pluginscenes);
    }

    private void load_scene_child(PCD_Scene[] sceneElements) {
        scenelist_childlist = new ArrayList<>();
        if (sceneElements==null) return; // dont try to insert scenes to childlist if there are no scenes!

        Collections.addAll(scenelist_childlist, sceneElements);
    }

    private void set_sceneGroupIndicatorToRight() {
        /* Get the screen width */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        scenelistview.setIndicatorBounds(width - getDipsFromPixel(35), width
                - getDipsFromPixel(5));
    }

    // Convert pixel to dip
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    public static GradientDrawable NewGradient(GradientColor gc) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{gc.getStartColor(), gc.getEndColor()});
        gd.setCornerRadius(0f);
        gd.setStroke(2, gc.getStartColor());
        gd.setCornerRadius(10f);
        return gd;
    }

    public void UpdateChanneloverview() {
        int StartChannel = ((SeekBar) findViewById(R.id.channelslider)).getProgress();
        QueryChannelvalues(StartChannel + 1, 8);

        ((TextView) findViewById(R.id.ch1lbl)).setText("Ch"+Integer.toString(StartChannel+1));
        ((TextView) findViewById(R.id.ch2lbl)).setText("Ch"+Integer.toString(StartChannel+2));
        ((TextView) findViewById(R.id.ch3lbl)).setText("Ch"+Integer.toString(StartChannel+3));
        ((TextView) findViewById(R.id.ch4lbl)).setText("Ch"+Integer.toString(StartChannel+4));
        ((TextView) findViewById(R.id.ch5lbl)).setText("Ch"+Integer.toString(StartChannel+5));
        ((TextView) findViewById(R.id.ch6lbl)).setText("Ch"+Integer.toString(StartChannel+6));
        ((TextView) findViewById(R.id.ch7lbl)).setText("Ch"+Integer.toString(StartChannel+7));
        ((TextView) findViewById(R.id.ch8lbl)).setText("Ch"+Integer.toString(StartChannel+8));

        ((SeekBar) findViewById(R.id.ch1slider)).setProgress(Channelvalues[StartChannel]);
        ((SeekBar) findViewById(R.id.ch2slider)).setProgress(Channelvalues[StartChannel+1]);
        ((SeekBar) findViewById(R.id.ch3slider)).setProgress(Channelvalues[StartChannel+2]);
        ((SeekBar) findViewById(R.id.ch4slider)).setProgress(Channelvalues[StartChannel+3]);
        ((SeekBar) findViewById(R.id.ch5slider)).setProgress(Channelvalues[StartChannel+4]);
        ((SeekBar) findViewById(R.id.ch6slider)).setProgress(Channelvalues[StartChannel+5]);
        ((SeekBar) findViewById(R.id.ch7slider)).setProgress(Channelvalues[StartChannel+6]);
        ((SeekBar) findViewById(R.id.ch8slider)).setProgress(Channelvalues[StartChannel+7]);

        ((TextView) findViewById(R.id.ch1valuelbl)).setText(Integer.toString(Channelvalues[StartChannel]));
        ((TextView) findViewById(R.id.ch2valuelbl)).setText(Integer.toString(Channelvalues[StartChannel+1]));
        ((TextView) findViewById(R.id.ch3valuelbl)).setText(Integer.toString(Channelvalues[StartChannel+2]));
        ((TextView) findViewById(R.id.ch4valuelbl)).setText(Integer.toString(Channelvalues[StartChannel+3]));
        ((TextView) findViewById(R.id.ch5valuelbl)).setText(Integer.toString(Channelvalues[StartChannel+4]));
        ((TextView) findViewById(R.id.ch6valuelbl)).setText(Integer.toString(Channelvalues[StartChannel + 5]));
        ((TextView) findViewById(R.id.ch7valuelbl)).setText(Integer.toString(Channelvalues[StartChannel + 6]));
        ((TextView) findViewById(R.id.ch8valuelbl)).setText(Integer.toString(Channelvalues[StartChannel + 7]));
    }

    private Runnable myTimerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (fragment_setup_isvisible) {
                    if (NetworkConnectionOK) {
                        ((ImageView) findViewById(R.id.networkstateimage)).setImageResource(R.drawable.ic_uplinkok);
                    } else {
                        ((ImageView) findViewById(R.id.networkstateimage)).setImageResource(R.drawable.ic_uplinksearching);
                    }
                }
            }catch(Exception e){
                if (BuildConfig.DEBUG) {
                    System.out.println(e.toString());
                }
            }

            if (DownloadStageview==3) {
                DownloadStageview=0;

                if (stageviewdownload!=null){
                    ImageView stageviewimage = (ImageView) findViewById(R.id.stageviewimage);

                    if (stageviewimage!=null) {
                        stageviewimage.setImageBitmap(stageviewdownload);
                        ViewGroup.LayoutParams params = findViewById(R.id.stageviewimage).getLayoutParams();
                        params.height = stageviewimage.getHeight();
                        findViewById(R.id.stageviewimage).requestLayout();
                    }
                }
            }

            if (!NetworkCommandStringQueue.equals("")) {
                if (NetworkThreadAlive) {
                    NetworkCommandString = NetworkCommandStringQueue;
                    NetworkCommandStatus = 1;
                    NetworkCommandStringQueue="";
                }
            }

            if (RefreshAddressEdit) {
                RefreshAddressEdit=false;
                EditText stagesetup_newaddressedit = (EditText) findViewById(R.id.stagesetup_newaddressedit);
                if (stagesetup_newaddressedit!=null) {
                    stagesetup_newaddressedit.setText(Integer.toString(Main.CurrentSetupDevice.Startaddress));
                }
            }

            // call this runnable again in 100ms
            handler.postDelayed(this, 100);
        }
    };

    public void FindPresets() {
        File[] paths;
        File f = getFilesDir();
        paths=f.listFiles();

        int FileCount=0;
        for (File path : paths) {
            if (!(path.toString().substring(path.toString().lastIndexOf("/") + 1).equals("Settings"))) {
                FileCount++;
            }
        }

        AvailablePresetNames = null;
        AvailablePresetNames = new String[FileCount];
        int FileCount2=0;
        for (File path : paths) {
            if (!(path.toString().substring(path.toString().lastIndexOf("/") + 1).equals("Settings"))) {
                AvailablePresetNames[FileCount2] = path.toString().substring(path.toString().lastIndexOf("/") + 1);
                FileCount2++;
            }
        }

/*
        AvailablePresetNames = null;
        AvailablePresetNames = new String[paths.length];
        for(int i=0; i<paths.length; i++) {
            AvailablePresetNames[i] = paths[i].toString().substring(paths[i].toString().lastIndexOf("/") + 1);
        }
*/

        try {
            Spinner presetbox = (Spinner) findViewById(R.id.presetbox);
            ArrayAdapter<String> presetAdapter = new ArrayAdapter<>(this, R.layout.devicelist_child_item, AvailablePresetNames);
            presetAdapter.setDropDownViewResource(R.layout.devicelist_child_item);
            presetbox.setAdapter(presetAdapter);
            presetbox.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (!firstcall_presetbox) {
                        // Load the chosen preset
                        if (!CurrentPresetName.equals(AvailablePresetNames[pos])) {
                            CurrentPresetName = AvailablePresetNames[pos];
                            LoadPreset();

                            // Refresh controlpanel-fragment
                            ControlpanelCallbackToMain(R.layout.fragment_controlpanel);
                        }
                    }
                    firstcall_presetbox = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }catch(Exception e){
            if (BuildConfig.DEBUG) {
                System.out.println(e.toString());
            }
        }
    }

    public void SavePreset() {
        // get inputdialog.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.inputdialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                CurrentPresetName = userInput.getText().toString();

                                try {
                                    // Save file
                                    FileOutputStream fileos = openFileOutput(CurrentPresetName, Context.MODE_PRIVATE);
                                    ObjectOutputStream outputWriter = new ObjectOutputStream(fileos);

                                    int i;
                                    int j;

                                    if (mPCD.Devices != null) {
                                        outputWriter.writeInt(mPCD.Devices.length);
                                        for (i = 0; i < mPCD.Devices.length; i++) {
                                            outputWriter.writeObject(mPCD.Devices[i].ID);
                                            outputWriter.writeObject(mPCD.Devices[i].Name);
                                        }
                                    } else {
                                        outputWriter.writeInt(0);
                                    }

                                    if (mPCD.Groups != null) {
                                        outputWriter.writeInt(mPCD.Groups.length);
                                        for (i = 0; i < mPCD.Groups.length; i++) {
                                            outputWriter.writeObject(mPCD.Groups[i].ID);
                                            outputWriter.writeObject(mPCD.Groups[i].Name);
                                        }
                                    } else {
                                        outputWriter.writeInt(0);
                                    }

                                    if (mPCD.Scenes != null) {
                                        for (i = 0; i < mPCD.Scenes.length; i++) {
                                            if (mPCD.Scenes[i] != null) {
                                                outputWriter.writeInt(mPCD.Scenes[i].length);
                                                for (j = 0; j < mPCD.Scenes[i].length; j++) {
                                                    outputWriter.writeObject(mPCD.Scenes[i][j].ID);
                                                    outputWriter.writeObject(mPCD.Scenes[i][j].Name);
                                                }
                                            } else {
                                                outputWriter.writeInt(0);
                                            }
                                        }
                                    } else {
                                        outputWriter.writeInt(0);
                                    }

                                    if (mPCD.Nodesets != null) {
                                        outputWriter.writeInt(mPCD.Nodesets.length);
                                        for (i = 0; i < mPCD.Nodesets.length; i++) {
                                            outputWriter.writeObject(mPCD.Nodesets[i].ID);
                                            outputWriter.writeObject(mPCD.Nodesets[i].Name);
                                            outputWriter.writeInt(mPCD.Nodesets[i].stretching);
                                            outputWriter.writeInt(mPCD.Nodesets[i].contrast);
                                            outputWriter.writeInt(mPCD.Nodesets[i].fadetime);
                                            outputWriter.writeBoolean(mPCD.Nodesets[i].ChangeRGB);
                                            outputWriter.writeBoolean(mPCD.Nodesets[i].ChangeA);
                                            outputWriter.writeBoolean(mPCD.Nodesets[i].ChangeW);
                                            outputWriter.writeBoolean(mPCD.Nodesets[i].ChangeDimmer);

                                            if (mPCD.Nodesets[i].Nodes != null) {
                                                outputWriter.writeInt(mPCD.Nodesets[i].Nodes.length);
                                                for (j = 0; j < mPCD.Nodesets[i].Nodes.length; j++) {
                                                    outputWriter.writeObject(mPCD.Nodesets[i].Nodes[j].ID);
                                                    outputWriter.writeObject(mPCD.Nodesets[i].Nodes[j].Name);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].X);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].Y);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].R);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].G);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].B);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].A);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].W);
                                                    outputWriter.writeInt(mPCD.Nodesets[i].Nodes[j].Dimmer);
                                                    outputWriter.writeBoolean(mPCD.Nodesets[i].Nodes[j].UseRGB);
                                                    outputWriter.writeBoolean(mPCD.Nodesets[i].Nodes[j].UseA);
                                                    outputWriter.writeBoolean(mPCD.Nodesets[i].Nodes[j].UseW);
                                                    outputWriter.writeBoolean(mPCD.Nodesets[i].Nodes[j].UseDimmer);
                                                }
                                            } else {
                                                outputWriter.writeInt(0);
                                            }
                                        }
                                    } else {
                                        outputWriter.writeInt(0);
                                    }

                                    if (mPCD.ControlpanelButtons != null) {
                                        outputWriter.writeInt(mPCD.ControlpanelButtons.length);
                                        for (i = 0; i < mPCD.ControlpanelButtons.length; i++) {
                                            if (mPCD.ControlpanelButtons[i] != null) {
                                                outputWriter.writeInt(mPCD.ControlpanelButtons[i].length);
                                                for (j = 0; j < mPCD.ControlpanelButtons[i].length; j++) {
                                                    outputWriter.writeObject(mPCD.ControlpanelButtons[i][j].ID);
                                                    outputWriter.writeObject(mPCD.ControlpanelButtons[i][j].Name);
                                                    outputWriter.writeObject(mPCD.ControlpanelButtons[i][j].Type);
                                                    outputWriter.writeInt(mPCD.ControlpanelButtons[i][j].R);
                                                    outputWriter.writeInt(mPCD.ControlpanelButtons[i][j].G);
                                                    outputWriter.writeInt(mPCD.ControlpanelButtons[i][j].B);
                                                    outputWriter.writeInt(mPCD.ControlpanelButtons[i][j].X);
                                                    outputWriter.writeInt(mPCD.ControlpanelButtons[i][j].Y);
                                                }
                                            } else {
                                                outputWriter.writeInt(0);
                                            }
                                        }
                                    } else {
                                        outputWriter.writeInt(0);
                                    }

                                    outputWriter.flush();
                                    outputWriter.close();
                                    fileos.flush();
                                    fileos.close();

                                    Toast.makeText(getBaseContext(), R.string.str_presetsaved, Toast.LENGTH_SHORT).show();

                                    // Presetbox aktualisieren
                                    FindPresets();
                                } catch (IOException e) {
                                    System.out.println(e.toString());
                                }
                            }
                        })
                .setNegativeButton(R.string.str_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void LoadPreset() {
        try {
            // Connect, if Network-Thread is not alive
            if (!NetworkThreadAlive) {
                SetupCallbackToMain(R.id.connectBtn);
            }

            // Read file
            FileInputStream fileis = openFileInput(CurrentPresetName);
            ObjectInputStream InputRead= new ObjectInputStream(fileis);

            int i;
            int j;
            int mylength;

            mPCD = null;
            mPCD = new PCD();
            mPCD.Scenes = new PCD_Scene[12][]; // is always 12 because of 12 types of scenes

            mylength=InputRead.readInt();
            if (mylength > 0) {
                mPCD.Devices = new PCD_Device[mylength];
                DeviceNames = new String[mylength];
                for (i = 0; i < mPCD.Devices.length; i++) {
                    mPCD.Devices[i] = new PCD_Device();
                    mPCD.Devices[i].ID = (String) InputRead.readObject();
                    mPCD.Devices[i].Name = (String) InputRead.readObject();
                    DeviceNames[i] = mPCD.Devices[i].Name;
                }
            }

            mylength=InputRead.readInt();
            if (mylength > 0) {
                mPCD.Groups = new PCD_Group[mylength];
                GroupNames = new String[mylength];
                for (i = 0; i < mPCD.Groups.length; i++) {
                    mPCD.Groups[i] = new PCD_Group();
                    mPCD.Groups[i].ID = (String) InputRead.readObject();
                    mPCD.Groups[i].Name = (String) InputRead.readObject();
                    GroupNames[i] = mPCD.Groups[i].Name;
                }
            }

            // mPCD.Scenes is always !=null
            for (i = 0; i < mPCD.Scenes.length; i++) {
                mylength = InputRead.readInt();
                if (mylength > 0) {
                    mPCD.Scenes[i] = new PCD_Scene[mylength];
                    for (j = 0; j < mPCD.Scenes[i].length; j++) {
                        mPCD.Scenes[i][j] = new PCD_Scene();
                        mPCD.Scenes[i][j].ID = (String) InputRead.readObject();
                        mPCD.Scenes[i][j].Name = (String) InputRead.readObject();
                    }
                }
            }

            mylength=InputRead.readInt();
            if (mylength > 0) {
                mPCD.Nodesets = new PCD_Nodeset[mylength];
                NodesetNames = new String[mylength];
                for (i = 0; i < mPCD.Nodesets.length; i++) {
                    mPCD.Nodesets[i] = new PCD_Nodeset();
                    mPCD.Nodesets[i].ID = (String) InputRead.readObject();
                    mPCD.Nodesets[i].Name = (String) InputRead.readObject();
                    NodesetNames[i] = mPCD.Nodesets[i].Name;

                    mPCD.Nodesets[i].stretching = InputRead.readInt();
                    mPCD.Nodesets[i].contrast = InputRead.readInt();
                    mPCD.Nodesets[i].fadetime = InputRead.readInt();
                    mPCD.Nodesets[i].ChangeRGB = InputRead.readBoolean();
                    mPCD.Nodesets[i].ChangeA = InputRead.readBoolean();
                    mPCD.Nodesets[i].ChangeW = InputRead.readBoolean();
                    mPCD.Nodesets[i].ChangeDimmer = InputRead.readBoolean();

                    mylength=InputRead.readInt();
                    if (mylength > 0) {
                        mPCD.Nodesets[i].Nodes = new PCD_Node[mylength];
                        for (j = 0; j < mPCD.Nodesets[i].Nodes.length; j++) {
                            mPCD.Nodesets[i].Nodes[j] = new PCD_Node();
                            mPCD.Nodesets[i].Nodes[j].ID = (String) InputRead.readObject();
                            mPCD.Nodesets[i].Nodes[j].Name = (String) InputRead.readObject();
                            mPCD.Nodesets[i].Nodes[j].X = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].Y = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].R = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].G = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].B = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].A = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].W = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].Dimmer = InputRead.readInt();
                            mPCD.Nodesets[i].Nodes[j].UseRGB = InputRead.readBoolean();
                            mPCD.Nodesets[i].Nodes[j].UseA = InputRead.readBoolean();
                            mPCD.Nodesets[i].Nodes[j].UseW = InputRead.readBoolean();
                            mPCD.Nodesets[i].Nodes[j].UseDimmer = InputRead.readBoolean();
                        }
                    }
                }
            }

            mylength = InputRead.readInt();
            if (mylength > 0) {
                mPCD.ControlpanelButtons = new PCD_ControlpanelButton[mylength][];
                for (i = 0; i < mPCD.ControlpanelButtons.length; i++) {
                    mylength=InputRead.readInt();
                    if (mylength > 0) {
                        mPCD.ControlpanelButtons[i] = new PCD_ControlpanelButton[mylength];
                        for (j = 0; j < mPCD.ControlpanelButtons[i].length; j++) {
                            mPCD.ControlpanelButtons[i][j] = new PCD_ControlpanelButton();
                            mPCD.ControlpanelButtons[i][j].ID = (String) InputRead.readObject();
                            mPCD.ControlpanelButtons[i][j].Name = (String) InputRead.readObject();
                            mPCD.ControlpanelButtons[i][j].Type = (String) InputRead.readObject();
                            mPCD.ControlpanelButtons[i][j].R = InputRead.readInt();
                            mPCD.ControlpanelButtons[i][j].G = InputRead.readInt();
                            mPCD.ControlpanelButtons[i][j].B = InputRead.readInt();
                            mPCD.ControlpanelButtons[i][j].X = InputRead.readInt();
                            mPCD.ControlpanelButtons[i][j].Y = InputRead.readInt();
                        }
                    }
                }
            }

            InputRead.close();
            fileis.close();

            Toast.makeText(getBaseContext(), R.string.str_presetloaded,Toast.LENGTH_SHORT).show();

        }catch(IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
            Toast.makeText(getBaseContext(), R.string.str_errorinpreset, Toast.LENGTH_SHORT).show();
        }
    }

    public void DeletePreset() {
        File file = new File(getFilesDir() + "/" + CurrentPresetName);
        if (file.delete()) {
            Toast.makeText(getBaseContext(), R.string.str_presetdeleted, Toast.LENGTH_SHORT).show();
        }

        // Presetbox aktualisieren
        FindPresets();
    }

    public void SaveSettings() {
        try {
            FileOutputStream fileos = openFileOutput("Settings", Context.MODE_PRIVATE);
            ObjectOutputStream outputWriter = new ObjectOutputStream(fileos);

            outputWriter.writeObject(((EditText) findViewById(R.id.ipAddressEdit)).getText().toString());
            outputWriter.writeObject(((EditText) findViewById(R.id.portEdit)).getText().toString());
            outputWriter.writeObject(((EditText) findViewById(R.id.fadetimeEdit)).getText().toString());
            outputWriter.writeObject(CurrentPresetName);

            outputWriter.close();
            fileos.close();

            Toast.makeText(getBaseContext(), R.string.str_settingssaved,Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            //System.out.println(e.toString());
            Toast.makeText(getBaseContext(), R.string.str_erroronsave,Toast.LENGTH_SHORT).show();
        }
    }

    public void LoadSettings() {
        try {
            FileInputStream fileis = openFileInput("Settings");
            ObjectInputStream InputRead= new ObjectInputStream(fileis);

            // set variables
            NetworkIPAddress=(String) InputRead.readObject();
            NetworkPort=Integer.parseInt((String) InputRead.readObject());
            GlobalFadetime=Integer.parseInt((String) InputRead.readObject());
            LastPresetName=(String) InputRead.readObject();

            if (BuildConfig.DEBUG) {
                System.out.println(NetworkIPAddress);
                System.out.println(NetworkPort);
                System.out.println(GlobalFadetime);
                System.out.println(LastPresetName);
            }

            // set views
            ((EditText) findViewById(R.id.ipAddressEdit)).setText(NetworkIPAddress);
            ((EditText) findViewById(R.id.portEdit)).setText(Integer.toString(NetworkPort));
            ((EditText) findViewById(R.id.fadetimeEdit)).setText(Integer.toString(GlobalFadetime));

            InputRead.close();
            fileis.close();

            //Toast.makeText(getBaseContext(), "Einstellungen geladen",Toast.LENGTH_SHORT).show(); // will be shown on every fragment-loading - so dont show it
        }catch(Exception e){
            //System.out.println(e.toString());
        }
    }

    public void ShowMessage(String msg) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(Main.this);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(R.string.str_pcdimmer);
        dlgAlert.setPositiveButton(R.string.str_ok2, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public static void SetAddressEdit() {
        RefreshAddressEdit=true;
    }
}
