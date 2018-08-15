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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.renzo.wikipoff.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.webkit.WebView;


public class AboutActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "AboutActivity";
	private WebView webview;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.title_about));
		setContentView(R.layout.activity_about);

		this.webview= (WebView) findViewById(R.id.aboutwebview);

		try {
			showHTML();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void showHTML() throws IOException {
		AssetManager am = getAssets();
		InputStream in = am.open("About.html");
		StringBuilder data=new StringBuilder();
	   
	    BufferedReader buf= new BufferedReader(new InputStreamReader(in));
	    String str;
	    while ((str=buf.readLine()) != null) {
	      data.append(str);
	    }

	    in.close();
		this.webview.loadDataWithBaseURL("file:///android-assets", data.toString(), "text/html","UTF-8",null);
	}

}
