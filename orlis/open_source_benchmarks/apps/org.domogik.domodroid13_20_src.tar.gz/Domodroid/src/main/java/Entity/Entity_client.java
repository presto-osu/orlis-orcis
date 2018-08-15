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

import android.os.Handler;


public class Entity_client {
    private int client_type = -1;        // 0= Main , 1 = Map, 2 = MapView
    private int client_id = -1;        //unique ID for client
    private int cache_id;                // pointer into cache list
    private int devId;                    // Reference to feature
    private String skey;                // 	//
    private String currentState;        // last value exchanged with client
    private String client_name;            // To have a clear debugging !
    private String timestamp;
    private Boolean miniwidget;
    private Handler client_handler = null;


    public Entity_client(int devId, String skey, String Name, Handler handler, int session_type) {
        //super();
        this.devId = devId;
        this.skey = skey;
        this.client_name = Name;
        this.client_handler = handler;
        this.client_id = -1;    //Initially not connected
        this.client_type = session_type;
        this.miniwidget = false;    //By default, it's not a map widget
    }

    /*
     * Methods to set content
     */
    public void setClientType(int type) {
        this.client_type = type;
    }

    public void setClientId(int id) {
        this.client_id = id;
    }

    public void setcacheId(int Id) {
        this.cache_id = Id;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public void setskey(String skey) {
        this.skey = skey;
    }

    public void setValue(String Value) {
        this.currentState = Value;
    }

    public void setName(String Name) {
        this.client_name = Name;
    }

    public void setTimestamp(String Timestamp) {
        this.timestamp = Timestamp;
    }

    public void setType(Boolean type) {
        this.miniwidget = type;
    }

    public void setHandler(Handler handler) {
        this.client_handler = handler;
    }

    /*
     * Public methods to get content
     */
    public int getClientType() {
        return client_type;
    }

    public int getClientId() {
        return client_id;
    }

    public int getcacheId() {
        return cache_id;
    }

    public int getDevId() {
        return devId;
    }

    public String getskey() {
        return skey;
    }

    public String getValue() {
        return currentState;
    }

    public String getName() {
        return client_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Boolean is_Miniwidget() {
        return miniwidget;
    }

    public Handler getClientHandler() {
        return client_handler;
    }


}
