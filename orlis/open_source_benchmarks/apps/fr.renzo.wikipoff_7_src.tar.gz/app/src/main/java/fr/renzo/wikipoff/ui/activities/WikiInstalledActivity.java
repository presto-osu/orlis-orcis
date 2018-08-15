package fr.renzo.wikipoff.ui.activities;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import fr.renzo.wikipoff.ConfigManager;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;

public class WikiInstalledActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "WikiInstalledActivity";
	private Wiki wiki;
	private CheckedTextView selectedforreading;
	private int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_installed_wiki);

		Intent intent = getIntent();
		this.position = intent.getIntExtra("position", -1);
		this.wiki = (Wiki) intent.getExtras().getSerializable("wiki");
		// WARNING Wiki needs a context, it was lost on serializing...
		wiki.setContext(this);

		setTitle(wiki.getType()+" - "+wiki.getLangcode());

		selectedforreading = (CheckedTextView) findViewById(R.id.wikiSelectCheckbox);

		setViews();
	}

	private void setViews() {
		setIcon();
		setSelected();
		setLanguage();
		setGenDate();
		setSource();
		setAuthor();
		setType();
		setSize();
		setFiles();
		setDelete();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == WikiManagerActivity.REQUEST_DELETE_CODE) {
			setResult(resultCode);
			finish();
		}  
	}

	private void setDelete() {
		Button b = (Button) findViewById(R.id.wikiDeleteButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent outputintent = new Intent(WikiInstalledActivity.this, DeleteDatabaseActivity.class);
				outputintent.putStringArrayListExtra("dbtodelete", wiki.getDBFilesnamesAsList());
				outputintent.putExtra("dbtodeleteposition", position);
				startActivityForResult(outputintent,WikiManagerActivity.REQUEST_DELETE_CODE);
				ConfigManager.removeFromSelectedDBs(WikiInstalledActivity.this, wiki);

			}
		});
	}

	private void setFiles() {
		TextView d = (TextView) findViewById(R.id.wikiFilesTextView);
		d.setText(wiki.getFilenamesAsString());
	}

	private void setSize() {
		TextView d = (TextView) findViewById(R.id.wikiSizeTextView);
		d.setText(wiki.getSizeReadable(true));
	}

	private void setType() {
		TextView d = (TextView) findViewById(R.id.wikiTypeTextView);
		d.setText(wiki.getType());
	}

	private void setLanguage() {
		TextView d = (TextView) findViewById(R.id.wikiLanguageTextView);
		d.setText(wiki.getLanglocal()+" / "+wiki.getLangcode());
	}

	private void setAuthor() {
		TextView d = (TextView) findViewById(R.id.wikiAuthorTextView);
		d.setText(wiki.getAuthor());
	}

	private void setSource() {
		TextView d = (TextView) findViewById(R.id.wikiSourceTextView);
		d.setText(wiki.getSource());
	}

	private void setGenDate() {
		TextView d = (TextView) findViewById(R.id.wikiGenDateTextView);
		d.setText(wiki.getGendateAsString());
	}

	private void setIcon() {
		ImageView iconview = (ImageView) findViewById(R.id.wikiIcon);
		if (wiki.hasIcon()){
			// TODO
			iconview.setImageBitmap(BitmapFactory.decodeStream(wiki.getIcon()));
		} else {
			AssetManager am = getAssets();
			try {
				InputStream in = am.open("icons/wiki-default-icon.png");
				iconview.setImageBitmap(BitmapFactory.decodeStream(in));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setSelected() {
		selectedforreading.setChecked(ConfigManager.isInSelectedDBs(WikiInstalledActivity.this,wiki));
		selectedforreading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ConfigManager.isInSelectedDBs(WikiInstalledActivity.this,wiki)) {
					selectedforreading.setChecked(false);
					ConfigManager.removeFromSelectedDBs(WikiInstalledActivity.this, wiki);
				} else {
					selectedforreading.setChecked(true);
					ConfigManager.addToSelectedDBs(WikiInstalledActivity.this, wiki);
				}
			}
		});
	}
}
