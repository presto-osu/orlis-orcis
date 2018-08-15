package gq.nulldev.animeopenings.app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import gq.nulldev.animeopenings.app.R;

/**
 * Project: AnimeOpenings
 * Created: 07/03/16
 * Author: nulldev
 */
public class TutorialView extends FrameLayout {

    public static final String PREF_SHOW_CONTROLS_TUTORIAL = "prefShowControls";

    public TutorialView(Context context) {
        super(context);
        setup();
    }

    public TutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public TutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.view_tutorial, this);
    }

    public void setOnDoneListener(OnClickListener clickListener) {
        if(clickListener != null) {
            findViewById(R.id.btn_gotit).setOnClickListener(clickListener);
        }
    }
}
