package de.karbach.tac.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.List;

import de.karbach.tac.R;
import de.karbach.tac.core.Move;
import de.karbach.tac.ui.fragments.ExportedImagesFragment;
import de.karbach.tac.ui.fragments.MoveListFragment;

/**
 * Activity framing the movelistfragment.
 * Shows a list of moves.
 */
public class ExportedImagesActivity extends SingleFragmentActivity {
    /**
     * Parameter for this activity which should contain the list of files, which should be displayed
     */
    public static final String FILE_LIST = "de.karbach.tac.ui.file_list";

    @Override
    protected Fragment createFragment() {
        ExportedImagesFragment imagefragment = new ExportedImagesFragment();
        List<String> filelist = getIntent().getStringArrayListExtra(FILE_LIST);
        if(filelist != null) {
            imagefragment.setFiles(filelist);
        }
        return imagefragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.exported_games_title));
    }
}
