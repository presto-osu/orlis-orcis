package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.presenters.SelectPagePresenter;
import com.jparkie.aizoban.presenters.SelectPagePresenterImpl;
import com.jparkie.aizoban.views.SelectPageView;

public class SelectPageFragment extends DialogFragment implements SelectPageView {
    public static final String TAG = SelectPageFragment.class.getSimpleName();

    public static final String CURRENT_PAGE_ARGUMENT_KEY = TAG + ":" + "CurrentPageArgumentKey";
    public static final String PAGE_SIZE_ARGUMENT_KEY = TAG + ":" + "PageSizeArgumentKey";

    private SelectPagePresenter mSelectPagePresenter;

    private Spinner mSpinner;

    public static SelectPageFragment newInstance(int currentPage, int pageSize) {
        SelectPageFragment newInstance = new SelectPageFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(CURRENT_PAGE_ARGUMENT_KEY, currentPage);
        arguments.putInt(PAGE_SIZE_ARGUMENT_KEY, pageSize);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectPagePresenter = new SelectPagePresenterImpl(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater dialogInflater = getActivity().getLayoutInflater();
        View selectPageView = dialogInflater.inflate(R.layout.fragment_select_page, null);
        mSpinner = (Spinner)selectPageView.findViewById(R.id.pageSpinner);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(selectPageView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectPagePresenter.onOkButtonClick();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectPagePresenter.onCancelButtonClick();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectPagePresenter.restoreState(savedInstanceState);
        } else {
            mSelectPagePresenter.handleInitialArguments(getArguments());
        }

        mSelectPagePresenter.initializeViews();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mSelectPagePresenter.saveState(outState);
    }

    // SelectPageView:

    @Override
    public void initializeSpinner(BaseAdapter adapter) {
        if (mSpinner != null) {
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSelectPagePresenter.onItemSelected(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do Nothing.
                }
            });
        }
    }

    @Override
    public void setSpinnerPageNumber(int pageNumber) {
        if (mSpinner != null) {
            mSpinner.setSelection(pageNumber);
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
