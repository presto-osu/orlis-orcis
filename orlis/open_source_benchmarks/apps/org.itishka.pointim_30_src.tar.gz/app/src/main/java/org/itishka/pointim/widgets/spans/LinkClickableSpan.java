package org.itishka.pointim.widgets.spans;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Tishka17 on 26.04.2016.
 */
public class LinkClickableSpan extends ClickableSpan {
    private final String mUri;

    public LinkClickableSpan(String link) {
        mUri = link;
    }

    @Override
    public void onClick(View view) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));
            ActivityCompat.startActivity((Activity) view.getContext(), browserIntent, null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(view.getContext(), "Cannot open link: " + mUri, Toast.LENGTH_SHORT).show();
        }
    }
}
