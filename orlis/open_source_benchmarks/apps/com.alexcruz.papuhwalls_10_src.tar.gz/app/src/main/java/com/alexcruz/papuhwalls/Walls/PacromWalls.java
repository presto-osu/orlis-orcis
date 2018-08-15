package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class PacromWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_pacrom_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_pacromwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "pacrom_walls";
    }

}
