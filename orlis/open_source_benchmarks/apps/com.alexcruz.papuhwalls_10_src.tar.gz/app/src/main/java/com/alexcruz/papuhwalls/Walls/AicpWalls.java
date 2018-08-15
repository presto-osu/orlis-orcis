package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class AicpWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_aicp_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_aicpwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "aicp_walls";
    }

}
