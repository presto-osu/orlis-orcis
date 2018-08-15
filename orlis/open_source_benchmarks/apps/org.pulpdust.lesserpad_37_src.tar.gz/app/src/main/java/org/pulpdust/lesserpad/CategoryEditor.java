package org.pulpdust.lesserpad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
//import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CategoryEditor extends Activity {
	final static String TAG = "Lesser Pad Category Editor";
	File path;
	String default_dir;
	float font_size;
	int look_style;
	TextView label;
	ListView mlist;
	Button newbtn;
	Button renbtn;
	Button delbtn;
	EditText input;
	List<String> dirs = new ArrayList<String>();
	ArrayAdapter<String> adirs;
	File parent;
	String name;
	boolean isable;
	libLesserPad llp = new libLesserPad();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        readPrefs();
    	if (look_style > 0){ 
    		setTheme(R.style.AppTheme_Dialog_Dark);
    	} else {
    		setTheme(R.style.AppTheme_Dialog);
    	}
        super.onCreate(savedInstanceState);
        path = new File(Environment.getExternalStorageDirectory(), default_dir);
        parent = path.getParentFile();
        name = path.getName();
        setContentView(R.layout.activity_category_editor);
        label = (TextView) findViewById(R.id.textView3);
        mlist = (ListView) findViewById(R.id.listView2);
        newbtn = (Button) findViewById(R.id.button2);
        renbtn = (Button) findViewById(R.id.button3);
        delbtn = (Button) findViewById(R.id.button4);
//        if (Build.VERSION.SDK_INT >= 11){
//        	label.setVisibility(View.GONE);
//        }
        isable = setAble(false, false);
        newbtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		namingOhji(null);
        	}
        });
        renbtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		namingOhji(dirs.get(mlist.getCheckedItemPosition()));
        	}
        });
        delbtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sureDelete();
			}
        });
        adirs = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, dirs);
        mlist.setAdapter(adirs);
        mlist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mlist.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos,
					long id) {
				String choice = dirs.get(mlist.getCheckedItemPosition());
				if (name.equals(choice)){
					isable = setAble(false, isDeletable(choice));
				} else {
					isable = setAble(true, isDeletable(choice));
				}
			}
        });
        llp.listDir(path, adirs, dirs, null, null, null);
    }
    
    public boolean isDeletable(String name){
    	File object = new File(parent, name);
    	String list[] = object.list();
    	if (list.length > 0){
    		return false;
    	} else {
    		return true;
    	}
    }
    public void namingOhji(final String oldname){
    	Context context = this;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.folder_name_dialog, (ViewGroup) findViewById(R.id.layout_naming));
		builder.setTitle(R.string.dialog_edit_folder);
		builder.setCancelable(true);
		input = (EditText) layout.findViewById(R.id.editText2);
//		Button okbtn = (Button) findViewById(R.id.button5);
		if (oldname != null){
			input.setText(oldname);
			input.selectAll();
		}
		builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				boolean success;
				String naming = input.getText().toString();
				if (oldname != null && !naming.equals("")){
					File dir = new File(parent, oldname);
					File newname = new File(parent, naming);
					success = dir.renameTo(newname);
				} else if (!naming.equals("")) {
					File newdir = new File(parent, naming);
					success = newdir.mkdirs();
				} else {
					success = false;
				}
				if (success){
					llp.listDir(path, adirs, dirs, null, null, null);
					dialog.dismiss();
				} else {
					Toast.makeText(getApplicationContext(), R.string.mes_edit_fail_dir, Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setView(layout);
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
    }

    public void sureDelete(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getString(R.string.dialog_delete_sure_dir))
    	       .setTitle(R.string.dialog_delete_dir)
    	       .setCancelable(false)
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   if (LesserPadActivity.doDelete(parent, dirs.get(mlist.getCheckedItemPosition()))){
    	        	       llp.listDir(path, adirs, dirs, null, null, null);
    	        		   dialog.dismiss();
    	        	   } else {
    	        		   Toast.makeText(getApplicationContext(), R.string.mes_del_fail_dir, Toast.LENGTH_SHORT).show();
    	        		   dialog.dismiss();
    	        	   }
    	           }
    	       })
    	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.setOwnerActivity(this);
    	alert.show();
    }
    public boolean setAble(boolean set, boolean deletable){
    	renbtn.setEnabled(set);
    	if (deletable){
    		delbtn.setEnabled(set);
    	} else {
    		delbtn.setEnabled(false);
    	}
    	return set;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_category_editor, menu);
//        return true;
//    }
    public void readPrefs(){
    	SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	default_dir = sprefs.getString("default_dir", getString(R.string.app_default_dir));
    	font_size = Float.parseFloat(sprefs.getString("font_size", "16.0f"));
    	look_style = Integer.parseInt(sprefs.getString("look_style", "0"));
    }
}
