package org.ligi.scr;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import net.steamcrafted.loadtoast.LoadToast;

import org.ligi.axt.AXT;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.metadude.java.library.halfnarp.ApiModule;
import info.metadude.java.library.halfnarp.TalkPreferencesService;
import info.metadude.java.library.halfnarp.model.CreateTalkPreferencesSuccessResponse;
import info.metadude.java.library.halfnarp.model.GetTalksResponse;
import info.metadude.java.library.halfnarp.model.UpdateTalkPreferencesSuccessResponse;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    final String KEY_LAST_POSITION = "last_scroll_position";
    final String KEY_LAST_UPDATE_SAVED = "last_update_saved";

    @Bind(R.id.trackRecycler)
    RecyclerView trackRecycler;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @OnClick(R.id.fab)
    void onFabClick() {
        final String uuidOrNull = getPrefs().getString("uuid", null);

        setStateSaved();

        if (uuidOrNull == null) {

            final LoadToast loadToast = new LoadToast(this).setText("Initial upload").show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ApiModule.getTalkPreferencesService().createTalkPreferences(App.talkIds).enqueue(new Callback<CreateTalkPreferencesSuccessResponse>() {
                        @Override
                        public void onResponse(Response<CreateTalkPreferencesSuccessResponse> response, Retrofit retrofit) {
                            getPrefs().edit().putString("uuid", response.body().getUid()).commit();
                            loadToast.success();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            loadToast.error();
                            setStateChanged();
                        }
                    });
                }
            }, 1500);

        } else {
            final LoadToast loadToast = new LoadToast(this).setText("Uploading new selection").show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ApiModule.getTalkPreferencesService().updateTalkPreferences(uuidOrNull, App.talkIds).enqueue(new Callback<UpdateTalkPreferencesSuccessResponse>() {
                        @Override
                        public void onResponse(Response<UpdateTalkPreferencesSuccessResponse> updateTalkPreferencesSuccessResponse, Retrofit retrofit) {
                            loadToast.success();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            loadToast.error();
                            setStateChanged();
                        }
                    });
                }
            }, 1500);
        }
    }

    private EventViewHolderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setIcon(R.drawable.logo);

        getSupportActionBar().setSubtitle("Schedule Conflict Resolver");
        ButterKnife.bind(this);


        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.rows), OrientationHelper.VERTICAL);
        trackRecycler.setLayoutManager(layoutManager);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                trackRecycler.setBackgroundResource(R.drawable.bg_src);
            } else {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.bg_src);
                trackRecycler.setBackgroundDrawable(new BitmapDrawable(getResources(), Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // form follows function
        }

        App.bus.register(this);
        App.talkIds.load();

        trackRecycler.getLayoutManager().scrollToPosition(getSharedPrefs().getInt(KEY_LAST_POSITION, 0));

        if (getPrefs().getBoolean(KEY_LAST_UPDATE_SAVED,false)) {
            fab.hide();
        } else {
            fab.show();
        }
    }

    private SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPause() {
        App.bus.unregister(this);
        App.talkIds.save();

        int lastFirstVisiblePosition = ((StaggeredGridLayoutManager) trackRecycler.getLayoutManager()).findFirstVisibleItemPositions(null)[0];
        getSharedPrefs().edit().putInt(KEY_LAST_POSITION, lastFirstVisiblePosition).commit();

        super.onPause();
    }


    private void loadData() {
        final TalkPreferencesService service = ApiModule.getTalkPreferencesService();
        service.getTalks().enqueue(new DefaultRetrofitCallback<List<GetTalksResponse>>(true, this) {
            @Override
            public void onResponse(Response<List<GetTalksResponse>> response, Retrofit retrofit) {
                adapter = new EventViewHolderAdapter(response.body());
                trackRecycler.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                AXT.at(this).startCommonIntent().activityFromClass(HelpActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences("prefs", MODE_PRIVATE);
    }

    @Subscribe
    public void onEvent(TalkIdsChangeEvent scopeChangeEvent) {
        setStateChanged();
    }

    private void setStateChanged() {
        fab.show();
        getPrefs().edit().putBoolean(KEY_LAST_UPDATE_SAVED,false).commit();
    }


    private void setStateSaved() {
        fab.hide();
        getPrefs().edit().putBoolean(KEY_LAST_UPDATE_SAVED,true).commit();
    }


}