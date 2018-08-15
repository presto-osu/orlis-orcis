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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Simple button with a picture as button space
 * 
 * @author Carsten Karbach
 *
 */
public abstract class MyButton {

	/**
	 * Decoded picture
	 */
	private Bitmap picBitmap;
	
	
	/**
	 * Instance to work with
	 */
	private Board board;
	
	/**
	 * Area where the button is painted the last time
	 */
	private Rect paintArea=null;
	
	/**
	 * Color, painted above the button, if it is disabled
	 */
	private int disabledColor = Color.argb(128, 128, 128, 128);
	
	/**
	 * Color, painted above the button, if it is enabled
	 */
	private int enabledColor = Color.argb(0, 128, 255, 128);
	
	/**
	 * Defines if button is enabled or disabled
	 */
	private boolean enabled;
	
	public MyButton(int id, Board board){
		this.board = board;
		Resources res = board.getContext().getResources();
		picBitmap = BitmapFactory.decodeResource(res, id);
		
		enabled = true;
	}
	
	/**
	 * En/Disables button
	 * @param enabled true, if button should be enabled
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	/**
	 * Set the color painted, when button is disabled
	 * @param color the disabled color
	 */
	public void setDisabledColor(int color){
		disabledColor = color;
	}
	
	/**
	 * Set the color painted, when button is enabled
	 * @param color the enabled color
	 */
	public void setEnabledColor(int color){
		enabledColor = color;
	}
	
	/**
	 * Draw the button in a given area
	 * @param canvas
	 * @param area
	 * @param paint
	 */
	public void drawButton(Canvas canvas, Rect area, Paint paint){
		paint.setColor(Color.WHITE);
		paintArea = new Rect(area);
		canvas.drawBitmap(picBitmap, new Rect(0,0,picBitmap.getWidth()-1, picBitmap.getHeight()-1),
							area, paint);
		//Grey out button if disabled
		if(! enabled){
			paint.setColor(disabledColor);
		}
		else{
			paint.setColor(enabledColor);
		}
		canvas.drawRect(area, paint);
	}
	
	/**
	 * @return the original width of the bitmap
	 */
	public int getPreferredWidth(){
		return picBitmap.getWidth();
	}
	
	/**
	 * @return the original height of the bitmap
	 */
	public int getPreferredHeight(){
		return picBitmap.getHeight();
	}
	
	/**
	 * This function is called every time the button is clicked
	 */
	public abstract void onClick();
	
	/**
	 * @param x
	 * @param y
	 * @return true, if point (x,y) is in paint area
	 */
	public boolean isPointInArea(int x, int y){
		if(paintArea == null){
			return false;
		}
		if(x < paintArea.left ){
			return false;
		}
		if(x > paintArea.right ){
			return false;
		}
		
		if(y > paintArea.bottom ){
			return false;
		}
		if(y < paintArea.top ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return board, on which this button is working on
	 */
	public Board getBoard(){
		return board;
	}
	
}
