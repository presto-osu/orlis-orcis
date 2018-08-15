package com.example.tobiastrumm.freifunkautoconnect;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;


/**
 * See https://guides.codepath.com/android/Using-the-RecyclerView for a tutorial on RecyclerView and
 * RecyclerView.Adapter
 */
public class NodeRecyclerAdapter extends RecyclerView.Adapter<NodeRecyclerAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tv_name;
        public TextView tv_status;
        public TextView tv_distance;

        public ViewHolder(final View itemView) {
            super(itemView);

            tv_name = (TextView) itemView.findViewById(R.id.tv_node_item_name);
            tv_status = (TextView) itemView.findViewById(R.id.tv_node_item_status);
            tv_distance = (TextView) itemView.findViewById(R.id.tv_node_item_distance);

            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if(listener != null){
                        listener.onItemClick(itemView, getLayoutPosition(), getAdapterPosition());
                    }
                }
            });
        }
    }

    // List that holds all node elements that are shown in the RecyclerView
    private List<Node> nodelist;

    // Define the listener interface
    public interface OnItemClickListener{
        void onItemClick(View itemView, int layoutPosition, int adapterPosition);
    }
    // Listener member variable
    private OnItemClickListener listener;

    // Define the method that allows the parent fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public NodeRecyclerAdapter(List<Node> nodelist){
        this.nodelist = nodelist;
    }

    /**
     *  Clean all elements of the recycler
     */
    public void clear(){
        nodelist.clear();
        notifyDataSetChanged();
    }

    /**
     * Add a list of items to the recycler
     * @param list A list containing the items that should be added.
     */
    public void addAll(List<Node> list){
        nodelist.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Returns pointer to the Node object at the passed position in the NodeRecyclerAdapter
     * @param adapterPosition Position in the NodeRecyclerAdapter
     * @return Pointer to the Node object in the NodeRecyclerAdapter
     */
    public Node getNode(int adapterPosition){
        return nodelist.get(adapterPosition);
    }

    @Override
    public NodeRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the layout
        View nodeView = inflater.inflate(R.layout.node_item, parent, false);

        // return a new holder instance
        return new ViewHolder(nodeView);
    }

    @Override
    public void onBindViewHolder(NodeRecyclerAdapter.ViewHolder holder, int position) {
        // Get the node based on the position
        Node node = nodelist.get(position);

        // Get the context (Needed to access the strings in string.xml)
        Context context = holder.tv_status.getContext();

        //Populate the data into the template view
        holder.tv_name.setText(node.name);

        if(node.online){
            holder.tv_status.setText(context.getString(R.string.online));
            holder.tv_status.setTextColor(ContextCompat.getColor(context, R.color.node_status_online));
        }
        else{
            holder.tv_status.setText(context.getString(R.string.offline));
            holder.tv_status.setTextColor(Color.RED);
        }

        if(node.distance < 1000){
            holder.tv_distance.setText(context.getString(R.string.distance_m, (int)node.distance));
        }
        else{
            double distance_km = node.distance / 1000.0d;
            String distance_string = new DecimalFormat("###.000").format(distance_km);
            holder.tv_distance.setText(context.getString(R.string.distance_km, distance_string));
        }
    }

    @Override
    public int getItemCount() {
        return nodelist.size();
    }
}
