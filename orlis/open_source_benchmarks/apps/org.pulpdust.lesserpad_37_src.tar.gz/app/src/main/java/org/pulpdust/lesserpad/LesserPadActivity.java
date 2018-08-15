package org.pulpdust.lesserpad;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;













import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

public class LesserPadActivity extends FragmentActivity implements TextWatcher {
	final static String TAG = "Lesser Pad Main";
	final static int FILE_NEW = 0;
	final static int FILE_OPEN = 1;
	static int OPEN_AS_DEC = 0;
	static int OPEN_AS_ENC = 1;
	static int SAVE_AS_DEC = 2;
	static int SAVE_AS_ENC = 3;
	static int ENC_FROM_MENU = 4;
	static int DEC_FROM_MENU = 5;
	static int CLOSE_AS_DEC = 6;
	static int CLOSE_AS_ENC = 7;
	final static int REQ_PASS_FOR_ENC = 2;
	final static int REQ_PASS_FOR_DEC = 3;
	static int fmode;
	static int lmode;
	static boolean dontsave = true;
	boolean oncreate = true;
	static File path;
	static String name;
	String default_dir;
	int cur_pos;
	float font_size;
	static int look_style;
	static boolean hide_ext;
	boolean spec_path;
	String path_to;
	boolean abarnotsplit;
	boolean wosave;
	EditText etxt;
//	static Editable edit;
	Spinner ebox;
	static TextView label;
	static List<String> dirs = new ArrayList<String>();
	static ArrayAdapter<String> adirs;
	static String disuse = "[\"|:;*?<>/\\\\]";
	String undo_text;
	Integer undo_pos;
	Integer undo_mode;
	Integer undo_length;
	boolean undo_flag;
	Byte[] origin;
	String former;
	static String action;
	String sname;
	int sfmode;
	libLesserPad llp = new libLesserPad();
	static boolean priv = false;
	String pass = null;
	int prostep;
	
	public static class EditMemo extends EditText {
		private Rect rt;
		private Paint pt;
		
		public EditMemo(Context cn, AttributeSet as){
			super(cn, as);
			rt = new Rect();
			pt = new Paint();
			pt.setStyle(Paint.Style.STROKE);
			if (look_style > 0){
				pt.setColor(Color.rgb(68,68,68));
			} else {
				pt.setColor(Color.rgb(188,188,188));
			}
			pt.setPathEffect(new DashPathEffect(new float[]{ 2.0f, 2.0f }, 0));
		}
		@Override
		protected void onDraw(Canvas cv){
			int mhp = getMeasuredHeight() - getExtendedPaddingTop();
			int lh = getLineHeight();
			int dlc = mhp / lh;
			int lc = getLineCount();
			int count = Math.max(dlc, lc);
			Rect r = rt;
			Paint p = pt;
			int bl = getLineBounds(0, r);
			for (int i = 0; i < count; i++){
				cv.drawLine(r.left, bl + 1, r.right, bl + 1, p);
				bl = bl + lh;
			}
			super.onDraw(cv);
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        action = getIntent().getAction();
        readPrefs();
        if ((action.equals(Intent.ACTION_EDIT) || action.equals(Intent.ACTION_VIEW))
        		&& (Build.VERSION.SDK_INT < 11 || Build.VERSION.SDK_INT > 13)){
        	if (look_style >0){
        		setTheme(R.style.AppTheme_Dialog_Dark);
        	} else {
        		setTheme(R.style.AppTheme_Dialog);
        	}
       } else if (look_style > 0){ 
    		setTheme(R.style.AppTheme_Dark);
    	} else {
    		setTheme(R.style.AppTheme);
    	}
        if (Build.VERSION.SDK_INT >= 14 &&
				Build.VERSION.SDK_INT < 21 &&
				abarnotsplit == false){
        	forICS fics = new forICS();
        	fics.setUiOptions(1, this);
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (action.equals(libLesserPad.LPAD_NEW) || action.equals(Intent.ACTION_MAIN)){
        	this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
        	this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        }
        setContentView(R.layout.activity_lesser_pad);
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
        	Toast.makeText(this, R.string.mes_nosd, Toast.LENGTH_LONG).show();
        	finish();
        }
        etxt = (EditText) findViewById(R.id.editText1);
    	etxt.setTextSize(font_size);
        etxt.addTextChangedListener(this);
        ebox = (Spinner) findViewById(R.id.spinner1);
        label = (TextView) findViewById(R.id.textView1);
        if (look_style > 0 && Build.VERSION.SDK_INT < 21) label.setBackgroundResource(R.drawable.palmtitle_dark);
        dirs = new ArrayList<String>();
        adirs = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dirs);
        adirs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ebox.setAdapter(adirs);
        label.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
   		     });
        Uri curi = getIntent().getData();
        if (getIntent().getStringExtra(libLesserPad.CRYPT_PASS) != null){
        	pass = getIntent().getStringExtra(libLesserPad.CRYPT_PASS);
        }
        if (savedInstanceState != null){
        	name = savedInstanceState.getString("name");
        	String pt = savedInstanceState.getString("path");
        	if (pt != null) path = new File(pt);
        	if (savedInstanceState.getString("pass") != null) pass = savedInstanceState.getString("pass");
        	if (savedInstanceState.getString("former") != null) former = savedInstanceState.getString("former");
        	if (savedInstanceState.getBoolean("priv") != false) priv = savedInstanceState.getBoolean("priv");
        }
        if (savedInstanceState == null){
        	fmode = FILE_NEW;
        	name = "";
        } else if (path != null & name != null && !name.equals("")){
        	curi = Uri.fromFile(new File(path, name));
        }
        if (spec_path == true){
        	path = new File(new File(path_to), default_dir);
        } else {
        	path = new File(Environment.getExternalStorageDirectory(), default_dir);
        }
        if (!path.exists()){
        	path.mkdirs();
        }
        if (Intent.ACTION_EDIT.equals(action) || 
        		Intent.ACTION_VIEW.equals(action) || 
        		libLesserPad.LPAD_EDIT.equals(action)){
        	if (savedInstanceState != null && curi != null){
        		fileOpened(curi);
        	} else if (curi != null && fileOpen(curi)){
        		fileOpened(curi);
        	} else {
        		Log.e(TAG, "fileOpen failed.");
        		Toast.makeText(this, R.string.mes_not_open, Toast.LENGTH_SHORT).show();
        	}
        	if (libLesserPad.LPAD_EDIT.equals(action)){
        		lmode = 1;
        	} else {
        		lmode = 0;
        	}
        } else if (libLesserPad.LPAD_NEW.equals(action)){
        	if (savedInstanceState != null && curi != null && fileOpen(curi)){
        		fileOpened(curi);
        	} else {
        		path = new File(getIntent().getStringExtra("PATH"));
        		name = "";
        	}
        	lmode = 1;
        	if (savedInstanceState != null) Log.d(TAG, "gatt!");
        	if (curi != null) Log.d(TAG, curi.toString());
        	if (name.equals("")) Log.d(TAG, "name empty");
        	if (fmode == 0) Log.d(TAG, "fmode 0");
        	if (etxt.length() >= 1) Log.d(TAG, "content exist");
        } else {
        	lmode = 0;
        }
		if (Build.VERSION.SDK_INT >= 21){
			forLollipop.readyActionBar(findViewById(R.id.toolBar1), this, 1, look_style, abarnotsplit);
		}
        if ((Build.VERSION.SDK_INT >= 11)){
        	forHoneycomb fhc = new forHoneycomb();
    		fhc.setEditText(etxt, getApplicationContext(), this, look_style);
			if ((Build.VERSION.SDK_INT < 21)) {
				if ((!action.equals(Intent.ACTION_EDIT) & !action.equals(Intent.ACTION_VIEW))
						|| (Build.VERSION.SDK_INT >= 11 & Build.VERSION.SDK_INT <= 13)) {
					ebox.setVisibility(View.GONE);
					label.setVisibility(View.GONE);
					if (path.equals(Environment.getExternalStorageDirectory())) {
						fhc.setActionBar(this, 0, adirs, 0);
					} else {
						fhc.setActionBar(this, 0, adirs, 1);
					}
					if (fmode == FILE_NEW) fhc.setText(this, getString(R.string.label_new));
					if (fmode == FILE_OPEN) {
						if (hide_ext == true) {
							fhc.setText(this, name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
						} else {
							fhc.setText(this, name);
						}
					}
				}
			}
        }
        if (fmode == FILE_NEW){
        	label.setText(R.string.label_new);
        } else if (fmode == FILE_OPEN && name != ""){
        	if (hide_ext == true){
        		label.setText(name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
        	} else {
        		label.setText(name);
        	}
        } else {
        }
        if (path.equals(Environment.getExternalStorageDirectory())){
        	ebox.setEnabled(false);
        } else {
        	llp.listDir(path, adirs, dirs, ebox, this, action);
        }
        ebox.setOnItemSelectedListener(new Mover());
        if (savedInstanceState == null){
        	if (cur_pos == 1){
        		etxt.setSelection(etxt.getText().length());
        		etxt.setSelection(0);
        	} else if (cur_pos == 2){
        		etxt.setSelection(etxt.getText().length());
        	}
        }
    	if (look_style > 0){
    		etxt.setTextColor(Color.rgb(192,192,192));
    		etxt.setHighlightColor(Color.rgb(0,0,255));
    		etxt.setBackgroundColor(Color.rgb(25,25,25));
			if (Build.VERSION.SDK_INT >= 21) label.setTextColor(Color.rgb(190,190,190));
    	} else if (look_style == 0){
    		etxt.setTextColor(Color.rgb(50,50,50));
    		etxt.setHighlightColor(Color.rgb(255,255,0));
    		etxt.setBackgroundColor(Color.rgb(250,250,250));
			if (Build.VERSION.SDK_INT >= 21) label.setTextColor(Color.rgb(50,50,50));
    	}
    }
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG, "onStart");
    }
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d(TAG, "onResume");
    }
    @Override
    public void onRestart(){
    	super.onRestart();
    	Log.d(TAG, "onRestart");
    }
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(TAG, "onPause");
    	if (fmode == FILE_OPEN){
    		Log.d(TAG, "oooo");
    	} else if (fmode == FILE_NEW){
    		Log.d(TAG, "nnnn");
    	}
    	if (fmode == FILE_OPEN && former != null && former.equals(etxt.getText().toString())){
//    	if (fmode == FILE_OPEN && origin != null && Arrays.equals(origin, getSum(etxt.getText().toString()))){
    		dontsave = true;
    	}
        if (fmode == FILE_NEW && etxt.getText().toString().length() == 0){
        	dontsave = true;
        }
    	if (dontsave){
    		
    	} else {
    		if (priv){
    			doSave(toEncrypt(pass, etxt.getText().toString()));
    		} else {
    			doSave(etxt.getText().toString());
    		}
    		dontsave = true;
    	}
    }
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(TAG, "onStop");
    }
    @Override
    public void onDestroy(){
    	super.onDestroy();
		priv = false;
		pass = null;
    	Log.d(TAG, "onDestroy");
    }
    @Override
    public void onSaveInstanceState(Bundle sis){
    	if (path != null) sis.putString("path", path.toString());
    	sis.putString("name", name);
    	if (pass != null) sis.putString("pass", pass);
    	if (former != null) sis.putString("former", former);
    	if (priv != false) sis.putBoolean("priv", priv);
    	super.onSaveInstanceState(sis);
    }
    
    public class Mover implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> av, View v, int pos,
				long id) {
			doMove(getApplicationContext(), pos);
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
    }
    public static void doMove(Context context, int pos){
		if (fmode == FILE_NEW){
			File base = path.getParentFile();
			path = new File(base, "/" + dirs.get(pos));
		} else if (fmode == FILE_OPEN && !path.getName().equals(dirs.get(pos))){
			String base = path.getParent();
			String newpath = base + "/" + dirs.get(pos);
			File from = new File(path, name);
			File to = new File(newpath, name);
			if (!to.exists() && from.renameTo(to)){
				path = new File(newpath);
			} else {
				Log.e(TAG, "onItemSelected::MoveFailed");
				Toast.makeText(context, R.string.mes_move_fail, Toast.LENGTH_SHORT).show();
			}
		}
    }
    public boolean fileOpen(Uri uri){
    	String suri = uri.toString();
		if (suri.startsWith("file://")){
			suri = suri.substring(7);
		}
    	try {
        	File file = new File(URLDecoder.decode(suri, "UTF-8"));
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String content;
			StringBuilder contb = new StringBuilder(4096);
//			content = br.readLine();
			contb.append(br.readLine());
			String line;
			while ((line = br.readLine()) != null){
//				content = content + "\n" + line;
				contb.append("\n" + line);
			}
			content = contb.toString();
			if (content == null) content = "";
			if (suri.endsWith(".len") && pass != null){
				forFroyo ffy = new forFroyo();
				content = ffy.doDecrypt(pass, content);
				priv = true;
				
			}
//			origin = getSum(content);
			former = content;
			etxt.setText(content);
			br.close();
			return true;
		} catch (FileNotFoundException e) {
			Log.e(TAG, "fileOpen::NotFound");
			return false;
		} catch (UnsupportedEncodingException e){
			Log.e(TAG, "fileOpen::DecodeError");
			return false;
		} catch (IOException e) {
			Log.e(TAG, "fileOpen::IOError");
			return false;
		} catch (GeneralSecurityException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
    }
    void fileOpened(Uri curi){
		fmode = FILE_OPEN;
		String gds = curi.toString();
		if (gds.startsWith("file://")){
			gds = gds.substring(7);
		}
		File fl = new File(gds);
		try {
			path = new File(URLDecoder.decode(fl.getParent(), "UTF-8"));
    		name = URLDecoder.decode(fl.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "hogeeeeeee!!!");
		}
    }
    public void doSave(String text){
    	final Activity av = this;
	    final boolean issave = textFiling(text);
	    	if (issave == false){
	    		Log.d(TAG, "textFiling failed.");
	    		Toast.makeText(getApplicationContext(), R.string.mes_not_save, Toast.LENGTH_SHORT).show();
	    	} else {
	    		Toast.makeText(getApplicationContext(), R.string.mes_save, Toast.LENGTH_SHORT).show();
	    		fmode = FILE_OPEN;
	    		if (Build.VERSION.SDK_INT >= 11 &
						Build.VERSION.SDK_INT < 21 &
						!(action.equals(Intent.ACTION_EDIT) |
	    				action.equals(Intent.ACTION_VIEW))){
	    			forHoneycomb fhc = new forHoneycomb();
	    			if (hide_ext == true){
	    				fhc.setText(av, name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
	    			} else {
	    				fhc.setText(av, name);
	    			}
	    		} else {
	    			if (hide_ext == true){
	    				label.setText(name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
	    			} else {
	    				label.setText(name);
	    			}
	    		}
	    	}
    }
    public boolean textFiling(String text){
        if (!path.exists()){
        	path.mkdirs();
        }
        if (name.equals("")){
        	name = enTitle(0, etxt.getText().toString().split("\n")[0]);
        } else {
        }
        File object = new File(path, name);
		while (fmode == FILE_NEW && object.exists()){
			name = enTitle(1, etxt.getText().toString().split("\n")[0]);
			object = new File(path, name);
		}
        try {
//			FileWriter fw = new FileWriter(object, false);
			FileOutputStream fop = new FileOutputStream(object, false);
			byte[] bu = new byte[4096];
			int lng;
			ByteArrayInputStream bais = new ByteArrayInputStream(text.concat("\n").getBytes("UTF-8"));
			while ((lng = bais.read(bu, 0, bu.length)) > 0){
				fop.write(bu, 0, lng);
			}
			bais.close();
			fop.close();
//			fw.write(text + "\n");
//			fw.close();
//			origin = getSum(text);
			former = etxt.getText().toString();
			return true;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
    }
    public static String enTitle(int mode, String line){
    	String newname;
    	String ext;
//       String text = etxt.getText().toString();
//       String line = etxt.getText().toString().split("\n")[0];
    	String rename = line.replaceAll(disuse, "-");
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SSSZ");
    	if (priv == true){
    		ext = ".len";
    	} else {
    		ext = ".txt";
    	}
        if (!line.equals("") && mode == 0){
        	if (rename.length() > 62){
        		rename = rename.substring(0, 62);
        	}
            newname = rename + ext;
        } else if (!line.equals("") && mode == 1){
        	if (rename.length() > 48){
        		rename = rename.substring(0, 48);
        	}
        	newname = rename + "-" + format.format(new Date()) + ext;
        } else if (line.equals("")){
        	newname = format.format(new Date()) + ext;
        } else {
        	newname = "";
        }
    	return newname;
    }
    public void doCopy(CharSequence text){
    	if (Build.VERSION.SDK_INT >= 11){
    		forHoneycomb.doCopy(getApplicationContext(), text);
    	} else {
    		forBase.doCopy(getApplicationContext(), text);
    	}
//		Toast.makeText(getApplicationContext(), getString(R.string.menu_copy) + ":" + text,
//				Toast.LENGTH_SHORT).show();
    }
    public CharSequence getCopy(){
		if (Build.VERSION.SDK_INT >= 11){
			return forHoneycomb.getCopy(getApplicationContext());
		} else {
			return forBase.getCopy(getApplicationContext());
		}
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	super.onPrepareOptionsMenu(menu);
    	if (llp.isIntentAvailable(this, libLesserPad.DA_LAUNCH)){
    		MenuItem mda = (MenuItem) menu.findItem(R.id.menu_da);
    		mda.setVisible(true);
    	} else {
    		MenuItem mda = (MenuItem) menu.findItem(R.id.menu_da);
    		mda.setEnabled(false);
    	}
		MenuItem mdec = (MenuItem) menu.findItem(R.id.menu_dec);
		MenuItem menc = (MenuItem) menu.findItem(R.id.menu_enc);
		if (Build.VERSION.SDK_INT < 8){
			mdec.setVisible(false);
			mdec.setEnabled(false);
			menc.setVisible(false);
			menc.setEnabled(false);
		} else if (priv == true){
    		mdec.setVisible(true);
    		menc.setVisible(false);
    		mdec.setEnabled(true);
    		menc.setEnabled(false);
    	} else {
    		menc.setVisible(true);
    		mdec.setVisible(false);
    		menc.setEnabled(true);
    		mdec.setEnabled(false);
    	}
		if (etxt.getText().toString().equals("")){
			menc.setEnabled(false);
			mdec.setEnabled(false);
		}
		MenuItem mwosave = (MenuItem) menu.findItem(R.id.menu_cancel);
		mwosave.setVisible(wosave);
    	if (Build.VERSION.SDK_INT >= 11){
    		forActionBarMenu(menu);
    		return true;
    	}
    	return true;
    }
    @TargetApi(11)
    public void forActionBarMenu(Menu menu){
    	if (Build.VERSION.SDK_INT >= 11){
    		MenuItem mix = (MenuItem) menu.findItem(R.id.menu_cut);
    		MenuItem mic = (MenuItem) menu.findItem(R.id.menu_copy);
    		MenuItem mif = (MenuItem) menu.findItem(R.id.menu_search);
    		mix.setVisible(false);
    		mic.setVisible(false);
    		mif.setVisible(false);
    	}
		if (Build.VERSION.SDK_INT >= 21){
			forLollipop.setMenuItems(menu, abarnotsplit, llp.isIntentAvailable(this, libLesserPad.DA_LAUNCH));
		}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (Build.VERSION.SDK_INT >= 11 && look_style > 0){
    		getMenuInflater().inflate(R.menu.activity_lesser_pad_dark, menu);
    	} else {
    		getMenuInflater().inflate(R.menu.activity_lesser_pad, menu);
    	}
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem mi){
    	switch (mi.getItemId()){
    	case R.id.menu_da:
    		Intent dal = llp.daLaunch(this, etxt, look_style);
    		startActivityForResult(Intent.createChooser(dal, getString(R.string.menu_da)), 1);
    		return true;
    	case R.id.menu_cut:
    		doCut();
    		return true;
    	case R.id.menu_copy:
    		todoCopy();
    		return true;
    	case R.id.menu_paste:
    		doPaste();
    		return true;
    	case R.id.menu_undo:
    		if (undo_mode != null && undo_pos != null && undo_text != null){
    			switch (undo_mode){
    			case 0:
//    				edit = etxt.getEditableText();
    				etxt.getEditableText().replace(undo_pos, undo_length, undo_text);
    				undo_mode = null;
    				break;
    			case 1:
//    				edit = etxt.getEditableText();
    				etxt.getEditableText().delete(undo_pos, undo_pos + undo_text.length());
    				undo_mode = null;
    				break;
    			}
    		}
    		return true;
    	case R.id.menu_selectall:
    		etxt.selectAll();
    		return true;
    	case R.id.menu_delete:
    		DialogFragment dd = DeleteDialog.newInstance(this);
    		dd.show(getSupportFragmentManager(), "fuga");
    		return true;
    	case R.id.menu_detail:
    		showDetail(this, this);
    		return true;
    	case R.id.menu_enc:
    		echoProtect(REQ_PASS_FOR_ENC);
    		return true;
    	case R.id.menu_dec:
    		echoProtect(REQ_PASS_FOR_DEC);
    		return true;
    	case R.id.menu_cancel:
    		dontsave = true;
    		finish();
    		return true;
    	case R.id.menu_search:
    		llp.goSearch(this, etxt);
    		return true;
    	case R.id.menu_share:
    		Intent intent = new Intent();
    		intent.setAction(Intent.ACTION_SEND);
    		intent.setType("text/plain");
    		intent.putExtra(Intent.EXTRA_TEXT, etxt.getText().toString());
    		startActivity(intent);
    		return true;
    	case R.id.menu_settings:
    		Intent goprefs = new Intent();
    		goprefs.setClassName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.LesserPadPrefs");
    		startActivityForResult(goprefs, 0);
    		return true;
    	case R.id.menu_ime:
    		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.toggleSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0, 0);
    		return true;
    	case R.id.menu_save:
    		boolean res;
    		if (priv){
    			res = textFiling(toEncrypt(pass, etxt.getText().toString()));
    		} else {
    			res = textFiling(etxt.getText().toString());
    		}
			if (res){
				Toast.makeText(getApplicationContext(), R.string.mes_save, Toast.LENGTH_SHORT).show();
				dontsave = true;
			} else {
				Toast.makeText(getApplicationContext(), R.string.mes_not_save, Toast.LENGTH_SHORT).show();
			}
    		return true;
		case android.R.id.home:
			if (doBack()){

			} else {
				finish();
			}
			return true;
    	default:
    		return super.onOptionsItemSelected(mi);
    	}
    }
    public void echoProtect(int req){
    	Intent it = new Intent();
    	it.setClassName("org.pulpdust.lesserpad", "org.pulpdust.lesserpad.ProtectActivity");
        if (name != null && !name.equals("")) {
            it.putExtra(libLesserPad.CRYPT_FILE, name);
        }
        switch (req){
        case REQ_PASS_FOR_ENC:
        	it.putExtra(libLesserPad.REQ_PASS_MODE, libLesserPad.REQ_PASS_FOR_ENC);
        	break;
        case REQ_PASS_FOR_DEC:
        	it.putExtra(libLesserPad.REQ_PASS_MODE, libLesserPad.REQ_PASS_FOR_DEC);
        	break;
        default:
        	it.putExtra(libLesserPad.REQ_PASS_MODE, libLesserPad.REQ_PASS_FOR_ENC);
        	break;
        }
    	startActivityForResult(it, req);
    }
    public void doCut(){
    	String select = llp.getSelection(true, etxt);
    	doCopy(select);
    }
    public void todoCopy(){
    	String select;
    	select = llp.getSelection(false, etxt);
		doCopy(select);
    }
    public void doPaste(){
//		edit = etxt.getEditableText();
		int start = etxt.getSelectionStart();
		int end = etxt.getSelectionEnd();
		CharSequence obj = getCopy();
		if (obj != null){
			etxt.getEditableText().replace(Math.min( start, end ), Math.max( start, end ), obj);
		}
    }
    public void doInsert(CharSequence obj){
//		edit = etxt.getEditableText();
		int start = etxt.getSelectionStart();
		int end = etxt.getSelectionEnd();
		if (obj != null){
			etxt.getEditableText().replace(Math.min( start, end ), Math.max( start, end ), obj);
		}
    }
    
    public static class DetailDialog extends DialogFragment {
    	static Context cx;
    	static int ln;
    	static String li;
    	public static DetailDialog newInstance(Context c, int length, String line){
    		DetailDialog dd = new DetailDialog();
    		cx = c;
    		ln = length;
    		li = line;
    		return dd;
    	}
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState){
    		return echoDetail(cx, getActivity(), ln, li);
    	}
    }
    public static class DeleteDialog extends DialogFragment {
    	static Context cx;
    	public static DeleteDialog newInstance(Context c){
    		DeleteDialog dd = new DeleteDialog();
    		cx = c;
    		return dd;
    	}
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState){
    		return sureDelete(cx, getActivity());
    	}
    }
    public void showDetail(Context c, FragmentActivity av){
		DialogFragment df = DetailDialog.newInstance(c, etxt.length(), etxt.getText().toString().split("\n")[0]);
		df.show(av.getSupportFragmentManager(), "hoge");
    }
    public static AlertDialog echoDetail(final Context c, final FragmentActivity av, int length, final String line){
    	String update;
    	String dispname;
    	if (name.equals("")){
    		update = c.getString(R.string.dialog_presave);
    		dispname = c.getString(R.string.dialog_notfiled);
    	} else {
    		File object = new File(path, name);
    		update = DateUtils.formatDateTime(c, object.lastModified(), 
    		    DateUtils.FORMAT_SHOW_YEAR |
    		    DateUtils.FORMAT_SHOW_DATE |
    		    DateUtils.FORMAT_SHOW_TIME);
    		dispname = name;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
    	builder.setMessage(c.getString(R.string.dialog_name)+": "+dispname+"\n"+c.getString(R.string.dialog_update)+": "+update+"\n"+
    			c.getString(R.string.dialog_length)+": "+length)
    			.setTitle(R.string.dialog_detail)
    			.setCancelable(true)
    			.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener(){
    				public void onClick(DialogInterface dialog, int id){
    					dialog.dismiss();
    				}
    			});
    	if (fmode == FILE_OPEN){
    			builder.setNeutralButton(R.string.dialog_rename, new DialogInterface.OnClickListener(){
    				public void onClick(DialogInterface dialog, int id){
    					String newname = enTitle(0, line);
    					if (!name.equals(newname)){
    						File from = new File(path, name);
    						File to = new File(path, newname);
    						while (to.exists()){
    							newname = enTitle(1, line);
    							to = new File(path, newname);
    						}
    						if (!to.exists() & from.renameTo(to)){
    							name = newname;
    							if (Build.VERSION.SDK_INT >= 11 &
										Build.VERSION.SDK_INT < 21 &
										!(action.equals(Intent.ACTION_EDIT) |
    				    				action.equals(Intent.ACTION_VIEW))){
    								forHoneycomb fhc = new forHoneycomb();
    								if (hide_ext == true){
    									fhc.setText(av, name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
    								} else {
    									fhc.setText(av, name);
    								}
    							} else {
    								if (hide_ext == true){
    									label.setText(name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
    								} else {
    									label.setText(name);
    								}
    							}
    							Toast.makeText(c, av.getString(R.string.mes_rename)+": "+name, Toast.LENGTH_SHORT).show();
    						} else {
    							Toast.makeText(c, R.string.mes_rename_fail, Toast.LENGTH_SHORT).show();
    						}
    					}
						dialog.dismiss();
    				}
    			});
    	}
    	AlertDialog alert = builder.create();
    	return alert;
    }
    public static AlertDialog sureDelete(final Context c, final Activity av){
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
    	builder.setMessage(c.getString(R.string.dialog_delete_sure))
    			.setTitle(R.string.dialog_delete)
    	       .setCancelable(false)
    	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
					   if (doDelete(path, name)) {
						   dontsave = true;
						   if (lmode == 1) {
							   Intent result = new Intent();
							   result.putExtra("PATH", path.toString());
							   av.setResult(RESULT_OK, result);
						   }
						   av.finish();
					   } else {
						   Toast.makeText(c, R.string.mes_del_fail, Toast.LENGTH_SHORT).show();
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
    	return alert;
    }
    public static boolean doDelete(File path, String name){
    	File object;
    	if (name == null){
    		object = new File(path.toString());
    	} else {
    		object = new File(path, name);
    	}
        if (object.delete()){
        	return true;
        } else {
        	return false;
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent kev){
    	if (kev.getAction() == KeyEvent.ACTION_DOWN){
    		switch (kev.getKeyCode()){
    		case KeyEvent.KEYCODE_BACK:
				if (doBack()){
					return true;
				} else {
					return super.dispatchKeyEvent(kev);
				}
    		case KeyEvent.KEYCODE_MENU:
    			if (Build.VERSION.SDK_INT == 9 || Build.VERSION.SDK_INT == 10){
    				return true;
    			} else {
    				return super.dispatchKeyEvent(kev);
    			}
    		case KeyEvent.KEYCODE_TAB:
    			if (Build.VERSION.SDK_INT >= 11 && etxt.isFocused()){
    				doInsert("\t");
    				return true;
    			} else {
    				return super.dispatchKeyEvent(kev);
    			}
    		default:
    			return super.dispatchKeyEvent(kev);
    		}
    	} else if (kev.getAction() == KeyEvent.ACTION_UP){
    		switch(kev.getKeyCode()){
    		case KeyEvent.KEYCODE_SEARCH:
    			llp.goSearch(this, etxt);
    			return true;
    		case KeyEvent.KEYCODE_MENU:
    			if (Build.VERSION.SDK_INT == 9 || Build.VERSION.SDK_INT == 10){
    				openOptionsMenu();
    				return true;
    			} else {
    				return super.dispatchKeyEvent(kev);
    			}
    		default:
    			return super.dispatchKeyEvent(kev);
    		}
    	} else {
    		return super.dispatchKeyEvent(kev);
    	}
    }
	public boolean doBack(){
		if (fmode == FILE_OPEN && former != null && former.equals(etxt.getText().toString())){
			dontsave = true;
		}
		if (fmode == FILE_NEW && etxt.getText().toString().length() == 0){
			dontsave = true;
		}
		if (!dontsave && etxt.getText().toString().length() != 0){
			boolean res;
			if (priv){
				res = textFiling(toEncrypt(pass, etxt.getText().toString()));
			} else {
				res = textFiling(etxt.getText().toString());
			}
			if (res){
				Toast.makeText(getApplicationContext(), R.string.mes_save, Toast.LENGTH_SHORT).show();
				dontsave = true;
			} else {
				Toast.makeText(getApplicationContext(), R.string.mes_not_save, Toast.LENGTH_SHORT).show();
				return true;
			}
		}
		if (lmode == 1){
			if (fmode == FILE_OPEN && etxt.getText().toString().length() == 0){
				doDelete(path, name);
				dontsave = true;
			}
			Intent result = new Intent();
			result.putExtra("PATH", path.toString());
			setResult(RESULT_OK, result);
			return false;
		} else {
			return false;
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	switch (requestCode){
    	case 0:
    		readPrefs();
        	etxt.setTextSize(font_size);
    		break;
    	case 1:
    		if (resultCode == RESULT_OK && data.getStringExtra(libLesserPad.DA_EX_RETURN) != null){
    			SharedPreferences props = getPreferences(MODE_PRIVATE);
    			int start = props.getInt("select_start", 0);
    			int end = props.getInt("select_end", 0);
//    			edit = etxt.getEditableText();
    			etxt.getEditableText().replace(Math.min( start, end ), Math.max( start, end ), 
    					data.getStringExtra(libLesserPad.DA_EX_RETURN));
    		}
    		break;
		case REQ_PASS_FOR_ENC:
			if (resultCode == RESULT_OK){
				try {
					pass = data.getStringExtra(libLesserPad.CRYPT_PASS);
//					String encd = forFroyo.doEncrypt(pass, etxt.getText().toString());
					if (name != null && !name.equals("")){
					} else if (data.getStringExtra(libLesserPad.CRYPT_FILE) != null &&
                            !data.getStringExtra(libLesserPad.CRYPT_FILE).equals("")){
						name = data.getStringExtra(libLesserPad.CRYPT_FILE);
                    }
					forFroyo ffy = new forFroyo();
					if (textFiling(ffy.doEncrypt(pass, etxt.getText().toString()))){
						priv = true;
						String name_to = name;
						if (name.endsWith(".txt")) name_to = name.replaceFirst("\\.txt$", ".len");
						File from = new File(path, name);
						File to = new File(path, name_to);
						while (to.exists()){
							name_to = enTitle(1, etxt.getText().toString().split("\n")[0]);
							to = new File(path, name_to);
						}
						if (from.renameTo(to)){
							name = name_to;
	    					Toast.makeText(getApplicationContext(), R.string.mes_save_encrypt, Toast.LENGTH_SHORT).show();
	    					setLabel(name);
							break;
						} else {
							Log.e(TAG, "Can not rename.");
							priv = false;
	    					Toast.makeText(getApplicationContext(), R.string.mes_change_ext_fail, Toast.LENGTH_SHORT).show();
							break;
						}
					} else {
						Log.e(TAG, "Can not save.");
    					Toast.makeText(getApplicationContext(), R.string.mes_not_save, Toast.LENGTH_SHORT).show();
						break;
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
					Toast.makeText(getApplicationContext(), R.string.mes_encrypt_fail, Toast.LENGTH_SHORT).show();
					break;
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
					Toast.makeText(getApplicationContext(), R.string.mes_encrypt_fail, Toast.LENGTH_SHORT).show();
					break;
				}
			}
			break;
		case REQ_PASS_FOR_DEC:
			if (resultCode == RESULT_OK){
//					pass = data.getStringExtra(libLesserPad.CRYPT_PASS);
					if (!pass.equals(data.getStringExtra(libLesserPad.CRYPT_PASS))){
						Log.e(TAG, "Password invalid.");
    					Toast.makeText(getApplicationContext(), R.string.mes_pass_invalid, Toast.LENGTH_SHORT).show();
						break;
					}
					if (name != null && !name.equals("")){
					} else if (data.getStringExtra(libLesserPad.CRYPT_FILE) != null &&
                            !data.getStringExtra(libLesserPad.CRYPT_FILE).equals("")){
						name = data.getStringExtra(libLesserPad.CRYPT_FILE);
                    }
					if (textFiling(etxt.getText().toString())){
						priv = false;
						String name_to = name;
						if (name.endsWith(".len")) name_to = name.replaceFirst("\\.len$", ".txt");
						File from = new File(path, name);
						File to = new File(path, name_to);
						while (to.exists()){
							name_to = enTitle(1, etxt.getText().toString().split("\n")[0]);
							to = new File(path, name_to);
						}
						if (from.renameTo(to)){
							name = name_to;
	    					Toast.makeText(getApplicationContext(), R.string.mes_save_clear, Toast.LENGTH_SHORT).show();
	    					setLabel(name);
							break;
						} else {
							Log.e(TAG, "Can not rename.");
							priv = false;
	    					Toast.makeText(getApplicationContext(), R.string.mes_change_ext_fail, Toast.LENGTH_SHORT).show();
							break;
						}
					} else {
						Log.e(TAG, "Can not save.");
    					Toast.makeText(getApplicationContext(), R.string.mes_not_save, Toast.LENGTH_SHORT).show();
						break;
					}
			}
			
			break;
    	}
    }
    public void setLabel(String name){
		if (Build.VERSION.SDK_INT >= 11 &
				Build.VERSION.SDK_INT < 21 &
				!(action.equals(Intent.ACTION_EDIT) |
				action.equals(Intent.ACTION_VIEW))){
			forHoneycomb fhc = new forHoneycomb();
			if (hide_ext == true){
				fhc.setText(this, name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
			} else {
				fhc.setText(this, name);
			}
		} else {
			if (hide_ext == true){
				label.setText(name.replaceFirst("(\\.txt$)|(\\.len$)", ""));
			} else {
				label.setText(name);
			}
		}

    }
    public String toEncrypt(String pass, String text){
    	try {
    		forFroyo ffy = new forFroyo();
    		return ffy.doEncrypt(pass, text);
    	} catch (UnsupportedEncodingException e){
    		Log.e(TAG, e.getMessage());
			Toast.makeText(getApplicationContext(), R.string.mes_encrypt_fail, Toast.LENGTH_SHORT).show();
    		return "";
    	} catch (GeneralSecurityException e){
    		Log.e(TAG, e.getMessage());
			Toast.makeText(getApplicationContext(), R.string.mes_encrypt_fail, Toast.LENGTH_SHORT).show();
    		return "";
    	}
    }
    public void readPrefs(){
    	SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	default_dir = sprefs.getString("default_dir", getString(R.string.app_default_dir));
    	cur_pos = Integer.parseInt(sprefs.getString("cur_pos", "1"));
    	font_size = Float.parseFloat(sprefs.getString("font_size", "18.0f"));
    	look_style = Integer.parseInt(sprefs.getString("look_style", "0"));
    	hide_ext = sprefs.getBoolean("hide_ext", false);
    	spec_path = sprefs.getBoolean("spec_path", false);
    	path_to = sprefs.getString("path_to", Environment.getExternalStorageDirectory().toString());
    	if (path_to.equals("")){
    		path_to = Environment.getExternalStorageDirectory().toString();
    	}
    	abarnotsplit = sprefs.getBoolean("abar_not_split", false);
		wosave = sprefs.getBoolean("show_close_wo_save", false);

    }
	@Override
	public void afterTextChanged(Editable s) {
//		Log.d(TAG, s.toString());
		if (oncreate && s.length() == 0){
			oncreate = false;
			dontsave = true;
		} else {
			dontsave = false;
		}
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
//		Log.d(TAG, s.toString());
		if (s.length() > 0){
			undo_mode = 0;
			undo_pos = start;
			undo_length = start + after;
			undo_text = s.toString().substring(undo_pos, undo_pos + count);
			undo_flag = true;
		} else {
			undo_flag = false;
		}
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
//		Log.d(TAG, s.toString());
		if (before == 0 && undo_flag){
			undo_mode = 1;
			undo_pos = start;
			undo_text = s.toString().substring(start, start + count);
		}
	}
	

}
