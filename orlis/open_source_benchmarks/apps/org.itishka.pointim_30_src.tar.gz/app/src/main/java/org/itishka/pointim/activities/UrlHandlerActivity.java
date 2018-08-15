package org.itishka.pointim.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public class UrlHandlerActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = null;
        Uri uri = getIntent().getData();
        String tag = uri.getQueryParameter("tag");
        String post = uri.getLastPathSegment();
        String comment = uri.getFragment();
        String user = getUser(uri.getHost());
        if (TextUtils.isEmpty(post)) {
            if (TextUtils.isEmpty(tag)) {
                if (TextUtils.isEmpty(user)) { //all
                    intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_TARGET, "all");
                } else { //blog
                    intent = new Intent(this, UserViewActivity.class);
                    intent.putExtra(UserViewActivity.EXTRA_USER, user);
                }
            } else { //tag
                intent = new Intent(this, TagViewActivity.class);
                intent.putExtra(TagViewActivity.EXTRA_TAG, tag);
                intent.putExtra(TagViewActivity.EXTRA_USER, user);
            }
        } else if ("recent".equals(post)
                || "all".equals(post)
                || "comments".equals(post)) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TARGET, post);
        } else if ("bookmarks".equals(post)) {
            intent = new Intent(this, BookmarksActivity.class);
            intent.putExtra(MainActivity.EXTRA_TARGET, post);
        } else { //post
            intent = new Intent(this, SinglePostActivity.class);
            intent.putExtra(SinglePostActivity.EXTRA_POST, post);
            intent.putExtra(SinglePostActivity.EXTRA_COMMENT, comment);
        }
        startActivity(intent);
        finish();
    }

    String getUser(String host) {
        if (host.endsWith(".point.im")) {
            return host.split("\\.point.im")[0];
        }
        return null;
    }
}
