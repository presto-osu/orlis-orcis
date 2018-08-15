package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.AizobanManager;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.SelectPageEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.presenters.mapper.ChapterMapper;
import com.jparkie.aizoban.utils.PreferenceUtils;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.ChapterView;
import com.jparkie.aizoban.views.activities.ChapterActivity;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.PagesAdapter;
import com.jparkie.aizoban.views.fragments.ChapterHelpFragment;
import com.jparkie.aizoban.views.fragments.SelectPageFragment;

import java.util.ArrayList;
import java.util.Collections;

import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ChapterPresenterOnlineImpl implements ChapterPresenter {
    public static final String TAG = ChapterPresenterOnlineImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";
    private static final String IMAGE_URLS_PARCELABLE_KEY = TAG + ":" + "ImageUrlsParcelableKey";

    private static final String INITIALIZED_PARCELABLE_KEY = TAG + ":" + "InitializedParcelableKey";
    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private ChapterView mChapterView;
    private ChapterMapper mChapterMapper;
    private PagesAdapter mPagesAdapter;

    private RequestWrapper mRequest;
    private Chapter mChapter;
    private RecentChapter mRecentChapter;
    private ArrayList<String> mImageUrls;

    private boolean mIsLazyLoading;
    private boolean mIsRightToLeftDirection;
    private boolean mIsLockOrientation;
    private boolean mIsLockZoom;

    private boolean mInitialized;
    private int mInitialPosition;

    private Subscription mQueryChapterSubscription;
    private Subscription mQueryRecentChapterSubscription;
    private Subscription mDownloadImageUrlsSubscription;

    public ChapterPresenterOnlineImpl(ChapterView chapterView, ChapterMapper chapterMapper) {
        mChapterView = chapterView;
        mChapterMapper = chapterMapper;
    }

    @Override
    public void handleInitialArguments(Intent arguments) {
        if (arguments != null) {
            if (arguments.hasExtra(ChapterActivity.REQUEST_ARGUMENT_KEY)) {
                mRequest = arguments.getParcelableExtra(ChapterActivity.REQUEST_ARGUMENT_KEY);

                arguments.removeExtra(ChapterActivity.REQUEST_ARGUMENT_KEY);
            }
            if (arguments.hasExtra(ChapterActivity.POSITION_ARGUMENT_KEY)) {
                mInitialPosition = arguments.getIntExtra(ChapterActivity.POSITION_ARGUMENT_KEY, 0);

                arguments.removeExtra(ChapterActivity.POSITION_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mChapterView.initializeHardwareAcceleration();
        mChapterView.initializeSystemUIVisibility();
        mChapterView.initializeToolbar();
        mChapterView.initializeViewPager();
        mChapterView.initializeEmptyRelativeLayout();
        mChapterView.initializeButtons();
        mChapterView.initializeTextView();
    }

    @Override
    public void initializeOptions() {
        mIsLazyLoading = PreferenceUtils.isLazyLoading();
        mIsRightToLeftDirection = PreferenceUtils.isRightToLeftDirection();
        mIsLockOrientation = PreferenceUtils.isLockOrientation();
        mIsLockZoom = PreferenceUtils.isLockZoom();

        mChapterMapper.applyIsLockOrientation(mIsLockOrientation);
        mChapterMapper.applyIsLockZoom(mIsLockZoom);
    }

    @Override
    public void initializeMenu() {
        mChapterView.setOptionDirectionText(mIsRightToLeftDirection);
        mChapterView.setOptionOrientationText(mIsLockOrientation);
        mChapterView.setOptionZoomText(mIsLockZoom);
    }

    @Override
    public void initializeDataFromUrl(FragmentManager fragmentManager) {
        mPagesAdapter = new PagesAdapter(fragmentManager);
        mPagesAdapter.setIsRightToLeftDirection(mIsRightToLeftDirection);

        mChapterMapper.registerAdapter(mPagesAdapter);

        initializeRecentChapter();

        if (!mInitialized) {
            queryChapterFromUrl();
            downloadImageUrlsFromUrl();
        } else {
            if (mChapter != null) {
                mChapterView.setTitleText(mChapter.getName());
            }
            if (mImageUrls != null && mImageUrls.size() != 0) {
                updateAdapter();

                preLoadImagesToCache();

                mChapterView.hideEmptyRelativeLayout();
            }
        }
    }

    @Override
    public void registerForEvents() {
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(SelectPageEvent event) {
        if (event != null) {
            setPosition(event.getSelectPage());
        }
    }

    @Override
    public void unregisterForEvents() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void saveState(Bundle outState) {
        if (mRequest != null) {
            outState.putParcelable(REQUEST_PARCELABLE_KEY, mRequest);
        }

        if (mChapter != null) {
            outState.putParcelable(Chapter.PARCELABLE_KEY, mChapter);
        }

        if (mImageUrls != null) {
            outState.putStringArrayList(IMAGE_URLS_PARCELABLE_KEY, mImageUrls);
        }

        outState.putBoolean(INITIALIZED_PARCELABLE_KEY, mInitialized);

        outState.putInt(POSITION_PARCELABLE_KEY, mInitialPosition);
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(REQUEST_PARCELABLE_KEY)) {
            mRequest = savedState.getParcelable(REQUEST_PARCELABLE_KEY);

            savedState.remove(REQUEST_PARCELABLE_KEY);
        }
        if (savedState.containsKey(Chapter.PARCELABLE_KEY)) {
            mChapter = savedState.getParcelable(Chapter.PARCELABLE_KEY);

            savedState.remove(Chapter.PARCELABLE_KEY);
        }
        if (savedState.containsKey(IMAGE_URLS_PARCELABLE_KEY)) {
            mImageUrls = savedState.getStringArrayList(IMAGE_URLS_PARCELABLE_KEY);

            savedState.remove(IMAGE_URLS_PARCELABLE_KEY);
        }
        if (savedState.containsKey(INITIALIZED_PARCELABLE_KEY)) {
            mInitialized = savedState.getBoolean(INITIALIZED_PARCELABLE_KEY, false);

            savedState.remove(INITIALIZED_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mInitialPosition = savedState.getInt(POSITION_PARCELABLE_KEY, 0);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void saveChapterToRecentChapters() {
        try {
            if (mChapter != null) {
                if (mRecentChapter == null) {
                    mRecentChapter = DefaultFactory.RecentChapter.constructDefault();
                    mRecentChapter.setSource(mChapter.getSource());
                    mRecentChapter.setUrl(mChapter.getUrl());
                    mRecentChapter.setParentUrl(mChapter.getParentUrl());
                    mRecentChapter.setName(mChapter.getName());
                    mRecentChapter.setOffline(false);
                }

                mRecentChapter.setThumbnailUrl(mImageUrls.get(getActualPosition()));
                mRecentChapter.setDate(System.currentTimeMillis());
                mRecentChapter.setPageNumber(getActualPosition());

                QueryManager.putObjectToApplicationDatabase(mRecentChapter);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryChapterSubscription != null) {
            mQueryChapterSubscription.unsubscribe();
            mQueryChapterSubscription = null;
        }
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }
        if (mDownloadImageUrlsSubscription != null) {
            mDownloadImageUrlsSubscription.unsubscribe();
            mDownloadImageUrlsSubscription = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        Glide.get(mChapterView.getContext()).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Glide.get(mChapterView.getContext()).clearMemory();
    }

    @Override
    public void onPageSelected(int position) {
        mChapterView.setSubtitlePositionText(getActualPosition() + 1);
        mChapterView.setImmersivePositionText(getActualPosition() + 1);

        mChapterMapper.applyViewSettings();
    }

    @Override
    public void onFirstPageOut() {
        if (mChapter != null) {
            if (mIsRightToLeftDirection) {
                nextChapter();
            } else {
                previousChapter();
            }
        }
    }

    @Override
    public void onLastPageOut() {
        if (mChapter != null) {
            if (mIsRightToLeftDirection) {
                previousChapter();
            } else {
                nextChapter();
            }
        }
    }

    @Override
    public void onPreviousClick() {
        previousChapter();
    }

    @Override
    public void onNextClick() {
        nextChapter();
    }

    @Override
    public void onOptionParent() {
        if (mChapter != null) {
            Intent mangaIntent = MangaActivity.constructOnlineMangaActivityIntent(mChapterView.getContext(), new RequestWrapper(mChapter.getSource(), mChapter.getParentUrl()));
            mangaIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            mChapterView.finishAndLaunchActivity(mangaIntent, false);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionRefresh() {
        if (!mInitialized) {
            downloadImageUrlsFromUrl();
        } else {
            mPagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onOptionSelectPage() {
        if (mInitialized) {
            if (mImageUrls != null) {
                if (((FragmentActivity) mChapterView.getContext()).getSupportFragmentManager().findFragmentByTag(SelectPageFragment.TAG) == null) {
                    SelectPageFragment selectPageFragment = SelectPageFragment.newInstance(getActualPosition(), mImageUrls.size());

                    selectPageFragment.show(((FragmentActivity) mChapterView.getContext()).getSupportFragmentManager(), SelectPageFragment.TAG);
                }
            }
        }
    }

    @Override
    public void onOptionDirection() {
        if (mInitialized) {
            mIsRightToLeftDirection = !mIsRightToLeftDirection;

            updateAdapter();

            swapPositions();

            mChapterView.setOptionDirectionText(mIsRightToLeftDirection);

            PreferenceUtils.setDirection(mIsRightToLeftDirection);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionOrientation() {
        if (mInitialized) {
            mIsLockOrientation = !mIsLockOrientation;

            mChapterMapper.applyIsLockOrientation(mIsLockOrientation);

            mChapterView.setOptionOrientationText(mIsLockOrientation);

            PreferenceUtils.setOrientation(mIsLockOrientation);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionZoom() {
        if (mInitialized) {
            mIsLockZoom = !mIsLockZoom;

            mChapterMapper.applyIsLockZoom(mIsLockZoom);

            mChapterView.setOptionZoomText(mIsLockZoom);

            PreferenceUtils.setZoom(mIsLockZoom);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionHelp() {
        if (((FragmentActivity)mChapterView.getContext()).getSupportFragmentManager().findFragmentByTag(ChapterHelpFragment.TAG) == null) {
            ChapterHelpFragment chapterHelpFragment = new ChapterHelpFragment();

            chapterHelpFragment.show(((FragmentActivity) mChapterView.getContext()).getSupportFragmentManager(), ChapterHelpFragment.TAG);
        }
    }

    private void initializeRecentChapter() {
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
        }

        if (mRequest != null) {
            mQueryRecentChapterSubscription = QueryManager
                    .queryRecentChapterFromRequest(mRequest, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Cursor>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor recentCursor) {
                            if (recentCursor != null && recentCursor.getCount() != 0) {
                                mRecentChapter = QueryManager.toObject(recentCursor, RecentChapter.class);
                            }
                        }
                    });
        }
    }

    private void queryChapterFromUrl() {
        if (mQueryChapterSubscription != null) {
            mQueryChapterSubscription.unsubscribe();
            mQueryChapterSubscription = null;
        }

        if (mRequest != null) {
            mQueryChapterSubscription = QueryManager
                    .queryChapterFromRequest(mRequest)
                    .map(new Func1<Cursor, Cursor>() {
                        @Override
                        public Cursor call(Cursor incomingCursor) {
                            if (incomingCursor != null && incomingCursor.getCount() != 0) {
                                return incomingCursor;
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Cursor>() {
                        @Override
                        public void onCompleted() {
                            if (mChapter != null) {
                                mChapterView.setTitleText(mChapter.getName());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor chapterCursor) {
                            if (chapterCursor != null) {
                                mChapter = QueryManager.toObject(chapterCursor, Chapter.class);
                            }
                        }
                    });
        }
    }

    private void downloadImageUrlsFromUrl() {
        if (mDownloadImageUrlsSubscription != null) {
            mDownloadImageUrlsSubscription.unsubscribe();
            mDownloadImageUrlsSubscription = null;
        }

        if (mRequest != null) {
            mImageUrls = new ArrayList<String>();

            mDownloadImageUrlsSubscription = AizobanManager
                    .pullImageUrlsFromNetwork(mRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            updateAdapter();

                            setPosition(mInitialPosition);

                            preLoadImagesToCache();

                            mChapterView.setSubtitlePositionText(getActualPosition() + 1);
                            mChapterView.setImmersivePositionText(getActualPosition() + 1);

                            mChapterView.hideEmptyRelativeLayout();

                            mChapterView.initializeFullscreen();

                            mInitialized = true;
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }

                            mChapterView.toastChapterError();
                        }

                        @Override
                        public void onNext(String imageUrl) {
                            if (imageUrl != null) {
                                mImageUrls.add(imageUrl);

                                mChapterView.setSubtitleProgressText(mImageUrls.size());
                            }
                        }
                    });
        }
    }

    private void preLoadImagesToCache() {
        if (!mIsLazyLoading) {
            if (mDownloadImageUrlsSubscription != null) {
                mDownloadImageUrlsSubscription.unsubscribe();
                mDownloadImageUrlsSubscription = null;
            }

            if (mImageUrls != null) {
                mDownloadImageUrlsSubscription = AizobanManager
                        .cacheFromImagesOfSize(mImageUrls)
                        .subscribe(new Observer<GlideDrawable>() {
                            @Override
                            public void onCompleted() {
                                // Do Nothing.
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (BuildConfig.DEBUG) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNext(GlideDrawable glideDrawable) {
                                // Do Nothing.
                            }
                        });
            }
        }
    }

    private void updateAdapter() {
        if (mImageUrls != null) {
            ArrayList<String> imageUrls = new ArrayList<String>(mImageUrls.size());

            if (mIsRightToLeftDirection) {
                for (String imageUrl : mImageUrls) {
                    imageUrls.add(new String(imageUrl));
                }

                Collections.reverse(imageUrls);
            } else {
                imageUrls = mImageUrls;
            }

            if (mPagesAdapter != null) {
                mPagesAdapter.setImageUrls(imageUrls);
                mPagesAdapter.setIsRightToLeftDirection(mIsRightToLeftDirection);
            }
        }
    }

    private void setPosition(int position) {
        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            if (position >= 0 && position <= mPagesAdapter.getCount() - 1) {
                int currentPosition = position;

                if (mIsRightToLeftDirection) {
                    currentPosition = mPagesAdapter.getCount() - currentPosition - 1;
                }

                mChapterMapper.setPosition(currentPosition);
            }
        }
    }

    private void swapPositions() {
        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            int oldPosition = mChapterMapper.getPosition();
            int newPosition = mPagesAdapter.getCount() - oldPosition - 1;

            mChapterMapper.setPosition(newPosition);
        }
    }

    private int getActualPosition() {
        int currentPosition = mChapterMapper.getPosition();

        if (mPagesAdapter != null && mPagesAdapter.getCount() != 0) {
            if (mPagesAdapter.getIsRightToLeftDirection()) {
                currentPosition = mPagesAdapter.getCount() - currentPosition - 1;
            }
        }

        return currentPosition;
    }

    private void nextChapter() {
        if (mChapter != null) {
            if (mQueryChapterSubscription != null) {
                mQueryChapterSubscription.unsubscribe();
                mQueryChapterSubscription = null;
            }

            mQueryChapterSubscription = QueryManager
                    .queryAdjacentChapterFromRequestAndNumber(new RequestWrapper(mChapter.getSource(), mChapter.getParentUrl()), mChapter.getNumber() + 1)
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor adjacentCursor) {
                            if (adjacentCursor != null && adjacentCursor.getCount() != 0) {
                                Chapter adjacentChapter = QueryManager.toObject(adjacentCursor, Chapter.class);

                                if (adjacentChapter != null) {
                                    return adjacentChapter.getUrl();
                                }
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            mChapterView.toastNoNextChapter();

                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(String adjacentChapterUrl) {
                            if (adjacentChapterUrl != null) {
                                Intent adjacentChapterIntent = ChapterActivity.constructOnlineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mChapter.getSource(), adjacentChapterUrl), 0);

                                mChapterView.finishAndLaunchActivity(adjacentChapterIntent, true);
                            } else {
                                mChapterView.toastNoNextChapter();
                            }
                        }
                    });
        } else {
            mChapterView.toastNoNextChapter();
        }
    }

    private void previousChapter() {
        if (mChapter != null) {
            if (mQueryChapterSubscription != null) {
                mQueryChapterSubscription.unsubscribe();
                mQueryChapterSubscription = null;
            }

            mQueryChapterSubscription = QueryManager
                    .queryAdjacentChapterFromRequestAndNumber(new RequestWrapper(mChapter.getSource(), mChapter.getParentUrl()), mChapter.getNumber() - 1)
                    .map(new Func1<Cursor, String>() {
                        @Override
                        public String call(Cursor adjacentCursor) {
                            if (adjacentCursor != null && adjacentCursor.getCount() != 0) {
                                Chapter adjacentChapter = QueryManager.toObject(adjacentCursor, Chapter.class);

                                if (adjacentChapter != null) {
                                    return adjacentChapter.getUrl();
                                }
                            }

                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                            // Do Nothing.
                        }

                        @Override
                        public void onError(Throwable e) {
                            mChapterView.toastNoPreviousChapter();

                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(String adjacentChapterUrl) {
                            if (adjacentChapterUrl != null) {
                                Intent adjacentChapterIntent = ChapterActivity.constructOnlineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mChapter.getSource(), adjacentChapterUrl), 0);

                                mChapterView.finishAndLaunchActivity(adjacentChapterIntent, true);
                            } else {
                                mChapterView.toastNoPreviousChapter();
                            }
                        }
                    });
        } else {
            mChapterView.toastNoPreviousChapter();
        }
    }
}
