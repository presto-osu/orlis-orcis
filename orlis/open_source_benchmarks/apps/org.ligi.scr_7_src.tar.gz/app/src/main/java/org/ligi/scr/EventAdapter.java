package org.ligi.scr;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.ligi.scr.model.Event;

import java.util.ArrayList;

class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    final ArrayList<Event> events;

    public EventAdapter(ArrayList<Event> events) {
        this.events = events;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final CardView res = (CardView) inflater.inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(res);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        holder.apply(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

}
