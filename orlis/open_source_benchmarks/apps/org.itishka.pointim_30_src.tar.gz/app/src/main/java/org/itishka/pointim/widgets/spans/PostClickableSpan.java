package org.itishka.pointim.widgets.spans;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.text.style.ClickableSpan;
import android.view.View;

import org.itishka.pointim.activities.SinglePostActivity;

/**
 * Created by Tishka17 on 17.01.2015.
 */
public class PostClickableSpan extends ClickableSpan {
    private final String mId;
    private final String mSubId;

    public PostClickableSpan(String id, String subId) {
        mId = id;
        mSubId = subId;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), SinglePostActivity.class);
        intent.putExtra("post", mId);
        intent.putExtra("comment", mSubId);
        ActivityCompat.startActivity((Activity) view.getContext(), intent, null);
    }
}
