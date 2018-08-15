package org.itishka.pointim.listeners;

import android.support.annotation.NonNull;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.Post;

/**
 * Created by Tishka17 on 08.05.2016.
 */
public interface OnCommentActionsListener {

    void onBookmark(@NonNull Post post, final CheckBox button);

    void onMenuClicked(@NonNull Post post, @NonNull Comment comment, Menu menu, MenuItem item);

    void updateMenu(Menu menu, ShareActionProvider provider, Comment comment);
}
