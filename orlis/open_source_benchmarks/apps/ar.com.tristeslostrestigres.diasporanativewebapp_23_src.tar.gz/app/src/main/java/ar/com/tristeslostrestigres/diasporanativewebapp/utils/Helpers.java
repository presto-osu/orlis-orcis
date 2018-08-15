/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.tristeslostrestigres.diasporanativewebapp.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebView;

public class Helpers {

    public static boolean isOnline(Context context){
        ConnectivityManager cnm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cnm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static void hideTopBar(WebView wv) {
        wv.loadUrl("javascript: ( function() {" +
                "    if(document.getElementById('main_nav')) {" +
                "        document.getElementById('main_nav').parentNode.removeChild(" +
                "        document.getElementById('main_nav'));" +
                "    } else if (document.getElementById('main-nav')) {" +
                "        document.getElementById('main-nav').parentNode.removeChild(" +
                "        document.getElementById('main-nav'));" +
                "    }" +
                "})();");
    }

    public static void getNotificationCount(WebView wv) {
        wv.loadUrl("javascript: ( function() {" +
                "    if (document.getElementById('notification')) {" +
                "       var count = document.getElementById('notification').innerHTML;" +
                "       NotificationCounter.setNotificationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
                "    } else {" +
                "       NotificationCounter.setNotificationCount('0');" +
                "    }" +
                "    if (document.getElementById('conversation')) {" +
                "       var count = document.getElementById('conversation').innerHTML;" +
                "       NotificationCounter.setConversationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
                "    } else {" +
                "       NotificationCounter.setConversationCount('0');" +
                "    }" +
                "})();");
    }
}
