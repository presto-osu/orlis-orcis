package fr.renzo.wikipoff;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public  class SearchCursorAdapter1 extends AbstractSearchCursorAdapter {

    public SearchCursorAdapter1(Context context, Cursor c, Database dbh) {
        super(context, c, dbh);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Context ctx, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(cursor.getString(1));
    }

}
