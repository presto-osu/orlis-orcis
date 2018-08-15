package com.idunnololz.igo;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.idunnololz.utils.AlertDialogFragment;
import com.idunnololz.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private View btnNewGame;
    private View btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewGame = findViewById(R.id.new_game);
        btnAbout = findViewById(R.id.about);

        btnNewGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                (NewGameDialogFragment.newInstance()).show(getSupportFragmentManager(), "adialog");
            }

        });

        btnAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialogFragment.Builder()
                        .setMessage("igo created by Gary Guo")
                        .create().show(getSupportFragmentManager(), "d");
            }

        });
    }

    public static class NewGameDialogFragment extends DialogFragment {
        private Spinner boardSize;
        private Spinner handicap;
        private Spinner komi;
        private View rootView;
        private Button startGame;

        private List<CharSequence> handicapOptions = new ArrayList<CharSequence>();

        private static NewGameDialogFragment newInstance() {
            NewGameDialogFragment f = new NewGameDialogFragment();
            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);

            // request a window without the title
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.dialog_new_game, container, false);
            boardSize = (Spinner) rootView.findViewById(R.id.boardSize);
            handicap = (Spinner) rootView.findViewById(R.id.handicap);
            komi = (Spinner) rootView.findViewById(R.id.komi);
            startGame = (Button) rootView.findViewById(R.id.btnStartGame);

            {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.board_size_array, R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                boardSize.setAdapter(adapter);
                boardSize.setSelection(2);
                boardSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View v,
                                               int position, long arg3) {
                        int maxHandicap = 9;

                        handicapOptions.clear();

                        switch (getChosenBoardSize()) {
                            case 9:
                                maxHandicap = 4;
                                break;
                            default:
                                break;
                        }

                        for (int i = 0; i <= maxHandicap; i++) {
                            handicapOptions.add(String.valueOf(i));
                        }
                        ((ArrayAdapter<?>)handicap.getAdapter()).notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // do nothing...
                    }

                });
            }

            {
                for (int i = 0; i < 9; i++) {
                    handicapOptions.add(String.valueOf(i));
                }

                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        R.layout.simple_spinner_item, handicapOptions);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                handicap.setAdapter(adapter);
            }

            {
                List<CharSequence> list = new ArrayList<CharSequence>();
                for (float i = 0; i < 10; i+=0.5f) {
                    list.add(String.valueOf(i));
                }

                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                komi.setAdapter(adapter);
                komi.setSelection((int)(6.5f / .5f));
            }

            startGame.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    int boardSize = getChosenBoardSize();

                    int handicaps = handicap.getSelectedItemPosition();

                    float komiPoints = Float.valueOf(komi.getSelectedItem().toString());

                    LogUtils.d(TAG, "Board Size: " + boardSize + " handicaps: " + handicaps + " komi: " + komiPoints);

                    Intent i = new Intent(getActivity(), GameActivity.class);
                    i.putExtra(GameActivity.ARGS_BOARD_SIZE, boardSize);
                    i.putExtra(GameActivity.ARGS_HANDICAP, handicaps);
                    i.putExtra(GameActivity.ARGS_KOMI, komiPoints);
                    startActivity(i);
                }

            });

            return rootView;
        }

        private int getChosenBoardSize() {
            String strBoardSize = boardSize.getSelectedItem().toString();
            return Integer.valueOf(strBoardSize.substring(0, strBoardSize.lastIndexOf('x')));
        }
    }
}
