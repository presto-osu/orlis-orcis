package org.itishka.pointim.listeners;

import android.support.annotation.NonNull;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import org.itishka.pointim.model.point.Post;

/**
 * Created by Tishka17 on 02.05.2016.
 */
public interface OnPostActionsListener {
    void onBookmark(@NonNull Post post, final CheckBox button);

    void onMenuClicked(@NonNull Post post, Menu menu, MenuItem item);

    void updateMenu(Menu menu, ShareActionProvider provider, Post post);
}
