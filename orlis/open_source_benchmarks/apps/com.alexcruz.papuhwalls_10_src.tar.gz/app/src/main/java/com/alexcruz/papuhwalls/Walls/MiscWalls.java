package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class MiscWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_misc_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_miscwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "misc_walls";
    }

}
