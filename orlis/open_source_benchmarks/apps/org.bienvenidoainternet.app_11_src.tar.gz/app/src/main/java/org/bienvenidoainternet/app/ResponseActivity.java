package org.bienvenidoainternet.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.bienvenidoainternet.app.structure.BoardItem;

import java.io.File;

import utils.ContentProviderUtils;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ResponseActivity extends AppCompatActivity {

    private BoardItem theReply = null;
    private SharedPreferences  settings;
    private String password;
    private String selectedFile = "";
    private final int PICK_IMAGE = 1;
    private boolean quoting = false, newthread = false;
    EditText filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager tm = new ThemeManager(this);
        this.setTheme(tm.getThemeForActivity());
        setContentView(R.layout.activity_response);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        password = settings.getString("pref_password", "12345678");

        if (savedInstanceState != null){
            this.theReply = savedInstanceState.getParcelable("theReply");
            this.quoting = savedInstanceState.getBoolean("quoting");
            this.newthread = savedInstanceState.getBoolean("newthread");
        }
        if (getIntent().getExtras() != null){
            this.theReply = getIntent().getParcelableExtra("theReply");
            this.quoting = getIntent().getBooleanExtra("quoting", false);
            this.newthread = getIntent().getBooleanExtra("newthread", false);
        }

        if (newthread){
            getSupportActionBar().setTitle("Nuevo hilo");
            getSupportActionBar().setSubtitle(theReply.getParentBoard().getBoardName());
        }else{
            getSupportActionBar().setTitle("Respondiendo");
        }

        TextView txtFilePath = (TextView) findViewById(R.id.txtFilePath);
        Button btnSelectFile = (Button) findViewById(R.id.btnSelectFiles);
        TextView txtThreadSubject = (TextView) findViewById(R.id.txtThreadSubject);

        txtThreadSubject.setVisibility(newthread ? View.VISIBLE : View.GONE);

        if (theReply != null && quoting){
            TextView txtMessage = (TextView) findViewById(R.id.txtResponse);
            if (theReply.getParentBoard().getBoardType() == 1){ // BBS
                txtMessage.setText(">>" + theReply.getBbsId());
            }else{
                txtMessage.setText(">>" + theReply.getId());
            }
        }else if (theReply != null){
            txtFilePath.setVisibility(theReply.getParentBoard().isCanAttachFiles() ? View.VISIBLE : View.GONE);
            btnSelectFile.setVisibility(theReply.getParentBoard().isCanAttachFiles() ? View.VISIBLE : View.GONE);
        }

        LinearLayout layoutProcess = (LinearLayout)findViewById(R.id.layoutPostProcess);
        layoutProcess.setVisibility(View.GONE);
        filePath = (EditText) findViewById(R.id.txtFilePath);

        Button bBold = (Button) findViewById(R.id.buttonBold);
        Button bStrike = (Button) findViewById(R.id.buttonStrike);
        Button bList = (Button) findViewById(R.id.buttonList);
        Button bCode = (Button) findViewById(R.id.buttonCode);
        Button bUnder = (Button) findViewById(R.id.buttonUnderline);
        Button bItalic = (Button) findViewById(R.id.buttonItalic);
        Button select = (Button) findViewById(R.id.btnSelectFiles);

        bBold.setVisibility(View.GONE);
        bStrike.setVisibility(View.GONE);
        bList.setVisibility(View.GONE);
        bCode.setVisibility(View.GONE);
        bUnder.setVisibility(View.GONE);
        bItalic.setVisibility(View.GONE);


        bBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTag("b");
            }
        });
        bItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTag("i");
            }
        });
        bStrike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                applyTag("strike");
            }
        });
        bList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTag("ul");
            }
        });
        bCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTag("code");
            }
        });
        bUnder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyTag("u");
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Seleccionar Archivo"), PICK_IMAGE);
            }
        });

    }

    private void applyTag(String tag){
        TextView txtMessage = (TextView) findViewById(R.id.txtResponse);
        if (txtMessage.getSelectionStart() == -1){
            txtMessage.setText(txtMessage.getText() + "<" + tag + "></" + tag +">");
        }else{
            String s = txtMessage.getText().toString();
            String a = s.substring(0, txtMessage.getSelectionStart());
            String b = s.substring(txtMessage.getSelectionStart(), txtMessage.getSelectionEnd());
            String c = s.substring(txtMessage.getSelectionEnd(), txtMessage.getText().length());
            txtMessage.setText(a + "<" + tag + ">" + b + "</" + tag + ">" + c);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_replyform, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_form_reply){
            TextView txtName = (TextView) findViewById(R.id.txtPosterName);
            TextView txtEmail = (TextView) findViewById(R.id.txtEmail);
            TextView txtMessage = (TextView) findViewById(R.id.txtResponse);
            TextView txtThreadSubject = (TextView) findViewById(R.id.txtThreadSubject);
            makePost(txtName.getText().toString(), txtEmail.getText().toString(), txtMessage.getText().toString(), txtThreadSubject.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String picturePath = ContentProviderUtils.getPath(getApplicationContext(), selectedImage);
            selectedFile = picturePath;
            filePath.setText(picturePath);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void makePost(String name, String email, String message, String subject){
        int parentId = theReply.realParentId();
        // Si el parentId = 0 && subject != "" ==> Nuevo hilo
        if (newthread){
            parentId = 0;
        }
        LinearLayout layoutProcess = (LinearLayout)findViewById(R.id.layoutPostProcess);
        layoutProcess.setVisibility(View.VISIBLE);
        final RelativeLayout formSendPost = (RelativeLayout) findViewById(R.id.layoutForm);
        formSendPost.setVisibility(View.GONE);
        ProgressBar progess = (ProgressBar) findViewById(R.id.barPosting);
        final TextView err = (TextView)findViewById(R.id.txtPostingState);
        err.setText("");
        File up = new File(selectedFile);

        if (newthread){
            if (selectedFile.isEmpty()){
                Ion.with(getApplicationContext())
                        .load("http://bienvenidoainternet.org/cgi/post")
                        .setLogging("posting", Log.VERBOSE)
                        .uploadProgressBar(progess)
                        .setMultipartParameter("board", theReply.getParentBoard().getBoardDir())
                        .setMultipartParameter("password", password)
                        .setMultipartParameter("fielda", name)
                        .setMultipartParameter("fieldb", email)
                        .setMultipartParameter("name", "")
                        .setMultipartParameter("email", "")
                        .setMultipartParameter("message", message)
                        .setMultipartParameter("subject", subject)
                        .setMultipartParameter("noimage", "on")
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.v("sendPost", result);
                                if (e != null) {
                                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error! ;_;", Toast.LENGTH_LONG).show();
                                    formSendPost.setVisibility(View.VISIBLE);
                                    err.setText("Error: " + e.getMessage());
                                    e.printStackTrace();
                                } else {
                                    if (result.contains("ERROR : Flood detectado.")){
                                        Toast.makeText(getApplicationContext(), "Error: Flood detectado.", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Post enviado", Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            }
                        });
            }else{
                Ion.with(getApplicationContext())
                        .load("http://bienvenidoainternet.org/cgi/post")
                        .uploadProgressBar(progess)
                        .setMultipartParameter("board", theReply.getParentBoard().getBoardDir())
                        .setMultipartParameter("password", password)
                        .setMultipartParameter("fielda", name)
                        .setMultipartParameter("fieldb", email)
                        .setMultipartParameter("name", "")
                        .setMultipartParameter("email", "")
                        .setMultipartParameter("message", message)
                        .setMultipartParameter("subject", subject)
                        .setMultipartFile("file", up)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.v("sendPost", result);
                                if (e != null){
                                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error! ;_;", Toast.LENGTH_LONG).show();
                                    formSendPost.setVisibility(View.VISIBLE);
                                    err.setText("Error: " + e.getMessage());
                                    e.printStackTrace();
                                }else{
                                    if (result.contains("ERROR : Flood detectado.")){
                                        Toast.makeText(getApplicationContext(), "Error: Flood detectado.", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Post enviado", Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            }
                        });
            }
        }else{
            if (selectedFile.isEmpty()){
                Ion.with(getApplicationContext())
                        .load("http://bienvenidoainternet.org/cgi/post")
                        .setLogging("posting", Log.VERBOSE)
                        .uploadProgressBar(progess)
                        .setMultipartParameter("board", theReply.getParentBoard().getBoardDir())
                        .setMultipartParameter("parent", String.valueOf(parentId))
                        .setMultipartParameter("password", password)
                        .setMultipartParameter("fielda", name)
                        .setMultipartParameter("fieldb", email)
                        .setMultipartParameter("name", "")
                        .setMultipartParameter("email", "")
                        .setMultipartParameter("message", message)
                        .setMultipartParameter("subject", subject)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.v("sendPost", result);
                                if (e != null) {
                                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error! ;_;", Toast.LENGTH_LONG).show();
                                    formSendPost.setVisibility(View.VISIBLE);
                                    err.setText("Error: " + e.getMessage());
                                    e.printStackTrace();
                                } else {
                                    if (result.contains("ERROR : Flood detectado.")){
                                        Toast.makeText(getApplicationContext(), "Error: Flood detectado.", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Post enviado", Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            }
                        });
            }else{
                Ion.with(getApplicationContext())
                        .load("http://bienvenidoainternet.org/cgi/post")
                        .uploadProgressBar(progess)
                        .setMultipartParameter("board", theReply.getParentBoard().getBoardDir())
                        .setMultipartParameter("parent", String.valueOf(parentId))
                        .setMultipartParameter("password", password)
                        .setMultipartParameter("fielda", name)
                        .setMultipartParameter("fieldb", email)
                        .setMultipartParameter("name", "")
                        .setMultipartParameter("email", "")
                        .setMultipartParameter("message", message)
                        .setMultipartParameter("subject", subject)
                        .setMultipartFile("file", up)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Log.v("sendPost", result);
                                if (e != null){
                                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error! ;_;", Toast.LENGTH_LONG).show();
                                    formSendPost.setVisibility(View.VISIBLE);
                                    err.setText("Error: " + e.getMessage());
                                    e.printStackTrace();
                                }else{
                                    if (result.contains("ERROR : Flood detectado.")){
                                        Toast.makeText(getApplicationContext(), "Error: Flood detectado.", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Post enviado", Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            }
                        });
            }
        }
    }
}
