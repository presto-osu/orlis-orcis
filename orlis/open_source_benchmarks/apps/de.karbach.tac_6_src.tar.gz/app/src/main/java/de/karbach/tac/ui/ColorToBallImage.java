/**
	MoTAC - digital board for TAC board game
    Copyright (C) 2013-2014  Carsten Karbach
    
    Contact by mail carstenkarbach@gmx.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.tac.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * This class helps to map colors to a bitmap for
 * the player marbles. It reads the bitmaps once
 * and provides a function for the mapping.
 * 
 * @author Carsten Karbach
 *
 */
public class ColorToBallImage {

	/**
	 * The bitmaps holding the ball pictures
	 */
	private Bitmap blue,red,green,black;
	
	/**
	 * Loads the images from the given context.
	 * 
	 * @param context the bundle allowing access to the images
	 */
	public ColorToBallImage(Context context){
		Resources res = context.getResources();
		
		black = BitmapFactory.decodeResource(res, de.karbach.tac.R.drawable.black);
		green = BitmapFactory.decodeResource(res, de.karbach.tac.R.drawable.green);
		red = BitmapFactory.decodeResource(res, de.karbach.tac.R.drawable.red);
		blue = BitmapFactory.decodeResource(res, de.karbach.tac.R.drawable.blue);
	}
	
	/**
	 * @param color the color provided by the Color class, for which a bitmap is searched, e.g. Color.red, Color.green ...
	 * @return the ball bitmap corresponding to the given color
	 */
	public Bitmap colorToBitmap(int color){
		int redCol = Color.rgb(255, 0, 0);
		int greenCol = Color.rgb(0, 255, 0);
		int blueCol = Color.rgb(0, 0, 255);
		int blackCol = Color.rgb(0, 0, 0);

		if(color == redCol){
			return red;
		}
		if(color == greenCol){
			return green;
		}
		if(color == blueCol){
			return blue;
		}
		if(color == blackCol){
			return black;
		}

		return null;
	}
	
}
