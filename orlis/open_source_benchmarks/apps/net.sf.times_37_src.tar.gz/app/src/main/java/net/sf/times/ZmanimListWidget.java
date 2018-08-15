/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Shows a scrollable list of halachic times (<em>zmanim</em>) for prayers in a
 * widget.
 *
 * @author Moshe Waisberg
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanimListWidget extends ZmanimWidget {

    /**
     * Constructs a new widget.
     */
    public ZmanimListWidget() {
    }

    @Override
    protected boolean isRemoteList() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

}
