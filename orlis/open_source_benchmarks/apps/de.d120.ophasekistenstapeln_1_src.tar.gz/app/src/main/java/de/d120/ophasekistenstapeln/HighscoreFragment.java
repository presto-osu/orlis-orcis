/**
 *   This file is part of Ophasenkistenstapeln.
 *
 *   Ophasenkistenstapeln is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Ophasenkistenstapeln is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Ophasenkistenstapeln.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.d120.ophasekistenstapeln;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import de.d120.ophasekistenstapeln.HighscoreEditDialog.HighscoreEditDialogListener;
import de.d120.ophasekistenstapeln.highscore.Highscore;
import de.d120.ophasekistenstapeln.highscore.HighscoreEntry;
import de.d120.ophasekistenstapeln.tower.Tower;

/**
 * @author saibot2013
 */
public class HighscoreFragment extends Fragment implements
        HighscoreEditDialogListener {

    private ListView listViewHighScore;

    private MainActivity mActivity;
    private Highscore highscore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_highscore,
                container, false);
        refreshUI();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		/*
         * =========================================================== Get View
		 * references
		 * ===========================================================
		 */

        this.listViewHighScore = (ListView) getView().findViewById(
                R.id.listViewHighScore);
        refreshUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.highscore, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.highscoreClear:
                Toast.makeText(getActivity(), "Highscore gelöscht",
                        Toast.LENGTH_LONG).show();
                this.clear();
                this.refreshUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        // set parent activity
        mActivity = (MainActivity) a;
        this.highscore = mActivity.getHighscore();
        refreshUI();
    }

    /**
     * Refreshs the UI. Inserts all {@link HighscoreEntry} from this.highscore
     * in the listview.
     */
    private void refreshUI() {
        if (listViewHighScore == null || this.highscore == null) {
            return;
        }

        String[] values = new String[this.highscore.getEntries().size()];
        int i = 0;
        for (HighscoreEntry he : this.highscore.getEntries()) {
            if (he == null || !(he instanceof HighscoreEntry)) {
                continue;
            }
            values[i++] = "Platz " + (i) + ": Gruppe " + he.getGroupId()
                    + " mit " + he.getScore() + " Punkten";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // Assign adapter to ListView
        listViewHighScore.setAdapter(adapter);

        // ListView Item Click Listener
        listViewHighScore.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO display data about specific tower
                HighscoreEntry he = highscore.getEntry(position);
                if (he == null) {
                    Toast.makeText(getActivity(), R.string.impossible,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                showEditDialog(he.getTower());
                // ListView Clicked item index
                // int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listViewHighScore
                        .getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getActivity(),
				/*
				 * "Position :" + itemPosition + "  ListItem : " +
				 */itemValue, Toast.LENGTH_LONG).show();

            }

        });
    }

    private void showEditDialog(Tower t) {
        FragmentManager fm = getFragmentManager();
        HighscoreEditDialog editNameDialog = new HighscoreEditDialog();
        editNameDialog.setTower(t);
        // TODO editNameDialog.setTargetFragment(this, 0);
        editNameDialog.show(fm, "fragment_highscore_edit");
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        Toast.makeText(getActivity(), "Hi, " + inputText, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Clears the current highscore
     */
    private void clear() {
        this.highscore.clear();
    }

}
