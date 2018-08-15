package co.loubo.icicle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.pterodactylus.fcp.Peer;

import java.util.concurrent.CopyOnWriteArrayList;

public class ListRecipientsFragment extends ListFragment {



    public interface OnItemSelectedListener {
        public void redrawMessageList();
    }
    private OnItemSelectedListener listener;
    private CopyOnWriteArrayList<Peer> values;
    // This is the Adapter being used to display the list's data
    private RecipientListArrayAdapter mAdapter;
    private int lastChecked = -1;
    private Menu menu;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GlobalState gs = (GlobalState) getActivity().getApplication();
        values = gs.getDarknetPeerList();
        mAdapter = new RecipientListArrayAdapter(getActivity(),values);

        ListView list = getListView();


        setListAdapter(mAdapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setDivider(getResources().getDrawable(R.drawable.divider));
        list.setSelector(getResources().getDrawable(R.drawable.list_selection_background));

        if(savedInstanceState!= null) {
            list.setItemChecked(savedInstanceState.getInt(Constants.CHECKED_ITEM), true);
            if (listener != null) {
                listener.redrawMessageList();
            }
        }
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CHECKED_ITEM, getListView().getCheckedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) activity;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if(lastChecked == position){
            l.setItemChecked(position, false);
            lastChecked = -1;
        }else {
            l.setItemChecked(position, true);
            lastChecked = position;
        }
        if(lastChecked >= 0){
            menu.findItem(R.id.action_forward).setVisible(true);
        }else{
            menu.findItem(R.id.action_forward).setVisible(false);
        }
    }

    public Peer getSelectedPeer() {
        if(lastChecked >= 0){
            return values.get(lastChecked);
        }
        return null;
    }

    private class RecipientListArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private CopyOnWriteArrayList<Peer> values;
        private GlobalState gs;

        public RecipientListArrayAdapter(Context context, CopyOnWriteArrayList<Peer> values) {
            super(context, R.layout.peer);
            this.context = context;
            this.values = values;
            this.gs = (GlobalState)getActivity().getApplication();
        }

        public int getCount (){
            return values.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = convertView;
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.peer, parent, false);
            }
            TextView peerName = (TextView) rowView.findViewById(R.id.peer_name);
            TextView peerAddress = (TextView) rowView.findViewById(R.id.peer_address);
            ImageView peerIcon = (ImageView)rowView.findViewById(R.id.peer_icon);
            Peer p = values.get(position);
            if(p.getMyName() != null){
                peerName.setText(p.getMyName());
            }else{
                peerName.setText(rowView.getContext().getString(R.string.unnamed_peer));
            }
            if(p.getVolatile("status").equals(Constants.FNconnected)){
                peerIcon.setColorFilter(getResources().getColor(R.color.green_500));
            }else if(p.getVolatile("status").equals(Constants.FNbackedoff)){
                peerIcon.setColorFilter(getResources().getColor(R.color.orange_500));
            }else{
                peerIcon.setColorFilter(getResources().getColor(R.color.grey_500));
            }
            peerAddress.setText(p.getPhysicalUDP().replace(";", "\n"));



            return rowView;
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public CopyOnWriteArrayList<Peer> getValues() {
        return values;
    }
}