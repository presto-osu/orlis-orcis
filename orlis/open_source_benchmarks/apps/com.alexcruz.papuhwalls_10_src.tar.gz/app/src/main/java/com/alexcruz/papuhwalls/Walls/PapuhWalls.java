package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class PapuhWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_papuh_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_papuhwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "papuh_walls";
    }

}
