package fr.tvbarthel.apps.simpleweatherforcast.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import fr.tvbarthel.apps.simpleweatherforcast.R;
import fr.tvbarthel.apps.simpleweatherforcast.model.App;

public class MoreAppsAdapter extends ArrayAdapter<App> {


    public MoreAppsAdapter(Context context, List<App> apps) {
        super(context, R.layout.row_more_apps, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout appView = (RelativeLayout) convertView;
        App app = getItem(position);

        if (appView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            appView = (RelativeLayout) inflater.inflate(R.layout.row_more_apps, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.appName = (TextView) appView.findViewById(R.id.row_more_apps_name);
            holder.appLogo = (ImageView) appView.findViewById(R.id.row_more_apps_logo);
            appView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) appView.getTag();
        holder.appName.setText(app.getNameResourceId());
        holder.appLogo.setImageResource(app.getLogoResourceId());

        return appView;
    }

    private static class ViewHolder {
        TextView appName;
        ImageView appLogo;
    }
}
