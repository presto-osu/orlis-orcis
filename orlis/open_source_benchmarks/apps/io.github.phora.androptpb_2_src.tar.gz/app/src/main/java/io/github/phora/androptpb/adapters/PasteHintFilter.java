package io.github.phora.androptpb.adapters;

import android.database.Cursor;
import android.widget.FilterQueryProvider;

import io.github.phora.androptpb.DBHelper;

/**
 * Created by phora on 9/15/15.
 */
public class PasteHintFilter implements FilterQueryProvider {

    private long serverId;
    private PasteHintsCursorAdapter filteree;

    public PasteHintFilter(long serverId, PasteHintsCursorAdapter filteree) {
        this.serverId = serverId;
        this.filteree = filteree;
    }

    @Override
    public Cursor runQuery(CharSequence charSequence) {
        String filter = charSequence.toString();

        filteree.setFilterString(filter);
        DBHelper sqlhelper = DBHelper.getInstance(filteree.getContext());

        if (charSequence == null) {
            return sqlhelper.getHintGroups(serverId);
        }
        else {
            return sqlhelper.getHintGroups(serverId, filter);
        }
    }
}
