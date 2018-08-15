package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class BrokenOsWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_brokenos_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_brokenoswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "brokenos_walls";
    }

}
