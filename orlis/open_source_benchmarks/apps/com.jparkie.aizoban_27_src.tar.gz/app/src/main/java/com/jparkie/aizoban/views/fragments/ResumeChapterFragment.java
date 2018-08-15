package com.jparkie.aizoban.views.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.jparkie.aizoban.R;
import com.jparkie.aizoban.models.databases.RecentChapter;
import com.jparkie.aizoban.presenters.ResumeChapterPresenter;
import com.jparkie.aizoban.presenters.ResumeChapterPresenterImpl;
import com.jparkie.aizoban.views.ResumeChapterView;

public class ResumeChapterFragment extends DialogFragment implements ResumeChapterView {
    public static final String TAG = ResumeChapterFragment.class.getSimpleName();

    public static final String RECENT_CHAPTER_ARGUMENT_KEY = TAG + ":" + "RecentChapterArgumentKey";

    private ResumeChapterPresenter mResumeChapterPresenter;

    public static ResumeChapterFragment newInstance(RecentChapter recentChapter) {
        ResumeChapterFragment newInstance = new ResumeChapterFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(RECENT_CHAPTER_ARGUMENT_KEY, recentChapter);
        newInstance.setArguments(arguments);

        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResumeChapterPresenter = new ResumeChapterPresenterImpl(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater dialogInflater = getActivity().getLayoutInflater();
        View resumeChapterView = dialogInflater.inflate(R.layout.fragment_resume_chapter, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(resumeChapterView)
                .setPositiveButton(R.string.resume_chapter_dialog_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mResumeChapterPresenter.onYesButtonClick();
                    }
                })
                .setNegativeButton(R.string.resume_chapter_dialog_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mResumeChapterPresenter.onNoButtonClick();
                    }
        });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mResumeChapterPresenter.restoreState(savedInstanceState);
        } else {
            mResumeChapterPresenter.handleInitialArguments(getArguments());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mResumeChapterPresenter.saveState(outState);
    }

    // ResumeChapterView:

    @Override
    public Context getContext() {
        return getActivity();
    }
}
