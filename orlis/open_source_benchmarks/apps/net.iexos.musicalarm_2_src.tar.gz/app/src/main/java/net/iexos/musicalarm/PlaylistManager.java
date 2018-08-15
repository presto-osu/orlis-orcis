package net.iexos.musicalarm;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

public final class PlaylistManager {
    private static final String LOGGING_TAG = "PlaylistManager";
    public static final String ID_KEY = MediaStore.Audio.Playlists._ID;
    public static final String NAME_KEY = MediaStore.Audio.Playlists.NAME;
    public static final String DATA_KEY = MediaStore.Audio.Media.DATA;
    private ContentResolver mResolver;
    public long mID;
    public String mName;

    public PlaylistManager(Context con, long id, String name) {
        mResolver = con.getContentResolver();
        if (id != 0) {
            final Uri playListUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
            if (playListUri != null) {
                mName = name;
            }
            else {
                id = 0;
                mName = con.getString(R.string.choose_playlist);
            }
        }
        else {
            mName = con.getString(R.string.choose_playlist);
        }
        mID = id;
    }

    public List<Uri> getSongs() {
        final Uri playListUri = MediaStore.Audio.Playlists.Members.getContentUri("external", mID);
        String[] proj = {DATA_KEY};
        final Cursor cursor = mResolver.query(playListUri, proj, null, null, null);
        List<Uri> songUris = new ArrayList<>();
        if (cursor != null) {
            for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
                songUris.add(Uri.parse("file:///" + cursor.getString(cursor.getColumnIndex(DATA_KEY))));
            }
            cursor.close();
        }
        for (Uri uri : songUris) {
            Log.v(LOGGING_TAG, "Songs in playlist:");
            Log.v(LOGGING_TAG, uri.toString());
        }
        return songUris;
    }

    public ListAdapter getPlaylistAdapter(Context con) {
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String[] columns = { ID_KEY, NAME_KEY };
        final String[] from = { NAME_KEY };
        final int[] to = {android.R.id.text1};
        final Cursor cursor = mResolver.query(uri, columns, null, null, null);
        if (cursor == null) {
            Log.e(LOGGING_TAG, "No playlist found.");
            return null;
        }
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                con, android.R.layout.simple_list_item_1, cursor, from, to, 0);
        return cursorAdapter;
    }
}
