package fr.tvbarthel.apps.simpleweatherforcast.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import fr.tvbarthel.apps.simpleweatherforcast.R;
import fr.tvbarthel.apps.simpleweatherforcast.model.App;
import fr.tvbarthel.apps.simpleweatherforcast.ui.MoreAppsAdapter;

public class MoreAppsDialogFragment extends DialogFragment {

    private static final String URI_ROOT_MARKET = "market://details?id=";
    private static final String URI_ROOT_PLAY_STORE = "http://play.google.com/store/apps/details?id=";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final ListView listView = (ListView) inflater.inflate(R.layout.dialog_more_apps, null);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setPositiveButton(android.R.string.ok, null);
        dialogBuilder.setTitle(R.string.dialog_more_apps_title);
        dialogBuilder.setView(listView);
        dialogBuilder.setInverseBackgroundForced(true);

        App chaseWhisply = new App();
        chaseWhisply.setLogoResourceId(R.drawable.ic_chase_whisply);
        chaseWhisply.setNameResourceId(R.string.dialog_more_apps_chase_whisply_app_name);
        chaseWhisply.setPackageNameResourceId(R.string.dialog_more_apps_chase_whisply_package_name);

        App googlyZoo = new App();
        googlyZoo.setLogoResourceId(R.drawable.ic_googly_zoo);
        googlyZoo.setNameResourceId(R.string.dialog_more_apps_googly_zoo_app_name);
        googlyZoo.setPackageNameResourceId(R.string.dialog_more_apps_googly_zoo_package_name);

        App simpleThermometer = new App();
        simpleThermometer.setLogoResourceId(R.drawable.ic_simple_thermometer);
        simpleThermometer.setNameResourceId(R.string.dialog_more_apps_simplethermometer_app_name);
        simpleThermometer.setPackageNameResourceId(R.string.dialog_more_apps_simplethermometer_package_name);

        final ArrayList<App> apps = new ArrayList<App>();
        apps.add(chaseWhisply);
        apps.add(googlyZoo);
        apps.add(simpleThermometer);

        listView.setAdapter(new MoreAppsAdapter(getActivity(), apps));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App appClicked = apps.get(position);
                launchPlayStoreDetails(getResources().getString(appClicked.getPackageNameResourceId()));
            }
        });

        return dialogBuilder.create();
    }

    private void launchPlayStoreDetails(String appPackageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_ROOT_MARKET + appPackageName)));
        } catch (android.content.ActivityNotFoundException activityNotFoundException) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_ROOT_PLAY_STORE + appPackageName)));
        }
    }
}
