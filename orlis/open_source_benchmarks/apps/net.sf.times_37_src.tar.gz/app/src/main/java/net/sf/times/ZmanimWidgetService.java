package net.sf.times;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * Service that provides the list of halachic times (<em>zmanim</em>) items for
 * the scrollable widget.
 *
 * @author Moshe
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimWidgetService extends RemoteViewsService {

    private ZmanimWidgetViewsFactory factory;

    public ZmanimWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if (factory == null) {
            factory = new ZmanimWidgetViewsFactory(this, intent);
        }
        return factory;
    }

}
