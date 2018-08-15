package co.loubo.icicle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

public class FriendNodeListFragment extends ListFragment {



    public interface OnItemSelectedListener {
        public void redrawFriendNodeManagement();
    }
    private OnItemSelectedListener listener;
    private CopyOnWriteArrayList<FriendNode> values;
    // This is the Adapter being used to display the list's data
    private NodeManagerArrayAdapter mAdapter;
    private int lastChecked = -1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GlobalState gs = (GlobalState) getActivity().getApplication();
        values = gs.getFriendNodes();
        mAdapter = new NodeManagerArrayAdapter(getActivity(),values);

        ListView list = getListView();


        setListAdapter(mAdapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setDivider(getResources().getDrawable(R.drawable.divider));
        list.setSelector(getResources().getDrawable(R.drawable.list_selection_background));

        if(savedInstanceState!= null) {
            list.setItemChecked(savedInstanceState.getInt(Constants.CHECKED_ITEM), true);
            if (listener != null) {
                listener.redrawFriendNodeManagement();
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
            listener.redrawFriendNodeManagement();
        }
    }

    private class NodeManagerArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private CopyOnWriteArrayList<FriendNode> values;

        public NodeManagerArrayAdapter(Context context, CopyOnWriteArrayList<FriendNode> values) {
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
            peerAddress.setText(getResources().getString(R.string.trust) + ": " + values.get(position).getTrust()+"\n"+
                    getResources().getString(R.string.visibility) + ": " + values.get(position).getVisibility());
            ImageView peerStatus = (ImageView)rowView.findViewById(R.id.peer_icon);
            peerStatus.setVisibility(View.INVISIBLE);

            return rowView;
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public CopyOnWriteArrayList<FriendNode> getValues() {
        return values;
    }
}