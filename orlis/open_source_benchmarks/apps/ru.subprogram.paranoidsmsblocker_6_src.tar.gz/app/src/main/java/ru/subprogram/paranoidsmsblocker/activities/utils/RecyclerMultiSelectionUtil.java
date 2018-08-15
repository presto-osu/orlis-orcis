/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.subprogram.paranoidsmsblocker.activities.utils;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ru.subprogram.paranoidsmsblocker.adapters.IAOnClickListener;
import ru.subprogram.paranoidsmsblocker.adapters.IAOnLongClickListener;
import ru.subprogram.paranoidsmsblocker.adapters.IMultiselectListAdapter;

public class RecyclerMultiSelectionUtil {
    public static Controller attachMultiSelectionController(
            final RecyclerView listView,
            final IMultiselectListAdapter adapter,
            final AppCompatActivity activity,
            final MultiChoiceModeListener listener) {
        return Controller.attach(listView, adapter, activity, listener);
    }

    public static class Controller implements
            ActionMode.Callback,
            IAOnClickListener,
            IAOnLongClickListener {
        private ActionMode mActionMode;
        private RecyclerView mListView = null;
        private IMultiselectListAdapter mAdapter = null;
        private AppCompatActivity mActivity = null;
        private MultiChoiceModeListener mListener = null;
        private IAOnClickListener mOldItemClickListener;

        private Controller() {
        }

        public static Controller attach(RecyclerView listView,
                                        IMultiselectListAdapter adapter,
                                        AppCompatActivity activity,
                                        MultiChoiceModeListener listener) {
            Controller controller = new Controller();
            controller.mListView = listView;
            controller.mActivity = activity;
            controller.mListener = listener;
            controller.mAdapter = adapter;
            adapter.setOnItemLongClickListener(controller);
            return controller;
        }

        public void finish() {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }

        private String getStateKey() {
            return RecyclerMultiSelectionUtil.class.getSimpleName() + "_" + mListView.getId();
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            if (mListener.onCreateActionMode(actionMode, menu)) {
                mActionMode = actionMode;
                mOldItemClickListener = mAdapter.getOnItemClickListener();
                mAdapter.setOnItemClickListener(Controller.this);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if (mListener.onPrepareActionMode(actionMode, menu)) {
                mActionMode = actionMode;
                return true;
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return mListener.onActionItemClicked(actionMode, menuItem);
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mListener.onDestroyActionMode(actionMode);
            mAdapter.clearSelection();
            mAdapter.setOnItemClickListener(mOldItemClickListener);
            mActionMode = null;
        }

        @Override
        public void onItemClick(View view, int pos) {
            boolean checked = mAdapter.toggleSelection(pos);
            mListener.onItemCheckedStateChanged(mActionMode, pos, 0L, checked);

            int numChecked = mAdapter.getSelectedItems().size();

            if (numChecked <= 0) {
                mActionMode.finish();
            }
        }

        @Override
        public boolean onItemLongClick(View view, int pos) {
            if (mActionMode != null) {
                return false;
            }

            mActionMode = mActivity.startSupportActionMode(Controller.this);
            onItemClick(view, pos);
            return true;
        }
    }

    /**
     * @see android.widget.AbsListView.MultiChoiceModeListener
     */
    public static interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * @see android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(
         * android.view.ActionMode, int, long, boolean)
         */
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked);
    }
}