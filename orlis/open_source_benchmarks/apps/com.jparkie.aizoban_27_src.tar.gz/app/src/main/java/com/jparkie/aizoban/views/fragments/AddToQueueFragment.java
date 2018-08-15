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
import com.jparkie.aizoban.presenters.AddToQueuePresenter;
import com.jparkie.aizoban.presenters.AddToQueuePresenterImpl;
import com.jparkie.aizoban.presenters.mapper.AddToQueueMapper;
import com.jparkie.aizoban.utils.wrappers.RequestWrapper;
import com.jparkie.aizoban.views.AddToQueueView;

public class AddToQueueFragment extends DialogFragment implements AddToQueueView, AddToQueueMapper {
    public static final String TAG = AddToQueueFragment.class.getSimpleName();

    public static final String REQUEST_ARGUMENT_KEY = TAG + ":" + "RequestArgumentKey";

    private AddToQueuePresenter mAddToQueuePresenter;

    private ListView mListView;
    private RelativeLayout mEmptyRelativeLayout;
    private Button mToggleButton;

    public static AddToQueueFragment newInstance(RequestWrapper mangaRequest) {
        AddToQueueFragment newInstance = new AddToQueueFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(REQUEST_ARGUMENT_KEY, mangaRequest);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAddToQueuePresenter = new AddToQueuePresenterImpl(this, this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View addToQueueView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_add_to_queue, null);
        mListView = (ListView)addToQueueView.findViewById(R.id.listView);
        mEmptyRelativeLayout = (RelativeLayout)addToQueueView.findViewById(R.id.emptyRelativeLayout);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(addToQueueView)
                .setPositiveButton(R.string.add_to_queue_dialog_button_queue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAddToQueuePresenter.onQueueButtonClick();
                    }
                })
                .setNeutralButton(R.string.add_to_queue_dialog_button_toggle_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Nothing.
                    }
                })
                .setNegativeButton(R.string.catalogue_filter_dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddToQueueFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mAddToQueuePresenter.restoreState(savedInstanceState);
        } else {
            mAddToQueuePresenter.handleInitialArguments(getArguments());
        }

        mAddToQueuePresenter.initializeViews();

        mAddToQueuePresenter.initializeDataFromDatabase();
    }

    @Override
    public void onStart() {
        super.onStart();

        mAddToQueuePresenter.overrideDialogButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mAddToQueuePresenter.saveState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAddToQueuePresenter.destroyAllSubscriptions();
        mAddToQueuePresenter.releaseAllResources();
    }

    // AddToQueueView:

    @Override
    public void initializeEmptyRelativeLayout() {
        if (mEmptyRelativeLayout != null) {
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setImageResource(R.drawable.ic_file_download_white_48dp);
            ((ImageView) mEmptyRelativeLayout.findViewById(R.id.emptyImageView)).setColorFilter(getResources().getColor(R.color.accentPinkA200), PorterDuff.Mode.MULTIPLY);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.emptyTextView)).setText(R.string.no_available_downloads);
            ((TextView) mEmptyRelativeLayout.findViewById(R.id.instructionsTextView)).setText(R.string.available_downloads_instructions);
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
                    mAddToQueuePresenter.onToggleButtonClick();
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


    // AddToQueueMapper:

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
