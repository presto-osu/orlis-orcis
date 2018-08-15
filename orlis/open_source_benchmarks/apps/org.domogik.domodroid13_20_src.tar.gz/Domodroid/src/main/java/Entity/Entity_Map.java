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

import misc.tracerengine;

public class Entity_Map extends Entity_Feature {
    private int id;
    private int posx;
    private int posy;
    private String map;
    private String currentState;
    private Boolean isalive = true;
    private Entity_client session;        //Structure to connect to WidgetUpdate, and receive notifications on change

    public Entity_Map(SharedPreferences params, tracerengine Trac, Activity context, String device_feature_model_id, int id, int devId, String device_usage_id, String address,
                      String device_type_id, String description, String name, String state_key, String parameters, String value_type,
                      int posx, int posy, String map) {
        super(params, Trac, context, device_feature_model_id, id, devId, device_usage_id, address, device_type_id,
                description, name, state_key, parameters, value_type);
        this.id = id;
        this.posx = posx;
        this.posy = posy;
        this.map = map;
        this.isalive = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public void setSession(Entity_client session) {
        this.session = session;
    }

    public Entity_client getSession() {
        return session;
    }
    public String getCurrentState() {
        return currentState;
    }
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
    public Boolean isalive() {
        return this.isalive;
    }
    public void setalive(Boolean mode) {
        this.isalive = mode;
    }

}
