package fr.renzo.wikipoff.ui.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import fr.renzo.wikipoff.ConfigManager;
import fr.renzo.wikipoff.DownloadUtils;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.StorageUtils;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.WikiException;
import fr.renzo.wikipoff.WikiXMLParser;
import fr.renzo.wikipoff.ui.fragments.FragmentAvailableTypes;
import fr.renzo.wikipoff.ui.fragments.FragmentInstalledTypes;

public class WikiManagerActivity extends SherlockFragmentActivity implements ActionBar.TabListener, OnQueryTextListener {

	@SuppressWarnings("unused")
	private static final String TAG = "WikiManagerActivity";
	public static final int REQUEST_DELETE_CODE = 1001;

	public String storage;

	// god bless https://gist.github.com/andreynovikov/4619215
	// All this is fragment managing related stuff
	enum TabType
	{
		INSTALLED, AVAILABLE
	}

	// Tab back stacks
	private HashMap<TabType, Stack<String>> backStacks;
	private SharedPreferences config;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		config= PreferenceManager.getDefaultSharedPreferences(this);
		this.storage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage(this));

		setTitle(getString(R.string.title_manage_wikis));

		// Initialize ActionBar
		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set back stacks
		if (savedInstanceState != null)
		{
			// Read back stacks after orientation change
			backStacks = (HashMap<TabType, Stack<String>>) savedInstanceState.getSerializable("stacks");
		}
		else
		{
			// Initialize back stacks on first run
			backStacks = new HashMap<TabType, Stack<String>>();
			backStacks.put(TabType.INSTALLED, new Stack<String>());
			backStacks.put(TabType.AVAILABLE, new Stack<String>());
		}

		// Create tabs
		bar.addTab(bar.newTab().setTag(TabType.INSTALLED).setText("Installed").setTabListener(this));
		bar.addTab(bar.newTab().setTag(TabType.AVAILABLE).setText("Available").setTabListener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.managedbmenu, menu);
		SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());

		searchView.setOnQueryTextListener(this);
		menu.add("Search")
		.setActionView(searchView)
		.setIcon(R.drawable.ic_action_search)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_update_available_wikis_xml:
			updateAvailableWikisXML();
			return true;
		case R.id.action_about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;
		case R.id.action_deselect_all_wikis:
			deSelectAllWikis();
			return true;
		case  android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void deSelectAllWikis() {
		ConfigManager.clearSelectedDBFiles(this);
		finish();
		startActivity(getIntent());
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (! backStack.isEmpty())
		{
			// Restore topmost fragment (e.g. after application switch)
			String tag = backStack.peek();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			if (fragment.isDetached())
			{
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.attach(fragment);
				ft.commit();
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (! backStack.isEmpty())
		{
			// Detach topmost fragment otherwise it will not be correctly displayed
			// after orientation change
			String tag = backStack.peek();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			ft.detach(fragment);
			ft.commit();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		// Restore selected tab
		int saved = savedInstanceState.getInt("tab", 0);
		if (saved != getSupportActionBar().getSelectedNavigationIndex())
			getSupportActionBar().setSelectedNavigationItem(saved);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// Save selected tab and all back stacks
		outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
		outState.putSerializable("stacks", backStacks);
	}

	@Override
	public void onBackPressed()
	{
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());
		String tag = backStack.pop();
		if (backStack.isEmpty())
		{
			// Let application finish
			super.onBackPressed();
		}
		else
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			// Animate return to previous fragment
			//	ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
			// Remove topmost fragment from back stack and forget it
			ft.remove(fragment);
			showFragment(backStack, ft);
			ft.commit();
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());
		if (backStack.isEmpty())
		{
			// If it is empty instantiate and add initial tab fragment
			Fragment fragment;
			switch ((TabType) tab.getTag())
			{
			case INSTALLED:
				fragment = Fragment.instantiate(this, FragmentInstalledTypes.class.getName());
				break;
			case AVAILABLE:
				fragment = Fragment.instantiate(this, FragmentAvailableTypes.class.getName());
				break;
			default:
				throw new java.lang.IllegalArgumentException("Unknown tab");
			}
			addFragment(fragment, backStack, ft);
		}
		else
		{
			// Show topmost fragment
			showFragment(backStack, ft);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());
		// Get topmost fragment
		String tag = backStack.peek();
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		// Detach it
		ft.detach(fragment);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		// Select proper stack
		Stack<String> backStack = backStacks.get(tab.getTag());

		//		if (backStack.size() > 1)
		//			ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
		// Clean the stack leaving only initial fragment
		while (backStack.size() > 1)
		{
			// Pop topmost fragment
			String tag = backStack.pop();
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
			// Remove it
			ft.remove(fragment);
		}
		showFragment(backStack, ft);
	}

	public void addFragment(Fragment fragment)
	{
		// Select proper stack
		Tab tab = getSupportActionBar().getSelectedTab();
		Stack<String> backStack = backStacks.get(tab.getTag());

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// Animate transfer to new fragment
		//		ft.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
		// Get topmost fragment
		String tag = backStack.peek();
		Fragment top = getSupportFragmentManager().findFragmentByTag(tag);
		ft.detach(top);
		// Add new fragment
		addFragment(fragment, backStack, ft);
		ft.commit();
	}

	private void addFragment(Fragment fragment, Stack<String> backStack, FragmentTransaction ft)
	{
		// Add fragment to back stack with unique tag
		String tag = UUID.randomUUID().toString();
		ft.add(android.R.id.content, fragment, tag);
		backStack.push(tag);
	}

	private void showFragment(Stack<String> backStack, FragmentTransaction ft)
	{
		// Peek topmost fragment from the stack
		String tag = backStack.peek();
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		// and attach it
		ft.attach(fragment);		
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO
		return true;
	}

	@Override
	@SuppressWarnings("deprecation") // Because we want to work with API 14
	public boolean onQueryTextChange(String newText) {
		// TODO
		return true;
	}

	// XML related stuff
	private void updateAvailableWikisXML() {
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.message_warning))
		.setMessage(
				getString(R.string.message_overwrite_file,getString(R.string.available_xml_file)))
				.setNegativeButton(getString(R.string.no), null)
				.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						new DownloadXMLFile().execute(getString(R.string.available_xml_web_url));
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();

	}

	private InputStream copyXML(String xml) throws IOException {
		AssetManager am = getAssets();
		try {
			InputStream in = am.open(xml);
			File outFile = new File(storage,getString(R.string.available_xml_file_external_path));
			if (!outFile.exists()) {
				FileOutputStream out = new FileOutputStream(outFile);

				byte[] buffer = new byte[1024];
				int read;
				while((read = in.read(buffer)) != -1){
					out.write(buffer, 0, read);
				}
				in.close();
				out.flush();
				out.close();
			} 

			return new FileInputStream(outFile);
		} catch (IOException e) {
			e.printStackTrace();
			return am.open(xml);
		}
	}

	class DownloadXMLFile extends AsyncTask<String, Integer, String> {

		protected String doInBackground(String... s) {
			String result="";
			try {
				URL url = new URL(s[0]);

				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.connect();
				InputStream input = new BufferedInputStream(con.getInputStream());

				File outFile = new File(storage,getString(R.string.available_xml_file_external_path));
				FileOutputStream out = new FileOutputStream(outFile,false);

				byte[] buffer = new byte[1024];
				int read;
				while((read = input.read(buffer)) != -1){
					out.write(buffer, 0, read);
				}
				input.close();
				out.flush();
				out.close();


			} catch (IOException e) {
				e.printStackTrace();
				result="failed "+e.getMessage();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result!="") {
				Toast.makeText(getApplicationContext(), "Error: "+result, Toast.LENGTH_LONG).show();
			}
		}
	}

	// current download management
	public int alreadyDownloaded(Wiki w_tocheck) {
		int res = Wiki.WIKINOTINSTALLED; // We don't have that Wiki, by default
		ArrayList<Wiki> wikis = getInstalledWikis();
		for (Iterator<Wiki> iterator = wikis.iterator(); iterator.hasNext();) {
			Wiki w = (Wiki) iterator.next();
			res= w.compareWithWiki(w_tocheck);
			if (res < Wiki.WIKINOTINSTALLED) {  // This is ugly, and relies on good choice of integers for the static enum... j'assume.
				return res;
			}
		}
		return res;
	}

	public ArrayList<Wiki> getInstalledWikis(){
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		HashMap<String, Wiki> multiwikis = new HashMap<String, Wiki>();

		Collection<String> currendl = DownloadUtils.getCurrentDownloads(this).values();
		for (File f : new File(storage,getString(R.string.DBDir)).listFiles()) {
			if (! f.getName().endsWith(".sqlite")) {
				continue;
			}
			String name = f.getName();
			if (name.indexOf("-")>0) {
				String root_wiki=name.substring(0, name.indexOf("-"));
				if (multiwikis.containsKey(root_wiki)){
					Wiki w = multiwikis.get(root_wiki);
					w.addDBFile(f);
				} else {
					try {
						Wiki w = new Wiki(this, f);
						multiwikis.put(root_wiki,w);
					} catch (WikiException e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			} else {
				try {
					Wiki w = new Wiki(this,f);
					if (! currendl.containsAll(w.getDBFilesnamesAsList())) {
						res.add(w);
					}
				} catch (WikiException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		}
		for (Wiki w : multiwikis.values()) {
			if (! currendl.contains(w.getDBFilesnamesAsList()))
				res.add(w);
		}

		Collections.sort(res, new Comparator<Wiki>() {
			public int compare(Wiki w1, Wiki w2) {
				if (w1.getLangcode().equals(w2.getLangcode())) {
					return w1.getGendateAsDate().compareTo(w2.getGendateAsDate());
				} else {
					return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
				}
			}
		});

		return res;
	}

	public ArrayList<Wiki> getAvailableWikis() {
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		InputStream xml;
		try {
			xml = copyXML(getString(R.string.available_xml_file));
			res=  WikiXMLParser.loadAvailableDBFromXML(this,xml);
		} catch (IOException e) {
			Toast.makeText(this, "Problem opening available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} 

		return res;
	}

	public ArrayList<String> getInstalledWikiTypes() {
		ArrayList<String> res = new ArrayList<String>();
		for (Iterator<Wiki> iterator = getInstalledWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			String wikitype = wiki.getType();
			if (!res.contains(wikitype)) {
				if (!DownloadUtils.isInCurrentDownloads(wiki,this)) {
					res.add(wikitype);
				}
			}
		}
		return res;
	}

	public ArrayList<Wiki> getInstalledWikiByTypes(String type) {
		ArrayList<Wiki> res = new ArrayList<Wiki>();

		for (Iterator<Wiki> iterator = getInstalledWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			if (wiki.getType().equals(type)) {
				res.add(wiki);
			}
		}
		return res;
	}

	public ArrayList<String> getAvailableWikiTypes()  {
		ArrayList<String> res = new ArrayList<String>();
		for (Iterator<Wiki> iterator = getAvailableWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			String wikitype = wiki.getType();
			if (!res.contains(wikitype)) {
				res.add(wikitype);
			}
		}

		return res ;
	}

	public ArrayList<Wiki> getAvailableWikiByTypes(String type) {
		ArrayList<Wiki> res = new ArrayList<Wiki>();
		for (Iterator<Wiki> iterator = getAvailableWikis().iterator(); iterator.hasNext();) {
			Wiki wiki = (Wiki) iterator.next();
			if (wiki.getType().equals(type)) {
				res.add(wiki);
			}
		}

		return res;
	}

	public boolean isInstalledWiki(Wiki wiki) {
		return getInstalledWikis().contains(wiki);
	}
}
