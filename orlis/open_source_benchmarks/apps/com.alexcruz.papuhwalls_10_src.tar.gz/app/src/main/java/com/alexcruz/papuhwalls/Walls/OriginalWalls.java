package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;

public class OriginalWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_original_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_originalwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "original_walls";
    }

}
