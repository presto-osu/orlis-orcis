package org.pulpdust.lesserpad;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.app.Activity;

@TargetApi(11)
public class forHoneycomb {
	
	public void setEditText(final EditText etxt, final Context context, final Activity av, 
			final int look){
		etxt.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			libLesserPad llp = new libLesserPad();
			@Override
			public boolean onActionItemClicked(ActionMode am, MenuItem mi) {
				int id = mi.getItemId();
				switch (id){
				case R.id.menu_search:
					llp.goSearch(context, etxt);
					return true;
				case R.id.menu_da:
					Intent dal = llp.daLaunch(av, etxt, look);
		    		av.startActivityForResult(Intent.createChooser(dal, av.getString(R.string.menu_da)), 1);
		    		return true;
				}
				return false;
			}

			@SuppressLint("AlwaysShowAction")
			@Override
			public boolean onCreateActionMode(ActionMode am, Menu m) {
				m.removeItem(android.R.id.selectAll);
				if (llp.isIntentAvailable(context, libLesserPad.DA_LAUNCH)){
					MenuItem mid = m.add(0, R.id.menu_da, 0, R.string.menu_da);
					if (look >= 1){
						mid.setIcon(R.drawable.ic_menu_da_dark);
					} else {
						mid.setIcon(R.drawable.ic_menu_da);
					}
					mid.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				}
				MenuItem mis = m.add(0, R.id.menu_search, 0, R.string.menu_search);
				mis.setIcon(android.R.drawable.ic_menu_search);
				mis.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode am) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean onPrepareActionMode(ActionMode am, Menu m) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
	}
	public void setActionBar(final Activity av, final int mode, ArrayAdapter<String> adirs, int navi){
		ActionBar ab = av.getActionBar();
		switch (navi){
		case ActionBar.NAVIGATION_MODE_STANDARD:
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			break;
		case ActionBar.NAVIGATION_MODE_LIST:
		default:
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			SpinnerAdapter sa = adirs;
			ab.setListNavigationCallbacks(sa, new OnNavigationListener(){

				@Override
				public boolean onNavigationItemSelected(int pos,
						long id) {
					if (mode == 0){
						LesserPadActivity.doMove(av.getApplicationContext(), pos);
					} else if (mode == 1){
						LesserPadListActivity.doChange(pos);
					}
					return false;
				}
				
			});
		}
	}
	public void setSelection(Activity av, int pos){
		ActionBar ab = av.getActionBar();
		ab.setSelectedNavigationItem(pos);
	}
	public void setText(Activity av, CharSequence text){
		ActionBar ab = av.getActionBar();
		ab.setTitle(text);
	}
	public static void doCopy(Context c, CharSequence text){
		ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData cd = ClipData.newPlainText("plain text", text);
		cm.setPrimaryClip(cd);
	}
	public static CharSequence getCopy(Context c){
		CharSequence text = "";
		ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData cd = cm.getPrimaryClip();
		if (cd != null){
			ClipData.Item ci = cd.getItemAt(0);
			text = ci.coerceToText(c);
		}
		return text;
	}
//	public static void updateOptionsMenu(Activity av){
//		av.invalidateOptionsMenu();
//	}

}
