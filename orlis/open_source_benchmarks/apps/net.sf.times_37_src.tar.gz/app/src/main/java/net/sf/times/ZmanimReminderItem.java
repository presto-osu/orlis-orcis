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

/**
 * Reminder item for a notification.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderItem {

    private final CharSequence title;
    private final CharSequence text;
    private final long time;

    public ZmanimReminderItem(CharSequence title, CharSequence text, long time) {
        this.title = title;
        this.text = text;
        this.time = time;
    }

    /**
     * Get the notification title.
     *
     * @return the title.
     */
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Get the notification text.
     *
     * @return the summary.
     */
    public CharSequence getText() {
        return text;
    }

    /**
     * Get the notification time when the zman is supposed to occur.
     *
     * @return the time.
     */
    public long getTime() {
        return time;
    }
}
