package com.sevag.unrealtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.sevag.unrealtracker.parser.MainParser;
import com.sevag.unrealtracker.ui.StatusDisplayer;
import com.sevag.unrealtracker.ui.ThemeManager;

public class MainActivity extends Activity implements StatusDisplayer {

    private int theme;
    private int fetched = 0;
    private int LAST_ACTION = 0;
    private boolean blockRadioGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        fetched = 0;

        rollTheme(ThemeManager.getTheme(this));
        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fetched == 0) {
            fetchData();
        }
    }

    private void fetchData() {
        fetched = 1;
        final Handler h = new Handler();
        h.postDelayed(new MainParser(this, this), 1000);
    }

    private void rollTheme(int paramTheme) {
        theme = paramTheme;

        switch (theme) {
            case 0:
                setContentView(R.layout.activity_main1);
                break;
            case 1:
                setContentView(R.layout.activity_main2);
                break;
            case 2:
                setContentView(R.layout.activity_main3);
                break;
            default:
                setContentView(R.layout.activity_main1);
                break;
        }
        setupRadioGroup();
    }

    private void rerollTheme(int paramTheme) {
        blockRadioGroup = true;
        rollTheme(paramTheme);
        switch (LAST_ACTION) {
            case 0:
                break;
            case 1:
                doneLoading();
                break;
            case 2:
                doneLoadingWithoutWifi();
                break;
            default:
                break;
        }
        blockRadioGroup = false;
    }

    private void setupRadioGroup() {
        final Activity mainActivity = this;
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio0:
                        if (!blockRadioGroup) {
                            if (theme != 0) {
                                ThemeManager.setTheme(0, mainActivity);
                                rerollTheme(0);
                            }
                        }
                        break;
                    case R.id.radio1:
                        if (!blockRadioGroup) {
                            if (theme != 1) {
                                ThemeManager.setTheme(1, mainActivity);
                                rerollTheme(1);
                            }
                        }
                        break;
                    case R.id.radio2:
                        if (!blockRadioGroup) {
                            if (theme != 2) {
                                ThemeManager.setTheme(2, mainActivity);
                                rerollTheme(2);
                            }
                        }
                        break;
                }
            }
        });

        switch (theme) {
            case 0:
                rg.check(R.id.radio0);
                break;
            case 1:
                rg.check(R.id.radio1);
                break;
            case 2:
                rg.check(R.id.radio2);
                break;
        }
    }

    public void selectUE4Blog(View v) {
        Intent i = new Intent(getApplicationContext(), ContentActivity.class);
        i.putExtra("display", 0);
        i.putExtra("theme", theme);
        startActivity(i);
    }

    public void selectUT5Blog(View v) {
        Intent i = new Intent(getApplicationContext(), ContentActivity.class);
        i.putExtra("display", 1);
        i.putExtra("theme", theme);
        startActivity(i);
    }

    public void selectUE4Marketplace(View v) {
        Intent i = new Intent(getApplicationContext(), ContentActivity.class);
        i.putExtra("display", 2);
        i.putExtra("theme", theme);
        startActivity(i);
    }

    public void selectUTLiveStream(View v) {
        Intent i = new Intent(getApplicationContext(), ContentActivity.class);
        i.putExtra("display", 3);
        i.putExtra("theme", theme);
        startActivity(i);
    }


    public void showAbout(View view) {
        Intent i = new Intent(getApplicationContext(), ContentActivity.class);
        i.putExtra("display", 4);
        i.putExtra("theme", theme);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doneLoading() {
        LAST_ACTION = 1;
        RelativeLayout loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.INVISIBLE);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setVisibility(View.VISIBLE);

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setVisibility(View.VISIBLE);

        Button button3  = (Button) findViewById(R.id.button3);
        button3.setVisibility(View.VISIBLE);

        Button button4  = (Button) findViewById(R.id.button4);
        button4.setVisibility(View.VISIBLE);

        Button button5  = (Button) findViewById(R.id.button5);
        button5.setVisibility(View.VISIBLE);

        makeThemeSelectorVisible();
    }

    @Override
    public void doneLoadingWithoutWifi() {
        LAST_ACTION = 2;
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setVisibility(View.VISIBLE);

        TextView progressText = (TextView) findViewById(R.id.progresstext);
        progressText.setText("Check internet");

        makeThemeSelectorVisible();
    }

    private void makeThemeSelectorVisible() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
        rg.setVisibility(View.VISIBLE);
    }

    private void makeThemeSelectorInvisible() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
        rg.setVisibility(View.INVISIBLE);
    }

    public void retryFetch(View view) {
        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setVisibility(View.INVISIBLE);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        makeThemeSelectorInvisible();

        TextView progressText = (TextView) findViewById(R.id.progresstext);
        progressText.setText("Retrying");

        fetchData();
    }
}
