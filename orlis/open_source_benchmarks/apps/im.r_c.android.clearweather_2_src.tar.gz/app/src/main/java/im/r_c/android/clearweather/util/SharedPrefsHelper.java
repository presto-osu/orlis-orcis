package im.r_c.android.clearweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import im.r_c.android.clearweather.db.CountyDAO;
import im.r_c.android.clearweather.model.County;

/**
 * ClearWeather
 * Created by richard on 16/5/3.
 */
public class SharedPrefsHelper {
    private static final String TAG = "SharedPrefsHelper";

    public static final String KEY_COUNTY_CODE_STRING = "county code string";

    private Context mContext;

    public SharedPrefsHelper(Context context) {
        mContext = context;
    }

    public void addCounty(County county) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String countyCodeString = preferences.getString(KEY_COUNTY_CODE_STRING, "");
        List<String> codeList = splitCodeList(countyCodeString);
        L.d(TAG, "Got code list: " + codeList.toString());
        codeList.add(county.getCode());
        L.d(TAG, "Added code: " + codeList.toString());
        preferences.edit()
                .putString(KEY_COUNTY_CODE_STRING, joinCodeList(codeList))
                .apply();
    }

    public void removeCounty(County county) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        List<String> codeList = splitCodeList(preferences.getString(KEY_COUNTY_CODE_STRING, ""));
        for (int i = 0; i < codeList.size(); i++) {
            if (codeList.get(i).equals(county.getCode())) {
                codeList.remove(i);
            }
        }
        preferences.edit()
                .putString(KEY_COUNTY_CODE_STRING, joinCodeList(codeList))
                .apply();
    }

    public List<County> getCounties() {
        List<String> codeList = splitCodeList(
                PreferenceManager.getDefaultSharedPreferences(mContext).getString(KEY_COUNTY_CODE_STRING, ""));
        List<County> countyList = new ArrayList<>();
        if (codeList.size() > 0) {
            CountyDAO dao = new CountyDAO(mContext);
            for (String code : codeList) {
                County county = dao.query(code);
                if (county != null) {
                    countyList.add(county);
                }
            }
            dao.close();
        }
        return countyList;
    }

    private String joinCodeList(List<String> codeList) {
        if (codeList.size() < 1) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(codeList.get(0));
        for (int i = 1; i < codeList.size(); i++) {
            stringBuilder.append(",")
                    .append(codeList.get(i));
        }
        return stringBuilder.toString();
    }

    private List<String> splitCodeList(String string) {
        if ("".equals(string)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }
}
