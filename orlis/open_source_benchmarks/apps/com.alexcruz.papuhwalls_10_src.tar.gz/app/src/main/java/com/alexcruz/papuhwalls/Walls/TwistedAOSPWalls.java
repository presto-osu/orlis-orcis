package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class TwistedAOSPWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_twistedaosp_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_twistedaospwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "twistedaosp_walls";
    }

}
