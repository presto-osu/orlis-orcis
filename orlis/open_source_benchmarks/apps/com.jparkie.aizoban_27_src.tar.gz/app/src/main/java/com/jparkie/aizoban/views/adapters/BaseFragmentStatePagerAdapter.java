/*
 * Copyright (C) 2011 The Android Open Source Project
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

// Altered Accordingly:
// http://speakman.net.nz/blog/2014/02/20/a-bug-in-and-a-fix-for-the-way-fragmentstatepageradapter-handles-fragment-restoration/

package com.jparkie.aizoban.views.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class BaseFragmentStatePagerAdapter extends PagerAdapter {
    private static final String TAG = "FragmentStatePagerAdapter";

    private static final boolean DEBUG = false;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private ArrayList<String> mSavedFragmentTags = new ArrayList<String>();

    private Fragment mCurrentPrimaryItem = null;

    public BaseFragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    // Custom Tag Function.
    public String getTag(int position) {
        return null;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        Fragment fragment = getItem(position);
        String fragmentTag = getTag(position);
        if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
        if (mSavedState.size() > position) {
            String savedTag = mSavedFragmentTags.get(position);
            if (TextUtils.equals(fragmentTag, savedTag)) {
                Fragment.SavedState fss = mSavedState.get(position);
                if (fss != null) {
                    fragment.setInitialSavedState(fss);
                }
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment, fragmentTag);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment) object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
            mSavedFragmentTags.add(null);
        }
        mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        mSavedFragmentTags.set(position, fragment.getTag());
        mFragments.set(position, null);
        mCurTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
            state.putStringArrayList("tags", mSavedFragmentTags);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();

            ArrayList<String> tags = bundle.getStringArrayList("tags");
            if (tags != null) {
                mSavedFragmentTags = tags;
            } else {
                mSavedFragmentTags.clear();
            }
            if (fss != null) {
                for (int i = 0; i < fss.length; i++) {
                    mSavedState.add((Fragment.SavedState) fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }
}