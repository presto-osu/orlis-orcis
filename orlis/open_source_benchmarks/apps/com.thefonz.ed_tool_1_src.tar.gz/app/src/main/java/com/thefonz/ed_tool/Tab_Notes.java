package com.thefonz.ed_tool;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thefonz.ed_tool.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

/**
 * Created by thefonz on 02/04/15.
 */
public class Tab_Notes extends Fragment {

    EditText textmsg;
    static final int READ_BLOCK_SIZE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View myFragmentView = inflater.inflate(R.layout.tab_notes, container, false);

        textmsg = (EditText) myFragmentView.findViewById(R.id.editText1);

        CheckForFile(myFragmentView);
        ReadNote(myFragmentView);

        final Button button_save = (Button) myFragmentView.findViewById(R.id.button1);

        button_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SaveNote(myFragmentView);
            }
        });

        textmsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    FileInputStream fileIn=getActivity().openFileInput("mytextfile.txt");
                    InputStreamReader InputRead= new InputStreamReader(fileIn);

                    char[] inputBuffer= new char[READ_BLOCK_SIZE];
                    String str="";
                    int charRead;

                    while ((charRead=InputRead.read(inputBuffer))>0) {
                        // char to string conversion
                        str = String.copyValueOf(inputBuffer,0,charRead);
                    }
                    InputRead.close();

                    String str_et = textmsg.getText().toString();
                    if (!Objects.equals(str, str_et)) {
                        button_save.setEnabled(true);
                        button_save.setBackgroundColor(getResources().getColor(R.color.my_teal));
                        button_save.setText("Save Now");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return myFragmentView;
    }

    // Check for note file
    public void CheckForFile(View context) {

        String FILENAME = "mytextfile.txt";
        String string = "";

        String LOGMETHOD = " CheckForFile ";
        String LOGBODY = " file exists ";

        File file = getActivity().getFileStreamPath(FILENAME);

        if(!file.exists()) {
            // add-write text into file
            try {
                FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(string.getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // write text to file
    public void SaveNote(View v) {
        // add-write text into file
        try {
            FileOutputStream fileout=getActivity().openFileOutput("mytextfile.txt", Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            String text = textmsg.getText().toString();
            outputWriter.write(text);
            outputWriter.close();

            //display file saved message
            String msg = "Note saved !";
            final Button button_save = (Button) getActivity().findViewById(R.id.button1);
            button_save.setBackgroundResource(android.R.drawable.btn_default);
            button_save.setEnabled(false);
            button_save.setText("Saved");
            Utils.showToast_Short(this.getActivity(), msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read text from file
    public void ReadNote(View v) {
        //reading text from file
        try {
            FileInputStream fileIn=getActivity().openFileInput("mytextfile.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            textmsg.setText(s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}