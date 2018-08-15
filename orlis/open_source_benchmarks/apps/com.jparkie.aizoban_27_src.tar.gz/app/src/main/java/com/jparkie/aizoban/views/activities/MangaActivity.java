package com.jparkie.aizoban.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.MangaPresenter;
import com.jparkie.aizoban.presenters.MangaPresenterOfflineImpl;
import com.jparkie.aizoban.presenters.MangaPresenterOnlineImpl;
import com.jparkie.aizoban.presenters.mapper.MangaMapper;
import com.jparkie.aizoban.utils.PaletteBitmapTarget;
import com.jparkie.aizoban.utils.PaletteBitmapTranscoder;
import com.jparkie.aizoban.utils.PaletteUtils;
import com.jparkie.aizoban.utils.wrappers.PaletteBitmapWrapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.MangaView;
import com.melnykov.fab.FloatingActionButton;

public class MangaActivity extends BaseActivity implements MangaView, MangaMapper {
    public static final String TAG = MangaActivity.class.getSimpleName();

    public static final String PRESENTER_ARGUMENT_KEY = TAG + ":" + "PresenterArgumentKey";
    public static final String REQUEST_ARGUMENT_KEY = TAG + ":" + "RequestArgumentKey";

    private MangaPresenter mMangaPresenter;

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;

    private View mHeaderInfoView;

    private ImageView mHeaderImageView;
    private View mMaskImageView;

    private TextView mTitleAuthorTextView;
    private TextView mTitleArtistTextView;
    private TextView mTitleGenreTextView;
    private TextView mTitleStatusTextView;

    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mAuthorTextView;
    private TextView mArtistTextView;
    private TextView mGenreTextView;
    private TextView mStatusTextView;

    private FloatingActionButton mFavouriteButton;

    private RelativeLayout mHeaderChapterView;

    private TextView mChapterErrorTextView;

    public static Intent constructOnlineMangaActivityIntent(Context context, RequestWrapper mangaRequest) {
        Intent argumentIntent = new Intent(context, MangaActivity.class);
        argumentIntent.putExtra(PRESENTER_ARGUMENT_KEY, MangaPresenterOnlineImpl.TAG);
        argumentIntent.putExtra(REQUEST_ARGUMENT_KEY, mangaRequest);

        return argumentIntent;
    }

    public static Intent constructOfflineMangaActivityIntent(Context context, RequestWrapper mangaRequest) {
        Intent argumentIntent = new Intent(context, MangaActivity.class);
        argumentIntent.putExtra(PRESENTER_ARGUMENT_KEY, MangaPresenterOfflineImpl.TAG);
        argumentIntent.putExtra(REQUEST_ARGUMENT_KEY, mangaRequest);

        return argumentIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(PRESENTER_ARGUMENT_KEY)) {
                String presenterType = intent.getStringExtra(PRESENTER_ARGUMENT_KEY);

                if (presenterType.equals(MangaPresenterOnlineImpl.TAG)) {
                    mMangaPresenter = new MangaPresenterOnlineImpl(this, this);
                } else if (presenterType.equals(MangaPresenterOfflineImpl.TAG)) {
                    mMangaPresenter = new MangaPresenterOfflineImpl(this, this);
                }
            }
        }

        setContentView(R.layout.activity_manga);

        mToolbar = (Toolbar)findViewById(R.id.mainToolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        mListView = (ListView)findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout)findViewById(R.id.emptyRelativeLayout);

        mHeaderInfoView = LayoutInflater.from(this).inflate(R.layout.header_manga_info, null);

        mHeaderImageView = (ImageView)mHeaderInfoView.findViewById(R.id.headerImageView);
        mMaskImageView = mHeaderInfoView.findViewById(R.id.maskImageView);

        mTitleAuthorTextView = (TextView)mHeaderInfoView.findViewById(R.id.authorTitleTextView);
        mTitleArtistTextView = (TextView)mHeaderInfoView.findViewById(R.id.artistTitleTextView);
        mTitleGenreTextView = (TextView)mHeaderInfoView.findViewById(R.id.genreTitleTextView);
        mTitleStatusTextView = (TextView)mHeaderInfoView.findViewById(R.id.statusTitleTextView);

        mNameTextView = (TextView)mHeaderInfoView.findViewById(R.id.nameTextView);
        mDescriptionTextView = (TextView)mHeaderInfoView.findViewById(R.id.descriptionTextView);
        mAuthorTextView = (TextView)mHeaderInfoView.findViewById(R.id.authorTextView);
        mArtistTextView = (TextView)mHeaderInfoView.findViewById(R.id.artistTextView);
        mGenreTextView = (TextView)mHeaderInfoView.findViewById(R.id.genreTextView);
        mStatusTextView = (TextView)mHeaderInfoView.findViewById(R.id.statusTextView);

        mFavouriteButton = (FloatingActionButton)mHeaderInfoView.findViewById(R.id.favouriteButton);

        mHeaderChapterView = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.header_manga_chapter, null);

        mChapterErrorTextView = (TextView)mHeaderChapterView.findViewById(R.id.chapterErrorTextView);

        if (savedInstanceState != null) {
            mMangaPresenter.restoreState(savedInstanceState);
        } else {
            mMangaPresenter.handleInitialArguments(getIntent());
        }

        mMangaPresenter.initializeViews();

        mMangaPresenter.initializeDataFromUrl();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMangaPresenter.registerForEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMangaPresenter.onResume();
    }

    @Override
    protected void onStop() {
        mMangaPresenter.unregisterForEvents();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMangaPresenter.destroyAllSubscriptions();
        mMangaPresenter.releaseAllResources();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMangaPresenter.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mMangaPresenter.onOptionRefresh();
                return true;
            case R.id.action_mark_read:
                mMangaPresenter.onOptionMarkRead();
                return true;
            case R.id.action_download:
                mMangaPresenter.onOptionDownload();
                return true;
            case R.id.action_to_top:
                mMangaPresenter.onOptionToTop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // MangaView:

    @Override
    public void initializeToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.fragment_manga);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.primaryBlue500));

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void initializeSwipeRefreshLayout() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics()));
            mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accentPinkA200));
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mMangaPresenter.onSwipeRefresh();
                }
            });
        }
    }

    @Override
    public void initializeAbsListView() {
        if (mListView != null) {
            mListView.setVisibility(View.INVISIBLE);

            if (mHeaderInfoView != null) {
                mListView.addHeaderView(mHeaderInfoView, null, false);
            }
            if (mHeaderChapterView != null) {
                mListView.addHeaderView(mHeaderChapterView, null, false);
            }

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int actualPosition = position - mListView.getHeaderViewsCount();

                    mMangaPresenter.onChapterClick(actualPosition);
                }
            });
        }
    }

    @Override
    public void initializeDeletionListView() {
        if (mListView != null) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(getResources().getString(R.string.manga_offline_selection_title) + " " + mListView.getCheckedItemCount());
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle(getResources().getString(R.string.manga_offline_selection_title) + " " + mListView.getCheckedItemCount());

                    MenuInflater menuInflater = getMenuInflater();
                    menuInflater.inflate(R.menu.manga_offline_selection, menu);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    int id = item.getItemId();

                    switch (id) {
                        case R.id.action_delete:
                            mMangaPresenter.onOptionDelete();
                            mode.finish();
                            return true;
                        case R.id.action_select_all:
                            mMangaPresenter.onOptionSelectAll();
                            return false;
                        case R.id.action_clear:
                            mMangaPresenter.onOptionClear();
                            return false;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Do Nothing.
                }
            });
        }
    }

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_image_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_manga);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.manga_instructions);
        }
    }

    @Override
    public void initializeFavouriteButton(boolean isFavourite) {
        if (mFavouriteButton != null) {
            if (isFavourite) {
                mFavouriteButton.setImageResource(R.drawable.ic_favourite_white_24dp);
            } else {
                mFavouriteButton.setImageResource(R.drawable.ic_favourite_outline_white_24dp);
            }

            mFavouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMangaPresenter.onFavourite();
                }
            });
        }
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
    public void showListViewIfHidden() {
        if (mListView != null) {
            if (mListView.getVisibility() != View.VISIBLE) {
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void showChapterStatusError() {
        if (mChapterErrorTextView != null) {
            mChapterErrorTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideChapterStatusError() {
        if (mChapterErrorTextView != null) {
            mChapterErrorTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void hideRefreshing() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void setTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void setName(String name) {
        if (mNameTextView != null) {
            mNameTextView.setText(name);
        }
    }

    @Override
    public void setDescription(String description) {
        if (mDescriptionTextView != null) {
            mDescriptionTextView.setText(Html.fromHtml(description));
        }
    }

    @Override
    public void setAuthor(String author) {
        if (mAuthorTextView != null) {
            mAuthorTextView.setText(author);
        }
    }

    @Override
    public void setArtist(String artist) {
        if (mArtistTextView != null) {
            mArtistTextView.setText(artist);
        }
    }

    @Override
    public void setGenre(String genre) {
        if (mGenreTextView != null) {
            mGenreTextView.setText(genre);
        }
    }

    @Override
    public void setIsCompleted(boolean isCompleted) {
        if (mStatusTextView != null) {
            if (isCompleted) {
                mStatusTextView.setText(R.string.manga_header_status_completed);
            } else {
                mStatusTextView.setText(R.string.manga_header_status_ongoing);
            }
        }
    }

    @Override
    public void setThumbnail(String url) {
        if (mHeaderImageView != null) {
            mHeaderImageView.setScaleType(ImageView.ScaleType.CENTER);

            Drawable placeHolderDrawable = getResources().getDrawable(R.drawable.ic_image_white_48dp);
            placeHolderDrawable.setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            Drawable errorHolderDrawable = getResources().getDrawable(R.drawable.ic_error_white_48dp);
            errorHolderDrawable.setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);

            Glide.with(this)
                    .load(url)
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(), PaletteBitmapWrapper.class)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(android.R.anim.fade_in)
                    .placeholder(placeHolderDrawable)
                    .error(errorHolderDrawable)
                    .into(new PaletteBitmapTarget(mHeaderImageView) {
                        @Override
                        public void onResourceReady(PaletteBitmapWrapper resource, GlideAnimation<? super PaletteBitmapWrapper> glideAnimation) {
                            super.onResourceReady(resource, glideAnimation);

                            mHeaderImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            int color = PaletteUtils.getColorWithDefault(resource.getPalette(), getResources().getColor(R.color.primaryBlue500));

                            applyColorOverlay(color);
                        }
                    });
        }
    }

    @Override
    public void setFavouriteButton(boolean isFavourite) {
        if (mFavouriteButton != null) {
            if (isFavourite) {
                mFavouriteButton.setImageResource(R.drawable.ic_favourite_white_24dp);
            } else {
                mFavouriteButton.setImageResource(R.drawable.ic_favourite_outline_white_24dp);
            }
        }
    }

    @Override
    public int getHeaderViewsCount() {
        if (mListView != null) {
            return mListView.getHeaderViewsCount();
        } else {
            return 0;
        }
    }

    @Override
    public void scrollToTop() {
        if (mListView != null) {
            mListView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void selectAll() {
        if (mListView != null) {
            for (int index = mListView.getHeaderViewsCount(); index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, true);
            }
        }
    }

    @Override
    public void clear() {
        if (mListView != null) {
            for (int index = mListView.getHeaderViewsCount(); index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, false);
            }
        }
    }

    @Override
    public void toastMangaError() {
        Toast.makeText(this, R.string.toast_manga_error, Toast.LENGTH_SHORT).show();

    }

    @Override
    public Context getContext() {
        return this;
    }


    // MangaMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions() {
        if (mListView != null) {
            return mListView.getCheckedItemPositions();
        } else {
            return null;
        }
    }

    @Override
    public Parcelable getPositionState() {
        if (mListView != null) {
            return mListView.onSaveInstanceState();
        } else {
            return null;
        }
    }

    @Override
    public void setPositionState(Parcelable state) {
        if (mListView != null) {
            mListView.onRestoreInstanceState(state);
        }
    }

    private void applyColorOverlay(int rgbColor) {
        mMangaPresenter.onApplyColorChange(rgbColor);

        if (mToolbar != null) {
            mToolbar.setBackgroundColor(rgbColor);
        }
        if (mMaskImageView != null) {
            mMaskImageView.setBackgroundColor(rgbColor);
        }
        if (mNameTextView != null) {
            mNameTextView.setTextColor(rgbColor);
        }
        if (mTitleAuthorTextView != null) {
            mTitleAuthorTextView.setTextColor(rgbColor);
        }
        if (mTitleArtistTextView != null) {
            mTitleArtistTextView.setTextColor(rgbColor);
        }
        if (mTitleGenreTextView != null) {
            mTitleGenreTextView.setTextColor(rgbColor);
        }
        if (mTitleStatusTextView != null) {
            mTitleStatusTextView.setTextColor(rgbColor);
        }
        if (mFavouriteButton != null) {
            mFavouriteButton.setColorNormal(rgbColor);
        }
        if (mHeaderChapterView != null) {
            mHeaderChapterView.setBackgroundColor(rgbColor);
        }
        if (mListView != null) {
            mListView.invalidateViews();
        }
    }
}
