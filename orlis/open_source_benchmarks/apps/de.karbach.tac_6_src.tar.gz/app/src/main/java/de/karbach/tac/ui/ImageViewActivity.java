package de.karbach.tac.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.karbach.tac.ui.fragments.ImageViewFragment;

/**
 * Created by Carsten on 12.11.2015.
 */
public class ImageViewActivity extends SingleFragmentActivity {

    /**
     * Argument name for the image path to display in this activity
     */
    public static final String IMAGEPATH = "de.karbach.tac.ui.fragments.ImageViewActivity.IMAGEPATH";

    @Override
    protected Fragment createFragment() {
        ImageViewFragment result = new ImageViewFragment();

        String imagepath = getIntent().getStringExtra(IMAGEPATH);

        Bundle args = new Bundle();
        args.putString(ImageViewFragment.IMAGEPATH, imagepath);
        result.setArguments(args);

        return result;
    }
}
