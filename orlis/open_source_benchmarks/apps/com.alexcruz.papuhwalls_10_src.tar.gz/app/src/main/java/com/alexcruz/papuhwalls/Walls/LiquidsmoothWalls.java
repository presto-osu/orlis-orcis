package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class LiquidsmoothWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_liquidsmooth_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_liquidsmoothwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "liquidsmooth_walls";
    }

}
