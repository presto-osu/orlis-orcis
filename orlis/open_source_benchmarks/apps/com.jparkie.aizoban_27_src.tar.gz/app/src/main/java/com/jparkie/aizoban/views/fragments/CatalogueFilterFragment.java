package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.controllers.factories.DefaultFactory;
import com.jparkie.aizoban.presenters.CatalogueFilterPresenter;
import com.jparkie.aizoban.presenters.CatalogueFilterPresenterImpl;
import com.jparkie.aizoban.presenters.mapper.CatalogueFilterMapper;
import com.jparkie.aizoban.utils.SearchUtils;
import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;
import com.jparkie.aizoban.views.CatalogueFilterView;
import com.jparkie.aizoban.views.adapters.CatalogueFilterAdapter;

import java.util.ArrayList;
import java.util.List;

public class CatalogueFilterFragment extends DialogFragment implements CatalogueFilterView, CatalogueFilterMapper {
    public static final String TAG = CatalogueFilterFragment.class.getSimpleName();

    private CatalogueFilterPresenter mCatalogueFilterPresenter;

    private TextView mGenreTextView;
    private GridView mGenreGridView;
    private RadioButton mAllRadioButton;
    private RadioButton mCompletedRadioButton;
    private RadioButton mOngoingRadioButton;
    private RadioButton mNameRadioButton;
    private RadioButton mRankRadioButton;

    public static CatalogueFilterFragment newInstance(SearchCatalogueWrapper searchCatalogueWrapper) {
        CatalogueFilterFragment newInstance = new CatalogueFilterFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(SearchCatalogueWrapper.PARCELABLE_KEY, searchCatalogueWrapper);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCatalogueFilterPresenter = new CatalogueFilterPresenterImpl(this, this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View catalogueFilterView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_catalogue_filter, null);
        mGenreTextView = (TextView) catalogueFilterView.findViewById(R.id.genreTextView);
        mGenreGridView = (GridView) catalogueFilterView.findViewById(R.id.genreGridView);
        mAllRadioButton = (RadioButton) catalogueFilterView.findViewById(R.id.radioAll);
        mCompletedRadioButton = (RadioButton) catalogueFilterView.findViewById(R.id.radioCompleted);
        mOngoingRadioButton = (RadioButton) catalogueFilterView.findViewById(R.id.radioOngoing);
        mNameRadioButton = (RadioButton) catalogueFilterView.findViewById(R.id.radioName);
        mRankRadioButton = (RadioButton) catalogueFilterView.findViewById(R.id.radioRank);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(catalogueFilterView)
                .setPositiveButton(R.string.catalogue_filter_dialog_button_filter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCatalogueFilterPresenter.onFilterButtonClick();
                    }
                })
                .setNeutralButton(R.string.catalogue_filter_dialog_button_clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do Notihng.
                    }
                })
                .setNegativeButton(R.string.catalogue_filter_dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CatalogueFilterFragment.this.getDialog().cancel();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCatalogueFilterPresenter.restoreState(savedInstanceState);
        } else {
            mCatalogueFilterPresenter.handleInitialArguments(getArguments());
        }

        mCatalogueFilterPresenter.initializeDataFromPreferenceSource();
    }

    @Override
    public void onStart() {
        super.onStart();

        mCatalogueFilterPresenter.overrideDialogButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCatalogueFilterPresenter.saveState(outState);
    }

    // CatalogueFilterView:

    @Override
    public void overrideClearButton() {
        AlertDialog currentDialog = (AlertDialog) getDialog();
        if (currentDialog != null) {
            Button clearButton = currentDialog.getButton(Dialog.BUTTON_NEUTRAL);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCatalogueFilterPresenter.onClearButtonClick();
                }
            });
        }
    }

    @Override
    public void hideGenres() {
        if (mGenreTextView != null && mGenreGridView != null) {
            mGenreTextView.setVisibility(View.GONE);
            mGenreGridView.setVisibility(View.GONE);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // CatalogueFilterMapper:

    @Override
    public void registerAdapter(BaseAdapter adapter) {
        if (mGenreGridView != null) {
            mGenreGridView.setAdapter(adapter);
        }
    }

    @Override
    public List<String> getSelectedGenres() {
        if (mGenreGridView != null) {
            return ((CatalogueFilterAdapter) mGenreGridView.getAdapter()).getSelectedGenres();
        }

        return new ArrayList<String>();
    }

    @Override
    public void setSelectedGenres(List<String> selectedGenres) {
        if (mGenreGridView != null) {
            ((CatalogueFilterAdapter) mGenreGridView.getAdapter()).setSelectedGenres(selectedGenres);
        }
    }

    @Override
    public String getSelectedStatus() {
        if (mAllRadioButton != null && mAllRadioButton.isChecked()) {
            return SearchUtils.STATUS_ALL;
        }
        if (mCompletedRadioButton != null && mCompletedRadioButton.isChecked()) {
            return SearchUtils.STATUS_COMPLETED;
        }
        if (mOngoingRadioButton != null && mOngoingRadioButton.isChecked()) {
            return SearchUtils.STATUS_ONGOING;
        }

        return DefaultFactory.SearchCatalogueWrapper.DEFAULT_STATUS;
    }

    @Override
    public void setSelectedStatus(String selectedStatus) {
        if (selectedStatus.equals(SearchUtils.STATUS_ALL)) {
            if (mAllRadioButton != null) {
                mAllRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedStatus.equals(SearchUtils.STATUS_COMPLETED)) {
            if (mCompletedRadioButton != null) {
                mCompletedRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedStatus.equals(SearchUtils.STATUS_ONGOING)) {
            if (mOngoingRadioButton != null) {
                mOngoingRadioButton.setChecked(true);
            }

            return;
        }
    }

    @Override
    public String getSelectedOrderBy() {
        if (mNameRadioButton != null && mNameRadioButton.isChecked()) {
            return SearchUtils.ORDER_BY_NAME;
        }
        if (mRankRadioButton != null && mRankRadioButton.isChecked()) {
            return SearchUtils.ORDER_BY_RANK;
        }

        return DefaultFactory.SearchCatalogueWrapper.DEFAULT_ORDER_BY;
    }

    @Override
    public void setSelectedOrderBy(String selectedOrderBy) {
        if (selectedOrderBy.equals(SearchUtils.ORDER_BY_NAME)) {
            if (mNameRadioButton != null) {
                mNameRadioButton.setChecked(true);
            }

            return;
        }
        if (selectedOrderBy.equals(SearchUtils.ORDER_BY_RANK)) {
            if (mRankRadioButton != null) {
                mRankRadioButton.setChecked(true);
            }

            return;
        }
    }

    @Override
    public Parcelable getPositionState() {
        if (mGenreGridView != null) {
            return mGenreGridView.onSaveInstanceState();
        } else {
            return null;
        }
    }

    @Override
    public void setPositionState(Parcelable state) {
        if (mGenreGridView != null) {
            mGenreGridView.onRestoreInstanceState(state);
        }
    }
}
