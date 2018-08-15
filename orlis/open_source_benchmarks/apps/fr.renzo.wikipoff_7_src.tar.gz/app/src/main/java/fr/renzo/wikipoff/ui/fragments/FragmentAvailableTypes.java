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

public class FragmentAvailableTypes extends SherlockFragment {
	@SuppressWarnings("unused")
	private static final String TAG = "FragmentAvailableTypes";
	private WikiManagerActivity manageractivity;
	private ArrayList<String> wikitypes;
	private TextView header;
	private ListView listView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		manageractivity = (WikiManagerActivity) getSherlockActivity();
		View resultview = inflater.inflate(R.layout.available_wiki_fragment, container, false);
		
		header =(TextView) resultview.findViewById(R.id.avaialbleHeader);
		header.setText(manageractivity.getString(R.string.message_select_available_wiki_type));
		
		listView = (ListView) resultview.findViewById(R.id.availableListView);
		
        return resultview;
    }

	@Override
	public void onResume() {
		super.onResume();
		setAdapter();
	}

	private void setAdapter(){
		this.wikitypes = manageractivity.getAvailableWikiTypes();
		listView.setAdapter(new ArrayAdapter<String>(manageractivity,
				android.R.layout.simple_list_item_1,
				this.wikitypes)
				);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentAvailableWikis fragment = new FragmentAvailableWikis();
				Bundle args = new Bundle();
				args.putString("type", wikitypes.get(position));
				fragment.setArguments(args);
				manageractivity.addFragment(fragment);
				
			}
		});
	}
	
}
