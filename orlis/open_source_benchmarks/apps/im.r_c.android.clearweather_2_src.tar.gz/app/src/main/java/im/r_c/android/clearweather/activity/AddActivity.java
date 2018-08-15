package im.r_c.android.clearweather.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import im.r_c.android.clearweather.R;
import im.r_c.android.clearweather.db.CountyDAO;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.service.FetchCountyListService;
import im.r_c.android.clearweather.util.L;

public class AddActivity extends BaseActivity {
    private static final String TAG = "AddActivity";

    public static final String KEY_SELECTED_COUNTY = "selected_county";

    public static Intent getIntent(Context context) {
        return new Intent(context, AddActivity.class);
    }

    @Bind(R.id.actv_county_input)
    AutoCompleteTextView mCountyInput;

    @Bind(R.id.pb_progress)
    ProgressBar mPbProgress;

    private ArrayAdapter<String> mAdapter;
    private List<County> mCountyList;
    private List<String> mCountyStringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        ButterKnife.bind(this);

        mCountyInput.setText(getString(R.string.loading_county_list));
        mCountyInput.setEnabled(false);

        mCountyList = new ArrayList<>();
        mCountyStringList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        mCountyInput.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onReceivedFetchCountyListResultCode(Integer resultCode) {
        L.v(TAG, "resultCode = " + resultCode);

        if (resultCode == FetchCountyListService.RESULT_OK) {
            mCountyInput.setText("");
            mCountyInput.setEnabled(true);
            mPbProgress.setVisibility(View.GONE);
            L.d(TAG, "Enabled input");

            CountyDAO dao = new CountyDAO(this);
            List<County> countyList = dao.queryAll();
            for (County county : countyList) {
                mCountyList.add(county);
                String string = county.getName() + ", " + county.getCity() + ", " + county.getProvince();
                mAdapter.add(string);
                mCountyStringList.add(string);
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.load_failed))
                    .setMessage(getString(R.string.load_county_list_failed_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.i_know), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AddActivity.this.finish();
                        }
                    })
                    .show();
            L.d(TAG, "Load failed");
        }
    }

    public void addCounty(View view) {
        String countyString = mCountyInput.getText().toString().trim();
        int position = mCountyStringList.indexOf(countyString);
        if (position < 0) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dont_modify_selected_county)
                    .show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(KEY_SELECTED_COUNTY, mCountyList.get(position));
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancel(View view) {
        finish();
    }
}
