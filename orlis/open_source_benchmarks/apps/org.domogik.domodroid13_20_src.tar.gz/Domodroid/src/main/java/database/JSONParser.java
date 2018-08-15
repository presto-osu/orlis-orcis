/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package database;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.Gravity;
import android.widget.Toast;

import Entity.Entity_Area;
import Entity.Entity_Feature;
import Entity.Entity_Feature_Association;
import Entity.Entity_Icon;
import Entity.Entity_Room;

public class JSONParser {
    private final String mytag = this.getClass().getName();


    //Parse JSON object and create list of AREA----------------------
    public static Entity_Area[] ListArea(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("area");
        Entity_Area[] listArea = new Entity_Area[itemArray.length()];

        //parsing JSON area list
        for (int i = 0; i < itemArray.length(); i++) {
            listArea[i] = new Entity_Area(null, null, null,
                    itemArray.getJSONObject(i).getString("description"),
                    itemArray.getJSONObject(i).getInt("id"),
                    itemArray.getJSONObject(i).getString("name"));
        }
        return listArea;
    }


    //Parse JSON object and create list of ROOM----------------------
    public static Entity_Room[] ListRoom(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("room");
        Entity_Room[] listRoom = new Entity_Room[itemArray.length()];
        int area_id;

        //parsing JSON room list
        for (int i = 0; i < itemArray.length(); i++) {
            if (itemArray.getJSONObject(i).getString("area_id").equals("")) area_id = 0;
            else area_id = itemArray.getJSONObject(i).getInt("area_id");
            listRoom[i] = new Entity_Room(null, null, null,
                    area_id,
                    itemArray.getJSONObject(i).getString("description"),
                    itemArray.getJSONObject(i).getInt("id"),
                    itemArray.getJSONObject(i).getString("name"));
        }
        return listRoom;
    }


    //Parse JSON object and create list of FEATURE-------------------------
    public static Entity_Feature[] ListFeature(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("feature");
        Entity_Feature[] listFeature = new Entity_Feature[itemArray.length()];

        //parsing JSON feature list
        for (int i = 0; i < itemArray.length(); i++) {
            listFeature[i] = new Entity_Feature(null, null, null,
                    itemArray.getJSONObject(i).getString("device_feature_model_id"),
                    itemArray.getJSONObject(i).getInt("id"),
                    itemArray.getJSONObject(i).getJSONObject("device").getInt("id"),
                    itemArray.getJSONObject(i).getJSONObject("device").getString("device_usage_id"),
                    itemArray.getJSONObject(i).getJSONObject("device").getString("address"),
                    itemArray.getJSONObject(i).getJSONObject("device").getString("device_type_id"),
                    itemArray.getJSONObject(i).getJSONObject("device").getString("description"),
                    itemArray.getJSONObject(i).getJSONObject("device").getString("name"),
                    itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("stat_key"),
                    itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("parameters"),
                    itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("value_type"));
        }
        return listFeature;
    }

    //Parse JSON object and create list of FEATURE ASSOCIATION-------------------------
    public static Entity_Feature_Association[] ListFeatureAssociation(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("feature_association");
        Entity_Feature_Association[] listFeatureAssociation = new Entity_Feature_Association[itemArray.length()];

        //parsing JSON feature list
        for (int i = 0; i < itemArray.length(); i++) {
            listFeatureAssociation[i] = new Entity_Feature_Association(
                    itemArray.getJSONObject(i).getInt("place_id"),
                    itemArray.getJSONObject(i).getString("place_type"),
                    itemArray.getJSONObject(i).getInt("device_feature_id"),
                    itemArray.getJSONObject(i).getInt("id"),
                    itemArray.getJSONObject(i).getString("device_feature"));
        }
        return listFeatureAssociation;
    }

    //Parse JSON object, result of a request-------------------------
    public static Boolean Ack(JSONObject json) {
        try {
            if (json.getString("status").equals("ERROR")) {
                //todo need to say this to user and log it.
                //add tracer access
                // tracer.d(mytag,"json status erro");
                Toast toast = Toast.makeText(null, R.string.error_sending_command, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            //todo add tracer
            //Tracer.d (mytag,""+ e.toString());
            return false;
        }

    }

    public static int StateValueINT(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("stats");
        return itemArray.getJSONObject(0).getInt("value");
    }

    public static String StateValueSTRING(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("stats");
        return itemArray.getJSONObject(0).getString("value");
    }


    //Parse JSON object and create list of ICON-------------------------
    public static Entity_Icon[] ListIcon(JSONObject json) throws JSONException {
        JSONArray itemArray = json.getJSONArray("ui_config");
        Entity_Icon[] listIcon = new Entity_Icon[itemArray.length()];

        //parsing JSON feature list
        for (int i = 0; i < itemArray.length(); i++) {
            listIcon[i] = new Entity_Icon(
                    itemArray.getJSONObject(i).getString("name"),
                    itemArray.getJSONObject(i).getString("value"),
                    itemArray.getJSONObject(i).getInt("reference"));
        }
        return listIcon;
    }
}



