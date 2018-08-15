package com.luorrak.ouroboros.util;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Exodus
public class SpoilerSpan extends ClickableSpan {
    private boolean clicked = false;

    @Override
    public void onClick(View widget) {
        clicked = true;
        widget.invalidate();
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        if (!clicked){
            ds.setColor(Color.argb(255, 00, 00, 00)); //FF000000
            ds.bgColor = Color.argb(255, 00, 00, 00); //FF000000
            ds.setUnderlineText(false);
        }
    }
}
