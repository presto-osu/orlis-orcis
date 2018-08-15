/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.renzo.wikipoff.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import fr.renzo.wikipoff.Article;
import fr.renzo.wikipoff.Database;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.WikipOff;


public class ArticleActivity extends SherlockActivity implements SearchView.OnQueryTextListener {

	@SuppressWarnings("unused")
	private static final String TAG = "ArticleActivity";
	private Database dbHandler;
	private WebView webview;
	private Article article;
	private String wanted_title;
    private String onlineURL;
	private SharedPreferences config;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		this.dbHandler= ((WikipOff) getApplication()).getDatabaseHandler(this);

		this.config=PreferenceManager.getDefaultSharedPreferences(this);;

		setContentView(R.layout.activity_article);

		this.webview= (WebView) findViewById(R.id.article_webview);
		this.webview.getSettings().setJavaScriptEnabled(true);

		Intent source_intent = getIntent();
		wanted_title = source_intent.getStringExtra(getString(R.string.intent_article_extra_key_title));

		new Thread(new Runnable() {
			@Override
			public void run() {
				article = dbHandler.searchArticleFromTitle(wanted_title);
				updateViews();
			}
		}).start();

	}

	private void displayNewArticle(String title) {
		Intent myIntent = new Intent(this, ArticleActivity.class);
		myIntent.putExtra(getString(R.string.intent_article_extra_key_title), title);
		startActivity(myIntent);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_manage_wikis:
			Intent ami = new Intent(this, WikiManagerActivity.class);
			startActivity(ami);
			return true;
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		case R.id.action_webbrowser:
			Intent webIntent = new Intent( Intent.ACTION_VIEW );
            Uri uri=Uri.parse("https://www.google.com/search?q="+wanted_title);
			if (this.article!=null) {
				uri = Uri.parse(this.article.wiki.getOnlineURL() + this.article.title);
			}
            webIntent.setData(uri);
			startActivity( webIntent );
			return true;
		case R.id.action_about:
			Intent i2 = new Intent(this, AboutActivity.class);
			startActivity(i2);
			return true;
		case  android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.articlemenu, menu);
		SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());

		searchView.setOnQueryTextListener(this);
		menu.add("Search")
		.setActionView(searchView)
		.setIcon(R.drawable.ic_action_search)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return true;
	}
	@Override
	public boolean onQueryTextSubmit(String query) {
		webview.findNext(true);
		return true;
	}

	@Override
	@SuppressWarnings("deprecation") // Because we want to work with API 14
	public boolean onQueryTextChange(String newText) {
		webview.findAll(newText);
		return true;
	}

	private void updateViews() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showHTML();
			}
		});
	}


	private void showHTML() {
		this.webview.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String article_title=url;
				if (url.startsWith("file:///")) {
					article_title=url.substring(8);
				}
				try {
					displayNewArticle(URLDecoder.decode(article_title, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return true;
			}
		});

		String data ="<html><head>\n";
		if (this.article != null) {
			setTitle(capitalize(this.article.title));
			data+="<meta name=\"viewport\" content=\"width=device-width,  user-scalable=yes\">\n";
			data+="<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
			data+="</head>";
			if (config.getBoolean(getString(R.string.config_key_use_mathjax),true)) {
				data+="<script type=\"text/javascript\" src=\""+getString(R.string.link_to_mathjax)+"\"></script>\n";
			}
			data+="<body>";
			data +="<h1>"+capitalize(this.article.title)+"</h1>";
			data += this.article.text;

		} else {
			data +=getString(R.string.html_message_no_article,wanted_title);
		}
		data+="</body></html>";
		this.webview.loadDataWithBaseURL("file:///android-assets", data, "text/html","UTF-8",null);
		this.webview.setVisibility(View.VISIBLE);
	}

	private String capitalize(String text){
		String res="";
		if (text.length() > 0) {
			res = String.valueOf(text.charAt(0)).toUpperCase() + text.subSequence(1, text.length());
		} else {
			res = text.toUpperCase();
		}
		return res;
	}


}
