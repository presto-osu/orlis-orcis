package com.luorrak.ouroboros.deepzoom;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Util;

import uk.co.senab.photoview.PhotoView;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class DeepZoomFragment extends Fragment{
    private PhotoView photoView;
    private ProgressBar progressBar;
    private NetworkHelper networkHelper;
    private InfiniteDbHelper infiniteDbHelper;
    private int position;
    private String boardName;
    private String resto;
    private Media mediaItem;
    private ImageView mediaPlayButton;
    private ActionProvider shareActionProvider;

    public Fragment newInstance(String boardName, String resto, int position) {
        DeepZoomFragment deepZoomFragment = new DeepZoomFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        args.putString("resto", resto);
        args.putInt("position", position);
        deepZoomFragment.setArguments(args);
        return deepZoomFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkHelper = new NetworkHelper();
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        if (getArguments() != null){
            boardName = getArguments().getString("boardName");
            resto = getArguments().getString("resto");
            position = getArguments().getInt("position");
        }

        if (savedInstanceState != null){
            boardName = savedInstanceState.getString("boardName");
            resto = savedInstanceState.getString("resto");
            position = savedInstanceState.getInt("position");
        }
        ((DeepZoomActivity) getActivity()).newMediaListInstance(infiniteDbHelper, resto);
        mediaItem = ((DeepZoomActivity) getActivity()).getMediaItem(position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_deepzoom, container, false);
        setHasOptionsMenu(true);
        final LinearLayout deepzoomContainer = (LinearLayout) rootView.findViewById(R.id.deepzoom_container);
        photoView = (PhotoView) rootView.findViewById(R.id.deepzoom_photoview);
        photoView.setMaximumScale(24);
        mediaPlayButton = (ImageView) rootView.findViewById(R.id.deepzoom_media_play_button);
        mediaPlayButton.setVisibility(View.GONE);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        Ion.with(photoView)
                .load(ChanUrls.getThumbnailUrl(boardName, mediaItem.fileName))
                .withBitmapInfo()
                .setCallback(new FutureCallback<ImageViewBitmapInfo>() {
                    @Override
                    public void onCompleted(Exception e, ImageViewBitmapInfo result) {
                        if (e != null) {
                            return;
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        if (result.getException() == null){
                            Util.setSwatch(deepzoomContainer, result);
                        }

                        if (mediaItem.ext.equals(".webm") || mediaItem.ext.equals(".mp4")) {
                            return;
                        }

                        Ion.with(photoView)
                                .crossfade(true)
                                .deepZoom()
                                .load(ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext))
                                .withBitmapInfo();
                    }
                });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mediaItem.ext.equals(".webm") || mediaItem.ext.equals(".mp4")){
            mediaPlayButton.setVisibility(View.VISIBLE);
            mediaPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "video/" + mediaItem.ext.substring(1));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("boardName", boardName);
        outState.putString("resto", resto);
        outState.putInt("position", position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_deep_zoom, menu);
        MenuItem saveImage = menu.findItem(R.id.action_save_image);
        MenuItem openExternalButton = menu.findItem(R.id.action_external_browser);
        MenuItem shareButton = menu.findItem(R.id.menu_item_share);

        shareButton.setVisible(true);
        saveImage.setVisible(true);
        openExternalButton.setVisible(true);
        getActivity().setTitle(mediaItem.fileName + mediaItem.ext);

        shareActionProvider = MenuItemCompat.getActionProvider(shareButton);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_image: {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.REQUEST_STORAGE_PERMISSION);
                } else {
                    startDownload();
                }
                break;
            }
            case R.id.action_external_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext)));
                startActivity(browserIntent);
                break;
            }
            case R.id.menu_item_share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = ChanUrls.getImageUrl(boardName, mediaItem.fileName, mediaItem.ext);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void startDownload(){
        Snackbar.make(getView(), "Downloading...", Snackbar.LENGTH_LONG).show();
        networkHelper.downloadFile(boardName, mediaItem.fileName, mediaItem.ext, getActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Util.REQUEST_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    startDownload();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(getView(), "Requires Permission", Snackbar.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
