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
package Entity;

import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONObject;

import activities.Graphics_Manager;
import database.DomodroidDB;
import misc.tracerengine;

public class Entity_Feature {
    private int id;
    private JSONObject device;
    private String description;
    private String device_usage_id;
    private String address;
    private String device_type_id;
    private int devId;
    private String name;
    private String device_feature_model_id;
    private String state_key;
    private String parameters;
    private String value_type;
    private String currentState;
    private int state;
    private final Activity context;
    private tracerengine Tracer = null;
    private final SharedPreferences params;
    public Boolean Develop;

    public Entity_Feature(SharedPreferences params, tracerengine Trac, Activity context, String device_feature_model_id, int id, int devId, String device_usage_id, String address, String device_type_id, String description, String name, String state_key, String parameters, String value_type) {
        this.device_feature_model_id = device_feature_model_id;
        this.id = id;
        this.devId = devId;
        this.device_usage_id = device_usage_id;
        this.address = address;
        this.device_type_id = device_type_id;
        this.description = description;
        this.name = name;
        this.state_key = state_key;
        this.parameters = parameters;
        this.value_type = value_type;
        this.Tracer = Trac;
        this.context = context;
        this.params = params;
        try {
            Develop = params.getBoolean("DEV", false);
        } catch (Exception e) {
            Develop = false;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JSONObject getDevice() {
        return device;
    }

/*Not used
        public void setDevice(JSONObject device) {
            this.device = device;
        }
*/

    public String getDescription() {

        String return_value;
        if (description != null) {
            if (description.length() < 1 || description.equalsIgnoreCase("null")) {
                return_value = name;
            } else {
                return_value = description;
            }
        } else {
            return_value = name;
        }
        //add debug option to change label adding its Id
        if (Develop)
            return_value = return_value + " (" + id + ")";

        return return_value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDevice_usage_id() {
        return device_usage_id;
    }

    public void setDevice_usage_id(String device_usage_id) {
        this.device_usage_id = device_usage_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice_feature_model_id() {
        return device_feature_model_id;
    }

    public void setDevice_feature_model_id(String device_feature_model_id) {
        this.device_feature_model_id = device_feature_model_id;
    }

    public String getState_key() {
        return state_key;
    }

    public void setState_key(String state_key) {
        this.state_key = state_key;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getValue_type() {
        return value_type;
    }

    public void setValue_type(String value_type) {
        this.value_type = value_type;
    }

    public int getRessources() {
        return Graphics_Manager.Map_Agent(getDevice_usage_id(), getState());
    }

    private int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDevice_type() {
        if (device_type_id != null) {
            String[] model = device_type_id.split("\\.");
            try {
                return model[1];
            } catch (Exception e) {
                return model[0];
            }
        } else
            return null;
    }

    public String getDevice_type_id() {
        return device_type_id;
    }

    public void setDevice_type_id(String device_type_id) {
        this.device_type_id = device_type_id;
    }

    public String getIcon_name() {
        String iconName = "unknow";
        DomodroidDB domodb = new DomodroidDB(Tracer, context, params);
        domodb.owner = "entity_feature";
        try {
            iconName = domodb.requestIcons(id, "feature").getValue();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (iconName.equals("unknow"))
            //todo adapt for 0.4 and + use final dt_type (open_close) for example to simplify.
            //Dt_type are in table table_feature in column device_feature_model_id before first "."
            iconName = device_usage_id;
        return iconName;
    }

}
