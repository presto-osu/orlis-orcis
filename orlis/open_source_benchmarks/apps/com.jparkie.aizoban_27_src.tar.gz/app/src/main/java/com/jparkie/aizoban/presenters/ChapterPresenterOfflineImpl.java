package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.events.SelectPageEvent;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.presenters.mapper.ChapterMapper;
import com.jparkie.aizoban.utils.PreferenceUtils;
import com.jparkie.aizoban.utils.wrappers.DownloadChapterSortCursorWrapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.ChapterView;
import com.jparkie.aizoban.views.activities.ChapterActivity;
import com.jparkie.aizoban.views.activities.MangaActivity;
import com.jparkie.aizoban.views.adapters.PagesAdapter;
import com.jparkie.aizoban.views.fragments.ChapterHelpFragment;
import com.jparkie.aizoban.views.fragments.SelectPageFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class ChapterPresenterOfflineImpl implements ChapterPresenter {
    public static final String TAG = ChapterPresenterOfflineImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";
    private static final String IMAGE_URLS_PARCELABLE_KEY = TAG + ":" + "ImageUrlsParcelableKey";

    private static final String INITIALIZED_PARCELABLE_KEY = TAG + ":" + "InitializedParcelableKey";
    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private ChapterView mChapterView;
    private ChapterMapper mChapterMapper;
    private PagesAdapter mPagesAdapter;

    private RequestWrapper mRequest;
    private DownloadChapter mDownloadChapter;
    private RecentChapter mRecentChapter;
    private ArrayList<String> mImageUrls;

    private boolean mIsRightToLeftDirection;
    private boolean mIsLockOrientation;
    private boolean mIsLockZoom;

    private boolean mInitialized;
    private int mInitialPosition;

    private Subscription mQueryDownloadChapterSubscription;
    private Subscription mQueryRecentChapterSubscription;

    public ChapterPresenterOfflineImpl(ChapterView chapterView, ChapterMapper chapterMapper) {
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
        } else {
            if (mDownloadChapter != null) {
                mChapterView.setTitleText(mDownloadChapter.getName());
            }
            if (mImageUrls != null && mImageUrls.size() != 0) {
                updateAdapter();

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

        if (mDownloadChapter != null) {
            outState.putParcelable(Chapter.PARCELABLE_KEY, mDownloadChapter);
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
            mDownloadChapter = savedState.getParcelable(Chapter.PARCELABLE_KEY);

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
            if (mInitialized) {
                if (mRecentChapter == null) {
                    mRecentChapter = DefaultFactory.RecentChapter.constructDefault();
                    mRecentChapter.setSource(mDownloadChapter.getSource());
                    mRecentChapter.setUrl(mDownloadChapter.getUrl());
                    mRecentChapter.setParentUrl(mDownloadChapter.getParentUrl());
                    mRecentChapter.setName(mDownloadChapter.getName());
                    mRecentChapter.setOffline(true);
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
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }
        if (mQueryRecentChapterSubscription != null) {
            mQueryRecentChapterSubscription.unsubscribe();
            mQueryRecentChapterSubscription = null;
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
        if (mDownloadChapter != null) {
            if (mIsRightToLeftDirection) {
                nextChapter();
            } else {
                previousChapter();
            }
        }
    }

    @Override
    public void onLastPageOut() {
        if (mDownloadChapter != null) {
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
        if (mDownloadChapter != null) {
            Intent mangaIntent = MangaActivity.constructOfflineMangaActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()));
            mangaIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            mChapterView.finishAndLaunchActivity(mangaIntent, false);
        } else {
            mChapterView.toastNotInitializedError();
        }
    }

    @Override
    public void onOptionRefresh() {
        if (mInitialized) {
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
                    .queryRecentChapterFromRequest(mRequest, true)
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
        if (mQueryDownloadChapterSubscription != null) {
            mQueryDownloadChapterSubscription.unsubscribe();
            mQueryDownloadChapterSubscription = null;
        }

        if (mRequest != null) {
            mQueryDownloadChapterSubscription = QueryManager
                    .queryDownloadChapterFromRequest(mRequest)
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
                            if (mDownloadChapter != null) {
                                File chapterDirectory = new File(mDownloadChapter.getDirectory());
                                if (chapterDirectory.exists() && chapterDirectory.isDirectory()) {
                                    initializeImageUrls(chapterDirectory.listFiles());

                                    updateAdapter();

                                    setPosition(mInitialPosition);

                                    mChapterView.hideEmptyRelativeLayout();

                                    mChapterView.setTitleText(mDownloadChapter.getName());

                                    mChapterView.setSubtitlePositionText(getActualPosition() + 1);
                                    mChapterView.setImmersivePositionText(getActualPosition() + 1);

                                    mChapterView.initializeFullscreen();

                                    mInitialized = true;
                                }
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
                                mDownloadChapter = QueryManager.toObject(chapterCursor, DownloadChapter.class);
                            }
                        }
                    });
        }
    }

    private void initializeImageUrls(File[] files) {
        mImageUrls = new ArrayList<String>();

        File[] imageFiles = files;
        Arrays.sort(imageFiles, new Comparator<File>() {
            @Override
            public int compare(File leftFile, File rightFile) {
                String leftFileNameAndExtension = leftFile.getName().substring(0, leftFile.getName().indexOf("."));
                String rightFileNameAndExtension = rightFile.getName().substring(0, rightFile.getName().indexOf("."));

                int leftFileNumber = Integer.parseInt(leftFileNameAndExtension);
                int rightFileNumber = Integer.parseInt(rightFileNameAndExtension);

                if (leftFileNumber > rightFileNumber) {
                    return 1;
                } else if (leftFileNumber == rightFileNumber) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        for (File imageFile : imageFiles) {
            mImageUrls.add(imageFile.getPath());
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
        if (mDownloadChapter != null) {
            if (mQueryDownloadChapterSubscription != null) {
                mQueryDownloadChapterSubscription.unsubscribe();
                mQueryDownloadChapterSubscription = null;
            }

            Observable<Cursor> queryDownloadChaptersFromUrlObservable = QueryManager
                    .queryDownloadChaptersOfDownloadManga(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()), true);
            Observable<List<String>> queryChapterUrlsFromUrlObservable = QueryManager
                    .queryChaptersOfMangaFromRequest(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()), true)
                    .flatMap(new Func1<Cursor, Observable<Chapter>>() {
                        @Override
                        public Observable<Chapter> call(Cursor chapterCursor) {
                            List<Chapter> chapters = QueryManager.toList(chapterCursor, Chapter.class);
                            return Observable.from(chapters.toArray(new Chapter[chapters.size()]));
                        }
                    })
                    .flatMap(new Func1<Chapter, Observable<String>>() {
                        @Override
                        public Observable<String> call(Chapter chapter) {
                            return Observable.just(chapter.getUrl());
                        }
                    })
                    .toList();

            mQueryDownloadChapterSubscription = Observable.zip(queryDownloadChaptersFromUrlObservable, queryChapterUrlsFromUrlObservable,
                    new Func2<Cursor, List<String>, Cursor>() {
                        @Override
                        public Cursor call(Cursor downloadChapterCursor, List<String> sortedChapterUrls) {
                            return new DownloadChapterSortCursorWrapper(downloadChapterCursor, sortedChapterUrls);
                        }
                    })
                    .flatMap(new Func1<Cursor, Observable<List<DownloadChapter>>>() {
                        @Override
                        public Observable<List<DownloadChapter>> call(Cursor sortedDownloadChapterCursor) {
                            List<DownloadChapter> downloadChapters = QueryManager.toList(sortedDownloadChapterCursor, DownloadChapter.class);
                            return Observable.just(downloadChapters);
                        }
                    })
                    .map(new Func1<List<DownloadChapter>, String>() {
                        @Override
                        public String call(List<DownloadChapter> downloadChapters) {
                            DownloadChapter currentChapter = null;
                            for (int index = 0; index < downloadChapters.size(); index++) {
                                currentChapter = downloadChapters.get(index);
                                if (currentChapter != null) {
                                    if (currentChapter.getUrl().equals(mDownloadChapter.getUrl())) {
                                        if (index + 1 < downloadChapters.size()) {
                                            return downloadChapters.get(index + 1).getUrl();
                                        } else {
                                            break;
                                        }
                                    }
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
                                Intent adjacentChapterIntent = ChapterActivity.constructOfflineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl), 0);

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
        if (mDownloadChapter != null) {
            if (mQueryDownloadChapterSubscription != null) {
                mQueryDownloadChapterSubscription.unsubscribe();
                mQueryDownloadChapterSubscription = null;
            }

            Observable<Cursor> queryDownloadChaptersFromUrlObservable = QueryManager
                    .queryDownloadChaptersOfDownloadManga(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()), true);
            Observable<List<String>> queryChapterUrlsFromUrlObservable = QueryManager
                    .queryChaptersOfMangaFromRequest(new RequestWrapper(mDownloadChapter.getSource(), mDownloadChapter.getParentUrl()), true)
                    .flatMap(new Func1<Cursor, Observable<Chapter>>() {
                        @Override
                        public Observable<Chapter> call(Cursor chapterCursor) {
                            List<Chapter> chapters = QueryManager.toList(chapterCursor, Chapter.class);
                            return Observable.from(chapters.toArray(new Chapter[chapters.size()]));
                        }
                    })
                    .flatMap(new Func1<Chapter, Observable<String>>() {
                        @Override
                        public Observable<String> call(Chapter chapter) {
                            return Observable.just(chapter.getUrl());
                        }
                    })
                    .toList();

            mQueryDownloadChapterSubscription = Observable.zip(queryDownloadChaptersFromUrlObservable, queryChapterUrlsFromUrlObservable,
                    new Func2<Cursor, List<String>, Cursor>() {
                        @Override
                        public Cursor call(Cursor downloadChapterCursor, List<String> sortedChapterUrls) {
                            return new DownloadChapterSortCursorWrapper(downloadChapterCursor, sortedChapterUrls);
                        }
                    })
                    .flatMap(new Func1<Cursor, Observable<List<DownloadChapter>>>() {
                        @Override
                        public Observable<List<DownloadChapter>> call(Cursor sortedDownloadChapterCursor) {
                            List<DownloadChapter> downloadChapters = QueryManager.toList(sortedDownloadChapterCursor, DownloadChapter.class);
                            return Observable.just(downloadChapters);
                        }
                    })
                    .map(new Func1<List<DownloadChapter>, String>() {
                        @Override
                        public String call(List<DownloadChapter> downloadChapters) {
                            DownloadChapter currentChapter = null;
                            for (int index = 0; index < downloadChapters.size(); index++) {
                                currentChapter = downloadChapters.get(index);
                                if (currentChapter != null) {
                                    if (currentChapter.getUrl().equals(mDownloadChapter.getUrl())) {
                                        if (index - 1 >= 0) {
                                            return downloadChapters.get(index - 1).getUrl();
                                        } else {
                                            break;
                                        }
                                    }
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
                                Intent adjacentChapterIntent = ChapterActivity.constructOfflineChapterActivityIntent(mChapterView.getContext(), new RequestWrapper(mDownloadChapter.getSource(), adjacentChapterUrl), 0);

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
