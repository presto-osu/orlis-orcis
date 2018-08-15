package org.itishka.pointim.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.utils.ImageSearchHelper;

/**
 * Created by Tishka17 on 29.04.2015.
 */
public abstract class ConnectedActivity extends ThemedActivity {
    @Override
    protected void onStop() {
        super.onStop();
        ImageSearchHelper.saveCache(this);
    }

    private static final int REQUEST_LOGIN = 0x6660;

    @Override
    protected void onStart() {
        super.onStart();
        if (!PointConnectionManager.getInstance().isAuthorized())
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!PointConnectionManager.getInstance().isAuthorized()) {
            finish();
        }
    }
}
