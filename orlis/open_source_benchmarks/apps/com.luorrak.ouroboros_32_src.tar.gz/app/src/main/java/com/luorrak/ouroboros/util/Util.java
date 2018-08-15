package com.luorrak.ouroboros.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.graphics.Palette;
import android.view.View;

import com.koushikdutta.ion.ImageViewBitmapInfo;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.services.AlarmReceiver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
 */
public class Util {
    public static final int REQUEST_STORAGE_PERMISSION = 55;
    public final static String INTENT_THREAD_NO = "com.luorrak.ouroboros.THREADNO";
    public final static String INTENT_BOARD_NAME = "com.luorrak.ouroboros.BOARDNAME";
    public final static String INTENT_THREAD_POSITION = "com.luorrak.ouroboros.THREADPOSITION";
    public final static String INTENT_REPLY_NO = "com.luorrak.ouroboros.REPLYNO";
    public final static String INTENT_REPLY_CHECKER = "com.luorrak.ouroboros.REPLYNO";
    public final static String TIM = "com.luorrak.ouroboros.TIM";
    public final static String EXT = "com.luorrak.ouroboros.EXT";

    private static final int THEME_DEFAULT = 0;
    private static final int THEME_DARK = 1;
    private static final int THEME_BLUE = 2;
    private static final int THEME_GREY = 3;



    public static final int THREAD_LAYOUT_VERTICAL = 0;
    public static final int THREAD_LAYOUT_HORIZONTAL = 1;

    public static final int CATALOG_LAYOUT_LIST = 0;
    public static final int CATALOG_LAYOUT_GRID= 1;

    public static final int REPLY_CHECKER_INTENT_ID = 5738295;

    public static String[] parseYoutube(String embed) {
        String[] youtubeData = new String[2];

        Document doc = Jsoup.parse(embed);
        if (doc.select("a.file").size() > 0){
            youtubeData[0] = doc.select("a.file").first().attr("href");
            youtubeData[1] = doc.select("img.post-image").first().attr("src").substring(2);
        }
        return youtubeData;
    }

    /*Set the theme */
    public static void onActivityCreateSetTheme(Context context, int themeValue)
    {
        switch (themeValue)
        {
            default:
            case THEME_DEFAULT:
                context.setTheme(R.style.AppTheme);
                break;
            case THEME_DARK:
                context.setTheme(R.style.AppThemeDark);
                break;
            case THEME_BLUE:
                context.setTheme(R.style.AppTheme_Blue);
                break;
            case THEME_GREY:
                context.setTheme(R.style.AppTheme_Grey);
                break;
        }
    }

    public static Media createMediaItem(String height, String width, String tim, String ext){
        Media mediaItem = new Media();
        mediaItem.height = height;
        mediaItem.width = width;
        mediaItem.fileName = tim;
        mediaItem.ext = ext;
        return mediaItem;
    }

    public static byte[] serializeObject (Object object) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ObjectOutput objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(object);
            objectOutput.close();

            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static Object deserializeObject(byte[] bytes) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = inputStream.readObject();
            inputStream.close();

            return object;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void copyToClipboard(Context context, String text){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("ComText", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static void setSwatch(final View view, ImageViewBitmapInfo result){
        if (result.getBitmapInfo() != null){
            Palette.from(result.getBitmapInfo().bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Palette.Swatch swatch = palette.getMutedSwatch();
                    if (swatch != null) {
                        view.setBackgroundColor(swatch.getRgb());
                    }
                }
            });
        }
    }

    public static void startReplyCheckerService(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("startReplyCheckerService", true);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, REPLY_CHECKER_INTENT_ID, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 15*60*1000, 15*60*1000, alarmIntent);
    }

    public static void stopReplyCheckerService(Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent senderstop = PendingIntent.getBroadcast(context,
                REPLY_CHECKER_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManagerstop = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManagerstop.cancel(senderstop);
    }
}
