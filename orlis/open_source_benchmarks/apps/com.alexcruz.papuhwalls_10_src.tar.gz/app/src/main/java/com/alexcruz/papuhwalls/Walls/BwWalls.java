package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;

public class BwWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_bw_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_bwwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "bw_walls";
    }

}
