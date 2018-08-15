package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class MustachesWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_mustaches_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_mustacheswalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "mustaches_walls";
    }

}
