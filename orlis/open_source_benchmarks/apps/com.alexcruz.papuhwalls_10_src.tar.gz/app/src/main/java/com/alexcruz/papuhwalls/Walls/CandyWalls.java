package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class CandyWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_candy_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_candywalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "candy_walls";
    }

}
