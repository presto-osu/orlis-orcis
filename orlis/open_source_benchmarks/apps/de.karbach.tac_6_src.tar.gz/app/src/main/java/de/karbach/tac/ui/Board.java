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

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import de.karbach.tac.R;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.BoardViewData;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.DataChangeEvent;
import de.karbach.tac.core.DataChangeEvent.ChangeType;
import de.karbach.tac.core.DataChangeListener;
import de.karbach.tac.core.Point;
import de.karbach.tac.core.Point2D;

/**
 * The painted board. Uses BoardData for storable data and BoardViewData
 * for attributes directly connected to the view. Note, that the board only READS data
 * from these data objects. It does not have any functions, which change the data model.
 * 
 * @author Carsten Karbach
 *
 */
public class Board extends View implements DataChangeListener{

	/**
	 * Background board image
	 */
	private Bitmap boardPic;

	/**
	 * Logical board data painted by this board
	 */
	protected BoardData data;

	/**
	 * Data directly connected to the view
	 */
	protected BoardViewData viewdata;

	/**
	 * Instance for painting
	 */
	private Paint paint;

	/**
	 * Mapping of ball Ids to their colors
	 */
	private SparseIntArray ballIDToColor;

	/**
	 * Mapping of ball Ids to their bitmaps
	 */
	private SparseArray<Bitmap> ballIDToBMP;

	/**
	 * Maps to each integer number a card, which is painted in the center of the board
	 */
	private Bitmap backsideBMP;

	/**
	 * Maps android colors to the ball images
	 */
	private ColorToBallImage colorToImage;
	
	/**
	 * The actionpoint is marked by a halo with the radius of a normal ball multiplied with this factor
	 */
	private final double actionRadiusFactor = 1.5;
	
	/**
	 * @param context
	 */
	public Board(Context context){
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public Board(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Board(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	/**
	 * Drawable ID of grey ball for indicating the position, where the ball is moved to
	 */
	private static final int greyBallId = R.drawable.grey;
	
	/**
	 * Reassign the colors painted for the players.
	 * 
	 */
	protected void initBallColors(){
		ballIDToColor = new SparseIntArray();
		ballIDToBMP = new SparseArray<Bitmap>();

		int players = 4;

		for(int col=0; col<players; col++ ){
			int color = data.getColors().get(col);
			for(int id = 0+col*players; id<(col+1)*players; id++){
				ballIDToColor.put(id, color);
				ballIDToBMP.put(id, colorToImage.colorToBitmap(color));
			}
		}
		
		Bitmap greyBallBitmap = BitmapFactory.decodeResource(this.getResources(), greyBallId);
		ballIDToBMP.put(greyBallId, greyBallBitmap);
	}

    /**
     *
     * @param id the id of the player
     * @return the bitmap for a corresponding ball image
     */
    public Bitmap getBallbitmapForId(int id){
        return ballIDToBMP.get(id);
    }

	/**
	 * Set new data model for this board view
	 * @param data new data, null is not allowed
	 */
	public void setData(BoardData data, BoardViewData viewdata) {
		if(this.data != null){
			this.data.removeListener(this);
		}
		this.data = data;
		this.data.addListener(this);

		if(this.viewdata != null){
			this.viewdata.removeListener(this);
		}
		this.viewdata = viewdata;
		this.viewdata.addListener(this);
		
		firstDraw = true;

		Resources res = getContext().getResources();
		boardPic = BitmapFactory.decodeResource(this.getResources(), de.karbach.tac.R.drawable.board);

		//Load card pictures
		backsideBMP = BitmapFactory.decodeResource(res, de.karbach.tac.R.drawable.backside);

		//Load bitmaps for balls
		colorToImage = new ColorToBallImage(getContext());

		paint = new Paint();

		initBallColors();

		this.invalidate();
	}

	/**
	 * @return the board data holding all information about the current game
	 */
	public BoardData getBoardData(){
		return data;
	}
	
	/**
	 * @return width, if width<height, otherwise height of this view
	 */
	protected int getMinimumOfHeightAndWidth(){
		int min = getWidth();
		if(getHeight() < min){
			min = getHeight();
		}
		return min;
	}
	
	/**
	 * Holds coordinates on this view of the currently moving ball.
	 */
	private Point2D movingBallCenter;
	
	/**
	 * Stores the field, on which the moving ball will be placed
	 */
	private Point endPos;
	
	/**
	 * Stores the radius of the painted moving ball in pixels
	 */
	private int scaledRadius;
	
	/**
	 * Stores rectangle, in which the distance card in the center of the board is painted
	 */
	private Rect distanceCardRect = new Rect();
	
	/**
	 * Factor with which the card width is calculated
	 */
	public static final double cardWidthFactor = 0.12;
	
	/**
	 * Rectangle used in paintings, to avoid constructor calls in draw function.
	 */
	private Rect helpRect1 = new Rect();
	
	/**
	 * Rectangle used in paintings, to avoid constructor calls in draw function.
	 */
	private Rect helpRect2 = new Rect();
	
	/**
	 * Is set to true, if the first draw is not made so far.
	 * False, if the board was painted before.
	 */
	private boolean firstDraw = true;

    /**
     * Render the board into a bitmap and return it.
     *
     * @param width the width of the bitmap
     * @param height the height of the bitmap
     * @return the generated bitmap containing the board
     */
    public Bitmap generateBitmapFromView(int width, int height) {
        Bitmap b = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        this.onScaledDraw(c, width, height);
        return b;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.onScaledDraw(canvas, getWidth(), getHeight());
    }

    /**
     * Draw the content of this view with any width or height
     * @param canvas the canvas to draw on
     * @param width width of the target view
     * @param height height of the target view
     */
	protected void onScaledDraw(Canvas canvas, int width, int height) {
		//Do not draw if not initialized
		if(data==null){
			return;
		}
		
		//Set board to the center of available space
		if(width > 0 && height > 0 && firstDraw){
			firstDraw = false;
			viewdata.centerBoard(width, height);
		}

		int min = width;
        if(height < width){
            min = height;
        }

		//Clear board
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);

		// Draw background
		Point2D upperLeft = viewdata.scale(0,0);
		Point2D lowerRight = viewdata.scale(min-1,min-1);
		
		//Draw shadow
		float shadowFactor = 1.025f;
		Point2D shadowLowerRight = viewdata.scale(min*shadowFactor, min*shadowFactor);
		int sx = lowerRight.x;
		int sy = lowerRight.y;
		int minAlpha = 20;
		int maxAlpha = 0;
		paint.setColor(Color.BLACK);
		int i = 0;
		while(sx+i < shadowLowerRight.x){
			int alpha = (int)Math.round( (sx+i-lowerRight.x)/((double)(shadowLowerRight.x-lowerRight.x))*(maxAlpha-minAlpha)+minAlpha );
			
			paint.setAlpha(alpha);
			canvas.drawRect(upperLeft.x, upperLeft.y, sx+i, sy+i, paint);
			
			i++;
		}
		//Shadow drawn
		
		paint.setColor(Color.WHITE);
        helpRect1.set(0, 0, boardPic.getWidth() - 1, boardPic.getHeight() - 1);
        helpRect2.set(upperLeft.x, upperLeft.y, lowerRight.x, lowerRight.y);
        canvas.drawBitmap(boardPic, helpRect1, helpRect2, paint);

		int circleRadius = min/50;
		scaledRadius = (int)(viewdata.getScalex()*circleRadius);

		//Draw points and balls
		ArrayList<Point> points = data.getPoints();
		int emptyPointColor = Color.rgb(0, 217, 255);
		int startColor = Color.LTGRAY;
		int endColor = Color.GRAY;
		
		int actionColor = Color.argb(120, 255, 255, 100);
		
		int moveColor = 0;
		if(viewdata.getStartPoint() != null){
			if(viewdata.getStartPoint().getBallID() != -1){
				moveColor = ballIDToColor.get(viewdata.getStartPoint().getBallID());
			}
		}
		paint.setColor(emptyPointColor);
		for(Point p: points){
			
			int paintRadius = circleRadius;
			
			//Paint halo for the actionpoint
			if(p.isActionField()){
				paint.setColor(actionColor);
				Point2D center = viewdata.scale( (int)(p.getPosX()*min), (int)(p.getPosY()*min));
				
				double largestRadius = scaledRadius*actionRadiusFactor;
				int ri=0;
				int minBallAlpha = 80;
				int maxBallAlpha = 50;
				while(scaledRadius+ri < largestRadius){
					int alpha = (int)Math.round( ri/((double)(largestRadius-scaledRadius))*(maxBallAlpha-minBallAlpha)+minBallAlpha );
					
					paint.setAlpha(alpha);
					canvas.drawCircle( center.x, center.y, Math.round(scaledRadius+ri), paint);
					
					ri++;
				}
				
				paint.setAlpha(255);
			}
			
			boolean paintCircle = false;
			Bitmap pointBitmap = null;
			if(p.getBallID() != -1){
				paint.setColor(ballIDToColor.get(p.getBallID()));
				pointBitmap = ballIDToBMP.get(p.getBallID());
				paintCircle = true;
			}
			else{
				paint.setColor(emptyPointColor);
			}
			if(p == viewdata.getStartPoint()){
				paint.setColor(startColor);
				paintCircle = true;
			}
			if(p == viewdata.getEndPoint()){
				endPos = p;
				paint.setColor(endColor);
				pointBitmap = ballIDToBMP.get(greyBallId);
				paintCircle = true;
			}
			
			if(paintCircle){
				if(pointBitmap != null){
					Point2D dstUL = viewdata.scale((int)(p.getPosX()*min-paintRadius), (int)(p.getPosY()*min-paintRadius));
					Point2D dstLR = viewdata.scale((int)(p.getPosX()*min+paintRadius), (int)(p.getPosY()*min+paintRadius));
					helpRect1.set(0,0,pointBitmap.getWidth()-1, pointBitmap.getHeight()-1);
					Rect src =  helpRect1;
					helpRect2.set(dstUL.x, dstUL.y, dstLR.x, dstLR.y);
					Rect dst = helpRect2;
					canvas.drawBitmap(pointBitmap, src, dst, paint);
				}
				else{
					Point2D center = viewdata.scale( (int)(p.getPosX()*min), (int)(p.getPosY()*min));

					canvas.drawCircle( center.x, center.y, scaledRadius, paint);
				}
			}
		}
		//Paint moving ball
		if(viewdata.getEndPoint() != null){
			//Store position of moved ball for optimized invalidation of the view
			movingBallCenter = viewdata.scale(viewdata.getCx(), viewdata.getCy());
			
			paint.setColor(moveColor);
			Bitmap pointBitmap = ballIDToBMP.get(viewdata.getStartPoint().getBallID());
			if(pointBitmap != null){
				helpRect1.set(0,0,pointBitmap.getWidth()-1, pointBitmap.getHeight()-1);
				Rect src =  helpRect1;

				Point2D dstUL = viewdata.scale(viewdata.getCx()-circleRadius, viewdata.getCy()-circleRadius);
				Point2D dstLR = viewdata.scale(viewdata.getCx()+circleRadius, viewdata.getCy()+circleRadius);

				helpRect2.set(dstUL.x, dstUL.y, dstLR.x, dstLR.y);
				Rect dst = helpRect2;
				canvas.drawBitmap(pointBitmap, src, dst, paint);
			}
			else{
				Point2D center = movingBallCenter;
				canvas.drawCircle( center.x, center.y, scaledRadius, paint);
			}
		}

		drawDistanceInCenter(canvas, min);

	}
	
	/**
	 * Draw a card in the center of the board, which indicates the current
	 * distance of the current move.
	 * 
	 * @param canvas the canvas to draw on
     * @param mindistance the minimum of width and height of the view to draw
	 */
	protected void drawDistanceInCenter(Canvas canvas, int mindistance){
		int min = mindistance;
		
		if(viewdata.getDistance() > -1 && viewdata.getDistance() < 40){
			int cardWidth = (int)Math.round(min*cardWidthFactor);
			int cardHeight = (int)( ((float)cardWidth) / backsideBMP.getWidth() * backsideBMP.getHeight() );
			Point2D cardUL = viewdata.scale((int)(0.5*min-cardWidth/2), (int)(0.5*min-cardHeight/2));
			Point2D cardLR = viewdata.scale((int)(0.5*min+cardWidth/2), (int)(0.5*min+cardHeight/2));
			
			distanceCardRect.set(cardUL.x, cardUL.y, cardLR.x, cardLR.y);
			paintCardAndText(canvas, distanceCardRect, viewdata.getDistance());
		}
	}
	
	/**
	 * Paints a card onto a canvas and paints the card's value with
	 * a label on top of the card.
	 */
	private void paintCardAndText(Canvas canvas, Rect pos, int cardValue){
		Card card = new Card(String.valueOf(cardValue));
		card.setDistance(cardValue);
		paint.setColor(Card.getTextColorForDistance(cardValue));
		boolean drawText = Card.getCardType()==1;
		card.draw(this.getContext(), canvas, pos.width(), pos.height(), pos.left, pos.top, 0, drawText);
	}

	/**
	 * Creates an image for the card with the given distance
	 * painted on in. Displays this card for some time as a 
	 * toast on the board.
	 * @param cardValue the number of the card
	 * @param twidth width of toast in pixels
	 * @param theight height of toast in pixels
	 */
	private void toastCard(int cardValue, int twidth, int theight){
		
		ImageView imgView = new ImageView(getContext());
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap paintBMP = Bitmap.createBitmap(twidth, theight, conf); // this creates a MUTABLE bitmap
		Canvas canvas = new Canvas(paintBMP);
		
		paintCardAndText(canvas, new Rect(0,0,twidth,theight), cardValue);

		imgView.setImageBitmap(paintBMP);
		//Create and show the toast in the screen's center
		Toast imgToast = new Toast(getContext());
		imgToast.setGravity(Gravity.END|Gravity.BOTTOM, 0, 0);
		imgToast.setDuration(Toast.LENGTH_SHORT);
		imgToast.setView(imgView);

		imgToast.show();
	}

	@Override
	public void onDataChanged(DataChangeEvent event) {
		
		viewdata.updateToastDimension(getMinimumOfHeightAndWidth());
		
		switch(event.getEventType()){
		case moveBalls: 
			if(viewdata.getDistance() >= 0){
				toastCard(viewdata.getDistance(), viewdata.getToastWidth(), viewdata.getToastHeight() );
			}
			break;
		case ballStarted: toastCard(1, viewdata.getToastWidth(), viewdata.getToastHeight() );
		break;
		case colorSwitch: initBallColors();
		break;
		default:
			break;
		}
		//If only ball is moved, do not repaint everything => better performance
		if(event.getEventType() == ChangeType.ballMoving && movingBallCenter!= null && endPos!=null && distanceCardRect != null){
			int min = getMinimumOfHeightAndWidth();
			//Clear more to avoid artifacts
			int clearRadius = scaledRadius+2;
			
			//Repaint only the position of the moving ball and the new and old destination location
			
			//old Position of moving ball
			this.invalidate(movingBallCenter.x-clearRadius, movingBallCenter.y-clearRadius, movingBallCenter.x+clearRadius, movingBallCenter.y+clearRadius);
			//New position of moving ball, get position from changed data
			Point2D newMovingBallCenter = viewdata.scale(viewdata.getCx(), viewdata.getCy());
			this.invalidate(newMovingBallCenter.x-clearRadius, newMovingBallCenter.y-clearRadius, newMovingBallCenter.x+clearRadius, newMovingBallCenter.y+clearRadius);
			
			//Position of old destination
			Point2D center = viewdata.scale( (int)(endPos.getPosX()*min), (int)(endPos.getPosY()*min));
			this.invalidate(center.x-clearRadius, center.y-clearRadius, center.x+clearRadius, center.y+clearRadius);
			//Position of new destination:
			Point newEndPoint = viewdata.getEndPoint();
			if(newEndPoint != endPos){
				Point2D newCenter = viewdata.scale( (int)(newEndPoint.getPosX()*min), (int)(newEndPoint.getPosY()*min));
				this.invalidate(newCenter.x-clearRadius, newCenter.y-clearRadius, newCenter.x+clearRadius, newCenter.y+clearRadius);
			}
			
			//Repaint also the space for the distance card in the center
			this.invalidate(distanceCardRect);
			
		}
		else{
			//Paint entire view, because noone knows where exactly the changes occurred
			this.invalidate();
		}
	}
}
