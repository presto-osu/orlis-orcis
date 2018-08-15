/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class Notifier
{
    public static final int NEW_MESSAGE = 1;

    public static Notification build(int icon, CharSequence tickerText)
    {
        Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    public static void notify(Context context, int id, Notification notification)
    {
        getManager(context).notify(id, notification);
    }

    public static void notify(Context context, String tag, int id, Notification notification)
    {
        getManager(context).notify(tag, id, notification);
    }

    public static void cancel(Context context, int id)
    {
        getManager(context).cancel(id);
    }

    public static void cancel(Context context, String tag, int id)
    {
        getManager(context).cancel(tag, id);
    }

    private static NotificationManager getManager(Context context)
    {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
