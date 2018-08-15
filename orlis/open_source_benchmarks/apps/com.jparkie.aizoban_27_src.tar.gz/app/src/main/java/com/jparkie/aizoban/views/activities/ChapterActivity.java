package com.jparkie.aizoban.views.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.ChapterPresenter;
import com.jparkie.aizoban.presenters.ChapterPresenterOfflineImpl;
import com.jparkie.aizoban.presenters.ChapterPresenterOnlineImpl;
import com.jparkie.aizoban.presenters.mapper.ChapterMapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.ChapterView;
import com.jparkie.aizoban.views.widgets.GestureViewPager;
import com.melnykov.fab.FloatingActionButton;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class ChapterActivity extends BaseActivity implements ChapterView, ChapterMapper {
    public static final String TAG = ChapterActivity.class.getSimpleName();

    public static final String PRESENTER_ARGUMENT_KEY = TAG + ":" + "PresenterArgumentKey";
    public static final String REQUEST_ARGUMENT_KEY = TAG + ":" + "RequestArgumentKey";
    public static final String POSITION_ARGUMENT_KEY = TAG + ":" + "PositionArgumentKey";

    private ChapterPresenter mChapterPresenter;

    private Toolbar mToolbar;
    private MenuItem mDirectionMenuItem;
    private MenuItem mOrientationMenuItem;
    private MenuItem mZoomMenuItem;
    private GestureViewPager mViewPager;
    private RelativeLayout mEmptyRelativeLayout;
    private FloatingActionButton mPreviousButton;
    private FloatingActionButton mNextButton;
    private TextView mPageNumberView;

    private boolean mSystemUIVisibility;

    public static Intent constructOnlineChapterActivityIntent(Context context, RequestWrapper chapterRequest, int position) {
        Intent argumentIntent = new Intent(context, ChapterActivity.class);
        argumentIntent.putExtra(PRESENTER_ARGUMENT_KEY, ChapterPresenterOnlineImpl.TAG);
        argumentIntent.putExtra(REQUEST_ARGUMENT_KEY, chapterRequest);
        argumentIntent.putExtra(POSITION_ARGUMENT_KEY, position);

        return argumentIntent;
    }

    public static Intent constructOfflineChapterActivityIntent(Context context, RequestWrapper chapterRequest, int position) {
        Intent argumentIntent = new Intent(context, ChapterActivity.class);
        argumentIntent.putExtra(PRESENTER_ARGUMENT_KEY, ChapterPresenterOfflineImpl.TAG);
        argumentIntent.putExtra(REQUEST_ARGUMENT_KEY, chapterRequest);
        argumentIntent.putExtra(POSITION_ARGUMENT_KEY, position);

        return argumentIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(PRESENTER_ARGUMENT_KEY)) {
                String presenterType = intent.getStringExtra(PRESENTER_ARGUMENT_KEY);

                if (presenterType.equals(ChapterPresenterOnlineImpl.TAG)) {
                    mChapterPresenter = new ChapterPresenterOnlineImpl(this, this);
                } else if (presenterType.equals(ChapterPresenterOfflineImpl.TAG)) {
                    mChapterPresenter = new ChapterPresenterOfflineImpl(this, this);
                }
            }
        }

        setContentView(R.layout.activity_chapter);

        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        mViewPager = (GestureViewPager) findViewById(R.id.viewPager);
        mEmptyRelativeLayout = (RelativeLayout) findViewById(R.id.emptyRelativeLayout);
        mPreviousButton = (FloatingActionButton) findViewById(R.id.previousButton);
        mNextButton = (FloatingActionButton) findViewById(R.id.nextButton);
        mPageNumberView = (TextView) findViewById(R.id.numberTextView);

        if (savedInstanceState != null) {
            mChapterPresenter.restoreState(savedInstanceState);
        } else {
            mChapterPresenter.handleInitialArguments(getIntent());
        }

        mChapterPresenter.initializeViews();

        mChapterPresenter.initializeOptions();

        mChapterPresenter.initializeDataFromUrl(getSupportFragmentManager());
    }

    @Override
    protected void onStart() {
        super.onStart();

        mChapterPresenter.registerForEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mChapterPresenter.saveChapterToRecentChapters();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mChapterPresenter.unregisterForEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mChapterPresenter.destroyAllSubscriptions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mChapterPresenter.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter, menu);
        mDirectionMenuItem = menu.findItem(R.id.action_direction);
        mOrientationMenuItem = menu.findItem(R.id.action_orientation);
        mZoomMenuItem = menu.findItem(R.id.action_zoom);

        mChapterPresenter.initializeMenu();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mChapterPresenter.onOptionParent();
                return true;
            case R.id.action_refresh:
                mChapterPresenter.onOptionRefresh();
                return true;
            case R.id.action_select_page:
                mChapterPresenter.onOptionSelectPage();
                return true;
            case R.id.action_direction:
                mChapterPresenter.onOptionDirection();
                return true;
            case R.id.action_orientation:
                mChapterPresenter.onOptionOrientation();
                return true;
            case R.id.action_zoom:
                mChapterPresenter.onOptionZoom();
                return true;
            case R.id.action_help:
                mChapterPresenter.onOptionHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        mChapterPresenter.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mChapterPresenter.onLowMemory();
    }

    // ChapterView:

    @Override
    public void initializeHardwareAcceleration() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }

    @Override
    public void initializeSystemUIVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.VISIBLE
            );
        }

        mSystemUIVisibility = true;
    }

    @Override
    public void initializeToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.fragment_chapter);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.primaryBlue500));
            mToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(ChapterActivity.this, mToolbar.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void initializeViewPager() {
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(2);
            mViewPager.setPageMargin(16);

            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // Do Nothing.
                }

                @Override
                public void onPageSelected(int position) {
                    mChapterPresenter.onPageSelected(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // Do Nothing.
                }
            });

            mViewPager.setOnChapterBoundariesOutListener(new GestureViewPager.OnChapterBoundariesOutListener() {
                @Override
                public void onFirstPageOut() {
                    mChapterPresenter.onFirstPageOut();
                }

                @Override
                public void onLastPageOut() {
                    mChapterPresenter.onLastPageOut();
                }
            });

            mViewPager.setOnChapterSingleTapListener(new GestureViewPager.OnChapterSingleTapListener() {
                @Override
                public void onSingleTap() {
                    if (mSystemUIVisibility) {
                        enableFullscreen();
                    } else {
                        disableFullscreen();
                    }
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_image_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_chapter);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.chapter_instructions);
        }
    }

    @Override
    public void initializeButtons() {
        if (mPreviousButton != null) {
            mPreviousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChapterPresenter.onPreviousClick();
                }
            });
        }
        if (mNextButton != null) {
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChapterPresenter.onNextClick();
                }
            });
        }
    }

    @Override
    public void initializeTextView() {
        if (mPageNumberView != null) {
            mPageNumberView.setVisibility(View.GONE);
            mPageNumberView.getBackground().setAlpha(100);
        }
    }

    @Override
    public void initializeFullscreen() {
        Observable.just(null)
                .delay(2500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object nullObject) {
                        enableFullscreen();
                    }
                });
        }

    @Override
    public void hideEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyRelativeLayout() {
        // Do Nothing.
    }

    @Override
    public void enableFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
        }

        hideControls();
        showPageView();

        mSystemUIVisibility = false;
    }

    @Override
    public void disableFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.VISIBLE
            );
        }

        showControls();
        hidePageView();

        mSystemUIVisibility = true;
    }

    @Override
    public void setTitleText(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void setSubtitleProgressText(int imageUrlsCount) {
        if (mToolbar != null) {
            StringBuilder currentSubtitle = new StringBuilder(getString(R.string.chapter_subtitle_progress));
            currentSubtitle.append(" ");
            currentSubtitle.append(imageUrlsCount);

            mToolbar.setSubtitle(currentSubtitle.toString());
        }
    }

    @Override
    public void setSubtitlePositionText(int position) {
        if (mToolbar != null) {
            if (mViewPager.getAdapter() != null) {
                StringBuilder currentSubtitle = new StringBuilder(getString(R.string.chapter_subtitle_page));
                currentSubtitle.append(" ");
                currentSubtitle.append(position);
                currentSubtitle.append("/");
                currentSubtitle.append(mViewPager.getAdapter().getCount());

                mToolbar.setSubtitle(currentSubtitle.toString());
            }
        }
    }

    @Override
    public void setImmersivePositionText(int position) {
        if (mPageNumberView != null) {
            if (mViewPager.getAdapter() != null) {
                mPageNumberView.setText(position + "/" + mViewPager.getAdapter().getCount());
            }
        }
    }

    @Override
    public void setOptionDirectionText(boolean isRightToLeftDirection) {
        if (mDirectionMenuItem != null) {
            if (isRightToLeftDirection) {
                mDirectionMenuItem.setTitle(R.string.action_left_to_right);
            } else {
                mDirectionMenuItem.setTitle(R.string.action_right_to_left);
            }
        }
    }

    @Override
    public void setOptionOrientationText(boolean isLockOrientation) {
        if (mOrientationMenuItem != null) {
            if (isLockOrientation) {
                mOrientationMenuItem.setTitle(R.string.action_unlock_orientation);
            } else {
                mOrientationMenuItem.setTitle(R.string.action_lock_orientation);
            }
        }
    }

    @Override
    public void setOptionZoomText(boolean isLockZoom) {
        if (mZoomMenuItem != null) {
            if (isLockZoom) {
                mZoomMenuItem.setTitle(R.string.action_unlock_zoom);
            } else {
                mZoomMenuItem.setTitle(R.string.action_lock_zoom);
            }
        }
    }

    @Override
    public void toastNotInitializedError() {
        Toast.makeText(this, R.string.toast_not_initialized, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastChapterError() {
        Toast.makeText(this, R.string.toast_chapter_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastNoPreviousChapter() {
        Toast.makeText(this, R.string.toast_no_previous_chapter, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastNoNextChapter() {
        Toast.makeText(this, R.string.toast_no_next_chapter, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishAndLaunchActivity(Intent launchIntent, boolean isFadeTransition) {
        finish();

        if (isFadeTransition) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        startActivity(launchIntent);
    }

    @Override
    public Context getContext() {
        return this;
    }

    // ChapterMapper;

    @Override
    public void registerAdapter(PagerAdapter adapter) {
        if (mViewPager != null) {
            mViewPager.setAdapter(adapter);
        }
    }

    @Override
    public int getPosition() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        }

        return -1;
    }

    @Override
    public void setPosition(int position) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(position, false);
        }
    }

    @Override
    public void applyViewSettings() {
        if (mViewPager != null) {
            mViewPager.applyViewSettings();
        }
    }

    @Override
    public void applyIsLockOrientation(boolean isLockOrientation) {
        if (isLockOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void applyIsLockZoom(boolean isLockZoom) {
        if (mViewPager != null) {
            mViewPager.setIsLockZoom(isLockZoom);
        }
    }

    private void hideControls() {
        if (mToolbar != null) {
            if (mToolbar.getVisibility() != View.GONE) {
                mToolbar.animate()
                        .y(0)
                        .translationY(-1 * mToolbar.getHeight())
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mToolbar.setVisibility(View.GONE);
                            }
                        });
            }
        }
        if (mPreviousButton != null) {
            mPreviousButton.hide(true);
        }
        if (mNextButton != null) {
            mNextButton.hide(true);
        }
    }

    private void showControls() {
        if (mToolbar != null) {
            if (mToolbar.getVisibility() != View.VISIBLE) {
                mToolbar.setVisibility(View.VISIBLE);
                mToolbar.animate()
                        .translationY(mToolbar.getHeight())
                        .y(0)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setListener(null);
            }
        }
        if (mPreviousButton != null) {
            mPreviousButton.show(true);
        }
        if (mNextButton != null) {
            mNextButton.show(true);
        }
    }

    private void hidePageView() {
        if (mPageNumberView != null) {
            if (mPageNumberView.getVisibility() != View.GONE) {
                mPageNumberView.animate()
                        .alpha(0.0f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mPageNumberView.setVisibility(View.GONE);
                            }
                        });
            }
        }
    }

    private void showPageView() {
        if (mPageNumberView != null) {
            if (mPageNumberView.getVisibility() != View.VISIBLE) {
                mPageNumberView.setVisibility(View.VISIBLE);
                mPageNumberView.animate()
                        .alpha(1.0f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setListener(null);
            }
        }
    }
}
