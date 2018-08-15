/*This file is part of NumGuesser.

        NumGuesser is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        NumberGuesser is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with NumGuesser.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.numguesser.tonio_rpchp.numberguesser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class Guesser extends Activity {

    public int attempt = 5;

    Random rand_num = new Random();
    int rnd = rand_num.nextInt(100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guesser);

        final TextView tries_label = (TextView) findViewById(R.id.tries_label);
        tries_label.setText(getString(R.string.tries_text)+ attempt);

        final EditText number_txt = (EditText) findViewById(R.id.number);

        final Button button = (Button) findViewById(R.id.guess_button);

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Perform action on click

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                if (attempt != 0) {

                    try{

                        int num = Integer.parseInt(String.valueOf(number_txt.getText()));

                        if (num == 0 || num > 100){
                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.in_range), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else if (num == rnd) {
                            Alert(getString(R.string.end_game), getString(R.string.win));
                        }
                        else{
                            attempt--;
                            tries_label.setText(getString(R.string.tries_text) + attempt);
                            if (num < rnd && attempt != 0){
                                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.more), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            else {
                                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.less), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                    }
                    catch(Exception e){
                        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                number_txt.setText("");

                if (attempt ==0){
                    Alert(getString(R.string.end_game), getString(R.string.lose)+rnd);
                }
            }
        });

    }

    public void Alert(String tittle, String message){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setTitle(tittle);
        alertDialogBuilder.setMessage(message);


        alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        rnd = rand_num.nextInt(100);
                        attempt = 5;
                        final TextView tries_label = (TextView) findViewById(R.id.tries_label);
                        tries_label.setText(getString(R.string.tries_text)+ attempt);
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.exit),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(1);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }
}
