package com.jparkie.aizoban.views.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.downloads.DownloadService;
import com.jparkie.aizoban.utils.PreferenceUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {
    public static final String TAG = SplashActivity.class.getSimpleName();

    private Subscription mRestartSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mRestartSubscription = Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        try {
                            boolean isServiceRunning = false;

                            ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                                if (serviceInfo.service.getClassName().equals(DownloadService.class.getName())) {
                                    isServiceRunning = true;
                                    break;
                                }
                            }

                            subscriber.onNext(isServiceRunning);
                            subscriber.onCompleted();
                        } catch (Throwable e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .delay(1, TimeUnit.SECONDS, Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Intent startActivity = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity.putExtra(MainActivity.POSITION_ARGUMENT_KEY, PreferenceUtils.getStartupScreen());
                        startActivity(startActivity);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNext(Boolean isServiceRunning) {
                        if (!isServiceRunning) {
                            Intent startService = new Intent(SplashActivity.this, DownloadService.class);
                            startService.putExtra(DownloadService.INTENT_RESTART_DOWNLOAD, DownloadService.INTENT_RESTART_DOWNLOAD);
                            startService(startService);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRestartSubscription != null) {
            mRestartSubscription.unsubscribe();
            mRestartSubscription = null;
        }
    }
}
