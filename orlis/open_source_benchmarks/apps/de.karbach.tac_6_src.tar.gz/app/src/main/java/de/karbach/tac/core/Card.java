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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.SparseArray;
import android.view.View;
import de.karbach.tac.R;

/**
 * One card in the game. Identified by
 * an integer, which is also the id of the drawable
 * bitmap.
 * 
 * @author Carsten Karbach
 *
 */
public class Card implements Serializable{

	/**
	 * ID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Unique ID of this card among all cards in the game
	 */
	private int id;

	/**
	 * Id of the drawable card.
	 * This can be changed due to preferences.
	 * A card can change the drawable depending on the card type defined in the preferences.
	 */
	private int drawableId;

	/**
	 * Bitmap of the card.
	 */
	private transient Bitmap bitmap;

	/**
	 * Stores refrences to the bitmaps, in order to minimize the decodings
	 */
	private transient static SparseArray<Bitmap> idToBitmap = new SparseArray<Bitmap>();

	/**
	 * The distance attached to this card.
	 * Helpful for cards showing arbitrary distances.
	 */
	private int distance = -1;

	/**
	 * Useful for the CardGrid: if enabled==false the card is greyed
	 */
	private transient boolean enabled = true;

	/**
	 * Starting center point of animation of this card.
	 * This point is not yet scaled with viewdata.
	 * The scaling could change during the animation!
	 * 
	 */
	private Point animationStart = null;

	/**
	 * Target center point o animation of this card
	 * This point is not yet scaled with viewdata.
	 * The scaling could change during the animation!
	 */
	private Point animationEnd = null;

	/**
	 * Number of steps for the animation
	 */
	private static final int steps = 40;

	/**
	 * Time in milliseconds between each step of animation
	 * interval*steps is the time in milliseconds for the entire animation
	 */
	private static final int interval = 10;

	/**
	 * Current step of animation
	 */
	private int animationStep = 0;

	/**
	 * If true, this card is being animated and displayed as moving card.
	 * The draw function changes behaviour accordingly.
	 */
	private boolean isAnimated = false;

	/**
	 * If known, the player ID of the player, who played this card is stored here
	 */
	private int playedById = -1;

	/**
	 *
	 * @return Array holding the IDs for all balls involved in this move, or null, if this was not set
	 */
	public int[] getInvolvedBallIDs() {
		return involvedBallIDs;
	}

	/**
	 * Set all ball IDs, which are involved in the playing of this card
	 * @param involvedBallIDs Array holding the IDs for all balls involved in this move.
	 */
	public void setInvolvedBallIDs(int[] involvedBallIDs) {
		this.involvedBallIDs = involvedBallIDs;
	}

	/**
	 * Array holding the IDs for all balls involved in this move.
	 * This is especially interesting for the trickser card.
	 */
	private int[] involvedBallIDs;

	/**
	 * @return the player ID of the player, who played this card or -1, if the player is unknown
	 */
	public int getPlayedById() {
		return playedById;
	}

	/**
	 * Store the player ID of the player, who played this card.
	 * @param playedById the player ID of the player, who played this card
	 */
	public void setPlayedById(int playedById) {
		this.playedById = playedById;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * If selected the card is drawn with a green layer
	 */
	private transient boolean selected = false;

	/**
	 * Stores the string name for this card, allowed values are
	 * "trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13"
	 */
	private String cardName;
	
	/**
	 * Init the card with the given card name.
	 * @param cardName the name for this card, allowed values are "trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13" 
	 */
	public Card(String cardName){
		this.cardName = cardName;
		this.drawableId = getDrawableForCardName(cardName);
		bitmap = null;
		id = -1;

		setDistance(getDistanceForDrawable(drawableId));
	}

	/**
	 * Store the distance attached to this card.
	 * @param dist the integer distance for this card
	 */
	public void setDistance(int dist){
		this.distance = dist;
	}

	/**
	 * Get the distance stored for this card.
	 * 
	 * @return distance stored for this card
	 */
	public int getDistance(){
		return this.distance;
	}

	/**
	 * Get the card name provided through the constructor.
	 * @return the name of this card, possible values "trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13"
	 */
	public String getCardName(){
		return cardName;
	}
	
	/**
	 * Get the distance for a given card Name.
	 * @param cardName the name of the card with which a distance might be associated (e.g. "1", "2", ...)
	 * 
	 * @return the distance for this card name
	 */
	public static int getDistanceForCardName(String cardName){
		
		try{
			int res = Integer.parseInt(cardName);
			return res;
		}
		catch(NumberFormatException e){
			return -1;
		}
	}
	
	/**
	 * Inverse function to getDrawableForDistance.
	 * Input is a drawable ID, output is the distance of the card.
	 * 
	 * @param drawableId the ID of the drawable card
	 * @return the distance for this card or -1, if there is no distance (e.g. for TAC, Trickser...)
	 */
	public static int getDistanceForDrawable(int drawableId){
		switch(drawableId){
		case R.drawable.normalcards_eins: return 1;
		case R.drawable.normalcards_zwei: return 2;
		case R.drawable.normalcards_drei: return 3;
		case R.drawable.normalcards_vier: return 4;
		case R.drawable.normalcards_fuenf: return 5;
		case R.drawable.normalcards_sechs: return 6;
		case R.drawable.normalcards_sieben: return 7;
		case R.drawable.normalcards_acht: return 8;
		case R.drawable.normalcards_neun: return 9;
		case R.drawable.normalcards_zehn: return 10;
		case R.drawable.normalcards_zwoelf: return 12;
		case R.drawable.normalcards_dreizehn: return 13;
		}
		//Second card type
		switch(drawableId){
		case R.drawable.bigcards_eins: return 1;
		case R.drawable.bigcards_zwei: return 2;
		case R.drawable.bigcards_drei: return 3;
		case R.drawable.bigcards_vier: return 4;
		case R.drawable.bigcards_fuenf: return 5;
		case R.drawable.bigcards_sechs: return 6;
		case R.drawable.bigcards_sieben: return 7;
		case R.drawable.bigcards_acht: return 8;
		case R.drawable.bigcards_neun: return 9;
		case R.drawable.bigcards_zehn: return 10;
		case R.drawable.bigcards_zwoelf: return 12;
		case R.drawable.bigcards_dreizehn: return 13;
		}

		return -1;
	}

	/**
	 * Stores the cardType, which will be used by all cards to determine the used card layout
	 * Default is the original cards.
	 */
	private static int cardType=1;
	
	/**
	 * Change the card type used by all cards in future renderings.
	 * 
	 * @param type int, choose card type, either 1 for original cards or 2 for the mobile cards
	 */
	public static void setCardType(int type){
		if(type!=1 && type!=2){
			return;
		}
		cardType = type;
	}
	
	/**
	 * @return ID of card type, which should be used currently
	 */
	public static int getCardType(){
		return cardType;
	}

	/**
	 * Get the name for a card fom the specific drawable id 
	 * @param drawableId the drawable for which the card name is searched
	 * @return card name ("trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13") or null if no card was found
	 */
	public static String getCardNameFromDrawableId(int drawableId){
		switch(drawableId){
		case R.drawable.tac: return "tac";
		case R.drawable.teufel: return "teufel";
		case R.drawable.engel: return "engel";
		case R.drawable.narr: return "narr";
		case R.drawable.normalcards_trickser: return "trickser";
		case R.drawable.normalcards_krieger: return "krieger";
		case R.drawable.normalcards_eins: return "1";
		case R.drawable.normalcards_zwei: return "2";
		case R.drawable.normalcards_drei: return "3";
		case R.drawable.normalcards_vier: return "4";
		case R.drawable.normalcards_fuenf: return "5";
		case R.drawable.normalcards_sechs: return "6";
		case R.drawable.normalcards_sieben: return "7";
		case R.drawable.normalcards_acht: return "8";
		case R.drawable.normalcards_neun: return "9";
		case R.drawable.normalcards_zehn: return "10";
		case R.drawable.normalcards_zwoelf: return "12";
		case R.drawable.normalcards_dreizehn: return "13";
		//Second card type
		case R.drawable.bigcards_trickser: return "trickser";
		case R.drawable.bigcards_krieger: return "krieger";
		case R.drawable.bigcards_eins: return "1";
		case R.drawable.bigcards_zwei: return "2";
		case R.drawable.bigcards_drei: return "3";
		case R.drawable.bigcards_vier: return "4";
		case R.drawable.bigcards_fuenf: return "5";
		case R.drawable.bigcards_sechs: return "6";
		case R.drawable.bigcards_sieben: return "7";
		case R.drawable.bigcards_acht: return "8";
		case R.drawable.bigcards_neun: return "9";
		case R.drawable.bigcards_zehn: return "10";
		case R.drawable.bigcards_zwoelf: return "12";
		case R.drawable.bigcards_dreizehn: return "13";
		default: return null;
		}
	}
	
	/**
	 * Provide a card name as string.
	 * This function returns the drawable ID, which needs to be painted according to the current
	 * card type defined by the preferences.
	 * 
	 * @param cardName name of the searched card, allowed values: "trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13"
	 * @return drawable ID, which is to be painted
	 */
	public static int getDrawableForCardName(String cardName){
		if(cardName == null){
			return R.drawable.backside;
		}
		
		if(cardName.equals("tac")){
			return R.drawable.tac;
		}
		if(cardName.equals("teufel")){
			return R.drawable.teufel;
		}
		if(cardName.equals("engel")){
			return R.drawable.engel;
		}
		if(cardName.equals("narr")){
			return R.drawable.narr;
		}
		
		int cardType = getCardType();
		if(cardName.equals("trickser")){
			if(cardType == 1){
				return R.drawable.normalcards_trickser;
			}
			else if(cardType == 2){
				return R.drawable.bigcards_trickser;
			}
		}
		if(cardName.equals("krieger")){
			if(cardType==1){
				return R.drawable.normalcards_krieger;
			}
			else if(cardType == 2){
				return R.drawable.bigcards_krieger;
			}
		}

		try{
			int dist = Integer.parseInt(cardName);
			return getDrawableForDistance(dist);
		}
		catch(NumberFormatException e){
			return R.drawable.backside;
		}
	}
	
	/**
	 * Retrieve the card name as String for a distance.
	 * 
	 * @param distance distance between 1 and 13
	 * @return the name for the corresponding card
	 */
	public static String getCardNameForDistance(int distance){
		return ""+distance;
	}

	/**
	 * Searches for a drawable ID with the passed value.
	 * If there is no such card, the black backface of the tac cards
	 * is returned.
	 * 
	 * @param distance the distance or value of the card.
	 * 
	 * @return the drawable ID of a card for a given distance.
	 */
	public static int getDrawableForDistance(int distance){

		int cardType = getCardType();

		if(distance <= 0 || distance > 13 || distance == 11){
			return R.drawable.backside;
		}
		
		if(cardType == 1){
			switch(distance){
			case 1: return R.drawable.normalcards_eins;
			case 2: return R.drawable.normalcards_zwei;
			case 3: return R.drawable.normalcards_drei;
			case 4: return R.drawable.normalcards_vier;
			case 5: return R.drawable.normalcards_fuenf;
			case 6: return R.drawable.normalcards_sechs;
			case 7: return R.drawable.normalcards_sieben;
			case 8: return R.drawable.normalcards_acht;
			case 9: return R.drawable.normalcards_neun;
			case 10: return R.drawable.normalcards_zehn;
			case 12: return R.drawable.normalcards_zwoelf;
			case 13: return R.drawable.normalcards_dreizehn;
			}
		}
		else if(cardType==2){
			switch(distance){
			case 1: return R.drawable.bigcards_eins;
			case 2: return R.drawable.bigcards_zwei;
			case 3: return R.drawable.bigcards_drei;
			case 4: return R.drawable.bigcards_vier;
			case 5: return R.drawable.bigcards_fuenf;
			case 6: return R.drawable.bigcards_sechs;
			case 7: return R.drawable.bigcards_sieben;
			case 8: return R.drawable.bigcards_acht;
			case 9: return R.drawable.bigcards_neun;
			case 10: return R.drawable.bigcards_zehn;
			case 12: return R.drawable.bigcards_zwoelf;
			case 13: return R.drawable.bigcards_dreizehn;
			}
		}

		return R.drawable.backside;
	}

	/**
	 * Set a unique ID for this card.
	 * Note, that two instances with the same ID can exist 
	 * distributed on server and client.
	 * 
	 * @param id the unique ID for this card.
	 */
	public void setId(int id){
		this.id = id;
	}

	/**
	 * @return the unique id for this card, or -1 if not set yet
	 */
	public int getId(){
		return id;
	}

	/**
	 * Makes sure that the drawableId is up to date.
	 * In can change due to preference changes.
	 */
	protected void checkDrawable(){
		int checkDrawable = getDrawableForCardName(cardName);
		if(checkDrawable != drawableId){
			if(Card.idToBitmap.get(drawableId) != null){
				Card.idToBitmap.remove(drawableId);
			}
			this.drawableId = checkDrawable;
			bitmap = null;
		}
	}
	
	/**
	 * On first call, decodes the bitmap from the context.
	 * Afterwards, stores the bitmap in the attribute and returns that value.
	 * @param context image context
	 * @return the bitmap for this card or null, if the bitmap cannot be found
	 */
	public Bitmap getBitmap(Context context){
		checkDrawable();
		
		if(bitmap != null){
			return bitmap;
		}
		if(idToBitmap.get(drawableId) != null){
			bitmap = idToBitmap.get(drawableId);
			return bitmap;
		}

		//this card is loaded for the first time
		Resources res = context.getResources();
		bitmap = BitmapFactory.decodeResource(res, drawableId);

		idToBitmap.put(drawableId, bitmap);

		return bitmap;
	}

	/**
	 * 
	 * @return id for the card value, two cards with the same value (e.g. 7) will return the same id
	 */
	public int getDrawableId(){
		checkDrawable();
		return drawableId;
	}

	/**
	 * @return String representation for this card 
	 */
	public String toString(){
		if(distance != -1){
			return String.valueOf(distance);
		}

		return "";
	}

	/**
	 * 
	 * @return a color, which can be used to draw a text on this card
	 */
	public int getTextColorOnCard(){
		return getTextColorForDistance(distance);
	}

	/**
	 * 
	 * @param distance the distance, which will be drawn on the card
	 * 
	 * @return a color, which can be used to draw a text on this card
	 */
	public static int getTextColorForDistance(int distance){
		if(distance == 1 || distance == 4 || distance == 7 || distance == 8 || distance == 13 ){
			return Color.RED;
		}
		if(distance > 13 || distance == 11 || distance == 0){
			return Color.WHITE;
		}
		return Color.DKGRAY;
	}

	/**
	 * Execute one animation step for this card.
	 * Ends animation, if target step count is reached.
	 */
	public void nextAnimationStep(){
		if(! isAnimated){
			return;
		}

		this.animationStep++;
		if(animationStep >= steps){
			isAnimated = false;
		}
	}

	/**
	 * Start a card animation. Let the card fly from start to target point.
	 * Use the interval and number of steps for progress.
	 * @param start start point for animation, coordinates without scaling
	 * @param target target point for animation, coordinates without scaling
	 * @param forInValidation the view to repaintafter every animation step
	 */
	public void animate(Point start, Point target, final View forInValidation){
		this.animationStart = start;
		this.animationEnd = target;

		this.animationStep = 0;
		this.isAnimated = true;

		Runnable animator = new Runnable() {

			@Override
			public void run() {
				while(isAnimated){
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
					}
					nextAnimationStep();
					forInValidation.postInvalidate();
				}
			}
		};

		new Thread(animator).start();
	}
	
	/**
	 * Determines size of the text on a card
	 */
	private final double textSizeFactor = 0.2;

	/**
	 * Draw the card anywhere on a canvas. Allows to set the rotation and the target size of the card.
	 * 
	 * @param context the context for the draw operations (context of view)
	 * @param canvas the canvas to draw on
	 * @param cardWidth the target width of the card
	 * @param cardHeight height of the target card
	 * @param xpos the x position on the canvas to draw on, upper left corner
	 * @param ypos the y position on the canvas to draw on, upper left corner
	 * @param rotation in degrees (0..360)
	 * @param drawText if true, additional text is drawn for better detection of card value
	 */
	public void draw(Context context, Canvas canvas, int cardWidth, int cardHeight, float xpos, float ypos, int rotation, boolean drawText){
		drawText = drawText || this.drawableId == R.drawable.backside;
		
		Matrix matrix = new Matrix();
		Paint paint = new Paint();

		Bitmap cardBitmap = getBitmap(context);

		if(cardBitmap == null){
			return;
		}

		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap copied = Bitmap.createBitmap(cardBitmap.getWidth(), cardBitmap.getHeight(), conf); // this creates a MUTABLE bitmap
		Canvas cardCanvas = new Canvas(copied);

		cardCanvas.drawBitmap(cardBitmap, 0, 0, paint);

		if(rotation > 360){
			rotation = rotation%360;
		}
		Point2D cardUL = new Point2D(xpos, ypos);//Upper left corner of card
		Point2D cardLR = new Point2D(xpos+cardWidth-1, ypos+cardHeight-1);//Lower right corner of card

		matrix.set(null);
		//Scale from original card width to target cardWidth
		matrix.setScale(cardWidth/(float)cardBitmap.getWidth(), cardHeight/(float)cardBitmap.getHeight());
		//Rotate around center of scaled card
		matrix.postRotate(rotation, (float)((cardLR.x-cardUL.x)*0.5) , (float)((cardLR.y-cardUL.y)*0.5) );
		//Translate to the target position
		matrix.postTranslate( cardUL.x ,  cardUL.y);

		//Draw text for better recognition
		if(drawText){
			paint.setColor(getTextColorOnCard());
			paint.setTextAlign(Align.CENTER);
			float textSize = Math.round(cardBitmap.getHeight()*textSizeFactor);
			paint.setTextSize(textSize);

			if(rotation != 0){
				cardCanvas.drawText(toString(), cardBitmap.getWidth()/2, cardBitmap.getHeight()/2-textSize, paint);
			}
		}
		canvas.drawBitmap(copied, matrix, paint);
		//Better rendering for texts without rotation
		if(drawText && rotation == 0){
			float textSize = Math.round(cardHeight*textSizeFactor);
			paint.setTextSize(textSize);
			canvas.drawText(toString(), xpos+cardWidth/2, ypos+cardHeight/2-textSize, paint);
		}
	}

	/**
	 * Special drawing funtion for the board. Used to draw in the center of the board.
	 * Draw this card on the given canvas. The board area on which to draw is expected to
	 * be sideLength pixels wide, if no scaling is done. Scale factors are retrieved from the viewdata.
	 * The rotation provides degrees of rotation of the card.
	 * 
	 * @param context needed for decoding bitmap
	 * @param viewdata for scaling of the card
	 * @param canvas the canvas to draw the card on
	 * @param cardWidth the width of the card in not-scaled mode
	 * @param sideLength length of the board, used to center the card
	 * @param rotation in degrees of the card
	 */
	public void draw(Context context, BoardViewData viewdata, Canvas canvas, int cardWidth, int sideLength, int rotation){

		Bitmap cardBitmap = getBitmap(context);

		if(cardBitmap == null){
			return;
		}

		int cardHeight = (int)( ((float)cardWidth) / cardBitmap.getWidth() * cardBitmap.getHeight() );//Keep card ratio

		Point2D cardUL;
		/**
		 * Set up animation parameters
		 */
		if(isAnimated){
			rotation = (int)Math.round( (double)rotation/steps*animationStep );
			double centerX = animationStart.getPosX()+(animationEnd.getPosX()-animationStart.getPosX())/((double) steps)*animationStep;
			double centerY = animationStart.getPosY()+(animationEnd.getPosY()-animationStart.getPosY())/((double) steps)*animationStep;
			cardUL = viewdata.scale( Math.round(centerX-cardWidth/2), Math.round(centerY-cardHeight/2));
		}
		else{
			cardUL = viewdata.scale((int)(0.5*sideLength-cardWidth/2), (int)(0.5*sideLength-cardHeight/2));
		}
		boolean drawText = getCardType()==1;
		this.draw(context, canvas, Math.round(cardWidth*viewdata.getScalex()), Math.round(cardHeight*viewdata.getScaley()), cardUL.x, cardUL.y, rotation, drawText);
	}
}
