package com.github.dfa.diaspora_android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.github.dfa.diaspora_android.task.ImageDownloadTask;

import java.io.File;

/**
 * Created by Gregor Santner (gsantner) on 24.03.16.
 */
public class AvatarImageLoader {
    private File avatarFile;

    public AvatarImageLoader(Context context) {
        avatarFile = new File(context.getFilesDir(), "avatar0.png");
    }

    public boolean clearAvatarImage() {
        return (!isAvatarDownloaded() || avatarFile.delete());
    }

    public boolean loadToImageView(ImageView imageView) {
        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    public boolean isAvatarDownloaded() {
        return avatarFile.exists();
    }

    public void startImageDownload(ImageView imageView, String avatarUrl) {
        if (!avatarUrl.equals("")) {
            new ImageDownloadTask(imageView, avatarFile.getAbsolutePath()).execute(avatarUrl);
        }
    }
}
