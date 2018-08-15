package org.itishka.pointim.listeners;

import android.net.Uri;
import android.view.View;

/**
 * Created by Tishka17 on 26.04.2016.
 */
public interface OnPointClickListener {
    void onPostClicked(String post);
    void oCommentClicked(String post, String comment);
    void onUserClicked(String user);
    void onBrowserLinkClicked(Uri link);
    void onTagClicked(String tag);
}
