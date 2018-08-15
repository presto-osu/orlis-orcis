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
package net.sf.times.database;

import android.database.Cursor;

/**
 * An interface for filtering {@link Cursor} objects based on their columns or
 * other information.
 *
 * @author Moshe Waisberg
 */
public interface CursorFilter {

    /**
     * Indicating whether a specific database cursor should be included in a
     * query list.
     *
     * @param cursor
     *         the cursor to check.
     * @return {@code true} if the current cursor should be included,
     * {@code false} otherwise.
     */
    public abstract boolean accept(Cursor cursor);
}
