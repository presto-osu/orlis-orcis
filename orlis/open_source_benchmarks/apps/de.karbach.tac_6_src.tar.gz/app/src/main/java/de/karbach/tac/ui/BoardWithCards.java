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

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import de.karbach.tac.Preferences;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardActionListener;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.core.CardStack.CardEventType;
import de.karbach.tac.core.Point;

/**
 * This is the TAC board enhanced with the last played cards.
 * A cardstack instance can be set as data to be drawn in the
 * center of the board.
 * 
 * @author Carsten Karbach
 *
 */
public class BoardWithCards extends Board {
	/**
	 * The stack of cards, which can be drawn by this view
	 */
	protected CardStack playedCards;
	
	/**
	 * Maximum number of cards drawn here, all earlier cards are neglected
	 */
	protected int toDraw = 10;

    public boolean isAnimateCards() {
        return animateCards;
    }

    public void setAnimateCards(boolean animateCards) {
        this.animateCards = false;
    }

    /**
     * This attribute can be used to disable card animation inspite of the setting by the user
     */
    protected boolean animateCards = true;
	
	/**
	 * @param context
	 */
	public BoardWithCards(Context context){
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public BoardWithCards(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public BoardWithCards(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	
	/**
	 * Set the cards, which should be drawn.
	 * 
	 * @param played the cards played
	 */
	public void setCardStack(CardStack played){
		this.playedCards = played;
		
		playedCards.addCardListener(new CardActionListener() {
			
			@Override
			public void onCardAction(CardEventType type) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BoardWithCards.this.getContext());
				Boolean animateCards = sharedPref.getBoolean(Preferences.ANIMATION_KEY, true);
				//Check preferences, whether cards need to be animated
				if(isAnimateCards() && animateCards && type == CardEventType.ADDED){ //Animate the card, if a card was just played
					List<Card> cards = playedCards.getCards();
					if(cards != null && cards.size() > 0){
						Card newCard = cards.get(cards.size()-1);
						
						int min = getWidth();
						if(getHeight() < min){
							min = getHeight();
						}
						
						int playerId = BoardWithCards.this.data.getPlayerIDForLastMove();
						if(playerId == -1){
							playerId = newCard.getPlayedById();
						}
						
						Point start;
						//Change animation start, if player is known
						switch(playerId){
						case 0:
							start = new Point(0,0);
							break;
						case 1:
							start = new Point(min,0);
							break;
						case 2:
							start = new Point(min,min);
							break;
						case 3:
							start = new Point(0,min);
							break;
						default:
							start = new Point(min/2, 0);
							break;
						}
						
						
						Point target = new Point(min/2, min/2);
						
						newCard.animate(start, target, BoardWithCards.this);
					}
				}
				
				postInvalidate();
			}
		});
		
		this.invalidate();
	}

    @Override
    protected void onScaledDraw(Canvas canvas, int width, int height) {
        super.onScaledDraw(canvas, width, height);

        //Check if data model is available
        if(playedCards == null){
            return;
        }
        if(data == null || viewdata == null){
            return;
        }

        //first is the index of the first card, which is to be drawn
        int first = playedCards.getSize()-toDraw;
        if(first < 0){
            first = 0;
        }

        int min = width;
        if(height < min){
            min = height;
        }

        int degreeInc = 360/toDraw;//Rotation in degrees for each new card

        for(int i=first; i< playedCards.getSize(); i++){
            Card current = playedCards.getCards().get(i);
            int cardWidth = (int)Math.round(min*cardWidthFactor);
            int modDraw = (playedCards.getTotalSize() - playedCards.getSize() + i)%toDraw; //difference between totally played cards and stored cards
            int deg = modDraw*degreeInc;
            current.draw(getContext(), viewdata, canvas, cardWidth, min, deg);
        }
        //Draw distance card on top again
        if( first < playedCards.getSize() ){
            drawDistanceInCenter(canvas, min);
        }
    }

}
