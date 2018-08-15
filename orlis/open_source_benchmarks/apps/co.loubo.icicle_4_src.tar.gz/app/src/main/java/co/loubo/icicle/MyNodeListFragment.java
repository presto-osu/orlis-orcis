package co.loubo.icicle;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.app.ListFragment;
import android.widget.TextView;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyNodeListFragment extends ListFragment {



    public interface OnItemSelectedListener {
        public void redrawNodeManagementActionBar();
    }
    private OnItemSelectedListener listener;
    private CopyOnWriteArrayList<LocalNode> values;
    private GlobalState gs;
    // This is the Adapter being used to display the list's data
    private NodeManagerArrayAdapter mAdapter;
    private int lastChecked = -1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.gs = (GlobalState) getActivity().getApplication();
        values = this.gs.getLocalNodeList();
        mAdapter = new NodeManagerArrayAdapter(getActivity(),values);

        ListView list = getListView();


        setListAdapter(mAdapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setDivider(getResources().getDrawable(R.drawable.divider));
        list.setSelector(getResources().getDrawable(R.drawable.list_selection_background));

        if(savedInstanceState!= null) {
            list.setItemChecked(savedInstanceState.getInt(Constants.CHECKED_ITEM), true);
            if (listener != null) {
                listener.redrawNodeManagementActionBar();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CHECKED_ITEM,getListView().getCheckedItemPosition());
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
        if(listener != null) {
            listener.redrawNodeManagementActionBar();
        }
    }

    private class NodeManagerArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private CopyOnWriteArrayList<LocalNode> values;

        public NodeManagerArrayAdapter(Context context, CopyOnWriteArrayList<LocalNode> values) {
            super(context, R.layout.peer);
            this.context = context;
            this.values = values;
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
            peerName.setText(values.get(position).getName());
            peerAddress.setText(values.get(position).getAddress()+":"+values.get(position).getPort());
            ImageView peerStatus = (ImageView)rowView.findViewById(R.id.peer_icon);
            if(gs.getActiveLocalNodeIndex() == position){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    peerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white_36dp));
                    peerStatus.setBackground(getResources().getDrawable(R.drawable.round_button_green));
                }else{
                    peerStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_black_36dp));
                }
                peerStatus.setVisibility(View.VISIBLE);
            }else{
                peerStatus.setVisibility(View.INVISIBLE);
            }

            return rowView;
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public CopyOnWriteArrayList<LocalNode> getValues() {
        return values;
    }
}