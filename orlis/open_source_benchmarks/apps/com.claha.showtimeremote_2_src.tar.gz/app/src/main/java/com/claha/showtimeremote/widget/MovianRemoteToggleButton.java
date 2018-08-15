package com.claha.showtimeremote.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.claha.showtimeremote.R;

public class MovianRemoteToggleButton extends MovianRemoteButton {

    private Drawable src;
    private Drawable src2;

    public MovianRemoteToggleButton(Context context) {
        super(context);
    }

    public MovianRemoteToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
    }

    public MovianRemoteToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MovianRemoteToggleButton, 0, 0);
        try {
            src2 = a.getDrawable(R.styleable.MovianRemoteToggleButton_src2);
            src = getDrawable();
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (getDrawable() == src) {
            setImageDrawable(src2);
        } else {
            setImageDrawable(src);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.toggled = (getDrawable() == src2);
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.toggled) {
            setImageDrawable(src2);
        }
    }

    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public boolean toggled;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            toggled = (in.readInt() == 1);
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(toggled ? 1 : 0);
        }
    }
}
