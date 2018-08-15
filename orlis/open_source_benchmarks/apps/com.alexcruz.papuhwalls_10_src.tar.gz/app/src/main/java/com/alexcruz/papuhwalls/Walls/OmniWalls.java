package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;


public class OmniWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_omni_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_omniwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "omni_walls";
    }

}
