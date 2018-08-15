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
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardActionListener;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.core.CardStack.CardEventType;

/**
 * Draw the cards of one cardstack.
 * Updates on every card update.
 * 
 * @author Carsten Karbach
 *
 */
public class CardStackView extends View{

	/**
	 * Stack of cards painted by this view.
	 */
	private CardStack cards;
	
	/**
	 * If set to a value >= 0, then paint only as many cards into each line
	 * as defined here.
	 */
	private int cardsPerLine = -1;
	
	/**
	 * Factor multiplied with width and height of cards to define empty space between the cards
	 * Must be between 0 and 1
	 */
	private float spaceFactor = 0.01f;
	
	/**
	 * @param context
	 */
	public CardStackView(Context context){
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CardStackView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CardStackView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		rectsForCards = new ArrayList<Rect>();
	}
	
	/**
	 * Set the number of cards drawn per line
	 * 
	 * @param perLine a value <= 0 for deactivation, a higher value as number of cards per line
	 */
	public void setCardsPerLine(int perLine){
		this.cardsPerLine = perLine;
	}
	
	/**
	 * Set new cardstack painted by this view.
	 * @param cards the new cards, which are to be painted
	 */
	public void setCards(CardStack cards){
		this.cards = cards;
		//Listen for card events and repaint, if necessary
		cards.addCardListener(new CardActionListener() {
			
			@Override
			public void onCardAction(CardEventType type) {
				postInvalidate();
			}
		});
		
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
        int lines = 1;
        if(cardsPerLine > 0 && cards!=null){
        	lines = cards.getSize()/cardsPerLine;
        	if(cards.getSize() % cardsPerLine != 0){
        		lines++;
        	}
        }
        
        setMeasuredDimension(
        		getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), lines*cardHeight);
    }
	
	
	
	/**
	 * Extract the card at the relative location (x,y).
	 * Return the corresponding card at that location.
	 * 
	 * @param x
	 * @param y
	 * @return the card, which intersects with the given location, null if there is none
	 */
	public Card getCardAtLocation(double x, double y){
		int i = 0;
		for(Rect rect: rectsForCards){
			if(x >= rect.left && x<= rect.right
					&& y >= rect.top && y<=rect.bottom){
				return cards.getCards().get(i);
			}
			i++;
		}
		
		return null;
	}
	
	/**
	 * Source rectangle for bitmap painting, used for performance reasons
	 */
	private Rect src = new Rect();
	
	/**
	 * Destination rectangle for bitmap painting, used for performance reasons
	 */
	private Rect dst = new Rect();
	
	/**
	 * Paint instance needed for any draw operation.
	 */
	private Paint paint = new Paint();
	
	/**
	 * Float representation of dst rectangle 
	 */
	private RectF dstF = new RectF();
	
	/**
	 * Store the rectangles, in which the cards are painted.
	 */
	private List<Rect> rectsForCards;
	
	//Height of the card in the last drawing
	private int cardHeight = 10;
	
	public void suggestCardHeight(int height){
		cardHeight = height;
	}
	
	@SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(cards == null){
			return;
		}
		
		List<Card> theCards = cards.getCards();
		rectsForCards.clear();
		//Do nothing, if there is no card
		if(theCards.size() == 0){
			return;
		}
		
		int perLine = theCards.size();
		if(cardsPerLine >= 0){
			perLine = cardsPerLine;
		}
		
		int cardWidth = getWidth()/perLine;
		int origCardWidth = cardWidth;
		//Expects all cards to be equally sized
		Bitmap cardBitmap = theCards.get(0).getBitmap(this.getContext());
		int cardHeight = (int)Math.round( ((double)cardWidth) / cardBitmap.getWidth() * cardBitmap.getHeight() );
		if(cardsPerLine <= 0 && cardHeight > getHeight()){ //Make sure, that cardheight does not exceed the view's height
			cardHeight = getHeight();
			cardWidth = (int)Math.round( ((double)cardHeight) / cardBitmap.getHeight() * cardBitmap.getWidth() );
		}
		if(this.cardHeight != cardHeight){
			this.cardHeight = cardHeight;
			requestLayout();
		}
		int y = (getHeight()-cardHeight)/2;//Center the cards vertically
		if( perLine < theCards.size() ){
			y = 0;
		}
		
		int horSpace = Math.round(cardWidth*spaceFactor);
		int vertSpace = Math.round(cardHeight*spaceFactor);
		
		float roundRadius = (float)(70.0/539.0*cardWidth);
		
		for(int i=0; i< theCards.size(); i++){
			int x = (i%perLine)*origCardWidth;
			int cy = y+(i/perLine)*cardHeight;
			Card card = theCards.get(i);
			boolean drawText = Card.getCardType()==1;
			card.draw(this.getContext(), canvas, cardWidth - horSpace, cardHeight - vertSpace, x, cy, 0, drawText);
			
			dst.set(x,cy,x+cardWidth-horSpace,cy+cardHeight-vertSpace);
			rectsForCards.add(new Rect(dst));
			//Additional state painting for card
			
			if(card.isSelected()){ //No active color if blinking is enabled and blinkOn is off
				paint.setColor(Color.argb(100, 100, 255, 100));
			}
			if(! card.isEnabled()){
				paint.setColor(Color.argb(100, 100, 100, 100));
			}
			
			if(card.isSelected() || ! card.isEnabled()){
				paint.setStyle(Style.FILL);
				dstF.set(dst);
				canvas.drawRoundRect(dstF, roundRadius, roundRadius, paint);
			}
			paint.reset();
		}
		
	}
	
}
