package org.itishka.pointim.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.itishka.pointim.BuildConfig;
import org.itishka.pointim.R;
import org.itishka.pointim.model.imgur.Token;
import org.itishka.pointim.model.imgur.UploadResult;
import org.itishka.pointim.network.ImgurConnectionManager;
import org.itishka.pointim.utils.ImgurUploadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jp.wasabeef.glide.transformations.CropTransformation;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tishka17 on 31.12.2014.
 */
public class ImageUploadingPanel extends FrameLayout {

    private ViewGroup mLayout;
    private final ArrayList<Image> mImages = new ArrayList<>(1);

    private enum AuthProlongationState {
        NotStarted,
        Started,
        Finished,
        TemporarilyError,
        AuthError,
        NotAuthorized
    }

    private AuthProlongationState mAuthProlonged = AuthProlongationState.NotStarted;
    private AuthProlongationTask mAuthProlongationTask = null;
    private final Object mAuthProlongationLock = new Object();
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    private final Callback<Void> deleteCallback = new Callback<Void>() {
        @Override
        public void success(Void aVoid, Response response) {
            //do nothng
        }

        @Override
        public void failure(RetrofitError error) {
            //do nothng
        }
    };

    public ImageUploadingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageUploadingPanel(Context context) {
        super(context);
        init();
    }

    public ImageUploadingPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.image_uploading_panel, this);
        mLayout = (ViewGroup) findViewById(R.id.contentView);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
    }


    public void addUploadedImage(String url) {
        final Image img = new Image();
        final View newView = inflate(getContext(), R.layout.image_uploading_panel_item, null);
        img.imageView = (ImageView) newView.findViewById(R.id.imageView);
        img.viewFinished = (ImageView) newView.findViewById(R.id.viewFinished);
        img.viewFinished.setVisibility(VISIBLE);
        img.viewError = (ImageView) newView.findViewById(R.id.viewError);
        img.viewError.setVisibility(GONE);
        img.cancel = (ImageButton) newView.findViewById(R.id.action_cancel);
        img.uploaded = true;
        img.progress = (ProgressWheel) newView.findViewById(R.id.progress_wheel);
        img.progress.setVisibility(GONE);
        img.uploadInfo = new org.itishka.pointim.model.imgur.Image();
        img.uploadInfo.link = url;
        img.cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUploadingPanel.this.mLayout.removeView(newView);
                mImages.remove(img);
            }
        });
        Glide.with(getContext())
                .load(url)
                .bitmapTransform(new CropTransformation(getContext()))
                .centerCrop()
                .into(img.imageView);
        mLayout.addView(newView);
        mImages.add(img);

    }

    public void addImage(Uri uri, String mime) {
        final Image img = new Image();
        final View newView = inflate(getContext(), R.layout.image_uploading_panel_item, null);
        img.mime = mime;
        img.imageView = (ImageView) newView.findViewById(R.id.imageView);
        img.viewFinished = (ImageView) newView.findViewById(R.id.viewFinished);
        img.viewFinished.setVisibility(GONE);
        img.viewError = (ImageView) newView.findViewById(R.id.viewError);
        img.viewError.setVisibility(GONE);
        img.progress = (ProgressWheel) newView.findViewById(R.id.progress_wheel);
        img.cancel = (ImageButton) newView.findViewById(R.id.action_cancel);
        img.cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (img.task != null && !img.task.isCancelled()) {
                    img.task.cancel(true);
                }
                ImageUploadingPanel.this.mLayout.removeView(newView);
                mImages.remove(img);
                if (img.uploaded && !TextUtils.isEmpty(img.uploadInfo.deletehash)) {
                    ImgurConnectionManager.getInstance().imgurService.deleteImage(img.uploadInfo.deletehash, deleteCallback);
                }
            }
        });
        img.imageView.setColorFilter(Color.argb(220, 255, 255, 255), PorterDuff.Mode.LIGHTEN);
        Glide.with(getContext())
                .load(uri)
                .bitmapTransform(new CropTransformation(getContext()))
                .centerCrop()
                .into(img.imageView);
        mLayout.addView(newView);
        img.originalPath = uri;
        mImages.add(img);

        if (mImages.size() == 1)
            startAuthProlongation();
        img.task = new ImgUploadTask(img, getContext());
        img.task.executeOnExecutor(mExecutor);
        newView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!img.uploaded && img.task != null && img.task.getStatus() == AsyncTask.Status.FINISHED) {
                    startAuthProlongation();
                    img.task = new ImgUploadTask(img, getContext());
                    img.task.executeOnExecutor(mExecutor);
                }
            }
        });
    }

    private void startAuthProlongation() {
        if (mAuthProlonged != AuthProlongationState.Finished
                && mAuthProlonged != AuthProlongationState.NotAuthorized
                && mAuthProlonged != AuthProlongationState.AuthError) {
            mAuthProlongationTask = new AuthProlongationTask();
            mAuthProlongationTask.executeOnExecutor(mExecutor);
        }
    }

    public List<String> getLinks() {
        List<String> links = new ArrayList<>(mImages.size());
        for (Image i : mImages) {
            links.add(i.uploadInfo.link);
        }
        return links;
    }

    public void cancel() {
        for (Image i : mImages) {
            if (i.task != null && !i.task.isCancelled()) {
                i.task.cancel(true);
            } else if (i.uploaded && !TextUtils.isEmpty(i.uploadInfo.deletehash)) {
                //FIXME delete uploaded images on view cancel
                //PointConnectionManager.getInstance().imgurService.deleteImage(i.uploadInfo.deletehash, deleteCallback);
            }
        }
    }

    public boolean isUploadFinished() {
        for (Image i : mImages) {
            if (!i.uploaded) return false;
        }
        return true;
    }

    public void reset() {
        cancel();
        mImages.clear();
        mLayout.removeAllViews();
    }

    private final class ImgUploadTask extends ImgurUploadTask {
        final WeakReference<Image> img;

        ImgUploadTask(Image img, Context context) {
            super(context, img.originalPath, img.mime);
            this.img = new WeakReference<>(img);

        }

        @Override
        protected UploadResult doInBackground(String... params) {
            synchronized (mAuthProlongationLock) {
                while (mAuthProlonged == AuthProlongationState.Started) {
                    try {
                        mAuthProlongationLock.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
            if (mAuthProlonged != AuthProlongationState.Finished && mAuthProlonged != AuthProlongationState.NotAuthorized) {
                return null;
            }
            return super.doInBackground(params);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img.get().progress.setProgress(0);
            img.get().progress.setVisibility(VISIBLE);
            img.get().viewError.setVisibility(GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            img.get().progress.setProgress(values[0] * 0.9f);
            //progress 0..100 -> fkmaf 255..55
            img.get().imageView.setColorFilter(Color.argb(220 - values[0] * 2, 255, 255, 255));
        }

        @Override
        protected void onPostExecute(UploadResult result) {
            super.onPostExecute(result);
            if (result != null && result.success) {
                img.get().viewFinished.setVisibility(VISIBLE);
                img.get().progress.setVisibility(GONE);
                img.get().uploaded = true;
                img.get().imageView.setColorFilter(null);
                img.get().uploadInfo = result.data;
            } else {
                img.get().viewError.setVisibility(VISIBLE);
                img.get().progress.setVisibility(GONE);
                img.get().imageView.setColorFilter(Color.argb(128, 200, 100, 100));
                Toast.makeText(getContext(), String.valueOf(getError()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Image {
        String mime;
        Uri originalPath;
        ImageView imageView;
        ImageButton cancel;
        ImageView viewFinished;
        ImageView viewError;
        ProgressWheel progress;

        boolean uploaded = false;
        ImgurUploadTask task = null;
        org.itishka.pointim.model.imgur.Image uploadInfo;
    }

    private class AuthProlongationTask extends AsyncTask<Void, Void, Token> {
        @Override
        protected Token doInBackground(Void... voids) {
            mAuthProlonged = AuthProlongationState.Started;
            Token token = ImgurConnectionManager.getInstance().token;
            if (token == null || token.refresh_token == null) {
                mAuthProlonged = AuthProlongationState.NotAuthorized;
                return null;
            }
            try {
                return ImgurConnectionManager.getInstance().imgurAuthService.refreshToken(
                        BuildConfig.IMGUR_ID,
                        BuildConfig.IMGUR_SECRET,
                        "refresh_token",
                        token.refresh_token);
            } catch (RetrofitError e) {
                if (e.getKind() == RetrofitError.Kind.HTTP && e.getResponse().getStatus() == 403)
                    mAuthProlonged = AuthProlongationState.AuthError;
                mAuthProlonged = AuthProlongationState.TemporarilyError;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Token newToken) {
            super.onPostExecute(newToken);
            if (newToken != null && newToken.error == null && newToken.access_token != null) {
                ImgurConnectionManager.getInstance().updateAuthorization(getContext(), newToken);
                mAuthProlonged = AuthProlongationState.Finished;
            }
            synchronized (mAuthProlongationLock) {
                mAuthProlongationLock.notifyAll();
            }
        }
    }

}
