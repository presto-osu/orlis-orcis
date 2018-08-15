package info.staticfree.SuperGenPass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;

import java.security.NoSuchAlgorithmException;

public class VisualHashEditText extends EditText {
    private boolean mShowVisualHash;
    @NonNull
    private final VisualHash mVh;

    public VisualHashEditText(@NonNull final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);

        try {
            mVh = new VisualHash();
        } catch (@NonNull final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        init(context, attrs);
    }

    public VisualHashEditText(@NonNull final Context context, final AttributeSet attrs) {
        super(context, attrs);

        try {
            mVh = new VisualHash();
        } catch (@NonNull final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        init(context, attrs);
    }

    public VisualHashEditText(@NonNull final Context context) {
        super(context);

        try {
            mVh = new VisualHash();
        } catch (@NonNull final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        init(context, null);
    }

    private void init(@NonNull final Context context, final AttributeSet attrs) {
        final int h, w;

        if (isInEditMode()) {
            h = 45;
            w = 45;
        } else {
            final TypedArray ta =
                    context.obtainStyledAttributes(attrs, R.styleable.VisualHashEditText);

            mShowVisualHash = ta.getBoolean(R.styleable.VisualHashEditText_showVisualHash, true);

            h = ta.getDimensionPixelSize(R.styleable.VisualHashEditText_visualHashHeight, 45);
            w = ta.getDimensionPixelSize(R.styleable.VisualHashEditText_visualHashWidth, 45);

            ta.recycle();
        }

        // this number is based on what looks good with standard edit boxes.
        mVh.setBounds(0, 0, h, w);

        refreshVisualHash();
    }

    public void setShowVisualHash(final boolean showVisualHash) {
        mShowVisualHash = showVisualHash;
        refreshVisualHash();
    }

    private void refreshVisualHash() {
        if (mShowVisualHash) {
            setCompoundDrawables(null, null, mVh, null);
        } else {
            setCompoundDrawables(null, null, null, null);
        }
    }

    @Override
    protected void onTextChanged(@Nullable final CharSequence text, final int start,
            final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if (text != null && mVh != null) {
            mVh.setData(text.toString().getBytes());
        }
    }

    @Override
    public void setError(@Nullable final CharSequence error, final Drawable icon) {
        super.setError(error, icon);

        if (error == null) {
            refreshVisualHash();
        }
    }
}

