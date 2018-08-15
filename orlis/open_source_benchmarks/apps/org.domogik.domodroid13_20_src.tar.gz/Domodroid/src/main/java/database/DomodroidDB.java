package database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Entity.Entity_Area;
import Entity.Entity_Feature;
import Entity.Entity_Icon;
import Entity.Entity_Map;
import Entity.Entity_Room;
import misc.tracerengine;

public class DomodroidDB {

    private final Activity context;
    // Added by Doume to clarify debugging
    private final String mytag = this.getClass().getName();
    public String owner = "";
    private tracerengine Tracer = null;
    private final SharedPreferences params;

    //////////////////////////////////////

    public DomodroidDB(tracerengine Trac, Activity context, SharedPreferences params) {
        this.context = context;
        this.Tracer = Trac;
        this.params = params;
        tracerengine.refresh_settings();
        Tracer.i(mytag, "Instance started...");
    }

    public DomodroidDB getDomodroidDB() {
        return this;
    }

    public void updateDb() {
        //That should clear all tables, except feature_map
        context.getContentResolver().delete(DmdContentProvider.CONTENT_URI_UPGRADE_FEATURE_STATE, null, null);
    }

    public void NewsyncDb() {
        //delete all feature
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE, null);
        //That should clear in all tables, call only for what refer to area id 1 if previous api >0.6f
        DomodroidDB domodb = new DomodroidDB(Tracer, context, params);
        domodb.owner = "Widgets_Manager.loadRoomWidgets";
        Tracer.i(mytag, "load widgets for area 1");
        Entity_Room[] listRoom = domodb.requestRoom(1);

        for (Entity_Room room : listRoom) {
            domodb.remove_one_room(room.getId());
            domodb.remove_one_place_type_in_Featureassociation(room.getId(), "room");
            domodb.remove_one_icon(room.getId(), "room");
        }

        domodb.remove_one_area(1);
        domodb.remove_one_place_type_in_Featureassociation(1, "area");
        domodb.remove_one_icon(1, "area");
    }

    public void closeDb() {
        try {
            context.getContentResolver().cancelSync(Uri.EMPTY);
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
    }

    ////////////////// INSERT

    public void insertArea(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("area");
        //context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_AREA, null);

        for (int i = 0; i < itemArray.length(); i++) {
            values.put("description", itemArray.getJSONObject(i).getString("description"));
            values.put("id", itemArray.getJSONObject(i).getInt("id"));
            values.put("name", itemArray.getJSONObject(i).getString("name"));
            Tracer.d(mytag, "Inserting Area " + itemArray.getJSONObject(i).getString("name"));

            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_AREA, values);
        }
    }

    public void insertRoom(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("room");
        //context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_ROOM, null);

        int area_id;
        for (int i = 0; i < itemArray.length(); i++) {
            if (itemArray.getJSONObject(i).getString("area_id").equals("")) area_id = 0;
            else area_id = itemArray.getJSONObject(i).getInt("area_id");
            values.put("area_id", area_id);
            values.put("description", itemArray.getJSONObject(i).getString("description"));
            values.put("id", itemArray.getJSONObject(i).getInt("id"));
            values.put("name", itemArray.getJSONObject(i).getString("name"));
            Tracer.d(mytag, "Inserting Room " + itemArray.getJSONObject(i).getString("name"));
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ROOM, values);
        }
    }

    public void insertIcon(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("ui_config");
        //context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_ICON, null);

        for (int i = 0; i < itemArray.length(); i++) {
            values.put("name", itemArray.getJSONObject(i).getString("name"));
            values.put("value", itemArray.getJSONObject(i).getString("value"));
            values.put("reference", itemArray.getJSONObject(i).getInt("reference"));
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ICON, values);
        }
    }

    public void insertFeature(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("feature");
        //context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE, null);
        for (int i = 0; i < itemArray.length(); i++) {
            try {
                values.put("device_feature_model_id", itemArray.getJSONObject(i).getString("device_feature_model_id"));
                values.put("id", itemArray.getJSONObject(i).getInt("id"));
                values.put("device_id", itemArray.getJSONObject(i).getJSONObject("device").getInt("id"));
                values.put("device_usage_id", itemArray.getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
                values.put("address", itemArray.getJSONObject(i).getJSONObject("device").getString("address"));
                values.put("device_type_id", itemArray.getJSONObject(i).getJSONObject("device").getString("device_type_id"));
                values.put("description", itemArray.getJSONObject(i).getJSONObject("device").getString("description"));
                values.put("name", itemArray.getJSONObject(i).getJSONObject("device").getString("name"));
                values.put("state_key", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("stat_key"));
                values.put("parameters", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("parameters"));
                values.put("value_type", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("value_type"));
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE, values);
            } catch (Exception e) {
                // Cannot parse JSON Array or JSONObject
                Tracer.e(mytag, "Exception inserting Features list in bdd (" + i + ")");
                Tracer.e(mytag, e.toString());
            }
        }
    }

    public void insertFeature_0_4(JSONObject itemArray) {
        ContentValues values = new ContentValues();
        try {
            values.put("device_feature_model_id", itemArray.getString("device_feature_model_id"));
            values.put("id", itemArray.getInt("id"));
            values.put("device_id", itemArray.getInt("device_id"));
            values.put("device_usage_id", itemArray.getString("device_usage_id"));
            values.put("address", itemArray.getString("adress"));
            values.put("device_type_id", itemArray.getString("device_type_id"));
            values.put("description", itemArray.getString("description"));
            values.put("name", itemArray.getString("name"));
            values.put("state_key", itemArray.getString("stat_key"));
            values.put("parameters", itemArray.getString("parameters"));
            values.put("value_type", itemArray.getString("value_type"));
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE, values);
        } catch (Exception e) {
            // Cannot parse JSON Array or JSONObject
            Tracer.e(mytag, "Exception inserting Features list in bdd");
            Tracer.e(mytag, e.toString());
        }
    }

    public void insertFeatureAssociation(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("feature_association");
        //context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_ASSOCIATION, null);

        for (int i = 0; i < itemArray.length(); i++) {
            values.put("place_id", itemArray.getJSONObject(i).getInt("place_id"));
            values.put("place_type", itemArray.getJSONObject(i).getString("place_type"));
            values.put("device_feature_id", itemArray.getJSONObject(i).getInt("device_feature_id"));
            values.put("id", itemArray.getJSONObject(i).getInt("id"));
            values.put("device_feature", itemArray.getJSONObject(i).getString("device_feature"));
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_ASSOCIATION, values);
        }
    }

    public void insertFeatureState(JSONObject json) throws JSONException {
        String skey = null;
        String Val = null;
        int dev_id = 0;
        ContentValues values = new ContentValues();
        JSONArray itemArray = json.getJSONArray("stats");
        String[] projection = {"COUNT(*)"};

        for (int i = 0; i < itemArray.length(); i++) {
            try {
                dev_id = itemArray.getJSONObject(i).getInt("device_id");
            } catch (Exception e) {
                Tracer.e(mytag + "(" + owner + ")", "Database feature No id : ");
                return;
            }
            try {
                skey = itemArray.getJSONObject(i).getString("skey");
            } catch (Exception e) {
                Tracer.e(mytag + "(" + owner + ")", "Database feature No skey for id : " + dev_id);
                skey = "_";
                Val = "0";
            }
            try {
                Val = itemArray.getJSONObject(i).getString("value");
            } catch (Exception e) {
                Tracer.e(mytag + "(" + owner + ")", "Database feature No Value for id : " + dev_id + " " + skey);
                Val = "0";
            }

            Cursor curs = null;
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_STATE, projection, "device_id = ? AND key = ?",
                    new String[]{Integer.toString(dev_id) + " ", skey}, null);
            curs.moveToFirst();
            values.put("device_id", dev_id);
            values.put("key", skey);
            values.put("value", Val);
            if (curs.getInt(0) == 0) {
                Tracer.e(mytag + "(" + owner + ")", "Insert for : " + dev_id + " (" + skey + ") (" + Val + ")");
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_STATE, values);

            } else {
                Tracer.e(mytag + "(" + owner + ")", "Update for : " + dev_id + " (" + skey + ") (" + Val + ")");
                context.getContentResolver().update(DmdContentProvider.CONTENT_URI_UPDATE_FEATURE_STATE, values, "device_id = ? AND key = ?",
                        new String[]{Integer.toString(dev_id) + " ", skey});
            }
            curs.close();
        }
    }

	/*
    public void insertFeatureState(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("stats");
		String skey = null;
		String Val = null;
		Boolean exists = false;

		//Tracer.e(mytag+"("+owner+")", "Processing FeatureState Array : <"+itemArray.toString()+">");
		// First, erase all old content
		context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_STATE, null);
		// Now, insert/update new rows
		for (int i =0; i < itemArray.length(); i++){
			try {
				exists = itemArray.getJSONObject(i).getBoolean("exists");
				// can be true or false, depends on server's response in version 0.3
			} catch (Exception e) {
				// Servers 0.2 does'nt return this kind of parameter : set true by default
				exists = true;
			}
			//if(exists) {
				try {
					skey = itemArray.getJSONObject(i).getString("skey");
				} catch (Exception e) {
					Tracer.e(mytag+"("+owner+")", "Database feature No skey for id : "+itemArray.getJSONObject(i).getInt("device_id"));
					skey = "_";
				}
				try {
					Val = itemArray.getJSONObject(i).getString("value");
				}catch (Exception e) {
					Tracer.e(mytag+"("+owner+")", "Database feature No Value for id : "+itemArray.getJSONObject(i).getInt("device_id")+" "+skey);
					Val = "0";
				}
				values.put("device_id", itemArray.getJSONObject(i).getInt("device_id"));
				values.put("key", skey);
				values.put("value", Val);
				context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_STATE, values);
				Tracer.v(mytag+"("+owner+")", "Database insert feature : "+itemArray.getJSONObject(i).getInt("device_id")+" "+skey+" "+Val);
			//} else {
				//Tracer.d(mytag+"("+owner+")", "Device : "+itemArray.getJSONObject(i).getInt("device_id")+" does'nt exist anymore....");
			//}

		}
	}

	 */

    public void insertFeatureMap(int id, int posx, int posy, String map) {
        //send value to database to add a widget on map
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("posx", posx);
        values.put("posy", posy);
        values.put("map", map);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_MAP, values);
    }

    ////////////////// REMOVE ONE but be careful it's by id and can delete more that just one

    public void remove_one_area(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_AREA, values);
    }

    public void remove_one_room(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_ROOM, values);
    }

    public void remove_one_icon(int id) {
        ContentValues values = new ContentValues();
        values.put("reference", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_ICON, values);
    }

    public void remove_one_icon(int id, String type) {
        ContentValues values = new ContentValues();
        values.put("reference", id);
        values.put("name", type);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_ICON, values);
    }

    public void remove_one_feature(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_FEATURE, values);
    }

    private void remove_one_feature_association(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_FEATURE_ASSOCIATION, values);
    }

    public void remove_one_feature_association(int id, int place_id, String place_type) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("place_id", place_id);
        values.put("place_type", place_type);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_unique_FEATURE_ASSOCIATION, values);
    }

    public void remove_one_feature_in_FeatureMap(int id) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_feature_in_FEATURE_MAP, values);
    }

    //add a public void to remove one widget in map
    public void remove_one_FeatureMap(int id, int posx, int posy, String map) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("posx", posx);
        values.put("posy", posy);
        values.put("map", map);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_FEATURE_MAP, values);
    }

    public void remove_one_place_type_in_Featureassociation(int place_id, String place_type) {
        ContentValues values = new ContentValues();
        values.put("place_id", place_id);
        values.put("place_type", place_type);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_one_place_type_in_FEATURE_ASSOCIATION, values);
    }


    ////////////////// REMOVE ALL

    public void cleanFeatureMap(String map) {
        //send map_name to DmdContentProvider to remove all widget on this map in database
        ContentValues values = new ContentValues();
        values.put("map", map);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_MAP, values);
    }

    ////////////////// UPDATE

    //Send custom name to DmdContentProvder so that it could be write in DB
    public void update_name(int id, String name, String type) {
        switch (type) {
            case "feature": {
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("newname", name);
                Tracer.d(mytag, "Description set to: " + name + " for device: " + id);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_FEATURE_NAME, values);
                break;
            }
            case "area": {
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("newname", name);
                Tracer.d(mytag, "Description set to: " + name + " for area: " + id);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_AREA_NAME, values);
                break;
            }
            case "room": {
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("newname", name);
                Tracer.d(mytag, "Description set to: " + name + " for room: " + id);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ROOM_NAME, values);
                break;
            }
            case "icon": {
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("newname", name);
                Tracer.d(mytag, "Description set to: " + name + " for icon: " + id);
                context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
                break;
            }
        }
    }

    public void move_one_feature_association(int id, int place_id, String place_type, String order) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("place_id", place_id);
        values.put("place_type", place_type);
        values.put("order", order);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_FEATURE_POSITION_ID, values);
        Tracer.d(mytag, "Moving " + order + " the feature id:" + id + " in place_id:" + place_id + " of type:" + place_type);
    }

    public void move_one_area(int id, int place_id, String place_type, String order) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("place_id", place_id);
        values.put("place_type", place_type);
        values.put("order", order);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_AREA_POSITION_ID, values);
        Tracer.d(mytag, "Moving " + order + " the area id:" + id + " in place_id:" + place_id + " of type:" + place_type);
    }

    public void move_one_room(int id, int place_id, String place_type, String order) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("place_id", place_id);
        values.put("place_type", place_type);
        values.put("order", order);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ROOM_POSITION_ID, values);
        Tracer.d(mytag, "Moving " + order + " the room id:" + id + " in place_id:" + place_id + " of type:" + place_type);
    }

    ////////////////// REQUEST


    public Entity_Area[] requestArea() {
        String[] projection = {"description", "id", "name"};
        Entity_Area[] areas = null;
        Cursor curs = null;
        try {
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_AREA, projection, null, null, null);
            areas = new Entity_Area[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                areas[i] = new Entity_Area(params, Tracer, context, curs.getString(0), curs.getInt(1), curs.getString(2));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return areas;
    }

    public JSONObject request_json_Area() {
        JSONObject json_AreaList = new JSONObject();
        String[] projection = {"description", "id", "name"};
        Cursor curs = null;
        try {
            json_AreaList.put("status", "OK");
            json_AreaList.put("code", 0);
            json_AreaList.put("description", "None");
            JSONArray list = new JSONArray();
            JSONObject map_area = null;
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_AREA, projection, null, null, null);
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                map_area = new JSONObject();
                if (curs.getString(0) == null) {
                    map_area.put("description", "");
                } else {
                    map_area.put("description", curs.getString(0));
                }

                map_area.put("id", curs.getString(1));
                map_area.put("name", curs.getString(2));
                list.put(map_area);
            }
            json_AreaList.put("area", list);
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return json_AreaList;
    }

    public int requestlastidArea() {
        String[] projection = {"description", "id", "name"};
        Cursor curs = null;
        int lastid = 1;
        try {
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_AREA, projection, null, null, null);
            curs.moveToLast();
            lastid = curs.getInt(1);
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request last area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return lastid;
    }

    public Entity_Room[] requestallRoom() {
        Entity_Room[] rooms = null;
        String[] projection = {"area_id", "description", "id", "name"};
        Cursor curs = null;
        try {
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ROOM,
                    projection,
                    null,
                    null,
                    "id Asc");
            rooms = new Entity_Room[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                rooms[i] = new Entity_Room(params, Tracer, context, curs.getInt(0), curs.getString(1),
                        curs.getInt(2), curs.getString(3));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "Exception requesting all rooms ");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return rooms;
    }

    public JSONObject request_json_Room() {
        JSONObject json_RoomList = new JSONObject();
        String[] projection = {"area_id", "description", "id", "name"};
        Cursor curs = null;
        try {
            json_RoomList.put("status", "OK");
            json_RoomList.put("code", 0);
            json_RoomList.put("description", "None");
            JSONArray rooms = new JSONArray();
            JSONObject room = null;
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ROOM, projection, null, null, "id Asc");
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                room = new JSONObject();
                room.put("area_id", curs.getString(0));
                room.put("description", curs.getString(1));
                room.put("id", curs.getString(2));
                room.put("name", curs.getString(3));
                rooms.put(room);
            }
            json_RoomList.put("room", rooms);
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return json_RoomList;
    }

    public int requestidlastRoom() {
        String[] projection = {"area_id", "description", "id", "name"};
        Cursor curs = null;
        int lastid = 0;
        try {
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ROOM,
                    projection,
                    null,
                    null,
                    "id Asc");
            curs.moveToLast();
            lastid = curs.getInt(2);
        } catch (Exception e) {
            Tracer.v(mytag + "(" + owner + ")", "request last room error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return lastid;
    }

    //Request to remove association if no more device
    public void CleanFeatures_association() {
        Cursor curs1 = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ALL, null, null, null, null);
        int[] device_feature_id_associated_somewhere = requestAllFeatures_association();
        boolean found;
        //parcourir la liste des feature avec comme device_feature_id_associated_somewhere=int[i]
        // Si erreur c'est que le feature n'existe plus, on peut supprimer des tables feature_map et feature_associated l
        // on utilise int[i]
        for (int aDevice_feature_id_associated_somewhere : device_feature_id_associated_somewhere) {
            found = false;
            for (int j = 0; j < curs1.getCount(); j++) {
                curs1.moveToPosition(j);
                if (aDevice_feature_id_associated_somewhere == curs1.getInt(1)) {
                    found = true;
                }
            }
            if (!found) {
                remove_one_feature_association(aDevice_feature_id_associated_somewhere);
                remove_one_feature_in_FeatureMap(aDevice_feature_id_associated_somewhere);
            }
        }
        curs1.close();

    }

    //Add a request for all device_feature_id in feature_association and feature_map
    //It's used to be sure that the url always contains all associated devices
    //to grab information if they're displayed somewhere.
    public int[] requestAllFeatures_association() {
        Cursor curs1 = null;
        Cursor curs2 = null;
        int[] dev_id = null;

        try {
            Tracer.v(mytag + "(" + owner + ")", "requesting features list");

            curs1 = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ASSOCIATION_ALL, null, null, null, null);
            curs2 = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_MAP_ALL, null, null, null, null);
            dev_id = new int[curs1.getCount() + curs2.getCount()];

            int count = curs1.getCount();
            for (int i = 0; i < count; i++) {
                curs1.moveToPosition(i);
                //We need only the device_feature_id in 3rd columns of table feature_association
                dev_id[i] = curs1.getInt(2);
            }
            count = curs2.getCount() + curs1.getCount();
            for (int i = curs1.getCount(); i < count; i++) {
                curs2.moveToPosition(i - curs1.getCount());
                //We need only the device_feature_id in 1st columns of table feature_map
                dev_id[i] = curs2.getInt(0);
            }

        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request feature_association error");
            Tracer.e(mytag, e.toString());
        }
        curs1.close();
        curs2.close();
        return dev_id;
    }

    public JSONObject request_json_Features_association() {
        JSONObject json_FeatureAssociationList = new JSONObject();
        Cursor curs = null;
        try {
            json_FeatureAssociationList.put("status", "OK");
            json_FeatureAssociationList.put("code", 0);
            json_FeatureAssociationList.put("description", "None");
            JSONArray ListFeature = new JSONArray();
            JSONObject Widget = null;
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ASSOCIATION_ALL, null, null, null, null);
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                Widget = new JSONObject();
                Widget.put("place_id", curs.getString(0));
                Widget.put("place_type", curs.getString(1));
                Widget.put("device_feature_id", curs.getString(2));
                Widget.put("id", curs.getString(3));
                if (curs.getString(4) == null) {
                    Widget.put("device_feature", "");
                } else {
                    Widget.put("device_feature", curs.getString(4));
                }
                ListFeature.put(Widget);
            }
            json_FeatureAssociationList.put("feature_association", ListFeature);
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return json_FeatureAssociationList;
    }

    public int requestidlastFeature_association() {
        String[] projection = {"place_id", "place_type", "device_feature_id", "id", "device_feature"};
        Cursor curs = null;
        int lastid = 0;
        try {
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ASSOCIATION,
                    projection,
                    null,
                    null,
                    null);
            curs.moveToLast();
            lastid = curs.getInt(3);
        } catch (Exception e) {
            Tracer.v(mytag + "(" + owner + ")", "request last room error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return lastid;
    }

    public Entity_Room[] requestRoom(int area_id) {
        Entity_Room[] rooms = null;
        String[] projection = {"area_id", "description", "id", "name"};
        Cursor curs = null;
        try {
            //curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_ROOM, projection, "area_id = \'"+area_id+"\'", null, null);
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ROOM,
                    projection,
                    "area_id = ?",
                    new String[]{area_id + ""},
                    "id Asc");
            rooms = new Entity_Room[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                rooms[i] = new Entity_Room(params, Tracer, context, curs.getInt(0), curs.getString(1),
                        curs.getInt(2), curs.getString(3));
            }
        } catch (Exception e) {
            Tracer.v(mytag + "(" + owner + ")", "Exception requesting for rooms of area_id " + area_id);
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return rooms;
    }

    public Entity_Icon requestIcons(int reference, String name) {
        Cursor curs = null;
        Entity_Icon icon = null;
        try {
            String[] projection = {"name", "value", "reference"};
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_ICON, projection, "reference = ? AND name = ?", new String[]{reference + "", name}, null);
            curs.moveToFirst();
            icon = new Entity_Icon(curs.getString(0), curs.getString(1), curs.getInt(2));
        } catch (Exception e) {
            //Tracer.e(mytag+"("+owner+")","request icon error for reference = "+reference+" name = "+name);
            //e.printStackTrace();
        }
        curs.close();
        return icon;
    }

    public Entity_Icon[] requestallIcon() {
        Entity_Icon[] Icon = null;
        String[] projection = {"name", "value", "reference"};
        Cursor curs = null;
        try {
            //curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_ROOM, projection, "area_id = \'"+area_id+"\'", null, null);
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ICON,
                    projection,
                    null,
                    null,
                    null);
            Icon = new Entity_Icon[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                Icon[i] = new Entity_Icon(curs.getString(0), curs.getString(1),
                        curs.getInt(2));
            }
        } catch (Exception e) {
            Tracer.v(mytag + "(" + owner + ")", "Exception requesting all rooms ");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return Icon;
    }

    public JSONObject request_json_Icon() {
        JSONObject json_IconList = new JSONObject();
        String[] projection = {"name", "value", "reference"};
        Cursor curs = null;
        try {
            json_IconList.put("status", "OK");
            json_IconList.put("code", 0);
            json_IconList.put("description", "None");
            JSONArray icons = new JSONArray();
            JSONObject icon = null;
            curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ICON,
                    projection,
                    null,
                    null,
                    null);
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                icon = new JSONObject();
                icon.put("name", curs.getString(0));
                if (curs.getString(1) == null) {
                    icon.put("value", "");
                } else {
                    icon.put("value", curs.getString(1));
                }
                icon.put("reference", curs.getString(2));
                icons.put(icon);
            }
            json_IconList.put("ui_config", icons);
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request area error");
            Tracer.e(mytag, e.toString());
        }
        if (curs != null)
            curs.close();
        return json_IconList;
    }

    public Entity_Feature[] requestFeatures(int id, String zone) {
        Cursor curs = null;
        Entity_Feature[] features = null;
        try {
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ID, null, null, new String[]{id + " ", zone}, "state_key ");
            features = new Entity_Feature[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                features[i] = new Entity_Feature(params, Tracer, context, curs.getString(0), curs.getInt(1), curs.getInt(2), curs.getString(3), curs.getString(4),
                        curs.getString(5), curs.getString(6), curs.getString(7), curs.getString(8), curs.getString(9), curs.getString(10));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request feature error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return features;
    }

    public Entity_Map[] requestFeatures(String map) {
        Cursor curs = null;
        String[] projection = {"value"};
        Entity_Map[] features = null;
        try {
            //Tracer.v(mytag+"("+owner+")","Getting database features for map : "+map);
            //Getting database features for map current map
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_MAP, projection,
                    "table_feature_map = ?",
                    new String[]{"\'" + map + "\' "},
                    null);
            //Count the number of widgets present in this map
            features = new Entity_Map[curs.getCount()];
            int count = curs.getCount();
            Tracer.v(mytag + "(" + owner + ")", count + " Entities_Map returned for map : " + map);

            for (int i = 0; i < count; i++) {
                //create the Entity_Map with all parameter needed on map for each widget present in the current map
                //careful has it his an innerjoin there is one more column.
                curs.moveToPosition(i);
                int Id = curs.getInt(1);
                String device_usage_id = curs.getString(3);
                String iconName = "unknow";
                try {
                    iconName = this.requestIcons(Id, "feature").getValue();
                    Tracer.i(mytag, "icon " + iconName);
                } catch (Exception e) {
                    //e.printStackTrace();
                    Tracer.i(mytag, "NO icon for device id" + Id);
                }
                if (iconName.equals("unknow"))
                    iconName = device_usage_id;

                features[i] = new Entity_Map(params, Tracer, context, curs.getString(0), Id, curs.getInt(2), iconName, curs.getString(4), curs.getString(5),
                        curs.getString(6), curs.getString(7), curs.getString(8), curs.getString(9), curs.getString(10), curs.getInt(12), curs.getInt(13), curs.getString(14));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request feature_map error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return features;
    }

    public Entity_Map[] requestMapSwitches(String map) {
        Cursor curs = null;
        String[] projection = {"value"};
        Entity_Map[] features = null;
        try {
            //Tracer.v(mytag+"("+owner+")","Getting database features for map : "+map);
            //Getting database features for map current map
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_MAP_SWITCHES, projection,
                    "table_feature_map = ?",
                    new String[]{"\'" + map + "\' "},
                    null);
            //Count the number of map switch widgets present in this map
            features = new Entity_Map[curs.getCount()];
            int count = curs.getCount();
            Tracer.v(mytag + "(" + owner + ")", count + " Entities_Map returned for map switches : " + map);

            for (int i = 0; i < count; i++) {
                //create the pseudo Entity_Map with all parameters present in table_feature_map : id, posx, posy and map_name

                curs.moveToPosition(i);
                features[i] = new Entity_Map(params, Tracer, context, "", curs.getInt(0), 0, "", "", "",
                        "", "", "", "", "",
                        curs.getInt(1), curs.getInt(2), curs.getString(3));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request map_switches error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return features;
    }

    public Entity_Feature[] requestFeatures() {
        Cursor curs = null;

        Entity_Feature[] features = null;
        try {
            Tracer.v(mytag + "(" + owner + ")", "requesting features list");

            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ALL, null, null, null, null);
            features = new Entity_Feature[curs.getCount()];
            int count = curs.getCount();
            for (int i = 0; i < count; i++) {
                curs.moveToPosition(i);
                features[i] = new Entity_Feature(params, Tracer, context, curs.getString(0), curs.getInt(1), curs.getInt(2), curs.getString(3), curs.getString(4), curs.getString(5), curs.getString(6), curs.getString(7), curs.getString(8), curs.getString(9), curs.getString(10));
            }
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request feature error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return features;
    }

    public Entity_Feature requestFeaturesbyid(String id) {
        Cursor curs = null;
        Entity_Feature feature = null;
        try {
            Tracer.v(mytag + "(" + owner + ")", "requesting features by id" + id);
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_BY_ID, null, null, new String[]{(id)}, null);
            curs.moveToFirst();
            feature = new Entity_Feature(params, Tracer, context, curs.getString(0), curs.getInt(1), curs.getInt(2), curs.getString(3), curs.getString(4), curs.getString(5), curs.getString(6), curs.getString(7), curs.getString(8), curs.getString(9), curs.getString(10));
        } catch (Exception e) {
            Tracer.e(mytag + "(" + owner + ")", "request features by id error");
            Tracer.e(mytag, e.toString());
        }
        curs.close();
        return feature;
    }

    public String requestFeatureState(int device_id, String key) {
        String state = " ";
        String[] projection = {"value"};
        String sortOrder = "key ";
        try {
            Cursor curs = null;
            curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_STATE,
                    projection,
                    "device_id = ? AND key = ?",
                    new String[]{device_id + "", key},
                    null);
            curs.moveToPosition(0);
            if ((curs != null) && (curs.getCount() != 0)) {
                state = curs.getString(0);
                curs.close();
                Tracer.v(mytag + "(" + owner + ")", "Database query feature : " + device_id + " " + key + " value : " + state);

            } else {
                Tracer.v(mytag + "(" + owner + ")", "Database query feature : " + device_id + " " + key + " not found ");
                return null;
            }
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }


        return state;
    }


}
