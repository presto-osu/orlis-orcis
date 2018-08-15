package im.r_c.android.clearweather.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import im.r_c.android.clearweather.R;
import im.r_c.android.clearweather.adapter.CardPagerAdapter;
import im.r_c.android.clearweather.db.WeatherInfoDAO;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.service.FetchCountyListService;
import im.r_c.android.clearweather.service.FetchWeatherInfoService;
import im.r_c.android.clearweather.util.L;
import im.r_c.android.clearweather.util.SharedPrefsHelper;
import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_ADD_COUNTY = 857;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.vp_main)
    ViewPager mVpMain;

    @Bind(R.id.ci_indicator)
    CircleIndicator mCiIndicator;

    private List<County> mCountyList = new ArrayList<>();
    private CardPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        SharedPrefsHelper helper = new SharedPrefsHelper(this);
        mCountyList = helper.getCounties();
        mPagerAdapter = new CardPagerAdapter(getSupportFragmentManager(), mCountyList);
        mVpMain.setAdapter(mPagerAdapter);
        mCiIndicator.setViewPager(mVpMain);

        FetchCountyListService.start(this);
        FetchWeatherInfoService.startAutoUpdate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add:
                Intent intent = AddActivity.getIntent(this);
                startActivityForResult(intent, REQUEST_CODE_ADD_COUNTY);
                break;
            case R.id.menu_item_remove:
                removeCurrentCounty();
                break;
            case R.id.menu_item_refresh:
                FetchWeatherInfoService.start(this);
                break;
//            case R.id.menu_item_settings:
//                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ADD_COUNTY: {
                if (resultCode == RESULT_OK) {
                    // Add a county
                    County county = (County) data.getSerializableExtra(AddActivity.KEY_SELECTED_COUNTY);
                    L.d(TAG, "Got add result: " + county.toString());
                    if (!mCountyList.contains(county)) {
                        SharedPrefsHelper helper = new SharedPrefsHelper(this);
                        helper.addCounty(county);
                        mCountyList.add(county);
                        refreshViewPager();
                        mVpMain.setCurrentItem(mPagerAdapter.getCount() - 1);
                    }
                }
                break;
            }
        }
    }

    private void removeCurrentCounty() {
        if (mCountyList.size() == 0) {
            return;
        }

        final int position = mVpMain.getCurrentItem();
        final County county = mCountyList.get(position);
        if (county != null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.remove_county))
                    .setMessage(String.format(getString(R.string.remove_county_msg), county.getName()))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPrefsHelper helper = new SharedPrefsHelper(MainActivity.this);
                            helper.removeCounty(county);
                            WeatherInfoDAO dao = new WeatherInfoDAO(MainActivity.this);
                            dao.delete(county);
                            dao.close();
                            mCountyList.remove(position);
                            refreshViewPager();
                        }
                    })
                    .show();
        }
    }

    private void refreshViewPager() {
        mPagerAdapter.notifyDataSetChanged();
        mCiIndicator.setViewPager(mVpMain);
    }
}
