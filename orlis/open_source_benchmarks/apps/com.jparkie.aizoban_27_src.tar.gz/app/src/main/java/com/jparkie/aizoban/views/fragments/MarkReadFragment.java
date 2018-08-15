package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.MarkReadPresenter;
import com.jparkie.aizoban.presenters.MarkReadPresenterOfflineImpl;
import com.jparkie.aizoban.presenters.MarkReadPresenterOnlineImpl;
import com.jparkie.aizoban.presenters.mapper.MarkReadMapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.MarkReadView;

public class MarkReadFragment extends DialogFragment implements MarkReadView, MarkReadMapper {
    public static final String TAG = MarkReadFragment.class.getSimpleName();

    public static final String PRESENTER_ARGUMENT_KEY = TAG + ":" + "PresenterArgumentKey";
    public static final String REQUEST_ARGUMENT_KEY = TAG + ":" + "RequestArgumentKey";

    private MarkReadPresenter mMarkReadPresenter;

    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;
    private Button mToggleButton;

    public static MarkReadFragment newOnlineInstance(RequestWrapper mangaRequest) {
        MarkReadFragment newInstance = new MarkReadFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PRESENTER_ARGUMENT_KEY, MarkReadPresenterOnlineImpl.TAG);
        arguments.putParcelable(REQUEST_ARGUMENT_KEY, mangaRequest);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    public static MarkReadFragment newOfflineInstance(RequestWrapper mangaRequest) {
        MarkReadFragment newInstance = new MarkReadFragment();

        Bundle arguments = new Bundle();
        arguments.putString(PRESENTER_ARGUMENT_KEY, MarkReadPresenterOfflineImpl.TAG);
        arguments.putParcelable(REQUEST_ARGUMENT_KEY, mangaRequest);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(PRESENTER_ARGUMENT_KEY)) {
                String presenterType = arguments.getString(PRESENTER_ARGUMENT_KEY);

                if (presenterType.equals(MarkReadPresenterOnlineImpl.TAG)) {
                    mMarkReadPresenter = new MarkReadPresenterOnlineImpl(this, this);
                } else if (presenterType.equals(MarkReadPresenterOfflineImpl.TAG)) {
                    mMarkReadPresenter = new MarkReadPresenterOfflineImpl(this, this);
                }
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View addToQueueView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_mark_read, null);
        mListView = (ListView)addToQueueView.findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout)addToQueueView.findViewById(R.id.emptyRelativeLayout);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(addToQueueView)
                .setPositiveButton(R.string.mark_read_dialog_button_mark, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMarkReadPresenter.onQueueButtonClick();
                    }
                })
                .setNeutralButton(R.string.mark_read_dialog_button_toggle_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Nothing.
                    }
                })
                .setNegativeButton(R.string.mark_read_dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MarkReadFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mMarkReadPresenter.restoreState(savedInstanceState);
        } else {
            mMarkReadPresenter.handleInitialArguments(getArguments());
        }

        mMarkReadPresenter.initializeViews();

        mMarkReadPresenter.initializeDataFromDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();

        mMarkReadPresenter.overrideDialogButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mMarkReadPresenter.saveState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMarkReadPresenter.destroyAllSubscriptions();
        mMarkReadPresenter.releaseAllResources();
    }

    // MarkReadView:

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_history_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_available_chapters);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.available_chapters_instructions);
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
        if (mEmptyRelativeLayout != null) {
            mEmptyRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void overrideToggleButton() {
        AlertDialog currentDialog = (AlertDialog)getDialog();
        if (currentDialog != null) {
            mToggleButton = currentDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            mToggleButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mMarkReadPresenter.onToggleButtonClick();
                }
            });
        }
    }

    @Override
    public void selectAll() {
        if (mListView != null) {
            for (int index = 0; index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, true);
            }
        }
    }

    @Override
    public void clear() {
        if (mListView != null) {
            for (int index = 0; index < mListView.getCount(); index++) {
                mListView.setItemChecked(index, false);
            }
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }


    // MarkReadMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public int getCheckedItemCount() {
        if (mListView != null) {
            return mListView.getCheckedItemCount();
        } else {
            return 0;
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
}
