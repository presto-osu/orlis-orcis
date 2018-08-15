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
package net.sf.times.location;

import android.content.Context;

import java.util.List;

/**
 * Location adapter for locations the user has "previously visited".
 *
 * @author Moshe Waisberg
 */
public class HistoryLocationAdapter extends SpecificLocationAdapter {

    public HistoryLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
    }

    @Override
    protected boolean isSpecific(ZmanimAddress address) {
        return address.getId() > 0L;
    }

}
