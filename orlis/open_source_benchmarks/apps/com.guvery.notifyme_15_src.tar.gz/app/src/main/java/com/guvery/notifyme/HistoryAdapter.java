package com.guvery.notifyme;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aaron on 10/27/2014.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<Notif> mDataset;
    private Context mContext;

    public HistoryAdapter(ArrayList<Notif> myDataset, Context c) {
        mContext = c;
        mDataset = myDataset;
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_history, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Notif n = mDataset.get(position);

        holder.mTitle.setText(n.getTitle());

        holder.mBody.setText(n.getBody());
        /*if (n.getBody().isEmpty()) {
            holder.mBody.setVisibility(View.GONE);
        } else {
            holder.mBody.setVisibility(View.VISIBLE);
            holder.mBody.setText(n.getBody());
        }*/

        // ~~~ Details
        String sDetails;
        if (n.getPriority() == 2) {
            sDetails = "High Priority";
        } else if (n.getPriority() == -2) {
            sDetails = "Low Priority";
        } else {
            sDetails = "";
        }
        if (n.isOngoing() && n.isBigTextStyle()) {
            sDetails += sDetails.equals("") ? "Ongoing, List" : ", Ongoing, List";
        } else if (n.isOngoing() && !n.isBigTextStyle()) {
            sDetails += sDetails.equals("") ? "Ongoing" : ", Ongoing";
        } else if (!n.isOngoing() && n.isBigTextStyle()) {
            sDetails += sDetails.equals("") ? "List" : ", List";
        }
        holder.mDetails.setText(sDetails);

        holder.mIcon.setImageResource(ImageAdapter.getDarkFromLight(n.getImageId()));
        /*if (sDetails.length() == 0) {
            holder.mDetails.setVisibility(View.GONE);
        } else {
            holder.mBody.setVisibility(View.VISIBLE);
            holder.mDetails.setText(sDetails);
        }*/
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onClick(View v) {
        //v.getContext().get
        //Toast.makeText(v.getContext(), "Tapped", Toast.LENGTH_SHORT).show();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTitle;
        public TextView mBody;
        public TextView mDetails;
        public ImageView mIcon;

        public ViewHolder(View v) {
            super(v);
            mTitle = (TextView) v.findViewById(R.id.title);
            mBody = (TextView) v.findViewById(R.id.body);
            mDetails = (TextView) v.findViewById(R.id.details);
            mIcon = (ImageView) v.findViewById(R.id.imageView);
        }
    }
}