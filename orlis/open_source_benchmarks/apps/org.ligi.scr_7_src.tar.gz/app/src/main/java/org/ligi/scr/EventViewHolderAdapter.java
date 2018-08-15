package org.ligi.scr;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import info.metadude.java.library.halfnarp.model.GetTalksResponse;

class EventViewHolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_IMAGE = 0;
    public static final int VIEW_TYPE_EVENT = 1;

    private final List<GetTalksResponse> talkResponses;

    public EventViewHolderAdapter(List<GetTalksResponse> talkPreferencesResponses) {
        this.talkResponses = talkPreferencesResponses;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == talkResponses.size() + 1) {
            return VIEW_TYPE_IMAGE;
        } else {
            return VIEW_TYPE_EVENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_EVENT:
                final CardView res = (CardView) inflater.inflate(R.layout.item_event, parent, false);
                return new EventViewHolder(res);

            default:
            case VIEW_TYPE_IMAGE:
                final ImageView iv = (ImageView) inflater.inflate(R.layout.item_image, parent, false);
                return new ImageViewHolder(iv);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_EVENT) {
            ((EventViewHolder)holder).apply(talkResponses.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return talkResponses.size() + 2;
    }

    class  ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageViewHolder(View itemView) {
            super(itemView);
        }
    }
}

