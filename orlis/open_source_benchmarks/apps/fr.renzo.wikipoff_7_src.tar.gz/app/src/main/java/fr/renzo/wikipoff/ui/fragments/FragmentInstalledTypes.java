package fr.renzo.wikipoff.ui.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.ui.activities.WikiManagerActivity;

public class FragmentInstalledTypes extends SherlockFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "FragmentInstalledTypes";
	private WikiManagerActivity manageractivity;
	private ArrayList<String> wikitypes;
	private TextView header;
	private ListView listView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		wikitypes = manageractivity.getInstalledWikiTypes();
		View resultview = inflater.inflate(R.layout.installed_wiki_fragment, container, false);
		
		header =(TextView) resultview.findViewById(R.id.installedHeader);
		
		listView = (ListView) resultview.findViewById(R.id.installedListView);
		listView.setAdapter(new ArrayAdapter<String>(manageractivity,
				android.R.layout.simple_list_item_1,
				wikitypes)
				);
		if (listView.getAdapter().getCount() > 0) {
			header.setText(manageractivity.getString(R.string.message_select_installed_wiki_type));
		} else {
			header.setText(manageractivity.getString(R.string.message_no_installed_wiki_type));
		}

		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentInstalledWikis fragment = new FragmentInstalledWikis();
				Bundle args = new Bundle();
				args.putString("type", wikitypes.get(position));
				fragment.setArguments(args);
				manageractivity.addFragment(fragment);
				
			}
		});
		
        return resultview;
    }

}
