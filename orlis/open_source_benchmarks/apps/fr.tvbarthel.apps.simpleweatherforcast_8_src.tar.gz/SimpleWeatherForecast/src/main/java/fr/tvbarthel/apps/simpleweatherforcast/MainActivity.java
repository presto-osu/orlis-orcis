package fr.tvbarthel.apps.simpleweatherforcast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import fr.tvbarthel.apps.billing.utils.SupportUtils;
import fr.tvbarthel.apps.simpleweatherforcast.fragments.AboutDialogFragment;
import fr.tvbarthel.apps.simpleweatherforcast.fragments.ForecastFragment;
import fr.tvbarthel.apps.simpleweatherforcast.fragments.LicenseDialogFragment;
import fr.tvbarthel.apps.simpleweatherforcast.fragments.MoreAppsDialogFragment;
import fr.tvbarthel.apps.simpleweatherforcast.fragments.TemperatureUnitPickerDialogFragment;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastJsonParser;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastModel;
import fr.tvbarthel.apps.simpleweatherforcast.receivers.WeatherWidgetReceiver;
import fr.tvbarthel.apps.simpleweatherforcast.services.DailyForecastUpdateService;
import fr.tvbarthel.apps.simpleweatherforcast.ui.AlphaForegroundColorSpan;
import fr.tvbarthel.apps.simpleweatherforcast.utils.SharedPreferenceUtils;

public class MainActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String EXTRA_PAGE_POSITION = "fr.tvbarthel.apps.simpleweatherforcast.MainActivity.Extra.PagePosition";

    private ViewGroup mRootView;
    private ForecastPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Toast mTextToast;
    private String mTemperatureUnit;
    private SpannableString mActionBarSpannableTitle;
    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private TypefaceSpan mTypefaceSpanLight;
    private int[] mBackgroundColors;
    private Menu mMenu;
    private SimpleDateFormat mActionBarTitleDateFormat;
    private ProgressBar mProgressBar;
    private String mLoadedWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        // Get the colors used for the background
        mBackgroundColors = getResources().getIntArray(R.array.background_colors);
        //Get the temperature unit symbol
        mTemperatureUnit = SharedPreferenceUtils.getTemperatureUnitSymbol(this);

        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(Color.WHITE);
        mTypefaceSpanLight = new TypefaceSpan("sans-serif-light");
        mActionBarTitleDateFormat = new SimpleDateFormat("EEEE dd MMMM", Locale.getDefault());

        mProgressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
        mRootView = (ViewGroup) findViewById(R.id.activity_main_root);

        initActionBar();
        initViewPager();
        initRootPadding();
    }

    private void initRootPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Resources resources = getResources();
            final boolean isPortrait = resources.getBoolean(R.bool.is_portrait);
            final ActionBar actionBar = getSupportActionBar();

            final ViewTreeObserver vto = mRootView.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mRootView.getViewTreeObserver().removeOnPreDrawListener(this);

                        int paddingBottom = mRootView.getPaddingBottom();
                        int paddingTop = mRootView.getPaddingTop();
                        int paddingRight = mRootView.getPaddingRight();
                        int paddingLeft = mRootView.getPaddingLeft();

                        // Add the status bar height to the top padding.
                        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            paddingTop += resources.getDimensionPixelSize(resourceId);
                        }

                        if (isPortrait) {
                            // Add the navigation bar height to the bottom padding.
                            resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                            if (resourceId > 0) {
                                paddingBottom += resources.getDimensionPixelSize(resourceId);
                            }
                        } else {
                            // Add the navigation bar width to the right padding.
                            resourceId = resources.getIdentifier("navigation_bar_width", "dimen", "android");
                            if (resourceId > 0) {
                                paddingRight += resources.getDimensionPixelSize(resourceId);
                            }
                        }

                        mRootView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                        return true;
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String lastKnownWeather = SharedPreferenceUtils.getLastKnownWeather(getApplicationContext());
        final Intent intent = getIntent();
        SharedPreferenceUtils.registerOnSharedPreferenceChangeListener(this, this);

        if (lastKnownWeather == null) {
            // There is no forecast, it should be the first launch
            mProgressBar.setVisibility(View.VISIBLE);
        } else if (mLoadedWeather != lastKnownWeather) {
            // Load the last known weather
            loadDailyForecast(lastKnownWeather);
        } else if (intent != null && intent.hasExtra(EXTRA_PAGE_POSITION)) {
            final int position = intent.getIntExtra(EXTRA_PAGE_POSITION, 0);
            mViewPager.setCurrentItem(position, true);
            intent.removeExtra(EXTRA_PAGE_POSITION);
        }

        //Check if the last known weather is out dated.
        if (SharedPreferenceUtils.isWeatherOutdated(this, false) || lastKnownWeather == null) {
            DailyForecastUpdateService.startForUpdate(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferenceUtils.unregisterOnSharedPreferenceChangeListener(this, this);
        hideToast();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        // If the user is supporting us, add a thanks action.
        SupportUtils.checkSupport(getApplicationContext(), new SupportUtils.OnCheckSupportListener() {
            @Override
            public void onCheckSupport(boolean supporting) {
                if (supporting) {
                    getMenuInflater().inflate(R.menu.thanks, mMenu);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean isActionConsumed;
        switch (id) {
            case R.id.menu_item_about:
                isActionConsumed = handleActionAbout();
                break;

            case R.id.menu_item_manual_refresh:
                isActionConsumed = handleActionManualRefresh();
                break;

            case R.id.menu_item_license:
                isActionConsumed = handleActionLicense();
                break;

            case R.id.menu_item_more_apps:
                isActionConsumed = handleActionMoreApps();
                break;

            case R.id.menu_item_unit_picker:
                isActionConsumed = handleActionUnitPicker();
                break;

            case R.id.menu_item_support:
                isActionConsumed = handleActionSupport();
                break;

            case R.id.menu_item_contact_us:
                isActionConsumed = handleActionContactUs();
                break;

            case R.id.menu_item_thanks:
                isActionConsumed = handleThanksButton();
                break;

            default:
                isActionConsumed = super.onOptionsItemSelected(item);
        }
        return isActionConsumed;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferenceUtils.KEY_TEMPERATURE_UNIT_SYMBOL)) {
            mTemperatureUnit = SharedPreferenceUtils.getTemperatureUnitSymbol(this);
            mSectionsPagerAdapter.notifyDataSetChanged();
            invalidatePageTransformer();

            // Broadcast change to the widgets
            Intent intent = new Intent(this, WeatherWidgetReceiver.class);
            intent.setAction(WeatherWidgetReceiver.APPWIDGET_DATA_CHANGED);
            sendBroadcast(intent);
        } else if (SharedPreferenceUtils.KEY_LAST_UPDATE.equals(key)) {
            final String lastKnownWeather = SharedPreferenceUtils.getLastKnownWeather(getApplicationContext());
            loadDailyForecast(lastKnownWeather);
        }
    }

    private boolean handleThanksButton() {
        makeTextToast(R.string.support_has_supported_us);
        return true;
    }

    private boolean handleActionContactUs() {
        final String uriString = getString(R.string.contact_us_uri,
                Uri.encode(getString(R.string.contact_us_email)),
                Uri.encode(getString(R.string.contact_us_default_subject)));
        final Uri mailToUri = Uri.parse(uriString);
        Intent sendToIntent = new Intent(Intent.ACTION_SENDTO);
        sendToIntent.setData(mailToUri);
        startActivity(sendToIntent);
        return true;
    }

    private boolean handleActionSupport() {
        final int colorSize = mBackgroundColors.length;
        final int currentPosition = mViewPager.getCurrentItem();
        final int currentColor = mBackgroundColors[currentPosition % colorSize];
        final Intent intent = new Intent(this, SupportActivity.class);
        intent.putExtra(SupportActivity.EXTRA_BG_COLOR, currentColor);
        startActivity(intent);
        return true;
    }

    private boolean handleActionUnitPicker() {
        final String[] temperatureUnitNames = getResources().getStringArray(R.array.temperature_unit_names);
        final String[] temperatureUnitSymbols = getResources().getStringArray(R.array.temperature_unit_symbols);
        (TemperatureUnitPickerDialogFragment.newInstance(temperatureUnitNames, temperatureUnitSymbols))
                .show(getSupportFragmentManager(), "dialog_unit_picker");
        return true;
    }

    private boolean handleActionAbout() {
        (new AboutDialogFragment()).show(getSupportFragmentManager(), "dialog_about");
        return true;
    }

    private boolean handleActionMoreApps() {
        (new MoreAppsDialogFragment()).show(getSupportFragmentManager(), "dialog_more_apps");
        return true;
    }

    private boolean handleActionLicense() {
        (new LicenseDialogFragment()).show(getSupportFragmentManager(), "dialog_license");
        return true;
    }

    private boolean handleActionManualRefresh() {
        if (SharedPreferenceUtils.isWeatherOutdated(this, true)) {
            DailyForecastUpdateService.startForUpdate(this);
            mSectionsPagerAdapter.clear();
            mLoadedWeather = null;
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            makeTextToast(R.string.toast_already_up_to_date);
        }
        return true;
    }


    private void initActionBar() {
        // Hide the app icon in the actionBar
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private void initViewPager() {
        mSectionsPagerAdapter = new ForecastPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.activity_main_view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ForecastPageTransformer());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int currentPosition, float positionOffset, int i2) {
                setGradientBackgroundColor(currentPosition, positionOffset);
                updateActionBarTitle(currentPosition, positionOffset);
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void makeTextToast(int stringResourceId) {
        hideToast();
        mTextToast = Toast.makeText(this, stringResourceId, Toast.LENGTH_LONG);
        mTextToast.show();
    }

    private void hideToast() {
        if (mTextToast != null) {
            mTextToast.cancel();
            mTextToast = null;
        }
    }

    private void loadDailyForecast(final String jsonDailyForecast) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (jsonDailyForecast != null) {
            new DailyForecastJsonParser() {
                @Override
                protected void onPostExecute(ArrayList<DailyForecastModel> dailyForecastModels) {
                    super.onPostExecute(dailyForecastModels);
                    mLoadedWeather = jsonDailyForecast;
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mSectionsPagerAdapter.updateModels(dailyForecastModels);

                    final Intent intent = getIntent();
                    if (intent != null && intent.hasExtra(EXTRA_PAGE_POSITION)) {
                        final int position = intent.getIntExtra(EXTRA_PAGE_POSITION, 0);
                        mViewPager.setCurrentItem(position);
                        intent.removeExtra(EXTRA_PAGE_POSITION);
                    } else {
                        invalidatePageTransformer();
                    }
                }
            }.execute(jsonDailyForecast);
        }
    }

    /**
     * Trick to notify the pageTransformer of a data set change.
     */
    private void invalidatePageTransformer() {
        if (mViewPager.getAdapter().getCount() > 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (mViewPager.beginFakeDrag()) {
                        mViewPager.fakeDragBy(0f);
                        mViewPager.endFakeDrag();
                    }
                }
            });
        }
    }

    @SuppressLint("NewApi")
    private void setGradientBackgroundColor(int currentPosition, float positionOffset) {
        final GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{getColor(currentPosition, positionOffset), getColor(currentPosition, (float) Math.pow(positionOffset, 0.40))});
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            mRootView.setBackgroundDrawable(g);
        } else {
            mRootView.setBackground(g);
        }
    }

    private void setActionBarAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mActionBarSpannableTitle.setSpan(mAlphaForegroundColorSpan, 0, mActionBarSpannableTitle.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(mActionBarSpannableTitle);
    }

    private void updateActionBarTitle(int currentPosition, float positionOffset) {
        float alpha = 1 - positionOffset * 2;
        if (positionOffset >= 0.5) {
            currentPosition++;
            alpha = (positionOffset - 0.5f) * 2;
        }
        setActionBarTitle(currentPosition);
        setActionBarAlpha(alpha);
    }

    private void setActionBarTitle(int position) {
        String newTitle = getActionBarTitle(position);
        if (mActionBarSpannableTitle == null || !mActionBarSpannableTitle.toString().equals(newTitle)) {
            mActionBarSpannableTitle = new SpannableString(newTitle);
            mActionBarSpannableTitle.setSpan(mTypefaceSpanLight, 0, mActionBarSpannableTitle.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private String getActionBarTitle(int currentPosition) {
        final DailyForecastModel currentModel = mSectionsPagerAdapter.getModel(currentPosition);
        return mActionBarTitleDateFormat.format(currentModel.getDateTime() * 1000);
    }


    private int getColor(int currentPosition, float positionOffset) {
        //retrieve current color and next color relative to the current position.
        final int colorSize = mBackgroundColors.length;
        final int currentColor = mBackgroundColors[(currentPosition) % colorSize];
        final int nextColor = mBackgroundColors[(currentPosition + 1) % colorSize];

        //Compute the deltas relative to the current position offset.
        final int deltaR = (int) ((Color.red(nextColor) - Color.red(currentColor)) * positionOffset);
        final int deltaG = (int) ((Color.green(nextColor) - Color.green(currentColor)) * positionOffset);
        final int deltaB = (int) ((Color.blue(nextColor) - Color.blue(currentColor)) * positionOffset);

        return Color.argb(255, Color.red(currentColor) + deltaR, Color.green(currentColor) + deltaG, Color.blue(currentColor) + deltaB);
    }


    /**
     * A {@link FragmentStatePagerAdapter} used for {@link DailyForecastModel}
     */
    public class ForecastPagerAdapter extends FragmentStatePagerAdapter {

        private final ArrayList<DailyForecastModel> mDailyForecastModels;

        public ForecastPagerAdapter(FragmentManager fm) {
            super(fm);
            mDailyForecastModels = new ArrayList<DailyForecastModel>();
        }

        public void updateModels(ArrayList<DailyForecastModel> newModels) {
            mDailyForecastModels.clear();
            mDailyForecastModels.addAll(newModels);
            notifyDataSetChanged();
        }

        public void clear() {
            mDailyForecastModels.clear();
            notifyDataSetChanged();
        }

        public DailyForecastModel getModel(int position) {
            if (position >= mDailyForecastModels.size()) {
                return new DailyForecastModel();
            }
            return mDailyForecastModels.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return ForecastFragment.newInstance(mDailyForecastModels.get(position), mTemperatureUnit);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mDailyForecastModels.size();
        }
    }

    /**
     * A {@link PageTransformer} used to scale the fragments.
     */
    public class ForecastPageTransformer implements PageTransformer {

        public void transformPage(View view, float position) {
            final int pageWidth = view.getWidth();

            if (position < -2.0) { // [-Infinity,-2.0)
                // This page is way off-screen to the left.
                ViewHelper.setAlpha(view, 0);

            } else if (position <= 2.0) { // [-2.0,2.0]
                final float normalizedPosition = Math.abs(position / 2);
                final float scaleFactor = 1f - normalizedPosition;
                final float horizontalMargin = pageWidth * normalizedPosition;

                //Translate back the page.
                if (position < 0) {
                    //left
                    ViewHelper.setTranslationX(view, horizontalMargin);
                } else {
                    //right
                    ViewHelper.setTranslationX(view, -horizontalMargin);
                }

                // Scale the page down relative to its size.
                ViewHelper.setScaleX(view, (float) Math.pow(scaleFactor, 0.80));
                ViewHelper.setScaleY(view, (float) Math.pow(scaleFactor, 0.80));

                // Fade the page relative to its size.
                ViewHelper.setAlpha(view, 1f * scaleFactor - (scaleFactor * 0.25f));

            } else { // (2.0,+Infinity]
                // This page is way off-screen to the right.
                ViewHelper.setAlpha(view, 0);
            }
        }
    }

}
