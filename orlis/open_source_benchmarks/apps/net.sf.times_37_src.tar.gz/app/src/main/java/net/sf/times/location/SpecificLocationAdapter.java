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

import java.util.ArrayList;
import java.util.List;

/**
 * Location adapter for specific type of locations.
 *
 * @author Moshe Waisberg
 */
public abstract class SpecificLocationAdapter extends LocationAdapter {

    private final List<LocationItem> specific = new ArrayList<LocationItem>();

    public SpecificLocationAdapter(Context context, List<LocationItem> items) {
        super(context, items);
        populateSpecific();
    }

    private void populateSpecific() {
        specific.clear();

        ZmanimAddress address;
        for (LocationItem item : objects) {
            address = item.getAddress();
            if (isSpecific(address))
                specific.add(item);
        }
    }

    /**
     * Is the address specific to this adapter?
     *
     * @param address
     *         the address.
     * @return {@code true} to include the address.
     */
    protected abstract boolean isSpecific(ZmanimAddress address);

    @Override
    public int getCount() {
        return specific.size();
    }

    @Override
    protected LocationItem getLocationItem(int position) {
        return specific.get(position);
    }

    @Override
    public int getPosition(LocationItem object) {
        final int size = specific.size();
        LocationItem item;
        for (int i = 0; i < size; i++) {
            item = specific.get(i);
            if (item.equals(object))
                return i;
        }
        return super.getPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        populateSpecific();
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        populateSpecific();
        super.notifyDataSetInvalidated();
    }

}
