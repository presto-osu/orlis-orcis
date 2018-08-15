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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fr.renzo.wikipoff.DownloadUtils;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.ui.activities.WikiAvailableActivity;
import fr.renzo.wikipoff.ui.activities.WikiInstalledActivity;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentAvailableWikis extends SherlockFragment {

	protected static final String TAG = "FragmentAvailableWikis";
	private WikiManagerActivity manageractivity;
	private ArrayList<Wiki> wikis;
	private String type;
	private TextView header;
	private ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		type = getArguments().getString("type");
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		View resultview = inflater.inflate(R.layout.available_wiki_fragment, container, false);

		header =(TextView) resultview.findViewById(R.id.avaialbleHeader);
		header.setText(manageractivity.getString(R.string.message_select_available_wiki));

		listView = (ListView) resultview.findViewById(R.id.availableListView);

		return resultview;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.wikis = manageractivity.getAvailableWikiByTypes(type);
		listView.setAdapter(new AvailableWikisListViewAdapter(manageractivity,this.wikis));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Wiki wiki = wikis.get(position);

				Intent myIntent;
				boolean installed = manageractivity.isInstalledWiki(wiki);
				if ( installed ) {
					myIntent = new Intent(getSherlockActivity(), WikiInstalledActivity.class);
				} else {
					myIntent = new Intent(getSherlockActivity(), WikiAvailableActivity.class);
				}

				myIntent.putExtra("wiki",  wiki);
				myIntent.putExtra("storage", manageractivity.storage);
				myIntent.putExtra("installed", manageractivity.isInstalledWiki(wiki));
				startActivityForResult(myIntent,WikiManagerActivity.REQUEST_DELETE_CODE);

			}
		});
	}

	public class AvailableWikisListViewAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<Wiki> data;
		public AvailableWikisListViewAdapter(Context context, ArrayList<Wiki> data){
			this.inflater = LayoutInflater.from(context);
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
			if(convertView == null){ 
				convertView = this.inflater.inflate(R.layout.available_wiki_item, parent, false);
			}

			TextView header = (TextView ) convertView.findViewById(R.id.availablewikiheader);
			header.setText(w.getLanglocal()+"("+w.getLangcode()+")"+" "+w.getType());
			TextView bot = (TextView ) convertView.findViewById(R.id.availablewikifooter);
			String bottext= w.getFilenamesAsString()+"("+w.getSizeReadable(true)+") "+w.getLocalizedGendate();
			TextView infos = (TextView) convertView.findViewById(R.id.availablewikiinfo);
			int isLocalWikiNewer = manageractivity.alreadyDownloaded(w);
			switch (isLocalWikiNewer) {
			case Wiki.WIKIEQUAL:
				infos.setText(manageractivity.getString(R.string.message_available_wiki_equal));
				break;
			case Wiki.WIKIOLDER: // Wiki on SD is older
				infos.setText(manageractivity.getString(R.string.message_available_wiki_older));
				break;
			case Wiki.WIKINEWER: // Wiki on SD is newer
				infos.setText(manageractivity.getString(R.string.message_available_wiki_newer));
				break;
			}
			bot.setText(bottext);
			if (DownloadUtils.isInCurrentDownloads(w,getSherlockActivity())){
				infos.setText(R.string.downloading);
			}
			return convertView;
		}
	}

}
