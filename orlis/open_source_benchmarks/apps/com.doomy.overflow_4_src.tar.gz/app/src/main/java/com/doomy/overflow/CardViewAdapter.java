/**
 * Copyright (C) 2013 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.overflow;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {

    // Declaring your view and variables
    private ArrayList<Message> mMessage;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardViewAdapter(ArrayList<Message> myMessage) {
        this.mMessage = myMessage;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.cardview, null);

        // Create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - Get element from your dataset at this position
        // - Replace the contents of the view with that element
        viewHolder.setTitle.setText(mMessage.get(position).getRecipient());
        viewHolder.setTitle.setTextColor(viewHolder.setTitle.getContext().getResources().getColor(mMessage.get(position).getColor()));
        viewHolder.setInfo.setText(mMessage.get(position).getText());
        viewHolder.setLogo.setColorFilter(viewHolder.setLogo.getContext().getResources().getColor(mMessage.get(position).getColor()));
        viewHolder.setNumber.setText(mMessage.get(position).getQuantity());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    // Inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Declaring your view and variables
        public TextView setTitle;
        public TextView setInfo;
        public ImageView setLogo;
        public TextView setNumber;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            setTitle = (TextView) itemLayoutView.findViewById(R.id.cardTitle);
            setInfo = (TextView) itemLayoutView.findViewById(R.id.cardInfo);
            setLogo = (ImageView) itemLayoutView.findViewById(R.id.cardLogo);
            setNumber = (TextView) itemLayoutView.findViewById(R.id.cardNumber);
        }
    }
}
