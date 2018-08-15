package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class StarsWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_stars_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_starswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "stars_walls";
    }

}
