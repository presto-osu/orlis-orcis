package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class AllWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_all_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_allwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "all_walls";
    }


}
