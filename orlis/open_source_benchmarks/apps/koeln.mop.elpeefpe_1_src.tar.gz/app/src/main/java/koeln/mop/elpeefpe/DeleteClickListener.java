package koeln.mop.elpeefpe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;

/**
 * Created by Andreas Streichardt on 03.07.2016.
 */
public class DeleteClickListener implements Dialog.OnClickListener {
    private Activity activity;
    private int id;

    public DeleteClickListener(Activity activity, int id) {
        this.activity = activity;
        this.id = id;
    }

    @Override
    public void onClick(DialogInterface dialog, int i) {
        // continue with delete
        DBHandler db = new DBHandler(activity);
        db.delete(id);

        dialog.cancel();
        NavUtils.navigateUpTo(activity, new Intent(activity, CharacterListActivity.class));
    }
}
