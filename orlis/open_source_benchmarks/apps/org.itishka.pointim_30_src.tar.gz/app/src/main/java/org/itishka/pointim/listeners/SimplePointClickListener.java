package org.itishka.pointim.listeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import org.itishka.pointim.R;
import org.itishka.pointim.activities.SinglePostActivity;
import org.itishka.pointim.activities.TagViewActivity;
import org.itishka.pointim.activities.UserViewActivity;

/**
 * Created by Tishka17 on 26.04.2016.
 */
public class SimplePointClickListener implements OnPointClickListener {

    private Activity mActivity = null;
    private Fragment mFragment = null;

    public SimplePointClickListener() {
    }

    public SimplePointClickListener(Fragment fragment) {
        mFragment = fragment;
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public Activity getActivity() {
        if (mFragment != null) return mFragment.getActivity();
        return mActivity;
    }

    @Override
    public void onPostClicked(String post) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), SinglePostActivity.class);
        intent.putExtra("post", post);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    @Override
    public void oCommentClicked(String post, String comment) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), SinglePostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("comment", comment);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    @Override
    public void onUserClicked(String user) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), UserViewActivity.class);
        intent.putExtra(UserViewActivity.EXTRA_USER, user);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    @Override
    public void onBrowserLinkClicked(Uri link) {
        if (getActivity() == null) return;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, link);
        ActivityCompat.startActivity(getActivity(), Intent.createChooser(browserIntent, getActivity().getString(R.string.title_choose_app)), new Bundle());
    }

    @Override
    public void onTagClicked(String tag) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), TagViewActivity.class);
        intent.putExtra("tag", tag);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }
}
