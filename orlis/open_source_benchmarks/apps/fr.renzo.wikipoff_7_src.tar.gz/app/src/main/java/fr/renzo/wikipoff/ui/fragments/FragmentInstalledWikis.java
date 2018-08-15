package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fr.renzo.wikipoff.ConfigManager;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.ui.activities.WikiInstalledActivity;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledWikis extends SherlockFragment {

	protected static final String TAG = "FragmentInstalledWikis";
	private WikiManagerActivity manageractivity;
	private ArrayList<Wiki> wikis;
	private String type;
	private TextView header;
	private ListView listView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		type = getArguments().getString("type");
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		wikis = manageractivity.getInstalledWikiByTypes(type);
		View resultview = inflater.inflate(R.layout.installed_wiki_fragment, container, false);

		header =(TextView) resultview.findViewById(R.id.installedHeader);
		header.setText(manageractivity.getString(R.string.message_select_installed_wiki));

		listView = (ListView) resultview.findViewById(R.id.installedListView);
		listView.setAdapter(new InstalledWikisListViewAdapter(manageractivity,wikis));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Wiki wiki = wikis.get(position);

				Intent myIntent = new Intent(getSherlockActivity(), WikiInstalledActivity.class);
				myIntent.putExtra("wiki",  wiki);
				myIntent.putExtra("position",position);
				startActivityForResult(myIntent,WikiManagerActivity.REQUEST_DELETE_CODE);
			}
		});
		return resultview;
	}

	public class InstalledWikisListViewAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<Wiki> data;
		@SuppressWarnings("unused")
		private int selectedPosition = 0;
		public InstalledWikisListViewAdapter(Context context, ArrayList<Wiki> data){
			// Caches the LayoutInflater for quicker use
			this.inflater = LayoutInflater.from(context);
			// Sets the events data
			this.data= data;
		}
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			if(position < getCount() && position >= 0 ){
				return position;
			} else {
				return -1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Wiki w = data.get(position);
			if(convertView == null){ // If the View is not cached
				// Inflates the Common View from XML file
				convertView = this.inflater.inflate(R.layout.installed_wiki_item, parent, false);
			}
			TextView header = (TextView ) convertView.findViewById(R.id.installedwikiheader);
			header.setText(w.getType()+" "+w.getLanglocal());
			TextView bot = (TextView ) convertView.findViewById(R.id.installedwikifooter);
			bot.setText(w.getFilenamesAsString()+" "+w.getLocalizedGendate());
			TextView rb = (TextView) convertView.findViewById(R.id.checked);
			if (ConfigManager.isInSelectedDBs(manageractivity, w)){
				rb.setText("\u2713");
			}			
			return convertView;

		}

	}

}
