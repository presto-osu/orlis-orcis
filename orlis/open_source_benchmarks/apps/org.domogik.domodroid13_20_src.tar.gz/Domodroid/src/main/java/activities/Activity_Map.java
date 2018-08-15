package activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.domogik.domodroid13.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import Dialog.Dialog_Map_Help;
import Dialog.Dialog_Map_Move;
import Entity.Entity_Feature;
import activities.Sliding_Drawer.OnPanelListener;
import database.WidgetUpdate;
import map.MapView;
import misc.CopyFile;
import misc.tracerengine;

public class Activity_Map extends AppCompatActivity implements OnPanelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Sliding_Drawer panel;
    private Sliding_Drawer topPanel;
    public static Dialog dialog_feature;
    private Entity_Feature[] listFeature;
    private HashMap<String, String> map;

    private Vector<String> list_usable_files;
    private MapView mapView;
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences params;
    private ViewGroup panel_widget;

    private ListView listeMap;
    private ArrayList<HashMap<String, String>> listItem;
    private Animation animation1;
    private Animation animation2;

    private WidgetUpdate widgetUpdate;
    private static Handler sbanim;
    private String[] files = null;
    private File destFile = null;
    private String extension;
    private String fileName;
    private tracerengine Tracer = null;
    private String owner = "Map";
    private Boolean dont_freeze = false;
    private final String mytag = this.getClass().getName();
    private Menu mainMenu;

    private Toolbar toolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int mSelectedId;

    private static final int PICK_IMAGE = 1;

    /*
     * WARNING : this class does'nt access anymore directly the database
     * 		It must use methods located into WidgetUpdate engine
     * 		which is permanently connected to local database
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        params = PreferenceManager.getDefaultSharedPreferences(this);
        com.orhanobut.logger.Logger.init(mytag).methodCount(0);

        //window manager to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Tracer = tracerengine.getInstance(params, this);
        prefEditor = params.edit();
        mapView = new MapView(Tracer, this, params);
        mapView.setParams(params);
        mapView.setUpdate(params.getInt("UPDATE_TIMER", 300));
        setContentView(R.layout.activity_map);
        ViewGroup parent = (ViewGroup) findViewById(R.id.map_container);

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this sets the button visible
            getSupportActionBar().setHomeButtonEnabled(true); // makes it clickable
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);// set your own icon
        }


        initView();

        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //disable longtouch and longclic event in map view to add a widget
                mapView.handler_longclic.removeCallbacks(mapView.mLongPressed);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);// set your own icon
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);// set your own icon
                drawerToggle.syncState();
            }
        };
        mDrawerLayout.setDrawerListener(drawerToggle);

        animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(500);
        animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(500);

        //read files from SDCARD + create directory

        createDirIfNotExists();
        File f = new File(Environment.getExternalStorageDirectory() + "/domodroid/");
        if (f.isDirectory()) {
            files = f.list();
            //Reorder method
            List<String> words = new ArrayList<>();
            Collections.addAll(words, files);
            Collections.sort(words);
            files = words.toArray(new String[words.size()]);

        }

        build_maps_list();

        //sliding drawer
        topPanel = panel = (Sliding_Drawer) findViewById(R.id.map_slidingdrawer);
        panel.setOnPanelListener(this);
        panel.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mapView.setTopDrawer(topPanel);

        panel_widget = (ViewGroup) findViewById(R.id.panelWidget);
        mapView.setPanel_widget(panel_widget);

        dialog_feature = new Dialog(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Add_widget_title);

        startCacheEngine();        //Get reference to WidgetUpdate engine
        //When back, the engine should be ready.... (mini widgets and widgets require it to connect !)

        try {
            listFeature = widgetUpdate.requestFeatures();
        } catch (Exception e) {
            Tracer.e(mytag,e.toString());
        }

        //listview feature
        ListView listview_feature = new ListView(this);
        ArrayList<HashMap<String, String>> listItem1 = new ArrayList<>();
        if (listFeature != null) {
            int size = listFeature.length;
            Entity_Feature feature;
            for (Entity_Feature aListFeature : listFeature) {
                feature = aListFeature;
                if (feature != null) {
                    map = new HashMap<>();
                    map.put("name", feature.getName());
                    if (feature.getParameters().contains("command")) {
                        map.put("type", getString(R.string.command) + "-" + feature.getValue_type());
                    } else {
                        map.put("type", feature.getValue_type());
                    }
                    try {
                        map.put("state_key", getResources().getString(Graphics_Manager.getStringIdentifier(getApplicationContext(), feature.getState_key().toLowerCase())));
                    } catch (Exception e) {
                        Tracer.d(mytag, "no translation for: " + feature.getState_key());
                        map.put("state_key", feature.getState_key());
                    }
                    map.put("icon", Integer.toString(feature.getRessources()));
                    listItem1.add(map);
                }
            }
        }
        int i;
        if (list_usable_files != null) {
            for (i = 0; i < list_usable_files.size(); i++) {
                map = new HashMap<>();
                map.put("name", getText(R.string.go_to_Map).toString());
                map.put("type", "");
                map.put("state_key", list_usable_files.elementAt(i));
                map.put("icon", Integer.toString(R.drawable.map_next));
                listItem1.add(map);
            }
        }
        if ((listItem1 != null) && (listItem1.size() > 0)) {
            SimpleAdapter adapter_feature = new SimpleAdapter(getBaseContext(), listItem1,
                    R.layout.item_feature_list_add_feature_map, new String[]{"name", "type", "state_key", "icon"}, new int[]{R.id.name, R.id.description, R.id.state_key, R.id.icon});
            listview_feature.setAdapter(adapter_feature);
            listview_feature.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < listFeature.length) {
                        // It's a feature element
                        mapView.map_id = -1;
                        mapView.temp_id = listFeature[position].getId();
                    } else {
                        //It's a map switch element
                        mapView.temp_id = -1;
                        mapView.map_id = (position - listFeature.length) + 99999;
                        Tracer.v(mytag, "map_id = <" + mapView.map_id + "> , map selected <" + list_usable_files.elementAt(mapView.map_id - 99999) + ">");
                    }
                    mapView.setAddMode(true);
                    dialog_feature.dismiss();
                }
            });
        }

        builder.setView(listview_feature);
        dialog_feature = builder.create();

        if (!list_usable_files.isEmpty()) {
            mapView.initMap();
            //mapView.updateTimer();
            parent.addView(mapView);
        } else {
            Dialog_Map_Help dialog_help = new Dialog_Map_Help(this);
            dialog_help.show();
        }
        //update thread
        sbanim = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                if(msg.what==0){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
				}else if(msg.what==1){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
				}else if(msg.what==2){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
				}else if(msg.what==3){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
				}
				 */
            }
        };

        try {
            mapView.drawWidgets();
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
    }

    private void startCacheEngine() {
        if (widgetUpdate == null) {
            Tracer.i(mytag, "Starting WidgetUpdate engine !");
            widgetUpdate = WidgetUpdate.getInstance();
            //Map is'nt the first caller, so init is'nt required (already done by View)
            widgetUpdate.set_handler(sbanim, 1);    //Put our main handler to cache engine (as Map)

            widgetUpdate.wakeup();
        }
        tracerengine.set_engine(widgetUpdate);
        Tracer.v(mytag, "WidgetUpdate engine connected !");
    }

    private void build_maps_list() {

        if (listeMap != null)
            listeMap = null;
        if (listItem != null)
            listItem = null;
        if (list_usable_files != null)
            list_usable_files = null;

        //list Map
        listeMap = (ListView) findViewById(R.id.listMap);
        listItem = new ArrayList<>();
        list_usable_files = new Vector<>();
        int i;
        for (i = 0; i < files.length; i++) {
            //#1968 don't list file without drawable extension or hidden
            if (!files[i].startsWith(".") && (files[i].toLowerCase().endsWith(".png") || files[i].toLowerCase()
                    .endsWith(".jpg") || files[i].toLowerCase().endsWith(".jpeg") || files[i].toLowerCase()
                    .endsWith(".svg"))) {
                try {
                    list_usable_files.add(files[i]);
                    map = new HashMap<>();
                    map.put("name", files[i].substring(0, files[i].lastIndexOf('.')));
                    map.put("position", String.valueOf(i));
                    map.put("icon", Integer.toString(R.drawable.ic_domain_white_24dp));
                    listItem.add(map);
                } catch (Exception badfileformat) {
                    Tracer.e(mytag, "Good extension but can't load file");
                }
            }
        }
        if (mapView != null)
            mapView.setFiles(list_usable_files);

        if ((Tracer != null) && (Tracer.Map_as_main)) {
            // Add possibility to invoke Main activity
            map = new HashMap<>();
            map.put("name", getText(R.string.go_Main).toString());
            map.put("position", String.valueOf(i));
            map.put("icon", Integer.toString(R.drawable.ic_arrow_back_white));
            listItem.add(map);
            i++;
        }
        //Add an element in map list to ADD a map
        map = new HashMap<>();
        map.put("name", getText(R.string.map_select_file).toString());
        map.put("position", String.valueOf(i));
        map.put("icon", Integer.toString(R.drawable.ic_add_to_photos_white_24dp));
        listItem.add(map);
        i++;

        SimpleAdapter adapter_map = new SimpleAdapter(getBaseContext(), listItem,
                R.layout.item_in_listview_navigation_drawer, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
        listeMap.setAdapter(adapter_map);
        listeMap.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tracer.v(mytag, "On click Map selected at Position = " + position);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                int last_map = list_usable_files.size() - 1;
                if ((position <= last_map) && (position > -1)) {
                    mapView.setCurrentFile(position);
                    mapView.initMap();
                } else {
                    //add the return to main screen menu entry if map start mode
                    if ((position == last_map + 1) && (Tracer.Map_as_main)) {
                        //Go to main screen...
                        Tracer.force_Main = true;    //Flag to allow widgets display, even if START_ON_MAP is set !
                        Intent mapI = new Intent(Activity_Map.this, Activity_Main.class);
                        Tracer.v(mytag, "Call to Main, run it now !");
                        startActivity(mapI);
                    }
                    //open the "ADD map"
                    if (((position == last_map + 1) && (!Tracer.Map_as_main)) || ((position == last_map + 2) && (Tracer.Map_as_main))) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                    }
                }
            }
        });
        listeMap.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Tracer.i(mytag, " longclic on a map");
                mDrawerLayout.closeDrawer(GravityCompat.START);
                //switch to select maps
                int last_map = list_usable_files.size() - 1;
                if ((position <= last_map) && (position > -1)) {
                    mapView.setCurrentFile(position);
                } else {
                    return false;
                }
                // On long click on a map ask if we want to remove this map we just selected
                // prepare an AlertDialog and display it
                final AlertDialog.Builder alert = new AlertDialog.Builder(Activity_Map.this);
                alert.setTitle(R.string.delete_map_title);
                alert.setMessage(R.string.delete_map__message);
                alert.setPositiveButton(R.string.delete_map__OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog_customname, int whichButton) {
                        // Don't forget to remove all widget before on this map.
                        mapView.clear_Widgets();
                        // Then remove the file
                        mapView.removefile();
                        Tracer.i(mytag, " User remove a map");
                        //Restart the activity to save change
                        restartactivity();
                    }
                });
                alert.setNegativeButton(R.string.delete_map__NO, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog_customname, int whichButton) {
                        Tracer.i(mytag, " User cancel remove a map");
                    }
                });
                alert.show();

                return false;
            }
        });

    }


    //Wait result of pickup image
    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
                Logger.e("onActivityResult");
                Uri _uri = data.getData();
                //User had pick an image.
                Cursor cursor = getContentResolver().query(_uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                Logger.e("cursormove");
                //Copy the select picture to Domodroid directory
                Uri uri = data.getData();
                Tracer.i(mytag, "Uri: " + uri.toString());
                File selectFile;
                if (cursor.getString(0) != null) {
                    Tracer.i(mytag, "Image from normal picker");
                    selectFile = new File(cursor.getString(0));
                } else {
                    Tracer.i(mytag, "Image from new picker with uri that may crash");
                    selectFile = new File(getDriveFileAbsolutePath(this, uri));
                }
                Tracer.d(mytag, "selectfile");
                fileName = selectFile.getName();
                Tracer.d(mytag, "filename");
                //filter for extension if not png or svg say it to user
                String filenameArray[] = fileName.split("\\.");
                Tracer.d(mytag, "split");
                //get file extension
                extension = filenameArray[filenameArray.length - 1];
                //put extension in lower case
                extension = extension.toLowerCase();
                if (extension.equals("png") || extension.equals("svg") || extension.equals("jpeg") || extension.equals("jpg")) {
                    // if jpg convert and save it to domodroid dir
                    if (extension.equals("jpeg") || extension.equals("jpg")) {
                        try {
                            //FileOutputStream out = new FileOutputStream(fileName);
                            fileName = fileName.substring(0, fileName.length() - extension.length() - 1) + ".png";
                            extension = "png";
                            File destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                            int i = 1;
                            while (destFile.exists()) {
                                destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                                fileName = "(" + i + ")" + fileName;
                                i++;
                            }
                            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                            Bitmap bmp = mapView.decodeFile(selectFile);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); //100-best quality
                            out.close();
                            bmp.recycle();
                            Tracer.i(mytag, "On activity result convert image to png !");
                        } catch (Exception e) {
                            Tracer.e(mytag, e.toString());
                        }
                        //else just copy svg or png to domodroid dir
                    } else {
                        File destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                        int i = 1;
                        while (destFile.exists()) {
                            destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + "(" + i + ")" + fileName);
                            i++;
                        }
                        CopyFile.copyDirectory(selectFile, destFile);
                    }
                    cursor.close();
                    Toast.makeText(this, R.string.map_add_file_ok, Toast.LENGTH_SHORT).show();
                    Tracer.i(mytag, "On activity result No error adding new file in map !");
                    //ask user if he want to rename the file before copy
                    AlertDialog.Builder rename = new AlertDialog.Builder(this);
                    rename.setTitle(R.string.Rename_file_title);
                    rename.setMessage(R.string.Rename_file_message);
                    // Set an EditText view to get user input
                    final EditText input = new EditText(Activity_Map.this);
                    input.setText(fileName.substring(0, fileName.length() - extension.length() - 1));
                    rename.setView(input);
                    rename.setPositiveButton(R.string.Rename_file_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog_customname, int whichButton) {
                            String renamefileName = input.getText().toString();
                            if (!renamefileName.equals(fileName.substring(0, fileName.length() - extension.length() - 1))) {
                                Tracer.i(mytag, "new fileName: " + renamefileName);
                                destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + renamefileName + "." + extension);
                                int i = 1;
                                while (destFile.exists()) {
                                    destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + "(" + i + ")" + renamefileName + "." + extension);
                                    i++;
                                }
                                new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName).renameTo(destFile);
                            }//Restart the activity to save change
                            restartactivity();
                        }
                    });
                    rename.setNegativeButton(R.string.Rename_file_NO, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog_customname, int whichButton) {
                            Tracer.i(mytag, "rename file Canceled.");
                            //Restart the activity to save change
                            restartactivity();
                        }
                    });
                    rename.show();
                    //just need to store the new name with his extension to "fileName"

                } else {
                    Toast.makeText(this, R.string.map_add_file_type_nok, Toast.LENGTH_LONG).show();
                    Tracer.d(mytag, "File type is not supported !");
                    return;
                }
            }
            super.onActivityResult(requestCode, resultCode, data);

        } catch (Exception e) {
            Tracer.e(mytag, "Error adding file in map !");
            Toast.makeText(this, R.string.map_add_file_nok, Toast.LENGTH_LONG).show();
            Tracer.e(mytag, e.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        panel.setOpen(false, false);
        if (Tracer != null) {
            Tracer.v(mytag, "onPause");
            if (Tracer.Map_as_main) {
                widgetUpdate.set_sleeping();    //We act as main screen : if going to pause, freeze cache engine
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (Tracer == null) {
            Tracer = tracerengine.getInstance(params, this);
        }
        Tracer.v(mytag, "Onresume Try to connect on cache engine !");

        if (widgetUpdate == null) {
            startCacheEngine();
        } else {
            widgetUpdate.wakeup();
            //build_maps_list();
            if (mapView != null) {
                System.gc();
                mapView.refreshMap();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Tracer != null)
            Tracer.v(mytag, "Ondestroy Leaving Map_Activity : disconnect from engines");

        if (widgetUpdate != null) {
            widgetUpdate.Disconnect(2);    //That'll purge all connected widgets for MapView
        }

        if (mapView != null)
            mapView.purge();
        mapView = null;

        if (Tracer != null) {
            Tracer.close();        //To eventually flush and close txt log file
            Tracer = null;        //Stop own Tracer engine
        }

        System.gc();
    }

    public void onStop() {
        System.gc();
        super.onStop();
        Tracer.v(mytag, "onStop");
        //onDestroy();
    }

    public void onPanelClosed(Sliding_Drawer panel) {
        if (Tracer != null)
            Tracer.v(mytag, "Onpanelclosepanel request to close");
        panel_widget.removeAllViews();
    }


    public void onPanelOpened(Sliding_Drawer panel) {
        //todo disable menu if set in option
        if (!params.getBoolean("map_menu_disable", false)) {
            if (Tracer != null)
                Tracer.v(mytag, "onPanelOpened panel request to be displayed");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.map_menu_exit).setVisible(params.getBoolean("START_ON_MAP", false));
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            Tracer.d(mytag, "clic on drawertoggle");
            return true;
        }
        //normal menu call.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.map_menu_add:
                //Add a widget
                panel.setOpen(false, true);
                if (list_usable_files.isEmpty()) {
                    Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
                } else {
                    //show list of feature available
                    dialog_feature.show();
                    mapView.setRemoveMode(false);
                    mapView.setMoveMode(false);
                }
                return true;
            case R.id.map_menu_move:
                //case when user want to move one widget
                // first step remove, second add the removed widget
                if (list_usable_files.isEmpty()) {
                    Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
                } else {
                    //Show the move dialog box to help user
                    Dialog_Map_Move dialog_move = new Dialog_Map_Move(this);
                    dialog_move.show();
                    if (!mapView.isMoveMode()) {
                        //if remove mode is select for the first time
                        //say Mapview.java to turn on remove mode
                        mapView.setRemoveMode(false);
                        mapView.setMoveMode(true);
                    } else {
                        //Remove mode was active, return to normal mode
                        //Turn menu text color back
                        mapView.setRemoveMode(false);
                    }
                    panel.setOpen(false, true);
                }
                return true;
            case R.id.map_menu_del:
                //case when user want to remove only one widget
                if (list_usable_files.isEmpty()) {
                    Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
                } else {
                    if (!mapView.isRemoveMode()) {
                        //if remove mode is select for the first time
                        //say Mapview.java to turn on remove mode
                        mapView.setMoveMode(false);
                        mapView.setRemoveMode(true);
                    } else {
                        //Remove mode was active, return to normal mode
                        //Turn menu text color back
                        mapView.setRemoveMode(false);
                    }
                    panel.setOpen(false, true);
                }
                return true;
            case R.id.map_menu_del_all:
                //case when user select remove all from menu
                if (list_usable_files.isEmpty()) {
                    Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
                } else {
                    panel.setOpen(false, true);
                    Tracer.i(mytag, "request to clear widgets");
                    mapView.clear_Widgets();
                    mapView.setRemoveMode(false);
                }
                return true;
            case R.id.map_menu_help:
                Dialog_Map_Help dialog_help = new Dialog_Map_Help(this);
                dialog_help.show();
                prefEditor.putBoolean("SPLASH", true);
                prefEditor.commit();
                return true;
            case R.id.map_menu_exit:
                Intent intent = new Intent(this, Activity_Main.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    //Physical button keycode 82 is menu button
    //Physical button keycode 4 is back button
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //todo disable menu if set in option
        Tracer.v(mytag, "onKeyUp keyCode = " + keyCode);
        if ((keyCode == 82 || keyCode == 4) && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }
        if ((keyCode == 82) && mainMenu != null) {
            mainMenu.performIdentifierAction(R.id.menu_overflow, 0);
        }
        return super.onKeyUp(keyCode, event);
    }

    private static void createDirIfNotExists() {
        File file = new File(Environment.getExternalStorageDirectory(), "/domodroid");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //this is called when the screen rotates.
        // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
        super.onConfigurationChanged(newConfig);
        System.gc();
        mapView.initMap();
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //save selected item so it will remains same even after orientation change
        outState.putInt("SELECTED_ID", mSelectedId);
    }

    private void initView() {
        mDrawer = (NavigationView) findViewById(R.id.map_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_map_layout);
    }

    private void restartactivity() {
        Intent intent = getIntent();
        System.gc();
        finish();
        startActivity(intent);
    }

    private static String getDriveFileAbsolutePath(Activity context, Uri uri) {
        if (uri == null) return null;
        ContentResolver resolver = context.getContentResolver();
        String filename = "";
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };
        ContentResolver cr = context.getApplicationContext().getContentResolver();
        Cursor metaCursor = cr.query(uri, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    filename = metaCursor.getString(0);
                    Logger.i("filename=" + filename);
                }
            } finally {
                metaCursor.close();
            }
        }
        FileInputStream input = null;
        FileOutputStream output = null;
        String outputFilePath = new File(context.getCacheDir(), filename).getAbsolutePath();
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(outputFilePath);
            int read = 0;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            System.gc();
            return new File(outputFilePath).getAbsolutePath();
        } catch (IOException ignored) {
            System.gc();// nothing we can do
        } finally {
            try {
                System.gc();
                input.close();
                output.close();
            } catch (IOException e) {
                System.gc();
                e.toString();
            }

        }
        return "";
    }
}
