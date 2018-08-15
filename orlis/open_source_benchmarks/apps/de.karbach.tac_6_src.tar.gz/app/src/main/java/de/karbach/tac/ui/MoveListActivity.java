package de.karbach.tac.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import de.karbach.tac.R;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.Move;
import de.karbach.tac.ui.fragments.MoveListFragment;

/**
 * Activity framing the movelistfragment.
 * Shows a list of moves.
 */
public class MoveListActivity extends SingleFragmentActivity {
    /**
     * Parameter for this activity which should contain the move list to show
     */
    public static final String MOVE_LIST = "de.karbach.tac.ui.move_list";

    /**
     * Parameter name for the list of colors currently set for the balls
     */
    public static final String COLOR_LIST = "de.karbach.tac.ui.color_list";

    @Override
    protected Fragment createFragment() {
        MoveListFragment movefragment = new MoveListFragment();
        List<Move> movelist = (List<Move>) getIntent().getSerializableExtra(MOVE_LIST);
        if(movelist != null) {
            for (Move m : movelist) {
                movefragment.addMove(m);
            }
        }

        List<Integer> colorlist = getIntent().getIntegerArrayListExtra(COLOR_LIST);
        if(colorlist != null){
            movefragment.setColors(colorlist);
        }

        return movefragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lastmoves);
    }
}
