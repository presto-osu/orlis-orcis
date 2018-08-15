package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class SlimWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_slim_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_slimwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "slim_walls";
    }

}
