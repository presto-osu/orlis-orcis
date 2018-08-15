package info.staticfree.SuperGenPass;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class GeneratedPasswordView extends TextView
        implements OnClickListener, OnMenuItemClickListener {
    public static final int MENU_ID_COPY = android.R.id.copy;

    private OnClickListener mOnClickListener;
    private CharSequence domain;

    public GeneratedPasswordView(final Context context) {
        this(context, null);
    }

    public GeneratedPasswordView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.generatedPasswordViewStyle);
    }

    public GeneratedPasswordView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);

        super.setOnClickListener(this);

        setKeyListener(new NumberKeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_NULL;
            }

            @NonNull
            @Override
            protected char[] getAcceptedChars() {
                return new char[] {};
            }
        });
    }

    @Override
    public void setOnClickListener(final OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(final View v) {
        Log.d("gpwv", "click!");

        // propagate the click
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    @Override
    protected void onCreateContextMenu(@NonNull final ContextMenu menu) {
        menu.add(Menu.NONE, MENU_ID_COPY, Menu.NONE, android.R.string.copy)
                .setOnMenuItemClickListener(this);
        menu.setHeaderTitle(R.string.generated_password);
    }

    @Override
    public boolean onTextContextMenuItem(final int id) {
        switch (id) {
            case MENU_ID_COPY:
                copyToClipboard();
                return true;

            default:
                return super.onTextContextMenuItem(id);
        }
    }

    /**
     * Sets the domain name that will be displayed when copying to clipboard.
     *
     * @param domainName the domain to show in the Toast
     */
    public void setDomainName(final CharSequence domainName) {
        domain = domainName;
    }

    @Override
    public void setText(@Nullable final CharSequence text, final BufferType type) {
        super.setText(text, type);
        setEnabled(text != null ? text.length() > 0 : false);
    }

    public void copyToClipboard() {
        final CharSequence genPw = getText();
        if (genPw == null) {
            return;
        }

        final ClipboardManager clipMan =
                (ClipboardManager) getContext().getSystemService(Application.CLIPBOARD_SERVICE);
        clipMan.setPrimaryClip(ClipData.newPlainText(getContext().getText(
                R.string.generated_password), genPw));
        if (genPw.length() > 0) {
            if (domain != null) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toast_copied, domain), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toast_copied_no_domain),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setHidePassword(final boolean hidePassword) {
        if (hidePassword) {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            setTransformationMethod(null);
        }
    }

    public boolean getHidePassword() {
        return getTransformationMethod() != null;
    }

    @Override
    public boolean onMenuItemClick(@NonNull final MenuItem item) {
        return onTextContextMenuItem(item.getItemId());
    }


    /* (for all the state-related code below)
     *
     * Copyright (C) 2006 The Android Open Source Project
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

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.text != null) {
            setText(ss.text);
        }
    }

    @NonNull
    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState ss = new SavedState(superState);
        ss.text = getText();

        return ss;
    }

    public static class SavedState extends BaseSavedState {

        CharSequence text;

        public SavedState(final Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            super.writeToParcel(dest, flags);

            TextUtils.writeToParcel(text, dest, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @NonNull
                    @Override
                    public SavedState createFromParcel(final Parcel in) {
                        return new SavedState(in);
                    }

                    @NonNull
                    @Override
                    public SavedState[] newArray(final int size) {
                        return new SavedState[size];
                    }
                };

        private SavedState(final Parcel in) {
            super(in);
            text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        }
    }
    /* end Copyright (C) 2006 The Android Open Source Project */
}
