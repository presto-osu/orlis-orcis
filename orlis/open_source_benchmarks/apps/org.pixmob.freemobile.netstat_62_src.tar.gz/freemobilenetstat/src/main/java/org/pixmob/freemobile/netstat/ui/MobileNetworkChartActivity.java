/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Application chart activity (enlarged Chart).
 * @author gilbsgilbs
 */
public class MobileNetworkChartActivity extends FragmentActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
	    
	    if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null)
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new MobileNetworkChartFragment()).commit();
	}

    @Override
     public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Enable "better" gradients:
        // http://stackoverflow.com/a/2932030/422906
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.getDecorView().getBackground().setDither(true);
    }
}
