package com.evenement.eveControl;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * NavDrawer fragment class
 * Fragment contains login form
 * OnClickListener is implemented for the submit button of the login form
 */
public class NavFragment extends Fragment implements View.OnClickListener {

    private EditText user;
    private EditText pass;
    private EditText server;
    private DrawerLayout drawerLayout;

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_left, null);

        Button loginBtn = (Button) view.findViewById(R.id.btn_login);
        user = (EditText) view.findViewById(R.id.input_email);
        pass = (EditText) view.findViewById(R.id.input_password);
        server = (EditText) view.findViewById(R.id.input_server);

        loginBtn.setOnClickListener(this);

        return view;
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {

        //retrieve sharedPreferences(saved login credentials)
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String username = user.getText().toString();
        String password = pass.getText().toString();
        String server = this.server.getText().toString();

        if (username.equals("") || password.equals("") || server.equals("")) {

            Toast.makeText(getActivity(), "Veuiller saisir les informations de connexion", Toast.LENGTH_SHORT).show();

        } else {
            //persist credentials into sharedPreferences
            editor.putString("username", username);
            editor.putString("password", password);
            editor.putString("server", server);

            editor.commit();

            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        //connect
        WebView webView = (WebView) getActivity().findViewById(R.id.webview);
        new LoginTask(webView, getActivity(), server, username, password, drawerLayout).execute();
    }

    /**
     * Pass drawerLayout to fragment without changing defaultconstructor
     *
     * @param drawerLayout
     */
    public void setDrawerLayout(DrawerLayout drawerLayout) {

        this.drawerLayout = drawerLayout;
    }


}
