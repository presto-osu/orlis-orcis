package fr.renzo.wikipoff;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchCursorAdapterN extends AbstractSearchCursorAdapter {

    @SuppressWarnings("deprecation")
    public SearchCursorAdapterN(Context context, Cursor c, Database dbh) {
        super(context, c, dbh);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.article_result, parent, false);
    }

    @Override
    public void bindView(View view, Context ctx, Cursor cursor) {
        TextView topTextView = (TextView) view.findViewById(android.R.id.text1);
        TextView bottomTextView = (TextView) view.findViewById(android.R.id.text2);
        topTextView.setText(cursor.getString(1));
        bottomTextView.setText(cursor.getString(2));
    }
}
