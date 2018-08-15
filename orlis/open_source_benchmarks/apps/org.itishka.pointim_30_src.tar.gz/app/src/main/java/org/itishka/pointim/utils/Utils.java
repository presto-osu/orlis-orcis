package org.itishka.pointim.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.itishka.pointim.R;
import org.itishka.pointim.activities.SinglePostActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class Utils {

    public static final String BASE_URL_STRING = "https://point.im/api/";
    public static final String AVATAR_URL_STRING = "https://i.point.im/";
    public static final String SITE_URL_STRING = "https://point.im/";
    public static final String BLOG_SITE_URL_TEMPLATE = "https://%s.point.im/blog";

    public static Uri generateSiteUri(String postId) {
        return Uri.parse(SITE_URL_STRING + postId);
    }

    public static Uri generateSiteUri(String postId, long commendId) {
        return Uri.parse(SITE_URL_STRING + postId + "#" + commendId);
    }

    public static Uri generateBlogUri(String login) {
        return Uri.parse(String.format(BLOG_SITE_URL_TEMPLATE, login));
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatDateOnly(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static String getAvatarByLogin(String login) {
        return "http://point.im/avatar/login/" + login + "/80";
    }

    public static void showAvatarByLogin(String login, ImageView imageView) {
        showAvatar(login, getAvatarByLogin(login), imageView);
    }

    public static void showAvatar(String login, String avatar, ImageView imageView) {
        imageView.setTag(R.id.imageView, login);
        imageView.setImageURI(null);
        final Context context = imageView.getContext();
        if (avatar == null) {
            Glide
                    .with(context)
                    .load(R.drawable.ic_account_grey600_36dp)
                    .fitCenter()
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(imageView);

            return;
        }
        Uri url;
        if (avatar.contains("/"))
            url = Uri.parse(avatar);
        else
            url = Uri.parse(AVATAR_URL_STRING + "/a/80/" + avatar);
        //imageView.setImageURI(url);
        Glide
                .with(context)
                .load(url)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView);
    }


    public static final int getGenderString(@Nullable Boolean gender) {
        if (gender == null) return R.string.gender_robot;
        else if (gender) return R.string.male;
        else return R.string.female;
    }

    public static void showPostSentSnack(final Activity activity, View view, final String postId) {
        Snackbar
                .make(view, String.format(activity.getString(R.string.snack_posted_template), postId), Snackbar.LENGTH_SHORT)
                .setAction(R.string.action_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, SinglePostActivity.class);
                        intent.putExtra(SinglePostActivity.EXTRA_POST, postId);
                        ActivityCompat.startActivity(activity, intent, null);
                    }
                })
                .show();
    }

    public static void setTint(TextView v) {
        Drawable[] ds = v.getCompoundDrawables();
        Drawable[] cs = new Drawable[ds.length];
        int c = v.getCurrentTextColor();
        for (int i = 0; i < ds.length; i++) {
            if (ds[i] == null) {
                cs[i] = null;
            } else {
                cs[i] = DrawableCompat.wrap(ds[i]);
                DrawableCompat.setTint(cs[i], c);
            }
        }
        v.setCompoundDrawablesWithIntrinsicBounds(cs[0], cs[1], cs[2], cs[3]);
    }
}
