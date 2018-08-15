package de.pinyto.exalteddicer;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        doFirstRun();

        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.title_pool)
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText(R.string.title_damage)
                        .setTabListener(this));

    }

    private void doFirstRun() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("firstShow", "").commit();
        SharedPreferences settings = this.getSharedPreferences("firstShow", getApplicationContext().MODE_PRIVATE);
        if (settings.getBoolean("isFirstRun", true)) {
            tutorialDialog();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }
    }

    public void tutorialDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.tutorial_title);

        alertDialog.setMessage(R.string.tutorial_text);

        alertDialog.setIcon(R.drawable.ic_tutorial);

        alertDialog.setPositiveButton(getString(R.string.tutorial_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();
        intent.setClass(this, SetPreferenceActivity.class);
        startActivityForResult(intent, 0);

        return true;

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    PoolFragment poolFragment = new PoolFragment();
                    return poolFragment;
                case 1:
                    return new DamageFragment();
            }

            return null;
        }

        public int getCount() {
            return 2;
        }

        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_pool).toUpperCase(l);
                case 1:
                    return getString(R.string.title_damage).toUpperCase(l);
            }
            return null;
        }
    }
}
