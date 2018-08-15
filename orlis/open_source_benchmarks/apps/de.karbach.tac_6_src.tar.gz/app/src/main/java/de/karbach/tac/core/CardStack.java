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
import java.util.ArrayList;
import java.util.List;

import de.karbach.tac.R;

/**
 * Stack of playing cards. Can be used for the cards of one
 * player or for the entire game stack.
 * 
 * @author Carsten Karbach
 *
 */
public class CardStack implements Serializable{

	/**
	 * Event types for listeners to distinguish different events
	 *
	 */
	public static enum CardEventType{
		ADDED,REMOVED, STACKADDED, CLEARED, OTHER
	}
	
	/**
	 * ID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * List of all cards in this stack
	 */
	private List<Card> cards;
	
	/**
	 * Store cards of the future to go back and forth in the history of this stack.
	 */
	private List<Card> futureCards;
	
	/**
	 * The cards list might contain only the last few
	 * played cards for memory reasons. To keep track
	 * of the total number of played cards, this variable
	 * stores exactly that.
	 */
	private int totalCards;
	
	/**
	 * The maximum number of cards stored in the stack.
	 * If this limit is going to be exceeded because of
	 * an added card, older cards are removed.
	 * Set this value to -1 to remove any limit.
	 */
	private transient int maximumCardsStored = -1;
	
	/**
	 * @return The maximum number of cards stored in the stack or -1 for no limit
	 */
	public int getMaximumCardsStored() {
		return maximumCardsStored;
	}


	/**
	 * Set a new maximum limit of stored cards.
	 * Remove those cards, which are too much already.
	 * 
	 * @param maximumCardsStored -1 for no limit or any value larger than 0
	 */
	public void setMaximumCardsStored(int maximumCardsStored) {
		if(maximumCardsStored == 0){
			return;
		}
		this.maximumCardsStored = maximumCardsStored;
		removeCardsByLimit();
	}

	/**
	 * Generate a list of moves from this card stack.
	 *
	 * @return list of moves stored in this stack
	 */
	public ArrayList<Move> toMoveList(){
		ArrayList<Move> result = new ArrayList<Move>();
		//Generate a reverse list of moves, the last one is stored in the result as the first
		for(int i=cards.size()-1; i>=0; i--){
			Card c = cards.get(i);
			int moveId = getTotalSize()-((cards.size()-1)-i);
			int[] involvedBalls = c.getInvolvedBallIDs();
			if(involvedBalls == null){
				involvedBalls = new int[]{c.getPlayedById()*4};
			}
			Move m = new Move(c, involvedBalls);//Here the ID is the actual ball's ID
			m.setId(moveId);
            if(c.getDrawableId() == R.drawable.backside){
                m.setDistance(c.getDistance());
            }
			result.add(m);
		}

		return result;
	}

	/**
	 * Remove the older cards, if there are more cards stored than allowed
	 * by the maximumCardsStored limit.
	 */
	protected void removeCardsByLimit(){
		boolean removed = false;
		if(maximumCardsStored > 0){
			while(cards.size() > maximumCardsStored){
				cards.remove(0);
				removed = true;
			}
		}
		if(removed){
			notifyListener(CardEventType.OTHER);
		}
	}

	/**
	 * Listeners for any actions on this stack.
	 */
	private transient List<CardActionListener> listeners;
	
	public CardStack(){
		cards = new ArrayList<Card>();
		futureCards = new ArrayList<Card>();
		
		listeners = new ArrayList<CardActionListener>();
		
		totalCards = 0;
		
		maximumCardsStored = -1;
	}
	

	/**
	 * @return list of card listeners, create a new list if it is null
	 */
	protected List<CardActionListener> getListeners(){
		if(listeners == null){
			listeners = new ArrayList<CardActionListener>();
		}
		return listeners;
	}
	
	/**
	 * Add a new listener for any card actions on this stack
	 * @param listener
	 */
	public void addCardListener(CardActionListener listener){
		getListeners().add(listener);
	}
	
	/**
	 * Notify all listeners about a new event
	 */
	public void notifyListener(CardEventType type){
		for(CardActionListener listener: getListeners()){
			listener.onCardAction(type);
		}
	}
	
	/**
	 * @return list of cards in this stack, just reference no copy
	 */
	public List<Card> getCards(){
		return cards;
	}
	
	/**
	 * Remove all elements in the stack
	 */
	public void clear(){
		cards.clear();
		totalCards = 0;
		notifyListener(CardEventType.CLEARED);
	}
	
	/**
	 * Add all cards from the passed stack to this card stack.
	 * 
	 * @param stack the card stack, which is integrated into this stack
	 */
	public void addStack(CardStack stack){
		if(stack == null){
			return;
		}
		for(Card card: stack.cards){
			addCard(card);
		}
		
		notifyListener(CardEventType.STACKADDED);
	}
	
	/**
	 * Add a new card to the card stack
	 * @param toAdd the card, which should be added
	 */
	public void addCard(Card toAdd){
		cards.add(toAdd);
		totalCards++;
		
		removeCardsByLimit();
		//Clear the future, if this is a brand new card
		if( ! getFutureCards().contains(toAdd) ){
			getFutureCards().clear();
		}
		
		notifyListener(CardEventType.ADDED);
	}
	
	/**
	 * Remove one card from this stack
	 * @param toRemove the card, which should be removed
	 */
	public void removeCard(Card toRemove){
		if(cards.contains(toRemove)){
			cards.remove(toRemove);
			totalCards--;
		}
		
		notifyListener(CardEventType.REMOVED);
	}
	
	/**
	 * @return list of cards already played, but taken back
	 */
	public List<Card> getFutureCards(){
		if(futureCards == null){
			futureCards = new ArrayList<Card>();
		}
		return futureCards;
	}
	
	/**
	 * Undo the last played card.
	 * Add the undone card to the future stack.
	 * If there are no cards played, do nothing.
	 */
	public void goBack(){
		if(getSize() == 0){
			return;
		}
		Card latest = getCards().get(getCards().size()-1);
		removeCard(latest);
		
		futureCards.add(latest);
	}
	
	/**
	 * Inverse function to goBack.
	 * Play the last card on the future stack.
	 */
	public void goForward(){
		if(getFutureCards().size() == 0){
			return;
		}
		
		Card latest = getFutureCards().get(getFutureCards().size()-1);
		addCard(latest);
		
		getFutureCards().remove(getFutureCards().size()-1);
	}
	
	/**
	 * How often the card stack is walked through to mix the cards
	 */
	private static final int walkThroughs = 5;
	
	/**
	 * Make random mix of the cards
	 */
	public void mix(){
		for(int i=0; i< cards.size()*walkThroughs; ++i){
			int rId = (int)(Math.random()*cards.size());
			//Switch first card with any other random card
			Card tmp = cards.get(rId);
			cards.set(rId, cards.get(0));
			cards.set(0, tmp);
		}
		
		notifyListener(CardEventType.OTHER);
	}
	
	/**
	 * @return the number of cards in this stack
	 */
	public int getSize(){
		return cards.size();
	}
	
	/**
	 * @return the total number of cards every played into this stack
	 */
	public int getTotalSize(){
		return totalCards;
	}
	
	/**
	 * @return list of drawable ids of all the cards in this stack
	 */
	public int[] getCardDrawableIds(){
		int[] res = new int[cards.size()];
		int i = 0;
		for(Card card: cards){
			res[i] = card.getDrawableId();
			i++;
		}
		return res;
	}
	
	/**
	 * @return list of unique ids of all the cards in this stack
	 */
	public int[] getUniqueIds(){
		int[] res = new int[cards.size()];
		int i = 0;
		for(Card card: cards){
			res[i] = card.getId();
			i++;
		}
		return res;
	}
	
	/**
	 * Add all cards defined by the ids array to this card stack.
	 * Two sets of IDs are passed: drawable Ids and unique ids for the
	 * cards. Each card has two Ids.
	 * 
	 * @param drawableids list of drawable ids
	 * 
	 * @param ids list of unique ids for the cards
	 */
	public void setCardsByIds(int[] drawableIds, int[] ids){
		clear();
		for(int i=0; i<drawableIds.length; i++){
			String cardName = Card.getCardNameFromDrawableId(drawableIds[i]);
			if(cardName == null){
				continue;
			}
			Card card = new Card(cardName);
			card.setId(ids[i]);
			addCard(card);
		}
		
		notifyListener(CardEventType.OTHER);
	}
}
