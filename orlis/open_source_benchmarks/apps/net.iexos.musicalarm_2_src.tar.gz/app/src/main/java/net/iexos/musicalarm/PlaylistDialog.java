package net.iexos.musicalarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;


public final class PlaylistDialog extends DialogFragment {
    public PlaylistDialog() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlarmViewActivity activity = (AlarmViewActivity) getActivity();
        final PlaylistManager playlistManager = activity.mPlaylistManager;
        final ListAdapter adapter = playlistManager.getPlaylistAdapter(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (adapter.isEmpty()) {
            return builder.setTitle(R.string.no_playlist_dialog_title)
                          .setMessage(R.string.no_playlist_dialog_message)
                          .setPositiveButton(R.string.ok, null)
                          .create();
        }
        return builder.setTitle(R.string.choose_playlist)
                      .setAdapter(adapter, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              Cursor cursor = (Cursor) adapter.getItem(which);
                              AlarmViewActivity ava = (AlarmViewActivity) getActivity();
                              playlistManager.mID = adapter.getItemId(which);
                              playlistManager.mName = cursor.getString(
                                      cursor.getColumnIndex(PlaylistManager.NAME_KEY));
                              ava.onPlaylistChosen();
                              cursor.close();
                          }
                      }).create();
    }
}
