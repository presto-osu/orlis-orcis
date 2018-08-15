package com.thefonz.ed_tool;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.thefonz.ed_tool.utils.Utils;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener
{
    ActionBar actionbar;
    ViewPager viewpager;
    FragmentPageTabAdapter ft;
    public static Context contextOfApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        contextOfApplication = getApplicationContext();

        Utils.checkInternet(MainActivity.this);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean immersiveMode = SP.getBoolean("immersiveMode", true);
        String selectTheme = SP.getString("selectTheme", "1");

        assert selectTheme != null;
        if (selectTheme.equalsIgnoreCase("1")) {
            setTheme(R.style.AppThemeDark);
        }
        else
        {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final View decorView = getWindow().getDecorView();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (immersiveMode) {
            // Set immersive mode
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

            // Register UI change listener to re-set immersive mode if refocused
            decorView.setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            // Note that system bars will only be "visible" if none of the
                            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                // The system bars are visible. Make any desired changes
                                decorView.setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                                // adjustments to your UI, such as showing the action bar or
                                // other navigational controls.
                            } else {
                                // The system bars are NOT visible. Make any desired changes
                                // adjustments to your UI, such as hiding the action bar or
                                // other navigational controls.
                            }
                        }
                    });
        }

        //instantiate the custom adapter
        ft = new FragmentPageTabAdapter(getSupportFragmentManager());

        viewpager = (ViewPager) findViewById(R.id.pager);
        viewpager.setAdapter(ft);

        //add tabs to the action bar
        actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionbar.addTab(actionbar.newTab().setText(R.string.tab_buttonbox).setTabListener(this));
        actionbar.addTab(actionbar.newTab().setText(R.string.tab_rares).setTabListener(this));
        actionbar.addTab(actionbar.newTab().setText(R.string.tab_notes).setTabListener(this));
        actionbar.addTab(actionbar.newTab().setText(R.string.tab_galnet).setTabListener(this));
        actionbar.addTab(actionbar.newTab().setText(R.string.tab_reddit).setTabListener(this));

        actionbar.setLogo(R.mipmap.ic_launcher);
        actionbar.setDisplayUseLogoEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);

        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageSelected(int arg0)
            {
                actionbar.setSelectedNavigationItem(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
                // TODO Auto-generated method stub
            }
        });
    }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            // String msg = getString(R.string.placeholder);
            // Utils.showToast_Long(getApplicationContext(), msg);
            startActivity(new Intent(this, com.thefonz.ed_tool.preferences.AppPreferences.class));

            return true;
        }
        if (id == R.id.action_exit)
        {
            finish();
            System.exit(0);
          return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft)
    {
        viewpager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft)
    {
        // TODO Auto-generated method stub
    }

    public class FragmentPageTabAdapter extends FragmentPagerAdapter
    {
        public FragmentPageTabAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0)
        {
            switch (arg0)
            {
                case 0:
                    return new Tab_ButtonBox();
                case 1:
                    return new Tab_Rares();
                case 2:
                    return new Tab_Notes();
                case 3:
                    return new Tab_Galnet();
                case 4:
                    return new Tab_Reddit();
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount()
        {
            return 5;
        }
    }

    @Override
    public void onBackPressed() {
        // Leave blank if you do not want anything to happen
    }
}