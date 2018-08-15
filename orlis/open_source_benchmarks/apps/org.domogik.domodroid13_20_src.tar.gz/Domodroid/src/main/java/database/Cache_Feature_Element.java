package database;

import java.util.ArrayList;

import Entity.Entity_client;


public class Cache_Feature_Element {
    public int DevId = -1;
    public String skey = null;
    public String Value = null;
    public String Value_timestamp = null;
    public ArrayList<Entity_client> clients_list = null;

    public Cache_Feature_Element(int devid, String skey, String value, String Value_timestamp) {
        this.DevId = devid;
        this.skey = skey;
        this.Value = value;
        this.Value_timestamp = Value_timestamp;
        clients_list = null;
    }

    public void add_client(Entity_client new_client) {
        // Check if it's for the good feature
        if ((new_client.getDevId() == DevId) && (new_client.getskey().equals(skey))) {
            // Ok, the good one !
            new_client.setValue(Value);
            new_client.setTimestamp(Value_timestamp);
            if (clients_list == null) {
                clients_list = new ArrayList<>();
                clients_list.add(new_client);
                new_client.setClientId(0);    //set index into caller structure
                return;
            }
            // clients_list already exist
            // Check if this client_session is already connected
            for (int i = 0; i < clients_list.size(); i++) {
                if (clients_list.get(i) == new_client) {
                    //already exist in List
                    return;
                }
            }
            //not yet exists in list
            clients_list.add(new_client);                    // add this client at end of list
            new_client.setClientId(clients_list.size() - 1);    //set index into caller structure
        }
    }

    public void remove_client(Entity_client client) {
        // Check if it's for the good feature
        if ((client.getDevId() == DevId) && client.getskey().equals(skey)) {
            // Ok, the good device !
            if ((client.getClientId() >= 0) && (client.getClientId() < clients_list.size())) {
                //index into list is coherent
                if (clients_list.get(client.getClientId()) == client) {
                    //index is the good one : remove this client from list...
                    clients_list.remove(client.getClientId());
                    client.setClientId(-1);    //Not anymore connected to this device
                }
            }
        }
    }

    public ArrayList<Entity_client> clone_clients_list() {
        ArrayList<Entity_client> result = new ArrayList<>();
        if (clients_list != null) {
            for (int i = 0; i < clients_list.size(); i++) {
                result.add(clients_list.get(i));
            }
            return result;
        } else {
            return null;
        }


    }
}
