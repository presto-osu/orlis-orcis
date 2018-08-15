package org.itishka.pointim.widgets.spans;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.text.style.ClickableSpan;
import android.view.View;

import org.itishka.pointim.activities.UserViewActivity;

/**
 * Created by Tishka17 on 17.01.2015.
 */
public class UserClickableSpan extends ClickableSpan {
    private final String mId;

    public UserClickableSpan(String id) {
        mId = id;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), UserViewActivity.class);
        intent.putExtra("user", mId);
        ActivityCompat.startActivity((Activity) view.getContext(), intent, null);
    }
}
