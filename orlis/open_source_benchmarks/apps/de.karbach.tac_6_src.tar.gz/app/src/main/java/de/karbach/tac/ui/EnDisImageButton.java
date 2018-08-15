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

import android.app.Notification.Action;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
import de.karbach.tac.R;

/**
 * An image button, which can be enabled and disabled visually.
 * 
 * @author Carsten Karbach
 *
 */
public class EnDisImageButton extends ImageButton{

	/**
	 * This is run to let the button blink in a given interval.
	 */
	private class BlinkRunner implements Runnable{

		private boolean active = true;
		
		/**
		 * Stop this thread running.
		 */
		public void deactivate(){
			active = false;
		}
		
		@Override
		public void run() {
			while(active){
				blinkOn = !blinkOn;
				postInvalidate();
				try {
					Thread.sleep(blinkInterval);
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
	
	/**
	 * paint instance needed on each draw action
	 */
	private Paint paint = new Paint();
	
	/**
	 * If true and active, the green transparent is blinking in a given interval.
	 */
	private boolean blinking=false;
	
	/**
	 * Runnable used for blinking.
	 */
	private BlinkRunner blinker;
	
	/**
	 * The interval the button is shown in one color when blinking
	 */
	private final static int blinkInterval=500;
	
	/**
	 * If blinkOn is true the current blink status is to show the green color
	 * Otherwise the color is not shown.
	 */
	private boolean blinkOn = true;
	
	/**
	 * if active, button is colored green, otherwise gray
	 */
	private boolean active = true;
	
	/**
	 * Image painted on top of the src image, if this button is active.
	 * If this bitmap is not set, the src image is overlayed with green and grey color
	 */
	private Bitmap activeImage;
	/**
	 * ID of the drawable for the activeImage
	 */
	private int activeImageDrawableID;
	/**
	 * stores the ID of the inactive image
	 */
	private Bitmap inactiveImage;
	
	/**
	 * Set to true, if user is holding finger down on this button
	 */
	private boolean mouseDown = false;
	
	/**
	 * @return true, if mouse is down, false otherwise
	 */
	public boolean isMouseDown() {
		return mouseDown;
	}

	/**
	 * Set to true, if user is doing mouse down action, false as soon as finger releases the button.
	 * @param mouseDown true for mouse holding down
	 */
	public void setMouseDown(boolean mouseDown) {
		this.mouseDown = mouseDown;
	}
	
	/**
	 * Change view output on touch down.
	 * Mark touch down/ up events with stroker.
	 */
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = super.onTouchEvent(event);
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			setMouseDown(true);
			invalidate();
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			setMouseDown(false);
			invalidate();
		}
		
		return consumed;
	}

	/**
	 * Let the button blink, if it is active.
	 * 
	 * @param blinking true for blinking on, false for no blinking
	 */
	public void setBlinking(boolean blinking){
		this.blinking = blinking;
		if(blinker!=null){
			blinker.deactivate();
		}
		if(blinking){
			blinker = new BlinkRunner();
			new Thread(blinker).start();
		}
	}
	
	/**
     * Sets as the active image shown for the button.
     * If button is inactive the normal image is shown, otherwise the inactive image.
     *
     * <p class="note">This does Bitmap reading and decoding on the UI
     * thread, which can cause a latency hiccup.  If that's a concern,
     * consider using {@link #setImageDrawable(android.graphics.drawable.Drawable)} or
     * {@link #setImageBitmap(android.graphics.Bitmap)} and
     * {@link android.graphics.BitmapFactory} instead.</p>
     *
     * @param resId the resource identifier of the drawable
     *
     * @attr ref android.R.styleable#ImageView_src
     */
    public void setActiveimage(int resId) {

		//this card is loaded for the first time
		Resources res = getContext().getResources();
		activeImage = BitmapFactory.decodeResource(res, resId);
		
		activeImageDrawableID = resId;
    }
    
    /**
     * 
     * @return
     */
    public int getActiveimage(){
    	return activeImageDrawableID;
    }
	
	public EnDisImageButton(Context context){
		super(context, null);
		init(context, null);
	}

	public EnDisImageButton(Context context, AttributeSet attrs){
		super(context, attrs, 0);
		init(context, attrs);
	}

	public EnDisImageButton(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	/**
	 * Common operations for all constructors
	 */
	private void init(Context context, AttributeSet attrs){
		paint.setStyle(Style.FILL);
		
		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.EnDisImageButton);
		Integer activeId = arr.getResourceId(R.styleable.EnDisImageButton_activeimage, -1);
		if (activeId != -1) {
			setActiveimage(activeId);
		}
		arr.recycle();  // Do this when done.
	}
	
	public void setActive(boolean active){
		boolean old = this.active;
		this.active = active;
		if(old != active){
			this.postInvalidate();
		}
	}
	
	public boolean isActive(){
		return active;
	}
	
	/**
	 * @return the rectangle in which to paint the image in this view 
	 */
	protected Rect getDestRectForImage(){
		if(activeImage == null){
			return null;
		}
		
		double widthFac = getWidth()/(double)activeImage.getWidth();
		double heightFac = getHeight()/(double)activeImage.getHeight();
		
		double fac = widthFac;
		if(heightFac < widthFac){
			fac = heightFac;
		}
		
		int newHeight = (int)Math.round(fac*activeImage.getHeight());
		int newWidth = (int)Math.round(fac*activeImage.getWidth());
		
		int spaceWidth = getWidth()-newWidth;
		int spaceHeight = getHeight()-newHeight;
		
		return new Rect(spaceWidth/2,spaceHeight/2,newWidth+spaceWidth/2, newHeight+spaceHeight/2);
	}
	
	/**
	 * Draw green color transparently on the button, if enabled.
	 * Otherwise gray color on top of the image.
	 */
	public void draw (Canvas canvas){
		
		boolean drawActive;
		
		if(active && !(blinking && !blinkOn)){ //No active color if blinking is enabled and blinkOn is off
			drawActive = true;
		}
		else{
			drawActive = false;
		}
		
		Rect dest = getDestRectForImage();//The target space for the image
		
		if(activeImage == null){
		
			super.draw(canvas);
			
			if(drawActive){ //No active color if blinking is enabled and blinkOn is off
				paint.setColor(Color.argb(100, 0, 255, 0));
			}
			else{
				paint.setColor(Color.argb(100, 100, 100, 100));
			}
			paint.setStyle(Style.FILL);
			
			float right = getWidth()-1;
			float bottom = getHeight()-1;
			
			if(getDrawable() != null){
				right = getDrawable().getBounds().width();
				bottom = getDrawable().getBounds().height();
			}
			
			float left = 0;
			float top = 0;
			
			if(getImageMatrix() != null){
				float[] edges ={left,top,right,bottom};
				getImageMatrix().mapPoints(edges);
				left = edges[0];
				top = edges[1];
				right = edges[2];
				bottom = edges[3];
			}
			
			canvas.drawRect(left+getPaddingLeft(),top+getPaddingTop(),right+getPaddingRight(), bottom+getPaddingBottom(),paint);
		}
		else{//There is an active image, use src and activeImage for indicating the button status

			if(! drawActive){
				super.draw(canvas);
			}
			else{
				Rect src = new Rect(0,0,activeImage.getWidth()-1,activeImage.getHeight()-1);
				
				canvas.drawBitmap(activeImage, src, dest, paint);
			}
			
			if(isMouseDown()){
				paint.setStyle(Style.STROKE);
				float strokeWidth = getWidth()*0.05f;
				paint.setStrokeWidth(strokeWidth);
				if(drawActive){
					paint.setColor(Color.argb(100, 100, 100, 100));
				}
				else{
					paint.setColor(Color.argb(100, 150, 150, 150));
				}
				
				float roundRadius = getWidth()*0.1f;
				
				RectF destf = new RectF(dest.left+strokeWidth/2, dest.top+strokeWidth/2, 
						dest.right-strokeWidth/2, dest.bottom-strokeWidth/2);
				
				canvas.drawRoundRect(destf, roundRadius, roundRadius, paint);
				
				paint.setColor(Color.argb(255, 100, 100, 100));
				paint.setStyle(Style.FILL);
			}
		}
		
	}
	
	
	
}
