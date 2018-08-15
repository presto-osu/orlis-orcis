package com.example.tobiastrumm.freifunkautoconnect;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddRemoveNetworksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddRemoveNetworksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRemoveNetworksFragment extends Fragment implements FragmentLifecycle, NetworkRecyclerAdapter.OnAdapterInteractionListener{

    private static final String TAG = AddRemoveNetworksFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    // Network
    private NetworkRecyclerAdapter networkRecyclerAdapter;


    // SearchView
    private SearchView searchView;
    private String last_filter_term;

    // ProgressBar
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private boolean showProgress;
    private int progress_max_value;

    @Override
    public void onRemoveSsidFailed() {
        mListener.showDialogSSIDRemovalFailed();
    }

    private class AddAllNetworksResponseReceiver extends BroadcastReceiver{
        private AddAllNetworksResponseReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getStringExtra(AddAllNetworksService.STATUS_TYPE)){
                case AddAllNetworksService.STATUS_TYPE_FINISHED:
                    networkRecyclerAdapter.updateNetworkStatus();
                    hideProgressBar();
                    break;
                case RemoveAllNetworksService.STATUS_TYPE_PROGRESS:
                    int progressBarProgress = intent.getIntExtra(AddAllNetworksService.STATUS_PROGRESS, 0);
                    updateProgressBar(progressBarProgress);
                    break;
            }
        }
    }

    private class RemoveAllNetworksResponseReceiver extends BroadcastReceiver{
        private RemoveAllNetworksResponseReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent){
            switch(intent.getStringExtra(RemoveAllNetworksService.STATUS_TYPE)){
                case RemoveAllNetworksService.STATUS_TYPE_FINISHED:
                    networkRecyclerAdapter.updateNetworkStatus();
                    int failed_removals = intent.getIntExtra(RemoveAllNetworksService.STATUS_FAILED_REMOVALS, 0);
                    hideProgressBar();
                    if(failed_removals != 0){
                        mListener.showDialogRemoveAllRemovalFailed(failed_removals);
                    }
                    break;
                case RemoveAllNetworksService.STATUS_TYPE_PROGRESS:
                    int progressBarProgress = intent.getIntExtra(AddAllNetworksService.STATUS_PROGRESS, 0);
                    updateProgressBar(progressBarProgress);
                    break;
            }
        }
    }

    private class DownloadSsidJsonResponseReceiver extends BroadcastReceiver {
        private DownloadSsidJsonResponseReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getStringExtra(DownloadSsidJsonService.STATUS_TYPE)){
                case DownloadSsidJsonService.STATUS_TYPE_REPLACED:
                    try {
                        // Read ssid from file again.
                        networkRecyclerAdapter.updateSSIDsFromJsonFile();
                        Log.d(TAG, "SSIDs were refreshed");

                        // Notify user that a new SSID list was downloaded.
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.message_ssids_updated), Toast.LENGTH_LONG);
                        toast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    AddAllNetworksResponseReceiver addAllNetworksResponseReceiver;
    RemoveAllNetworksResponseReceiver removeAllNetworksResponseReceiver;
    DownloadSsidJsonResponseReceiver downloadSsidJsonResponseReceiver;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment AddRemoveNetworksFragment.
     */
    public static AddRemoveNetworksFragment newInstance() {
        return new AddRemoveNetworksFragment();
    }

    public AddRemoveNetworksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);


        setupBroadcastReceivers();

        // Register downloadSsidJsonResponseReceiver to get notified if the ssid file is changed.
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter downloadSsidJsonIntentFilter = new IntentFilter(DownloadSsidJsonService.BROADCAST_ACTION);
        lbm.registerReceiver(downloadSsidJsonResponseReceiver, downloadSsidJsonIntentFilter);

        // Setup NodeRecyclerAdapter
        networkRecyclerAdapter = new NetworkRecyclerAdapter(getActivity());
        networkRecyclerAdapter.setOnAdapterInteractionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister the downloadSsidJsonResponseRecceiver
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.unregisterReceiver(downloadSsidJsonResponseReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_remove_networks, container, false);

        linearLayout = (LinearLayout) view.findViewById(R.id.ll_add_remove_networks);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_add_remove_networks);
        tv_progress = (TextView) view.findViewById(R.id.tv_progresbar);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.rl_add_remove_networks);

        // Setup RecyclerView
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_networks);
        rv.setAdapter(networkRecyclerAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set OnClickListeners for the buttons
        view.findViewById(R.id.btn_add_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showDialogAddAllNetworks();
            }
        });
        view.findViewById(R.id.btn_remove_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showDialogRemoveAllNetworks();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastReceivers();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceivers();
        // Necessary because the network configuration or the "show deprecated SSIDs" setting might be changed in the meantime.
        try{
            networkRecyclerAdapter.updateSSIDsFromJsonFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // The service could have finished while no Broadcast Receiver was registered that could have received the signal to set showProgress to false;
        if(showProgress && (isAddAllNetworkServiceRunning() || isRemoveAllNetworkServiceRunning())){
            showProgressBar(progress_max_value);
        }
        else{
            hideProgressBar();
        }
    }


    @Override
    public void onPauseFragment() {
        // Save the last filter term. It is necessary to recreate the last search if the user swiped/paged to a different tab.
        last_filter_term = searchView.getQuery().toString();
    }

    @Override
    public void onResumeFragment() {
        // Necessary because the network configuration or the "show deprecated SSIDs" setting might be changed in the meantime.
        try{
            networkRecyclerAdapter.updateSSIDsFromJsonFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_remove_fragment, menu);
        // Setup SearchView
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                networkRecyclerAdapter.getFilter().filter(newText);
                return true;
            }
        });
        // Recreate the last search if the screen was rotated
        if(last_filter_term != null && last_filter_term.length() > 0){
            searchView.setQuery(last_filter_term, true);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        // Save the last filter term. It is necessary to recreate the last search if the screen was rotated.
        if(searchView != null){
            last_filter_term = searchView.getQuery().toString();
        }

    }

    private void showProgressBar(int maxValue) {
        showProgress = true;
        progress_max_value = maxValue;
        progressBar.setMax(maxValue);
        tv_progress.setText(getString(R.string.progress, 0, progress_max_value));
        relativeLayout.setVisibility(RelativeLayout.GONE);
        searchView.setVisibility(SearchView.GONE);
        linearLayout.setVisibility(LinearLayout.VISIBLE);
    }

    private void hideProgressBar(){
        showProgress = false;
        linearLayout.setVisibility(LinearLayout.GONE);
        if(searchView != null){
            searchView.setVisibility(SearchView.VISIBLE);
        }
        relativeLayout.setVisibility(RelativeLayout.VISIBLE);
    }

    private void updateProgressBar(int value){
        progressBar.setProgress(value);
        tv_progress.setText(getString(R.string.progress, value, progress_max_value));
    }


    private void setupBroadcastReceivers(){
        addAllNetworksResponseReceiver = new AddAllNetworksResponseReceiver();
        removeAllNetworksResponseReceiver = new RemoveAllNetworksResponseReceiver();
        downloadSsidJsonResponseReceiver = new DownloadSsidJsonResponseReceiver();

    }

    private void registerBroadcastReceivers(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());

        IntentFilter addAllIntentFilter = new IntentFilter(AddAllNetworksService.BROADCAST_ACTION);
        lbm.registerReceiver(addAllNetworksResponseReceiver, addAllIntentFilter);

        IntentFilter removeAllIntentFilter = new IntentFilter(RemoveAllNetworksService.BROADCAST_ACTION);
        lbm.registerReceiver(removeAllNetworksResponseReceiver, removeAllIntentFilter);
    }

    private void unregisterBroadcastReceivers(){
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.unregisterReceiver(addAllNetworksResponseReceiver);
        lbm.unregisterReceiver(removeAllNetworksResponseReceiver);
    }



    public void addAllNetworks(){
        // Add only currently shown networks
        // Tell Activity to show a ProgressDialog
        int progressBarMax = networkRecyclerAdapter.getItemCount();
        showProgressBar(progressBarMax);
        updateProgressBar(0);

        //Start AddAllNetworksService
        Intent intent = new Intent(getActivity(), AddAllNetworksService.class);
        intent.putParcelableArrayListExtra(AddAllNetworksService.INPUT_NETWORKS, new ArrayList<>(networkRecyclerAdapter.getShownNetworks()));
        getActivity().startService(intent);
    }

    public void removeAllNetworks(){
        // Tell Activity to show a ProgressDialog
        int progressBarMax = networkRecyclerAdapter.getItemCount();
        showProgressBar(progressBarMax);
        updateProgressBar(0);

        //Start RemoveAllNetworksService
        Intent intent = new Intent(getActivity(), RemoveAllNetworksService.class);
        intent.putParcelableArrayListExtra(RemoveAllNetworksService.INPUT_NETWORKS, new ArrayList<>(networkRecyclerAdapter.getShownNetworks()));
        getActivity().startService(intent);
    }


    private boolean isAddAllNetworkServiceRunning(){
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AddAllNetworksService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRemoveAllNetworkServiceRunning(){
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        for( ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(RemoveAllNetworksService.class.getName().equals((service.service.getClassName()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Should open a dialog to confirm that all shown networks should be added to the network configuration.
         */
        void showDialogAddAllNetworks();

        /**
         * Should open a dialog to confirm that all shown networks should be removed from the network configuration.
         */
        void showDialogRemoveAllNetworks();

        /**
         * Should open a dialog that informs the user that the SSID he tried to remove could not be removed.
         */
        void showDialogSSIDRemovalFailed();

        /**
         * Should open a dialog that informs the user about the number of SSIDs that could not be removed.
         * @param number_failed_removal Number of SSIDs that could could not be removed.
         */
        void showDialogRemoveAllRemovalFailed(int number_failed_removal);
    }

}
