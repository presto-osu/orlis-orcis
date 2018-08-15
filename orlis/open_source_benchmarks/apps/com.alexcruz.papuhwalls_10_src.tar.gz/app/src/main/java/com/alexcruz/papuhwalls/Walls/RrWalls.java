package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class RrWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_rr_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_rrwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "rr_walls";
    }

}
