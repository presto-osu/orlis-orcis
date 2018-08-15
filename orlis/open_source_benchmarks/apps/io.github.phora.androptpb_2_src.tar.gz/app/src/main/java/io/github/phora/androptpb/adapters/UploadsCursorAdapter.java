package io.github.phora.androptpb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.R;

/**
 * Created by phora on 8/19/15.
 */
public class UploadsCursorAdapter extends ResourceCursorAdapter {

    private final View.OnClickListener mEditBtnListener;
    private Context mContext;

    private static int COL_COMPLETE_URL = -1;
    private static int COL_VANITY_URL = -1;
    private static int COL_EXPIRY = -1;
    private static int COL_UUID = -1;
    private static int COL_PRIVATE = -1;
    private static int COL_FORMAT = -1;
    private static int COL_STYLE = -1;

    public UploadsCursorAdapter(Context context, Cursor c, boolean autoRequery,
                                View.OnClickListener editBtnListener) {
        super(context, R.layout.upload_item, c, autoRequery);
        mContext = context;
        mEditBtnListener = editBtnListener;
    }

    public UploadsCursorAdapter(Context context, Cursor c, int flags,
                                View.OnClickListener editBtnListener) {
        super(context, R.layout.upload_item, c, flags);
        mContext = context;
        mEditBtnListener = editBtnListener;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView completeUrl = (TextView)view.findViewById(R.id.UploadItem_CompleteUrl);
        TextView expiresDate = (TextView)view.findViewById(R.id.UploadItem_Expires);
        ImageButton editButton = (ImageButton)view.findViewById(R.id.UploadItem_EditButton);
        View privateStripe = view.findViewById(R.id.UploadItem_IsPrivate);
        
        TextView prefFormat = (TextView) view.findViewById(R.id.UploadItem_PrefFormat);
        TextView prefStyle = (TextView) view.findViewById(R.id.UploadItem_PrefStyle);

        if (COL_COMPLETE_URL == -1) {
            COL_COMPLETE_URL = cursor.getColumnIndex("complete_url");
        }
        if (COL_VANITY_URL == -1) {
            COL_VANITY_URL = cursor.getColumnIndex("hvanity_url");
        }
        if (COL_EXPIRY == -1) {
            COL_EXPIRY = cursor.getColumnIndex("dt");
        }
        if (COL_UUID == -1) {
            COL_UUID = cursor.getColumnIndex(DBHelper.UPLOAD_UUID);
        }
        if (COL_PRIVATE == -1) {
            COL_PRIVATE = cursor.getColumnIndex(DBHelper.UPLOAD_PRIVATE);
        }
        if (COL_FORMAT == -1) {
            COL_FORMAT = cursor.getColumnIndex(DBHelper.UPLOAD_FORMAT);
        }
        if (COL_STYLE == -1) {
            COL_STYLE = cursor.getColumnIndex(DBHelper.UPLOAD_STYLE);
        }
        
        boolean is_private = cursor.getInt(COL_PRIVATE) == 1;
        if (is_private) {
            privateStripe.setVisibility(View.VISIBLE);
        }
        else {
            privateStripe.setVisibility(View.INVISIBLE);
        }

        String uuid = cursor.getString(COL_UUID);
        if (!TextUtils.isEmpty(uuid)) {
            editButton.setVisibility(View.VISIBLE);
        }
        else {
            editButton.setVisibility(View.INVISIBLE);
        }
        editButton.setTag(cursor.getPosition());
        editButton.setOnClickListener(mEditBtnListener);

        String vanity_url = cursor.getString(COL_VANITY_URL);
        String complete_url = cursor.getString(COL_COMPLETE_URL);
        String expiry = cursor.getString(COL_EXPIRY);
        String format = cursor.getString(COL_FORMAT);
        String style = cursor.getString(COL_STYLE);

        if (!TextUtils.isEmpty(vanity_url)) {
            completeUrl.setText(vanity_url);
        }
        else {
            completeUrl.setText(complete_url);
        }

        expiresDate.setText(expiry);
        prefFormat.setText(format);
        prefStyle.setText(style);
    }
}
