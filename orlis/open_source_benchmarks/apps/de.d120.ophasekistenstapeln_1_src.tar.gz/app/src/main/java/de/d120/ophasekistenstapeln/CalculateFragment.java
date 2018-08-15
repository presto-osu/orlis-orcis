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
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.d120.ophasekistenstapeln.highscore.Highscore;
import de.d120.ophasekistenstapeln.highscore.HighscoreEntry;
import de.d120.ophasekistenstapeln.tower.Box;
import de.d120.ophasekistenstapeln.tower.Tower;

/**
 * The fragment, which shows the calculation-interface to user.
 * @author exploide
 * @author bhaettasch
 */
public class CalculateFragment extends Fragment {
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button05;

    Button buttonReset;
    Button buttonDeleteLast;

    Button buttonSubmitHighscore;

    TextView txtFloors;
    TextView txtPoints;
    TextView txtHistory;

    EditText txtGroupName;

    private Highscore highscore;
    private Tower tower;
    private MainActivity mActivity;

    /**
     * The constructor
     *
     * @param highscore gets the current Highscore-Instance
     */
    public CalculateFragment() {
        this.tower = new Tower();
        this.tower.setName("Turm");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_control, container,
                false);
        return rootView;
    }

    /**
     * Handle input of a new button
     *
     * @param value Value of the button
     */
    public void handleNumButton(float value) {
        this.tower.add(new Box(value));
        refreshUI();
    }

    /**
     * Refresh UI (refresh shown values)
     */
    public void refreshUI() {
        txtFloors.setText(Integer.toString(this.tower.getNumFloors()));
        txtPoints.setText(Float.toString(this.tower.getValue()));
        txtHistory.setText(this.tower.toString());
    }

    /**
     * Reset points and floors
     */
    public void reset() {
        this.txtGroupName.setText("");
        this.tower = new Tower();
        this.tower.setName("Turm");
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.txtGroupName.getWindowToken(), 0);
        refreshUI();
    }

    /**
     * Revert last input
     */
    public void deleteLast() {
        if (this.tower.deleteLastBox()) {
            refreshUI();
        } else {
            Toast.makeText(getActivity(),
                    (String) getString(R.string.impossible), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void showEditDialog(Tower t) {
        FragmentManager fm = getFragmentManager();
        HighscoreEditDialog editNameDialog = new HighscoreEditDialog();
        editNameDialog.setTower(t);
        editNameDialog.show(fm, "fragment_highscore_edit");
    }

    /**
     * Adds the current groups score to highscore list. Using this.points and
     * the groupid from text-field
     */
    public void submitHighscore() {
        int groupId;
        // Get GroupId
        try {
            groupId = Integer.parseInt(this.txtGroupName.getText().toString());
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.impossible,
                    Toast.LENGTH_LONG).show();
            return;
        }
        // check if groupId exists in Highscore
        for (HighscoreEntry he : this.highscore.getEntries()) {
            if (he == null) {
                continue;
            }
            if (he.getGroupId() == groupId) {
                Toast.makeText(getActivity(),
                        (String) getString(R.string.highscoreGroupExists),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
        this.tower.setName("Gruppe " + groupId);
        // Save Entry
        HighscoreEntry he = new HighscoreEntry(groupId, this.tower);
        this.highscore.add(he);
        // Notify user
        Toast.makeText(getActivity(),
                (String) getString(R.string.highscoreSaved), Toast.LENGTH_LONG)
                .show();
        // Reset UI
        this.reset();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		/*
         * =========================================================== Get View
		 * references
		 * ===========================================================
		 */

        button1 = (Button) getView().findViewById(R.id.btnPoints1);
        button2 = (Button) getView().findViewById(R.id.btnPoints2);
        button3 = (Button) getView().findViewById(R.id.btnPoints3);
        button4 = (Button) getView().findViewById(R.id.btnPoints4);
        button5 = (Button) getView().findViewById(R.id.btnPoints5);
        button6 = (Button) getView().findViewById(R.id.btnPoints6);
        button05 = (Button) getView().findViewById(R.id.btnPoints05);

        buttonReset = (Button) getView().findViewById(R.id.btnReset);
        buttonDeleteLast = (Button) getView().findViewById(R.id.btnDeleteLast);

        buttonSubmitHighscore = (Button) getView().findViewById(
                R.id.btnSubmitHighscore);

        txtFloors = (TextView) getView().findViewById(R.id.txtFloors);
        txtPoints = (TextView) getView().findViewById(R.id.txtPoints);
        txtHistory = (TextView) getView().findViewById(R.id.txtHistory);

        txtGroupName = (EditText) getView().findViewById(R.id.txtGroupName);

		/*
		 * =========================================================== Register
		 * View callbacks
		 * ===========================================================
		 */

        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(1f);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(2f);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(3f);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(4f);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(5f);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(6f);
            }
        });

        button05.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleNumButton(0.5f);
            }
        });

        buttonDeleteLast.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteLast();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });

        buttonSubmitHighscore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitHighscore();
            }
        });

        txtHistory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showEditDialog(tower);
            }
        });
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        // set parent activity
        mActivity = (MainActivity) a;
        this.highscore = mActivity.getHighscore();
    }
}