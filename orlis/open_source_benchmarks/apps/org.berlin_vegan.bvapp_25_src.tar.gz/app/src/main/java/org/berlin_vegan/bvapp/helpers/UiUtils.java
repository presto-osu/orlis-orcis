/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import org.berlin_vegan.bvapp.BuildConfig;
import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.data.Preferences;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
 * Helper class, which creates all dialogs.
 */
public class UiUtils {

    public static final OnlineTileSourceBase GOOGLE_MAPS_TILE = new XYTileSource("Google Maps",
            10, 18, 512, ".png", new String[]{"http://mt3.google.com/vt/v=w2.97"}) {
        @Override
        public String getTileURLString(MapTile aTile) {
            return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
        }
    };

    public static MaterialDialog showMaterialDialog(Context context, String title, String content) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(content);
        MaterialDialog dialog = showMaterialDialog(context, title, spannableStringBuilder);
        dialog.show();
        return dialog;
    }


    public static MaterialDialog showMaterialDialogCustomView(Context context, String title, View customView, GastroLocationFilterCallback filterCallback) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(title)
                .customView(customView, true)
                .contentColorRes(R.color.material_dialog_content)
                .titleColorRes(R.color.material_dialog_title)
                .backgroundColorRes(R.color.material_dialog_background)
                .positiveText(android.R.string.ok)
                .positiveColorRes(R.color.material_dialog_buttons)
                .callback(filterCallback).build();
        dialog.show();
        return dialog;
    }

    public static String getFormattedDistance(float distance, Context context) {
        String distanceStr = String.valueOf(distance) + " ";
        if (Preferences.isMetricUnit(context)) {
            distanceStr += context.getString(R.string.km_string);
        } else {
            distanceStr += context.getString(R.string.mi_string);
        }
        return distanceStr;
    }

    private static MaterialDialog showMaterialDialog(Context context, String title, SpannableStringBuilder content) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .autoDismiss(true)
                .contentColorRes(R.color.material_dialog_content)
                .titleColorRes(R.color.material_dialog_title)
                .backgroundColorRes(R.color.material_dialog_background)
                .positiveText(android.R.string.ok)
                .positiveColorRes(R.color.material_dialog_buttons)
                .build();
        dialog.show();
        return dialog;
    }

    private static SpannableStringBuilder createAboutContent(final Context context) {
        // get app version
        String versionName = BuildConfig.VERSION_GIT_DESCRIPTION;

        // build the about body view and append the link to see OSS licenses
        String title = context.getString(R.string.app_name) + " " + context.getString(R.string.guide);
        SpannableStringBuilder aboutBody = new SpannableStringBuilder();
        aboutBody.append(Html.fromHtml(context.getString(R.string.about_version, title, versionName)));
        aboutBody.append("\n");
        aboutBody.append(context.getString(R.string.about_flavor, BuildConfig.FLAVOR));

        SpannableString licensesLink = new SpannableString(context.getString(R.string.about_licenses));
        licensesLink.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                showOpenSourceLicenses((Activity) context);
            }
        }, 0, licensesLink.length(), 0);
        aboutBody.append("\n\n");
        aboutBody.append(licensesLink);

        SpannableString creditsLink = new SpannableString(context.getString(R.string.about_credits));
        creditsLink.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                showCredits((Activity) context);
            }
        }, 0, creditsLink.length(), 0);
        aboutBody.append("\n\n");
        aboutBody.append(creditsLink);
        return aboutBody;
    }

    private static void showOpenSourceLicenses(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog_licenses");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        new OpenSourceLicensesDialog().show(fragmentTransaction, "dialog_licenses");
    }

    private static void showCredits(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("about_credits");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        new CreditsDialog().show(fragmentTransaction, "about_credits");
    }

    public static MaterialDialog showMaterialAboutDialog(Context context, String title) {
        SpannableStringBuilder content = createAboutContent(context);
        MaterialDialog dialog = showMaterialDialog(context, title, content);
        dialog.show();
        return dialog;
    }

    public static MaterialDialog showMaterialProgressDialog(Context context, String title, String content) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .autoDismiss(true)
                .contentColorRes(R.color.material_dialog_content)
                .titleColorRes(R.color.material_dialog_title)
                .backgroundColorRes(R.color.material_dialog_background)
                .progress(true, 0)
                .build();
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void rateApp(Context cnt, boolean googlePlay) {//true if Google Play, false if other
        try {
            if (googlePlay)
                cnt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                        + cnt.getPackageName())));

        } catch (ActivityNotFoundException exc) {
            try {
                //open browser if no play store installed
                cnt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://play.google.com/store/apps/details?id=" + cnt.getPackageName())));
            } catch (ActivityNotFoundException exc1) {
                //put some logic here for FDroid or Play Store
            }
        }
    }

    public static class OpenSourceLicensesDialog extends DialogFragment {

        public OpenSourceLicensesDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/licenses.html");
            return new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.about_licenses)
                    .setView(webView)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class CreditsDialog extends DialogFragment {

        public CreditsDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/credits.html");
            return new AlertDialogWrapper.Builder(getActivity())
                    .setTitle(R.string.about_credits)
                    .setView(webView)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }
}
