package com.luorrak.ouroboros.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.deepzoom.DeepZoomActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private String boardName;
    private String resto;
    private ArrayList<Media> mediaItems;
    private List<String> validExt = Arrays.asList(".png", ".jpg", ".jpeg", ".gif");
    private Context context;
    public GalleryAdapter(ArrayList<Media> mediaItems, String boardName, String resto, Context context) {
        this.mediaItems = mediaItems;
        this.boardName = boardName;
        this.resto = resto;
        this.context = context;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        final GalleryViewHolder galleryViewHolder = holder;
        final Media media = mediaItems.get(position);
        resetView(galleryViewHolder);

        Ion.with(galleryViewHolder.galleryImage)
                .load(ChanUrls.getThumbnailUrl(boardName, media.fileName))
                .withBitmapInfo();

        if (validExt.contains(media.ext)){
            galleryViewHolder.galleryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchDeepZoomIntent(media.fileName);
                }
            });
        } else if (media.ext.equals(".webm") || media.ext.equals(".mp4")){
            galleryViewHolder.playButton.setVisibility(View.VISIBLE);
            galleryViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchVideoIntent(galleryViewHolder, boardName, media.fileName, media.ext);
                    }
                });
        }
    }

    private void resetView(GalleryViewHolder galleryViewHolder){
        galleryViewHolder.playButton.setVisibility(View.GONE);
        galleryViewHolder.galleryImage.setOnClickListener(null);
        galleryViewHolder.playButton.setOnClickListener(null);
    }

    private void launchDeepZoomIntent(String fileName){
        Intent intent = new Intent(context, DeepZoomActivity.class);
        intent.putExtra(Util.TIM, fileName);
        intent.putExtra(Util.INTENT_THREAD_NO, resto);
        intent.putExtra(Util.INTENT_BOARD_NAME, boardName);
        context.startActivity(intent);
    }

    private void launchVideoIntent(GalleryViewHolder galleryViewHolder, String boardName, String fileName, String ext){
        Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, fileName, ext));
        String type = ext.equals(".webm") ? "video/webm" : "video/mp4";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }
    
    class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView galleryImage;
        ImageView playButton;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            galleryImage = (ImageView) itemView.findViewById(R.id.gallery_image);
            playButton = (ImageView) itemView.findViewById(R.id.gallery_video_play_button);
        }
    }
}
