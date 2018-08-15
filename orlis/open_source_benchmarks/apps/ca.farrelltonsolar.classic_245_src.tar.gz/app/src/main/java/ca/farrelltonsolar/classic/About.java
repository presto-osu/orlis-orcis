/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by Graham on 28/12/13.
 */
public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.webview, container, false);
        WebView engine = (WebView) theView.findViewById(R.id.webView);
        String locale = getResources().getConfiguration().locale.getLanguage();
        String aboutFile = "file:///android_asset/about.html";
        if (locale.compareTo("fr") == 0) {
            aboutFile = "file:///android_asset/about-fr.html";
        }
        else if (locale.compareTo("es") == 0) {
            aboutFile = "file:///android_asset/about-es.html";
        }
        else if (locale.compareTo("it") == 0) {
            aboutFile = "file:///android_asset/about-it.html";
        }
        else if (locale.compareTo("de") == 0) {
            aboutFile = "file:///android_asset/about-de.html";
        }
        engine.loadUrl(aboutFile);
        return theView;
    }
}