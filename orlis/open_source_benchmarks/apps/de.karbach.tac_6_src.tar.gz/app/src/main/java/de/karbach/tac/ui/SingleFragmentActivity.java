package de.karbach.tac.ui;

import android.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import de.karbach.tac.R;

/**
 * Created by carsten on 02.10.15.
 */
public abstract class SingleFragmentActivity extends FragmentActivity {

    /**
     *
     * @return Fragment shown for this acitivity
     */
    protected abstract Fragment createFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.single_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);

        if(f==null){
            f = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, f).commit();
        }
    }
}
