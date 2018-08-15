package com.example.tobiastrumm.freifunkautoconnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkRecyclerAdapter extends RecyclerView.Adapter<NetworkRecyclerAdapter.ViewHolder> {

    private static final String TAG = NetworkRecyclerAdapter.class.getSimpleName();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_ssid;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_ssid = (TextView) itemView.findViewById(R.id.tv_network_item_ssid);

            // Setup the click listener
            itemView.setOnClickListener(this);
        }

        /*
         * Handle the click on a ssid here. If the ssid is not already part of the network configuration, it
         * will be added, else it will be removed.
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if(adapterPosition >= 0) {
                // Get the Network object that was clicked.
                Network n = getNetwork(adapterPosition);
                // If the network is already saved in the network configuration, remove it
                if (n.active) {
                    List<WifiConfiguration> wificonf = wifiManager.getConfiguredNetworks();
                    if (wificonf != null) {
                        for (WifiConfiguration wc : wificonf) {
                            if (wc.SSID.equals(n.ssid)) {
                                boolean removal_successful = wifiManager.removeNetwork(wc.networkId);
                                // Save configuration
                                boolean save_successful = wifiManager.saveConfiguration();

                                // Only set active to false if the removal was successful
                                n.active = !(removal_successful && save_successful);
                                // If the network is still active, inform the object registered as
                                // onAdapterInteractionListener that the removal failed.
                                if(n.active && onAdapterInteractionListener != null){
                                    onAdapterInteractionListener.onRemoveSsidFailed();
                                }
                            }
                        }
                    }
                }
                // If the network is not in the network configuration, add it.
                else {
                    // Add Network to network configuration
                    WifiConfiguration wc = new WifiConfiguration();
                    wc.SSID = n.ssid;
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int networkId = wifiManager.addNetwork(wc);
                    boolean add_successful = wifiManager.enableNetwork(networkId, false);
                    // Save configuration
                    boolean save_successful = wifiManager.saveConfiguration();

                    // Only set active to true if the addition was successful
                    n.active = add_successful && save_successful;
                }


                /*****  Notify the networkRecyclerAdapter that the item at adapterPosition was changed. *****/
                SharedPreferences prefMan = PreferenceManager.getDefaultSharedPreferences(context);
                boolean show_deprecated = prefMan.getBoolean("pref_deprecated_ssids", false);
                // If a deprecated network was removed and depricated networks should not be shown, remove the network
                // from the RecyclerView completely.
                if(!show_deprecated && !n.active && n.deprecated){
                    filteredNetworks.remove(adapterPosition);
                    allNetworks.remove(n);
                    notifyItemRemoved(adapterPosition);
                }
                else{
                    notifyItemChanged(adapterPosition);
                }

            }
        }
    }

    /*
     * Necessary to filter the ssid list
     */
    private class NetworkFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            constraint = charSequence;

            if(charSequence == null || charSequence.length() == 0){
                results.values = allNetworks;
                results.count = allNetworks.size();
            }
            else{
                ArrayList<Network> filteredSSIDs = new ArrayList<>();

                charSequence = charSequence.toString().toLowerCase();
                for (Network n : allNetworks) {
                    String ssid = n.ssid;
                    if (ssid.toLowerCase().contains(charSequence)) {
                        filteredSSIDs.add(n);
                    }
                    results.values = filteredSSIDs;
                    results.count = filteredSSIDs.size();
                }

            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredNetworks.clear();
            filteredNetworks.addAll((ArrayList<Network>) filterResults.values);
            notifyDataSetChanged();
        }
    }

    // List that holds all network elements
    private List<Network> allNetworks;
    // List that holds all network elements that should be shown in the RecycleView
    private List<Network> filteredNetworks;

    // Filter
    private CharSequence constraint;
    private Filter mNetworkFilter;

    private Context context;
    private WifiManager wifiManager;


    public NetworkRecyclerAdapter(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        allNetworks = new ArrayList<>();
        filteredNetworks = new ArrayList<>();
        try{
            // Get the ssids from the ssids.json file
            updateSSIDsFromJsonFile();
        }
        catch(IOException e){
            // Could not read SSIDs from csv file.
        }
    }

    @Override
    public NetworkRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the layout
        View nodeView = inflater.inflate(R.layout.network_item, parent, false);

        // return a new holder instance
        return new ViewHolder(nodeView);
    }

    @Override
    public void onBindViewHolder(NetworkRecyclerAdapter.ViewHolder holder, int position) {
        // Get the network based on the position
        Network network = filteredNetworks.get(position);

        //Populate the data into the template view
        holder.tv_ssid.setText(network.ssid);
        if(network.active){
            if(network.deprecated){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.network_active_deprecated));
            }
            else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.network_active));
            }
        }
        else{
            if(network.deprecated){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.network_deprecated));
            }
            else{
                holder.itemView.setBackgroundColor(Color.WHITE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return filteredNetworks.size();
    }

    // For SearchView and Filter, see:
    // http://codetheory.in/android-filters/
    // https://coderwall.com/p/zpwrsg/add-search-function-to-list-view-in-android
    // https://stackoverflow.com/questions/11840344/android-custom-arrayadapter-doesnt-refresh-after-filter
    // https://stackoverflow.com/questions/27903361/searchmenuitem-getactionview-returning-null
    public Filter getFilter(){
        if(mNetworkFilter == null){
            mNetworkFilter = new NetworkFilter();
        }
        return mNetworkFilter;
    }


    /**
     * Returns pointer to the Network object at the passed position in the NodeRecyclerAdapter
     * @param adapterPosition Position in the NodeRecyclerAdapter
     * @return Pointer to the Node object in the NodeRecyclerAdapter
     */
    public Network getNetwork(int adapterPosition) {
        return filteredNetworks.get(adapterPosition);
    }

    /**
     * Returns pointer to the list of the shown networks in the RecyclerView
     * @return Pointer to the list of the shown networks
     */
    public List<Network> getShownNetworks() {
        return filteredNetworks;
    }

    /**
     * Updates the list of SSIDs by reading them from the ssids.json file. After that it checks
     * which ssids are already included in the network configuration (This function simply calls
     * updateNetworkStatus() at its end). It is NOT necessary to call notifyDataSetChanged() after
     * this function was used.
     * @throws IOException
     */
    public void updateSSIDsFromJsonFile() throws IOException {
        // Check if ssids.json exists in internal storage.
        File ssidsJson = context.getFileStreamPath("ssids.json");
        if(!ssidsJson.exists()){
            Log.d(TAG, "Copy ssids.json to internal storage.");
            // If not, copy ssids.json from assets to internal storage.
            FileOutputStream outputStream = context.openFileOutput("ssids.json", Context.MODE_PRIVATE);
            InputStream inputStream = context.getAssets().open("ssids.json");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            Log.d(TAG, "Finished copying ssids.json to internal storage");
        }

        // Read ssids.json from internal storage.
        String jsonString = "";
        InputStreamReader is = new InputStreamReader(new FileInputStream(ssidsJson));
        BufferedReader reader = new BufferedReader(is);
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString += line;
        }
        reader.close();

        // Read SSIDs from JSON file
        allNetworks.clear();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray ssidsJsonArray = json.getJSONArray("ssids");
            for(int i = 0; i<ssidsJsonArray.length(); i++){
                allNetworks.add(new Network('"' + ssidsJsonArray.getString(i) + '"'));
            }

            // Read the deprecated ssids
            JSONArray deprecatedJsonArray = json.optJSONArray("deprecated");
            if(deprecatedJsonArray != null){
                for(int i = 0; i<deprecatedJsonArray.length(); i++){
                    Network n = new Network('"' + deprecatedJsonArray.getString(i) + '"');
                    n.deprecated = true;
                    allNetworks.add(n);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(allNetworks);

        // Update the status of the ssids
        updateNetworkStatus();
    }

    /**
     * Updates the list of SSIDs by checking which SSIDs are already included in the network configuration.
     * It is NOT necessary to call notifyDataSetChanged() after this function was used.
     */
    public void updateNetworkStatus(){
        // Check which Network is already added to the network configuration
        List<WifiConfiguration> wifiConf = wifiManager.getConfiguredNetworks();
        if (wifiConf != null) {
            Collections.sort(wifiConf, new WifiConfigurationComparator());
            for (Network n : allNetworks) {
                // Search for the current network in the network configuration. If it is found (index will be >= 0), set active to true
                int index = Collections.binarySearch(wifiConf, n.ssid, new WifiConfigurationSSIDComparator());
                n.active = index >= 0;
            }
        }

        // if deprecated ssids should not be shown, remove all networks from the list that are deprecated and not included in the
        // network configuration.
        SharedPreferences prefMan = PreferenceManager.getDefaultSharedPreferences(context);
        boolean show_deprecated = prefMan.getBoolean("pref_deprecated_ssids", false);
        if(!show_deprecated){
            for(Iterator<Network> it = allNetworks.iterator(); it.hasNext();) {
                Network n = it.next();
                if( n.deprecated && !n.active){
                    it.remove();
                }
            }
        }

        // update filteredNetworks (and with it the RecycleView) by filtering with the last used constaint
        getFilter().filter(constraint);
    }


    /***************************
     * This listener is used to report back problems with removing networks to the element containing the
     * NetworkRecyclerAdapter.
     ***************************/

    public interface OnAdapterInteractionListener {
        void onRemoveSsidFailed();
    }

    private OnAdapterInteractionListener onAdapterInteractionListener = null;

    public void setOnAdapterInteractionListener(OnAdapterInteractionListener listener){
        onAdapterInteractionListener = listener;
    }
}
