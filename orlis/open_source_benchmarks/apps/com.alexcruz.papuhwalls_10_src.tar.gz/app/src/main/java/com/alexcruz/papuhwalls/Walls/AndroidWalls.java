package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class AndroidWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_android_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_androidwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "android_walls";
    }

}
