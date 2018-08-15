package org.itishka.pointim.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import org.itishka.pointim.model.imgur.UploadResult;
import org.itishka.pointim.network.ImgurConnectionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit.RetrofitError;

/**
 * Created by Tishka17 on 30.12.2014.
 */
public abstract class ImgurUploadTask extends AsyncTask<String, Integer, UploadResult> {
    private final Uri mUri;
    private File mFile;
    private Context mContext;
    private String mMime;
    private String mError = null;


    public ImgurUploadTask(Context context, Uri uri, String mime) {
        mUri = uri;
        mMime = mime;
        try {
            mFile = File.createTempFile("upload_", "", context.getCacheDir());
        } catch (IOException e) {
            mFile = null;
            e.printStackTrace();
        }
        mContext = context;
    }


    @Override
    protected UploadResult doInBackground(String... params) {
        String[] filePathColumn = {MediaStore.Images.Media.MIME_TYPE};
        String imageMime = mMime;
        if (imageMime == null) {
            Cursor cursor = mContext.getContentResolver().query(mUri, filePathColumn, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                imageMime = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            }
            if (cursor != null) cursor.close();
        }
        if (imageMime == null) {
            imageMime = "image/other";
        }

        //сохраняем локально
        InputStream in = null;
        try {
            in = mContext.getContentResolver().openInputStream(mUri);
            mFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(mFile);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mError = e.toString();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        publishProgress(40);

        mContext = null;
        final long totalSize = mFile.length();
        try {
            UploadResult res = ImgurConnectionManager.getInstance().imgurService.uploadFile(
                    new CountingTypedFile(imageMime, mFile, new CountingTypedFile.ProgressListener() {
                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 50));
                        }
                    })
            );
            publishProgress(100);
            return res;
        } catch (RetrofitError e) {
            e.printStackTrace();
            mError = e.toString();
            return null;
        } finally {
            mFile.delete();
        }
    }

    protected String getError() {
        return mError;
    }
}