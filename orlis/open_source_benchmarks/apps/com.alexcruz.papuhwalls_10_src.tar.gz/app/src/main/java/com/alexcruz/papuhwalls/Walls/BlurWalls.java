package com.alexcruz.papuhwalls.Walls;

import com.alexcruz.papuhwalls.R;

public class BlurWalls extends AbsWalls {

    @Override
    public int getTitleId() {
        return R.string.section_blur_walls;
    }

    @Override
    public int getUrlId() {
        return R.string.json_blurwalls_url;
    }

    @Override
    public String getJsonArrayName() {
        return "blur_walls";
    }

}
