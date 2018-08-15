package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class SolidsWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_solids_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_solidswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "solids_walls";
    }

}
