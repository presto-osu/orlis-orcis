package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class CloudsWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_clouds_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_cloudswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "clouds_walls";
    }

}
