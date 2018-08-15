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

public class ListMessagesFragment extends ListFragment {



    public interface OnItemSelectedListener {
        public void redrawMessageList();
    }
    private OnItemSelectedListener listener;
    private CopyOnWriteArrayList<FreenetMessage> values;
    // This is the Adapter being used to display the list's data
    private MessageListArrayAdapter mAdapter;
    private int lastChecked = -1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GlobalState gs = (GlobalState) getActivity().getApplication();
        values = gs.getMessageList();
        mAdapter = new MessageListArrayAdapter(getActivity(),values);

        ListView list = getListView();


        setListAdapter(mAdapter);
        list.setChoiceMode(ListView.CHOICE_MODE_NONE);
        list.setDivider(getResources().getDrawable(R.drawable.divider));
        list.setSelector(getResources().getDrawable(R.drawable.list_selection_background));

        if(savedInstanceState!= null) {
            list.setItemChecked(savedInstanceState.getInt(Constants.CHECKED_ITEM), true);
            if (listener != null) {
                listener.redrawMessageList();
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

    }

    private class MessageListArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private CopyOnWriteArrayList<FreenetMessage> values;
        private GlobalState gs;

        public MessageListArrayAdapter(Context context, CopyOnWriteArrayList<FreenetMessage> values) {
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
                rowView = inflater.inflate(R.layout.message_summary, parent, false);
            }
            TextView sender = (TextView) rowView.findViewById(R.id.message_name);
            TextView messageDate = (TextView) rowView.findViewById(R.id.message_date);
            TextView messageText = (TextView) rowView.findViewById(R.id.message_summary);
            sender.setText(values.get(position).getSender());
            messageText.setText(values.get(position).getMessage());
            getActivity().getApplication();
            messageDate.setText(gs.getPrettyDate(values.get(position).getDate()));
            return rowView;
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public CopyOnWriteArrayList<FreenetMessage> getValues() {
        return values;
    }
}