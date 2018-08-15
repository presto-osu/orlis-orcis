package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import com.jparkie.aizoban.BuildConfig;
import com.jparkie.aizoban.controllers.QueryManager;
import com.jparkie.aizoban.controllers.downloads.DownloadService;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.downloads.DownloadChapter;
import com.jparkie.aizoban.presenters.mapper.AddToQueueMapper;
import com.jparkie.aizoban.utils.wrappers.DownloadChapterFilteringCursorWrapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.AddToQueueView;
import com.jparkie.aizoban.views.adapters.AddToQueueAdapter;
import com.jparkie.aizoban.views.fragments.AddToQueueFragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class AddToQueuePresenterImpl implements AddToQueuePresenter {
    public static final String TAG = AddToQueuePresenterImpl.class.getSimpleName();

    private static final String REQUEST_PARCELABLE_KEY = TAG + ":" + "RequestParcelableKey";

    private static final String POSITION_PARCELABLE_KEY = TAG + ":" + "PositionParcelableKey";

    private AddToQueueView mAddToQueueView;
    private AddToQueueMapper mAddToQueueMapper;
    private AddToQueueAdapter mAddToQueueAdapter;

    private RequestWrapper mRequest;

    private Parcelable mPositionSavedState;

    private Subscription mQueryBothChaptersSubscription;

    public AddToQueuePresenterImpl(AddToQueueView addToQueueView, AddToQueueMapper addToQueueMapper) {
        mAddToQueueView = addToQueueView;
        mAddToQueueMapper = addToQueueMapper;
    }

    @Override
    public void handleInitialArguments(Bundle arguments) {
        if (arguments != null) {
            if (arguments.containsKey(AddToQueueFragment.REQUEST_ARGUMENT_KEY)) {
                mRequest = arguments.getParcelable(AddToQueueFragment.REQUEST_ARGUMENT_KEY);

                arguments.remove(AddToQueueFragment.REQUEST_ARGUMENT_KEY);
            }
        }
    }

    @Override
    public void initializeViews() {
        mAddToQueueView.initializeEmptyRelativeLayout();
    }

    @Override
    public void initializeDataFromDatabase() {
        mAddToQueueAdapter = new AddToQueueAdapter(mAddToQueueView.getContext());

        mAddToQueueMapper.registerAdapter(mAddToQueueAdapter);

        queryAvailableDownloadsFromDatabase();
    }

    @Override
    public void overrideDialogButtons() {
        mAddToQueueView.overrideToggleButton();
    }

    @Override
    public void saveState(Bundle outState) {
        if (mRequest != null) {
            outState.putParcelable(REQUEST_PARCELABLE_KEY, mRequest);
        }
        if (mAddToQueueMapper.getPositionState() != null) {
            outState.putParcelable(POSITION_PARCELABLE_KEY, mAddToQueueMapper.getPositionState());
        }
    }

    @Override
    public void restoreState(Bundle savedState) {
        if (savedState.containsKey(REQUEST_PARCELABLE_KEY)) {
            mRequest = savedState.getParcelable(REQUEST_PARCELABLE_KEY);

            savedState.remove(REQUEST_PARCELABLE_KEY);
        }
        if (savedState.containsKey(POSITION_PARCELABLE_KEY)) {
            mPositionSavedState = savedState.getParcelable(POSITION_PARCELABLE_KEY);

            savedState.remove(POSITION_PARCELABLE_KEY);
        }
    }

    @Override
    public void destroyAllSubscriptions() {
        if (mQueryBothChaptersSubscription != null) {
            mQueryBothChaptersSubscription.unsubscribe();
            mQueryBothChaptersSubscription = null;
        }
    }

    @Override
    public void releaseAllResources() {
        if (mAddToQueueAdapter != null) {
            mAddToQueueAdapter.setCursor(null);
            mAddToQueueAdapter = null;
        }
    }

    @Override
    public void onQueueButtonClick() {
        if (mAddToQueueAdapter != null) {
            ArrayList<Chapter> chaptersToDownload = new ArrayList<Chapter>();

            SparseBooleanArray checkedItems = mAddToQueueMapper.getCheckedItemPositions();
            for (int index = 0; index < mAddToQueueAdapter.getCount(); index++) {
                if (checkedItems.get(index)) {
                    Chapter chapter = (Chapter)mAddToQueueAdapter.getItem(index);
                    if (chapter != null) {
                        chaptersToDownload.add(chapter);
                    }
                }
            }

            if (chaptersToDownload.size() != 0) {
                Intent startService = new Intent(mAddToQueueView.getContext(), DownloadService.class);
                startService.putExtra(DownloadService.INTENT_QUEUE_DOWNLOAD, chaptersToDownload);
                mAddToQueueView.getContext().startService(startService);
            }
        }
    }

    @Override
    public void onToggleButtonClick() {
        if (mAddToQueueMapper.getCheckedItemCount() == 0) {
            mAddToQueueView.selectAll();
        } else {
            mAddToQueueView.clear();
        }
    }

    private void queryAvailableDownloadsFromDatabase() {
        if (mQueryBothChaptersSubscription != null) {
            mQueryBothChaptersSubscription.unsubscribe();
            mQueryBothChaptersSubscription = null;
        }

        if (mRequest != null) {
            Observable<Cursor> queryChaptersFromUrlObservable = QueryManager
                    .queryChaptersOfMangaFromRequest(mRequest, true);
            Observable<List<String>> queryDownloadChaptersFromUrlObservable = QueryManager
                    .queryDownloadChaptersOfDownloadManga(mRequest, false)
                    .flatMap(new Func1<Cursor, Observable<DownloadChapter>>() {
                        @Override
                        public Observable<DownloadChapter> call(Cursor downloadChapterCursor) {
                            List<DownloadChapter> downloadChapters = QueryManager.toList(downloadChapterCursor, DownloadChapter.class);
                            return Observable.from(downloadChapters.toArray(new DownloadChapter[downloadChapters.size()]));
                        }
                    })
                    .flatMap(new Func1<DownloadChapter, Observable<String>>() {
                        @Override
                        public Observable<String> call(DownloadChapter downloadChapter) {
                            return Observable.just(downloadChapter.getUrl());
                        }
                    })
                    .toList();

            mQueryBothChaptersSubscription = Observable.zip(queryChaptersFromUrlObservable, queryDownloadChaptersFromUrlObservable,
                    new Func2<Cursor, List<String>, Cursor>() {
                        @Override
                        public Cursor call(Cursor chaptersCursor, List<String> downloadChapterUrls) {
                            return new DownloadChapterFilteringCursorWrapper(chaptersCursor, downloadChapterUrls);
                        }
                    })
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
                            restorePosition();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNext(Cursor filteredCursor) {
                            if (mAddToQueueAdapter != null) {
                                mAddToQueueAdapter.setCursor(filteredCursor);
                            }

                            if (filteredCursor != null) {
                                mAddToQueueView.hideEmptyRelativeLayout();
                            } else {
                                mAddToQueueView.showEmptyRelativeLayout();
                            }
                        }
                    });
        }
    }

    private void restorePosition() {
        if (mPositionSavedState != null) {
            mAddToQueueMapper.setPositionState(mPositionSavedState);

            mPositionSavedState = null;
        }
    }
}
