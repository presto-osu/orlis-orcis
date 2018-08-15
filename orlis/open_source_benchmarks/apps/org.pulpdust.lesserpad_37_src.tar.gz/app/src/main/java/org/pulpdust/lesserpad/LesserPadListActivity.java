package org.pulpdust.lesserpad;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

public class LesserPadListActivity extends FragmentActivity {
	final static String TAG = "Lesser Pad List";
	static File path;
	String default_dir;
	float font_size;
	int look_style;
	static boolean hide_ext;
	boolean spec_path;
	String path_to;
	String last_dir;
	final static int SORT_OLD = 0;
	final static int SORT_NEW = 1;
	final static int SORT_ABC = 2;
	final static int SORT_ZYX = 3;
	static int sort_by = SORT_NEW;
	final static int REQ_PASS = 4;
	Spinner ebox;
	TextView label;
	ListView mlist;
	Button newbtn;
	static List<String> dirs = new ArrayList<String>();
	static ArrayAdapter<String> adirs;
	static ArrayAdapter<String> amemos;
	List<String> memos = new ArrayList<String>();
	static List<String> ls = new ArrayList<String>();
	libLesserPad llp = new libLesserPad();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        readPrefs();
    	if (look_style > 0){ 
    		setTheme(R.style.AppTheme_Dark);
    	} else {
    		setTheme(R.style.AppTheme);
    	}
    	super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lesser_pad_list);
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
        	Toast.makeText(this, R.string.mes_nosd, Toast.LENGTH_LONG).show();
        	finish();
        }
        ebox = (Spinner) findViewById(R.id.spinner2);
        label = (TextView) findViewById(R.id.textView2);
        if (look_style > 0 && Build.VERSION.SDK_INT < 21) label.setBackgroundResource(R.drawable.palmtitle_dark);
        mlist = (ListView) findViewById(R.id.listView1);
        newbtn = (Button) findViewById(R.id.button1);
        dirs = new ArrayList<String>();
        adirs = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dirs);
        adirs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ebox.setAdapter(adirs);
        newbtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		doNew();
        	}
        });
		if (Build.VERSION.SDK_INT >= 21){
			forLollipop.readyActionBar(findViewById(R.id.toolBar2), this, 0, look_style, false);
		}
        if ((Build.VERSION.SDK_INT >= 11) && (Build.VERSION.SDK_INT < 21)){
        	ebox.setVisibility(View.GONE);
        	label.setVisibility(View.GONE);
        }
		if (Build.VERSION.SDK_INT >= 11){
			newbtn.setVisibility(View.GONE);
		}
		if (look_style > 0 && Build.VERSION.SDK_INT >= 21){
			label.setTextColor(Color.rgb(192, 192, 192));
			mlist.setBackgroundColor(Color.rgb(25, 25, 25));
		} else if (look_style == 0 && Build.VERSION.SDK_INT >= 21){
			label.setTextColor(Color.rgb(50,50,50));
			mlist.setBackgroundColor(Color.rgb(250,250,250));
		}
        label.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
        });
        amemos = new ArrayAdapter<String>(this, R.layout.list_item, memos){
        	@Override
        	public View getView(int pos, View v, ViewGroup vg){
        		TextView tview = (TextView) super.getView(pos, v, vg);
        		tview.setTextSize(font_size);
				if (Build.VERSION.SDK_INT >= 21){
					tview.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				}
//				if (look_style > 0 && Build.VERSION.SDK_INT >= 21){
//					tview.setTextColor(Color.rgb(192, 192, 192));
//					tview.setBackgroundColor(Color.rgb(25, 25, 25));
//				} else if (look_style == 0 && Build.VERSION.SDK_INT >= 21){
//					tview.setTextColor(Color.rgb(50,50,50));
//					tview.setBackgroundColor(Color.rgb(250,250,250));
//				}
        		return tview;
        	}
        };
        mlist.setAdapter(amemos);
        mlist.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos,
					long id) {
				File data;
				if (ls.get(pos).endsWith(".len")){
					Intent pit = new Intent();
					pit.setClassName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.ProtectActivity");
					pit.putExtra(libLesserPad.CRYPT_FILE, (ls.get(pos)));
					pit.putExtra(libLesserPad.REQ_PASS_MODE, libLesserPad.REQ_PASS_FOR_OPEN);
					startActivityForResult(pit, REQ_PASS);
				} else {
					Intent intent = new Intent(libLesserPad.LPAD_EDIT);
					intent.setComponent(new ComponentName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.LesserPadActivity"));
	        		intent.putExtra("PATH", path.toString());
					if (hide_ext == true){
						data = new File(path, (ls.get(pos)));
					} else {
						data = new File(path, ((TextView) v).getText().toString());
					}
					intent.setData(Uri.fromFile(data));
					startActivityForResult(intent, 1);
				}
			}
        });
        mlist.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v,
					int pos, long id) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				File data;
				try {
/*
					if (hide_ext == true && !((TextView) v).getText().toString().endsWith(".len")){
						data = new File(path, ((TextView) v).getText().toString() + ".txt");
					} else {
						data = new File(path, ((TextView) v).getText().toString());
					}
*/
					data = new File(path, ls.get(pos));
					intent.setDataAndType(Uri.fromFile(data), "text/plain");
					startActivityForResult(intent, 3);
				} catch (ActivityNotFoundException e){
					Log.e(TAG, e.getMessage());
				}
				return true;
			}
        });
        if (spec_path == true){
        	path = new File(new File(path_to), default_dir);
        } else {
        	path = new File(Environment.getExternalStorageDirectory(), default_dir);
        }
        if (last_dir != null && !last_dir.equals(path.getName())){
        	File lpath = new File(path.getParent(), last_dir);
        	if (lpath.exists()){
        		path = lpath;
        	}
        }
        if (!path.exists() || !path.canWrite()){
        	if (!path.mkdirs()){
        		default_dir = "/";
        		path = new File(Environment.getExternalStorageDirectory(), default_dir);
        		Toast.makeText(this, R.string.mes_dis_spec_path, Toast.LENGTH_LONG).show();
        	}
        }
        listMemos(path);
        if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 21){
        	forHoneycomb fhc = new forHoneycomb();
        	if (default_dir.equals("/")){
        		fhc.setActionBar(this, 1, adirs, 0);
        	} else {
            	fhc.setActionBar(this, 1, adirs, 1);
        	}
        }
        if (default_dir.equals("/")){
        	ebox.setEnabled(false);
        } else {
        	llp.listDir(path, adirs, dirs, ebox, this, null);
        }
        ebox.setOnItemSelectedListener(new Changer());
    }
    public class Changer implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> av, View v, int pos,
				long id) {
			doChange(pos);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    }
    public static void doChange(int pos){
		File base = path.getParentFile();
		path = new File(base, "/" + dirs.get(pos));
		listMemos(path);
    }
    
    public static void listMemos(File path){
    	amemos.clear();
		ls.clear();
    	Comparator<File> cmp = new Comparator<File>(){
    		@Override
    		public int compare(File f1, File f2){
    			if (sort_by <= SORT_NEW){
    				return Long.valueOf(f1.lastModified()).compareTo(Long.valueOf(f2.lastModified()));
    			} else {
    				return f1.getName().compareToIgnoreCase(f2.getName());
    			}
    		}
    	};
    	File[] files = path.listFiles();
    	if (files == null) files = new File[0];
    	Arrays.sort(files, cmp);
    	String lsext = "(?i)(^.+\\.txt$)|(^.+\\.len$)";
    	if (Build.VERSION.SDK_INT < 8){
    		lsext = "(?i)^.+\\.txt$";
    	}
    	for(int index = 0 ; index < files.length; index++){
    		if (!files[index].isDirectory() && files[index].getName().matches(lsext)){
//    			if (hide_ext == true){
//    				ls.add(files[index].getName().replaceFirst("\\.txt$", ""));
//    			} else {
    				ls.add(files[index].getName());
//    			}
    		}
    	}
    	if (sort_by == SORT_NEW || sort_by == SORT_ZYX){
    		Collections.reverse(ls);
    	}
    	for (int index = 0; index < ls.size(); index++){
			if (hide_ext == true){
				amemos.add(ls.get(index).replaceFirst("(\\.txt$)|(\\.len$)", ""));
			} else {
				amemos.add(ls.get(index));
			}
    	}
    }

    public void doNew(){
		Intent intent = new Intent(libLesserPad.LPAD_NEW);
		intent.setComponent(new ComponentName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.LesserPadActivity"));
		intent.putExtra("PATH", path.toString());
		startActivityForResult(intent, 1);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	super.onPrepareOptionsMenu(menu);
    	if (default_dir.equals("/")){
    		MenuItem fld = (MenuItem) menu.findItem(R.id.menu_folder);
    		fld.setVisible(false);
    	}
    	if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 21){
    		forActionBarMenu(menu);
    	}
    	return true;
    }
    
    public void forActionBarMenu(Menu menu){
    	if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 21){
    		MenuItem min = (MenuItem) menu.findItem(R.id.menu_new);
    		min.setVisible(true);
    	}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (Build.VERSION.SDK_INT >= 11 && look_style > 0){
    		getMenuInflater().inflate(R.menu.activity_lesser_pad_list_dark, menu);
    	} else {
    		getMenuInflater().inflate(R.menu.activity_lesser_pad_list, menu);
    	}
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem mi){
    	switch (mi.getItemId()){
    	case R.id.menu_folder:
    		Intent editfolder = new Intent();
    		editfolder.setClassName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.CategoryEditor");
    		startActivityForResult(editfolder, 2);
    		return true;
    	case R.id.menu_settings:
    		Intent goprefs = new Intent();
    		goprefs.setClassName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.LesserPadPrefs");
    		startActivityForResult(goprefs, 0);
    		return true;
		case android.R.id.home:

    	case R.id.menu_new:
    		doNew();
    		return true;
    	case R.id.menu_sort:
    		DialogFragment sd = SortDialog.newInstance();
    		sd.show(getSupportFragmentManager(), "sort");
    		return true;
    	default:
    		return super.onOptionsItemSelected(mi);
    	
    	}
    }
    public static class SortDialog extends DialogFragment {
    	public static SortDialog newInstance(){
    		SortDialog sd = new SortDialog();
			return sd;
    	}
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState){
    		final CharSequence[] items = getResources().getStringArray(R.array.sort_labels);
    		return new AlertDialog.Builder(getActivity())
    			.setSingleChoiceItems(items, sort_by, new DialogInterface.OnClickListener(){
    				public void onClick(DialogInterface dialog, int item){
    					sort_by = item;
    					listMemos(path);
    					dialog.dismiss();
    				}
    			})
    			.setTitle(R.string.menu_sort)
    			.create();
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	switch (requestCode){
    	case 0:
    		readPrefs();
    		listMemos(path);
    		break;
    	case 1:
    		if (resultCode == RESULT_OK && data != null){
    			path = new File(data.getStringExtra("PATH"));
    			if (path.equals(Environment.getExternalStorageDirectory()) || default_dir.equals("/")){
    			} else {
    				llp.listDir(path, adirs, dirs, ebox, this, null);
    			}
    		}
    		readPrefs();
			listMemos(path);
    		break;
    	case 2:
    		if (!path.exists()){
    			if (spec_path == true){
    				path = new File(new File(path_to), default_dir);
    			} else {
    				path = new File(Environment.getExternalStorageDirectory(), default_dir);
    			}
    		}
            listMemos(path);
			if (default_dir.equals("/")){
			} else {
            	llp.listDir(path, adirs, dirs, ebox, this, null);
			}
            ebox.setOnItemSelectedListener(new Changer());
            break;
    	case 3:
    		listMemos(path);
    		break;
    	case REQ_PASS:
    		if (resultCode == RESULT_OK){
				Intent intent = new Intent(libLesserPad.LPAD_EDIT);
				intent.setComponent(new ComponentName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.LesserPadActivity"));
        		intent.putExtra("PATH", path.toString());
        		intent.putExtra(libLesserPad.CRYPT_PASS, data.getStringExtra(libLesserPad.CRYPT_PASS));
				File file = new File(path, data.getStringExtra(libLesserPad.CRYPT_FILE));
				intent.setData(Uri.fromFile(file));
				startActivityForResult(intent, 1);
    		}
    	}
    }
    public void readPrefs(){
    	SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	default_dir = sprefs.getString("default_dir", getString(R.string.app_default_dir));
    	if (default_dir.equals("")) {
    		default_dir = "/";
    	}
    	font_size = Float.parseFloat(sprefs.getString("font_size", "18.0f"));
    	look_style = Integer.parseInt(sprefs.getString("look_style", "0"));
    	hide_ext = sprefs.getBoolean("hide_ext", false);
    	spec_path = sprefs.getBoolean("spec_path", false);
    	path_to = sprefs.getString("path_to", Environment.getExternalStorageDirectory().toString());
    	if (path_to.equals("")){
    		path_to = Environment.getExternalStorageDirectory().toString();
    	}
    	SharedPreferences props = getPreferences(MODE_PRIVATE);
    	sort_by = props.getInt("sort_by", 1);
    	last_dir = props.getString("last_dir", null);
    }
    public void writeProps(){
    	SharedPreferences props = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor pedit = props.edit();
    	pedit.putInt("sort_by", sort_by);
    	pedit.putString("last_dir", path.getName());
    	pedit.commit();
    }
    @Override
    protected void onStop(){
    	super.onStop();
    	writeProps();
    }
}
