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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.d120.ophasekistenstapeln.tower.Box;
import de.d120.ophasekistenstapeln.tower.Tower;

/**
 * This dialog represents a fragement on which user could edit the
 * highscore-entity
 *
 * @author saibot2013
 */
public class HighscoreEditDialog extends DialogFragment {
    private Tower tower;
    private ListView listViewTower;

    /**
     * This interface has to be used if your class should be informed on finish
     * event
     *
     * @author saibot2013
     */
    public interface HighscoreEditDialogListener {
        void onFinishEditDialog(String inputText);
    }

    public HighscoreEditDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tower_edit, container);
        /*
		 * =========================================================== Get View
		 * references
		 * ===========================================================
		 */

        this.listViewTower = (ListView) view.findViewById(R.id.listViewTower);
        refreshUI();

        return view;
    }

    /**
     * Sets the tower which we should display in the dialog
     *
     * @param t
     */
    public void setTower(Tower t) {
        this.tower = t;
        // refreshUI();
    }

    /**
     * Refreshs the user interface, e.g. displays the tower name and all boxes
     */
    private void refreshUI() {
        if (this.tower == null) {
            return;
        }
        getDialog().setTitle(this.tower.getName());

        String[] values = new String[this.tower.getBoxes().size()];
        int i = 0;

        for (Box box : this.tower.getBoxes()) {
            if (box == null) {
                continue;
            }
            values[i++] = "Stockwerk " + (i) + ": Box-Wertigkeit: "
                    + box.getValue();
        }
        List<String> rlist = Arrays.asList(values);
        Collections.reverse(rlist);
        values = (String[]) rlist.toArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // Assign adapter to ListView
        listViewTower.setAdapter(adapter);

        // ListView Item Click Listener
        listViewTower.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO display data about specific tower

                String itemValue = (String) listViewTower
                        .getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getActivity(), itemValue, Toast.LENGTH_LONG)
                        .show();
            }

        });
    }

}
