package Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import org.domogik.domodroid13.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Abstract.common_method;
import Entity.Entity_Area;
import Entity.Entity_Feature;
import Entity.Entity_Icon;
import Entity.Entity_Room;
import activities.Graphics_Manager;
import database.Cache_management;
import database.DmdContentProvider;
import database.DomodroidDB;
import database.WidgetUpdate;
import misc.List_Icon_Adapter;
import misc.tracerengine;

public class Dialog_House extends Dialog implements OnClickListener {
    //private final Spinner spinner_area;
    //private final Spinner spinner_room;
    //private final Spinner spinner_feature;
    //private final Spinner spinner_icon;
    private final SharedPreferences params;
    private final Activity context;
    private tracerengine Tracer = null;
    private int area_id = 0;
    private int room_id = 0;
    private int feature_id = 0;
    private int lastid = 0;
    private String type = null;
    private String icon = null;
    private WidgetUpdate widgetUpdate;
    private Dialog dialog_feature;
    private final DomodroidDB domodb;
    private Entity_Area[] listArea;
    private Entity_Room[] listRoom;
    private Entity_Feature[] listFeature;
    private final String mytag = this.getClass().getName();
    private final SharedPreferences.Editor prefEditor;

    public Dialog_House(tracerengine Trac, SharedPreferences params, Activity context) {
        super(context);
        this.context = context;
        this.params = params;
        this.Tracer = Trac;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_house);
        prefEditor = this.params.edit();
        domodb = new DomodroidDB(Tracer, context, params);

        Button cancelButton = (Button) findViewById(R.id.house_Cancel);
        cancelButton.setTag("house_cancel");
        cancelButton.setOnClickListener(this);

        Button OKButton = (Button) findViewById(R.id.house_OK);
        OKButton.setTag("house_ok");
        OKButton.setOnClickListener(this);

        Button add_area_Button = (Button) findViewById(R.id.house_add_area);
        add_area_Button.setTag("add_area");
        add_area_Button.setOnClickListener(this);

        Button add_room_Button = (Button) findViewById(R.id.house_add_room);
        add_room_Button.setTag("add_room");
        add_room_Button.setOnClickListener(this);

        Button add_widget_Button = (Button) findViewById(R.id.house_add_widget);
        add_widget_Button.setTag("add_widget");
        add_widget_Button.setOnClickListener(this);

        //Button add_icon_Button = (Button) findViewById(R.id.house_add_icon);
        //add_icon_Button.setTag("add_icon");
        //add_icon_Button.setOnClickListener(this);

        //spinner_area = (Spinner) findViewById(R.id.spin_list_area);
        //spinner_room = (Spinner) findViewById(R.id.spin_list_room);
        //spinner_feature = (Spinner) findViewById(R.id.spin_list_feature);
        //spinner_icon = (Spinner) findViewById(R.id.spin_list_icon);
        // Loading spinner data from database
        loadSpinnerData();

    }

    public void onClick(final View v) {
        String tag = v.getTag().toString();

        //list area where to put room
        final AlertDialog.Builder list_area_choice = new AlertDialog.Builder(getContext());
        List<String> list_area = new ArrayList<>();
        List<String> list_area_icon = new ArrayList<>();
        for (Entity_Area area : listArea) {
            list_area.add(area.getName());
            list_area_icon.add(area.getIcon_name());
        }
        final CharSequence[] char_list_zone = list_area.toArray(new String[list_area.size()]);
        list_area_choice.setTitle(R.string.Wich_AREA_message);
        List_Icon_Adapter adapter = new List_Icon_Adapter(Tracer, getContext(), list_area, list_area_icon);
        list_area_choice.setAdapter(adapter, null);
        list_area_choice.setSingleChoiceItems(char_list_zone, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        area_id = listArea[item].getId();
                        dialog.dismiss();
                    }
                }
        );

        //list room where to put widget
        final AlertDialog.Builder list_room_choice = new AlertDialog.Builder(getContext());
        List<String> list_room = new ArrayList<>();
        List<String> list_room_icon = new ArrayList<>();
        for (Entity_Room room : listRoom) {
            list_room.add(room.getName());
            list_room_icon.add(room.getIcon_name());
        }
        final CharSequence[] char_list_room = list_room.toArray(new String[list_room.size()]);
        list_room_choice.setTitle(R.string.Wich_ROOM_message);
        List_Icon_Adapter adapter2 = new List_Icon_Adapter(Tracer, getContext(), list_room, list_room_icon);
        list_room_choice.setAdapter(adapter2, null);
        list_room_choice.setSingleChoiceItems(char_list_room, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        room_id = listRoom[item].getId();
                        dialog.dismiss();
                    }
                }
        );

        //list widget to put in room
        final AlertDialog.Builder list_feature_choice = new AlertDialog.Builder(getContext());
        List<String> list_feature = new ArrayList<>();
        List<String> list_feature_icon = new ArrayList<>();
        for (Entity_Feature feature : listFeature) {
            if (feature.getParameters().contains("command")) {
                try {
                    list_feature.add(feature.getName() + " " + context.getString(R.string.command) + "-" + context.getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), feature.getState_key().toLowerCase())));
                } catch (Exception e) {
                    Tracer.d(mytag, "no translation for: " + feature.getState_key());
                    list_feature.add(feature.getName() + " " + context.getString(R.string.command) + "-" + feature.getState_key());
                }
            } else {
                try {
                    list_feature.add(feature.getName() + " " + context.getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), feature.getState_key().toLowerCase())));
                } catch (Exception e) {
                    Tracer.d(mytag, "no translation for: " + feature.getState_key());
                    list_feature.add(feature.getName() + " " + feature.getState_key());
                }
            }
            list_feature_icon.add(feature.getIcon_name());
        }
        final CharSequence[] char_list_feature = list_feature.toArray(new String[list_feature.size()]);
        list_feature_choice.setTitle(R.string.Wich_feature_message);
        List_Icon_Adapter adapter1 = new List_Icon_Adapter(Tracer, getContext(), list_feature, list_feature_icon);
        list_feature_choice.setAdapter(adapter1, null);
        list_feature_choice.setSingleChoiceItems(char_list_feature, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        feature_id = listFeature[item].getId();
                        dialog.dismiss();
                    }
                }
        );
        //list icon from array R.array.icon_area_array
        final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
        List<String> list_icon = new ArrayList<>();
        String[] fiilliste;
        fiilliste = context.getResources().getStringArray(R.array.icon_area_array);
        Collections.addAll(list_icon, fiilliste);
        final CharSequence[] char_list_icon = list_icon.toArray(new String[list_icon.size()]);
        list_icon_choice.setTitle(R.string.Wich_ICON_message);
        List_Icon_Adapter adapter11 = new List_Icon_Adapter(Tracer, getContext(), fiilliste, fiilliste);
        list_icon_choice.setAdapter(adapter11, null);
        list_icon_choice.setSingleChoiceItems(char_list_icon, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                        icon = checkedItem.toString();
                        dialog.dismiss();
                    }
                }
        );
        //list type area,room, widget
        final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
        List<String> list_type = new ArrayList<>();
        List<String> list_type_icon = new ArrayList<>();
        if (!v.getTag().equals("add_icon")) {
            list_type.add(context.getString(R.string.place_root));
            list_type_icon.add("house");
        }
        list_type.add(context.getString(R.string.area));
        list_type_icon.add("area");
        list_type.add(context.getString(R.string.place_room));
        list_type_icon.add("map");
        if (!v.getTag().equals("add_widget")) {
            list_type.add(context.getString(R.string.place_widget));
            list_type_icon.add("usage");
        }

        final CharSequence[] char_list_type = list_type.toArray(new String[list_type.size()]);
        list_type_choice.setTitle(R.string.Wich_TYPE_message);
        List_Icon_Adapter adapter111 = new List_Icon_Adapter(Tracer, getContext(), list_type, list_type_icon);
        list_type_choice.setAdapter(adapter111, null);
        list_type_choice.setSingleChoiceItems(char_list_type, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                        type = checkedItem.toString();
                        if (v.getTag().equals("add_widget")) {
                            if (type.equals(context.getString(R.string.place_root)))
                                v.setTag("add_widget_root");
                            if (type.equals(context.getString(R.string.area)))
                                v.setTag("add_widget_area");
                            if (type.equals(context.getString(R.string.place_room)))
                                v.setTag("add_widget_room");
                        } else if (v.getTag().equals("add_icon")) {
                            if (type.equals(context.getString(R.string.area)))
                                v.setTag("add_icon_area");
                            if (type.equals(context.getString(R.string.place_room)))
                                v.setTag("add_icon_room");
                            if (type.equals(context.getString(R.string.place_widget)))
                                v.setTag("add_icon_widget");
                        }
                        dialog.dismiss();
                        Dialog_House.this.onClick(v);
                    }
                }
        );
        //ADD a room
        AlertDialog.Builder alert_Room = new AlertDialog.Builder(getContext());
        //set a title
        alert_Room.setTitle(R.string.New_ROOM_title);
        //set a message
        alert_Room.setMessage(R.string.New_ROOM_message);
        // Set an EditText view to get user input
        final EditText name = new EditText(getContext());
        alert_Room.setView(name);
        alert_Room.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                lastid = domodb.requestidlastRoom();
                ContentValues values = new ContentValues();
                values.put("area_id", (area_id));
                values.put("name", name.getText().toString());
                values.put("description", "");
                values.put("id", (lastid + 1));
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ROOM, values);
                //#76
                prefEditor.putString("ROOM_LIST", domodb.request_json_Room().toString());
                common_method.save_params_to_file(Tracer, prefEditor, mytag, getContext());
                loadSpinnerData();
            }
        });
        alert_Room.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {

            }
        });

        //ADD an area
        AlertDialog.Builder alert_Area = new AlertDialog.Builder(getContext());
        //set a title
        alert_Area.setTitle(R.string.New_AREA_title);
        //set a message
        alert_Area.setMessage(R.string.New_AREA_message);
        // Set an EditText view to get user input
        final EditText name1 = new EditText(getContext());
        alert_Area.setView(name1);
        alert_Area.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                ContentValues values = new ContentValues();
                values.put("name", name1.getText().toString());
                //put the next available id from db here
                int lastid = domodb.requestlastidArea();
                values.put("id", lastid + 1);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_AREA, values);
                //#76
                prefEditor.putString("AREA_LIST", domodb.request_json_Area().toString());
                common_method.save_params_to_file(Tracer, prefEditor, mytag, getContext());
                loadSpinnerData();
            }
        });
        alert_Area.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {

            }
        });

        //ADD a feature
        AlertDialog.Builder alert_Feature = new AlertDialog.Builder(getContext());
        //set a title
        alert_Feature.setTitle(R.string.Confirm_title);
        //set a message
        alert_Feature.setMessage(R.string.Confirm_message);
        // Set an EditText view to get user input
        //final EditText name2 = new EditText(getContext());
        //alert_Feature.setView(name2);
        alert_Feature.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                lastid = domodb.requestidlastFeature_association();
                ContentValues values = new ContentValues();
                if (type.equals(context.getString(R.string.place_root))) {
                    values.put("place_type", "root");
                    values.put("place_id", ("1"));
                }
                if (type.equals(context.getString(R.string.area))) {
                    values.put("place_type", "area");
                    values.put("place_id", (area_id));
                }
                if (type.equals(context.getString(R.string.place_room))) {
                    values.put("place_type", "room");
                    values.put("place_id", (room_id));
                }
                //device_feature_id must come from the selected  one in list
                values.put("device_feature_id", (feature_id));
                values.put("id", (lastid + 1));
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_ASSOCIATION, values);
                //#76
                prefEditor.putString("FEATURE_LIST_association", domodb.request_json_Features_association().toString());
                common_method.save_params_to_file(Tracer, prefEditor, mytag, getContext());
                //A device as been add re-check the cache URL
                Cache_management.checkcache(Tracer, context);
                loadSpinnerData();
            }
        });
        alert_Feature.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {

            }
        });

        //ADD an icon
        AlertDialog.Builder alert_Icon = new AlertDialog.Builder(getContext());
        //set a title
        alert_Icon.setTitle(R.string.Confirm_title);
        //set a message
        alert_Icon.setMessage(R.string.Confirm_message);
        // Set an EditText view to get user input
        //final EditText name2 = new EditText(getContext());
        //alert_Feature.setView(name2);
        alert_Icon.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                ContentValues values = new ContentValues();
                //type = area, room, feature
                //icon is the name of the icon wich will be select
                values.put("value", icon);
                //reference is the id of the area, room, or feature
                int reference = 0;
                if (type.equals(context.getString(R.string.area))) {
                    values.put("name", "area");
                    reference = area_id;
                }
                if (type.equals(context.getString(R.string.place_room))) {
                    values.put("name", "room");
                    reference = room_id;
                }
                if (type.equals(context.getString(R.string.place_widget))) {
                    values.put("name", "feature");
                    reference = feature_id;
                }
                values.put("reference", reference);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
                //#76
                prefEditor.putString("ICON_LIST", domodb.request_json_Icon().toString());
                common_method.save_params_to_file(Tracer, prefEditor, mytag, getContext());
                loadSpinnerData();
            }
        });
        alert_Icon.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {

            }
        });

        switch (tag) {
            case "house_cancel":
                dismiss();
                break;
            case "house_ok":
                SharedPreferences.Editor prefEditor = params.edit();
                try {
                    prefEditor = params.edit();
                    //To allow the area view we have to remove by usage option
                    prefEditor.putBoolean("BY_USAGE", false);
                    prefEditor.commit();

                } catch (Exception e) {
                    Tracer.e(mytag, e.toString());
                }

                prefEditor.commit();

                dismiss();
                break;
            case "add_area":
                alert_Area.show();
                break;
            case "add_room": {
                alert_Room.show();
                AlertDialog alert_list_area = list_area_choice.create();
                alert_list_area.show();
                break;
            }
            case "add_widget": {
                list_type_choice.show();
                AlertDialog alert_list_feature = list_feature_choice.create();
                alert_list_feature.show();
                break;
            }
            case "add_widget_root":
                alert_Feature.show();
                v.setTag("add_widget");
                break;
            case "add_widget_area": {
                alert_Feature.show();
                AlertDialog alert_list_area = list_area_choice.create();
                alert_list_area.show();
                v.setTag("add_widget");
                break;
            }
            case "add_widget_room": {
                alert_Feature.show();
                AlertDialog alert_list_room = list_room_choice.create();
                alert_list_room.show();
                v.setTag("add_widget");
                break;
            }
            case "add_icon":
                list_type_choice.show();
                AlertDialog alert_list_icon = list_icon_choice.create();
                alert_list_icon.show();
                //Ask user what icon i want to modify area, room, widget
                //in function display
                //display list of all icons
                //and change the tag for onclick() method
                break;
            case "add_icon_area": {
                alert_Icon.show();
                AlertDialog alert_list_area = list_area_choice.create();
                alert_list_area.show();
                v.setTag("add_icon");
                break;
            }
            case "add_icon_room": {
                alert_Icon.show();
                AlertDialog alert_list_room = list_room_choice.create();
                alert_list_room.show();
                v.setTag("add_icon");
                break;
            }
            case "add_icon_widget": {
                alert_Icon.show();
                AlertDialog alert_list_feature = list_feature_choice.create();
                alert_list_feature.show();
                v.setTag("add_icon");
                break;
            }
        }

    }

    private void loadSpinnerData() {
        listArea = domodb.requestArea();
        listRoom = domodb.requestallRoom();
        listFeature = domodb.requestFeatures();
        Entity_Icon[] listIcon = domodb.requestallIcon();

        //1st list area where to put room
        ArrayList<HashMap<String, String>> list_Area = new ArrayList<>();
        HashMap<String, String> map;
        for (Entity_Area area : listArea) {
            map = new HashMap<>();
            map.put("name", area.getName());
            map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(area.getIcon_name(), 0)));
            list_Area.add(map);
        }
        SimpleAdapter adapter_area = new SimpleAdapter(getContext(), list_Area,
                R.layout.item_in_spinner_dialog_house, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
        //spinner_area.setAdapter(adapter_area);

        //2nd list room where to put widget but contain also the area
        //widget could be place in an area or a room.
        ArrayList<HashMap<String, String>> list_Room = new ArrayList<>();
        for (Entity_Room room : listRoom) {
            map = new HashMap<>();
            map.put("name", room.getName());
            map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(room.getIcon_name(), 0)));
            list_Room.add(map);
        }
        SimpleAdapter adapter_room = new SimpleAdapter(getContext(), list_Room,
                R.layout.item_in_spinner_dialog_house, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
        //spinner_room.setAdapter(adapter_room);

        //3rd list feature to put somewhere
        ArrayList<HashMap<String, String>> list_Feature = new ArrayList<>();
        for (Entity_Feature feature : listFeature) {
            map = new HashMap<>();
            if (feature.getParameters().contains("command")) {
                try {
                    map.put("name", feature.getName() + " " + context.getString(R.string.command) + "-" + context.getResources().getString(Graphics_Manager.getStringIdentifier(context, feature.getState_key().toLowerCase())));
                } catch (Exception e) {
                    Tracer.d(mytag, "no translation for: " + feature.getState_key());
                    map.put("name", feature.getName() + " " + context.getString(R.string.command) + "-" + feature.getState_key());
                }
            } else {
                try {
                    map.put("name", feature.getName() + " " + context.getResources().getString(Graphics_Manager.getStringIdentifier(context, feature.getState_key().toLowerCase())));
                } catch (Exception e) {
                    Tracer.d(mytag, "no translation for: " + feature.getState_key());
                    map.put("name", feature.getName() + " " + feature.getState_key());
                }
            }
            map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(feature.getIcon_name(), 0)));
            list_Feature.add(map);
        }
        SimpleAdapter adapter_feature = new SimpleAdapter(getContext(), list_Feature,
                R.layout.item_in_spinner_dialog_house, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
        //spinner_feature.setAdapter(adapter_feature);

        //4th list icon to associate with area, room or widget
        //ArrayList<String> list_icon = new ArrayList<>();
        ArrayList<HashMap<String, String>> list_icon = new ArrayList<>();
        for (Entity_Icon icon : listIcon) {
            map = new HashMap<>();
            map.put("name",icon.getName() + "-" + icon.getReference());
            //Todo Replace name by Real value (get from type and id)
            // feature-445 or room-4 or area-5 sould be replace by the correct value
            map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(icon.getValue(), 0)));
            //list_icon.add(icon.getName() + "-" + icon.getReference() + "-" + icon.getValue());
            list_icon.add(map);
        }
        SimpleAdapter icon_adapter = new SimpleAdapter(getContext(), list_icon,
                R.layout.item_in_spinner_dialog_house, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});

        //spinner_icon.setAdapter(icon_adapter);
    }
}

