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

import org.json.JSONException;
import org.json.JSONObject;


public class Entity_Feature_Association {

    private int place_id;
    private String place_type;
    private int device_feature_id;
    private int id;
    private JSONObject json_device_feature;
    private String feat_model_id;
    private int feat_id;
    private int feat_device_id;


    public Entity_Feature_Association(int place_id, String place_type, int device_feature_id, int id, String device_feature) throws JSONException {
        this.place_id = place_id;
        this.place_type = place_type;
        this.device_feature_id = device_feature_id;
        this.id = id;
        json_device_feature = new JSONObject(device_feature);
        setFeat_model_id(json_device_feature.getString("device_feature_model_id"));
        setFeat_id(json_device_feature.getInt("id"));
        setFeat_device_id(json_device_feature.getInt("device_id"));
    }


    public int getPlace_id() {
        return place_id;
    }


    public void setPlace_id(int placeId) {
        place_id = placeId;
    }


    public String getPlace_type() {
        return place_type;
    }


    public void setPlace_type(String placeType) {
        place_type = placeType;
    }


    public int getDevice_feature_id() {
        return device_feature_id;
    }


    public void setDevice_feature_id(int deviceFeatureId) {
        device_feature_id = deviceFeatureId;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public JSONObject getDevice_feature() {
        return json_device_feature;
    }


    public void setDevice_feature(JSONObject json_deviceFeature) {
        json_device_feature = json_deviceFeature;
    }


    public String getFeat_model_id() {
        return feat_model_id;
    }


    private void setFeat_model_id(String featModelId) {
        feat_model_id = featModelId;
    }


    public int getFeat_id() {
        return feat_id;
    }


    private void setFeat_id(int featId) {
        feat_id = featId;
    }


    public int getFeat_device_id() {
        return feat_device_id;
    }


    private void setFeat_device_id(int featDeviceId) {
        feat_device_id = featDeviceId;
    }

}
