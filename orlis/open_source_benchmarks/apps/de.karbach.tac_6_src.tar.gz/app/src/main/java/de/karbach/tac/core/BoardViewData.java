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

package de.karbach.tac.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import de.karbach.tac.core.DataChangeEvent.ChangeType;

/**
 * Manages all data, which is connected to a board view, but which does not need to be
 * stored. These data attributes are directly connected to the view such as scale values,
 * the position of a moving ball or the state of the moving/color switch or tac buttons.
 * 
 * @author Carsten Karbach
 *
 */
public class BoardViewData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * When moving a ball, the startPoint is saved in this attribute
	 */
	private Point startPoint = null;

	/**
	 * When moving a ball, the possible endPoint is saved in this attribute
	 */
	private Point endPoint = null;
	
	/**
	 * Saves position of currently moved ball
	 */
	private float cx,cy;

	/**
	 * Factor, by which the board view is scaled
	 */
	private float scalex = 1.0f, scaley = 1.0f;

	/**
	 * Translation vectors
	 */
	private int vx=0,vy=0;

	/**
	 * Shows the distance between the two points, when moving a ball.
	 * If -1, the distance is not displayed
	 */
	private int distance = -1;

	/**
	 * Stores if map is moved by scrolling, or if balls are moved.
	 * Default: balls are moved.
	 */
	private boolean moving = false;

	/**
	 * If true, balls will be exchanged.
	 * If false balls are simply moved
	 */
	private boolean tricksing = false;

	/**
	 * If true, the colors of two players are switched
	 */
	private boolean colorSwitch = false;

	/**
	 * Maximum allowed scale factor
	 */
	public static final float maxscale=3.0f;

	/**
	 * Minimum allowed scale factor
	 */
	public static final float minscale=1.0f;

	/**
	 * Width and height of the toasted images
	 */
	private int toastWidth=60, toastHeight=80;

	/**
	 * Stores, if toast dim was adjusted against new dimension of 
	 * the board.
	 */
	private transient boolean toastDimSet = false;
	
	/**
	 * With this factor the width of the card toast is calculated
	 */
	private static final float toastFactor = 0.2f;
	
	/**
	 * Update width and height of the toast according to
	 * the boards size.
	 * @param minDim minimum of views width or height
	 */
	public void updateToastDimension(int minDim){
		if(toastWidth == 1 || toastHeight == 1){
			toastDimSet = false;
		}
		
		if(toastDimSet){
			return;
		}
		//Define dimensions of card toast
		int cwidth = Math.round(minDim*toastFactor);
		//Known aspect ratio for all cards required here
		int cheight = (cwidth*273)/172;
		if(cwidth == 0){
			cwidth = 1;
		}
		if(cheight == 0){
			cheight = 1;
		}
		
		if(cwidth != toastWidth){
			setToastWidth(cwidth);
		}
		
		if(cheight != toastHeight){
			setToastHeight(cheight);
		}
		
		toastDimSet=true;
	}

	/**
	 * Sets a new toast width when showing the last played card.
	 * @param toastWidth new toast width in pixels
	 */
	public void setToastWidth(int toastWidth) {
		this.toastWidth = toastWidth;
		notifyChange(ChangeType.toastdimension);
	}

	/**
	 * Sets a new toast height when showing the last played card.
	 * @param toastHeight new toast height in pixels
	 */
	public void setToastHeight(int toastHeight) {
		this.toastHeight = toastHeight;
		notifyChange(ChangeType.toastdimension);
	}

	/**
	 * @return width of the toasted cards
	 */
	public int getToastWidth() {
		return toastWidth;
	}

	/**
	 * @return height of the toasted cards
	 */
	public int getToastHeight() {
		return toastHeight;
	}

	/**
	 * List of registered listeners for data changes
	 */
	private transient List<DataChangeListener> listeners;

	public BoardViewData(){
		init();
	}
	
	/**
	 * Call this function, if the state of the action points was changed.
	 * E.g. a point was marked as action starting point.
	 */
	public void actionStateChanged(){
		notifyChange(ChangeType.setActionPoint);
	}
	
	/**
	 * Init all transient values.
	 */
	private void init(){
		listeners = new ArrayList<DataChangeListener>();
		toastDimSet = false;
	}

	/**
	 * @return true, if currently colors are switched, false otherwise
	 */
	public boolean isColorSwitch() {
		return colorSwitch;
	}

	/**
	 * Turn on/off color switch mode
	 * @param colorSwitch new state of color switch mode
	 */
	public void setColorSwitch(boolean colorSwitch) {
		this.colorSwitch = colorSwitch;
		if(colorSwitch){
			setMoving(false);
		}
		notifyChange(ChangeType.colorSwitchMode);
	}

	/**
	 * @return true, if the board itself is moved, false if the balls are moved
	 */
	public boolean isMoving(){
		return moving;
	}

	/**
	 * Enable/Disable moving mode.
	 * @param moving true, if board can be moved, false otherwise
	 */
	public void setMoving(boolean moving){
		this.moving = moving;
		if(moving){
			setDistance(-1);
			setColorSwitch(false);
		}
		notifyChange(ChangeType.moveMode);
	}

	/**
	 * @return true, if balls have to be swapped or if they are simply moved.
	 */
	public boolean isTricksing(){
		return tricksing;
	}

	/**
	 * Enable/Disable tricksing mode
	 * @param tricksing true, if tricksing should be enabled, false otherwise
	 */
	public void setTricksing(boolean tricksing){
		this.tricksing = tricksing;
		if(tricksing){
			setDistance(-1);
		}
		notifyChange(ChangeType.tricksMode);
	}

	/**
	 * Add a new listener waiting for data changes.
	 * @param listener the new listener, which is registered with this function
	 */
	public void addListener(DataChangeListener listener){
		listeners.add(listener);
	}

	/**
	 * Stop a listener from listening.
	 * @param listener the unnecessary listener
	 */
	public void removeListener(DataChangeListener listener){
		listeners.remove(listener);
	}

	/**
	 * Notify all listeners about a change event.
	 * 
	 * @param changeType the type of change
	 */
	protected void notifyChange(ChangeType changeType){

		DataChangeEvent event = new DataChangeEvent(changeType, this);

		for(DataChangeListener listener: listeners){
			listener.onDataChanged(event);
		}
	}

	/**
	 * @return the current startPoint of a ball move
	 */
	public Point getStartPoint() {
		return startPoint;
	}

	/**
	 * @param startPoint When moving a ball, the startPoint is saved in this attribute
	 */
	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	/**
	 * @return the current endPoint of a ball move
	 */
	public Point getEndPoint() {
		return endPoint;
	}
	
	/**
	 * @param endPoint When moving a ball, the possible endPoint is saved in this attribute
	 */
	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}

	/**
	 * @return the distance of adjacent fields between the two points, when moving a ball.
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * @param distance the current distance of adjacent fields between the two points, when moving a ball
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * @return x position of currently moved ball
	 */
	public float getCx() {
		return cx;
	}

	/**
	 * @param cx the current x position of moved ball
	 */
	public void setCx(float cx) {
		this.cx = cx;
		notifyChange(ChangeType.ballMoving);
	}

	/**
	 * @return y position of currently moved ball
	 */
	public float getCy() {
		return cy;
	}

	/**
	 * @param cy new y position of currently moved ball
	 */
	public void setCy(float cy) {
		this.cy = cy;
		notifyChange(ChangeType.ballMoving);
	}

	/**
	 * Center the board in the middle of the available space.
	 */
	public void centerBoard(int width, int height){
		int min = width;
		if(height < min){
			min = height;
		}
		
		setVx((int)(width/2-scalex*min/2));
		setVy((int)(height/2-scaley*min/2));
		checkBoardBorders(width, height);
	}
	
	/**
	 * @return x-coordinate of translation vector
	 */
	public int getVx() {
		return vx;
	}

	/**
	 * @param vx x-coordinate of translation vector
	 */
	public void setVx(int vx) {
		this.vx = vx;
		notifyChange(ChangeType.moved);
	}

	/**
	 * @return y-coordinate of translation vector
	 */
	public int getVy() {
		return vy;
	}

	/**
	 * @param vx x-coordinate of translation vector
	 */
	public void setVy(int vy) {
		this.vy = vy;
		notifyChange(ChangeType.moved);
	}

	/**
	 * Sets start and endpoint to null.
	 * Stops moving and painting of the movement.
	 */
	public void clearMarkers(){
		startPoint = null;
		endPoint = null;
		notifyChange(ChangeType.markersCleared);
	}

	/**
	 * @return Factor, by which the board view is scaled in x-dimension
	 */
	public float getScalex() {
		return scalex;
	}

	/**
	 * @param scalex Factor, by which the board view is scaled in x-dimension
	 */
	public void setScalex(float scalex) {
		this.scalex = scalex;
		notifyChange(ChangeType.scaled);
	}

	/**
	 * @return Factor, by which the board view is scaled in y-dimension
	 */
	public float getScaley() {
		return scaley;
	}

	/**
	 * @param scaley Factor, by which the board view is scaled in y-dimension
	 */
	public void setScaley(float scaley) {
		this.scaley = scaley;
		notifyChange(ChangeType.scaled);
	}

	/**
	 * Convert from normal coordinates to scaled coordinates
	 * @param x normal x coordinate
	 * @param y normal y coordinate
	 * @return scaled point
	 */
	public Point2D scale(double x, double y){
		return new Point2D(x*scalex+vx, y*scaley+vy);
	}

	/**
	 * Convert from scaled coordinates to normal coordinates
	 * @param x scaled x coordinate
	 * @param y scaled y coordinate
	 * @return normal point
	 */
	public Point2D invScale(double x, double y){
		return new Point2D((x-vx)/scalex, (y-vy)/scaley);
	}

	/**
	 * Reset view to standard viewpoint
	 */
	public void resetView(){
		vx = 0;
		vy = 0;
		scalex = 1.0f;
		scaley = 1.0f;
	}

	/**
	 * Make sure, that the board area is entirely covered by the board
	 * @param width width of the view in pixels
	 * @param height height of the view in pixels
	 */
	public void checkBoardBorders(int width, int height){
		int min = width;
		if(height < min){
			min = height;
		}
		
		//If board fits entirely on the screen
		if(min*scalex <= width ){
			//If board leaves the screen on the right
			if(vx+min*scalex > width){
				//Make sure, that the board is aligned to the right border
				vx = (int)(width-min*scalex);
			}
			//No space wasted on the left
			if(vx < 0){
				vx = 0;
			}
		}
		else{
			//Make sure, that there is no space on the right
			if( min*scalex+vx <= width){
				vx = (int)(width-min*scalex+0.5);
			}
			
			//board does not fit onto screen,
			//check that there is no free space on the left
			if(vx > 0){
				vx = 0;
			}
		}
		//If board fits entirely on the screen
		if(min*scaley <= height){
			//If board leaves the screen on bottom
			if(vy+min*scaley > height){
				vy = (int)(height-min*scaley);
			}
			//No space wasted on top
			if(vy < 0){
				vy = 0;
			}
		}
		else{
			//Ensure, that there is no space on the bottom
			if( min*scaley+vy <= height){
				vy = (int)(height-min*scaley+0.5);
			}
			
			//board does not fit onto screen,
			//check that there is no free space on top
			if(vy > 0){
				vy = 0;
			}
		}

		notifyChange(ChangeType.bordersChecked);
	}

	public static final String filename = "viewdata.obj";
	
	/**
	 * Save this board data to file
	 * @param activity needed activity to save data.
	 */
	public void save(Activity activity){
		try {
			ObjectOutputStream oOut = new ObjectOutputStream(activity.openFileOutput(filename, Context.MODE_PRIVATE));
			oOut.writeObject(this);
			oOut.close();
			
			notifyChange(ChangeType.dataSaved);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allows static loading of a boardviewdata instance.
	 * 
	 * @param activity
	 * @return last saved instance of boarddata
	 * @throws StreamCorruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static BoardViewData loadInstance(Activity activity) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream oIn = new ObjectInputStream(activity.openFileInput(filename));
		BoardViewData loaded = (BoardViewData) oIn.readObject();
		loaded.init();
		
		return loaded;
	}

    /**
     * Make a deep copy of this boardviewdata.
     * Do not copy the transient members.
     * @return deep copy of this boardviewdata or null, if error occurred
     */
    public BoardViewData copy(){
        BoardViewData result = BoardData.copy(this);
        if(result != null){
            result.init();
        }
        return result;
    }
	
}
