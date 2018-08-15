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

import android.content.Context;
import android.util.AttributeSet;

import org.pixmob.freemobile.netstat.R;


/**
 * Custom component showing mobile network use with a pie chart.
 * 
 * @author Pixmob
 */
public class MobileNetworkChart extends PieChartView {

    public MobileNetworkChart(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setData(75, 10, 10, 80);
    }

    private void setData(int percentOnOrange, int percentOnFreeMobile, int percentOnOrange2G, int percentOnFreeMobile3G) {
    	clear();
    	PieChartComponent orange =
    		new PieChartComponent(R.color.orange_network_color1, R.color.orange_network_color2, percentOnOrange);
    	PieChartComponent free =
        		new PieChartComponent(R.color.free_mobile_network_color1, R.color.free_mobile_network_color2, percentOnFreeMobile);

    	new PieChartComponent(R.color.orange_2G_network_color1, R.color.orange_2G_network_color2, percentOnOrange2G, orange);
    	new PieChartComponent(R.color.orange_3G_network_color1, R.color.orange_3G_network_color2, 100 - percentOnOrange2G, orange);
    	new PieChartComponent(R.color.free_mobile_3G_network_color1, R.color.free_mobile_3G_network_color2,
        				percentOnFreeMobile3G, free);
    	new PieChartComponent(R.color.free_mobile_4G_network_color1, R.color.free_mobile_4G_network_color2,
        				100 - percentOnFreeMobile3G, free);

    	addPieChartComponent(free);
    	addPieChartComponent(orange);
    }

}
