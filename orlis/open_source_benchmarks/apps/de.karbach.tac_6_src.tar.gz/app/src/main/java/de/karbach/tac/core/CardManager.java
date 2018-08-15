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

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.karbach.tac.R;
import de.karbach.tac.core.CardStack.CardEventType;

/**
 * Manages all card stacks of the game: players stacks, played stack
 * and available cards. Allows to mix cards and allows players to play
 * a card.
 * 
 * @author Carsten Karbach
 *
 */
public class CardManager {

	/**
	 * list of the card stacks of all players.
	 */
	private List<CardStack> playerStacks;

	/**
	 * This map stores the cards, which are exchanged between the players on
	 * the beginning of each round.
	 * Maps playerId to the card, which the player wants to forward to the other teammate.
	 * This stack is used as tmp structure for the forwarded cards.
	 * As soon as the cards are exchanged, the exchangeCards will be duplicated in the
	 * player stacks.
	 */
	private SparseArray<Card> exchangeCards;

	/**
	 * Number of players in the game
	 */
	private final int playerCount = 4;

	/**
	 * Main Stack of the game
	 */
	private CardStack globalStack;

	/**
	 * Card stack of already played cards.
	 */
	private CardStack playedStack;

	/**
	 * Maps card name to the number of this card type in the game
	 */
	private Map<String,Integer> cardnameToNumber;

	/**
	 * Number of cards in the game in total
	 */
	private int totalCards = 0;

	/**
	 * Id of currently active player
	 */
	private int activePlayer;

	/**
	 * All the listeners waiting for action
	 */
	private List<CardActionListener> listeners;
	
	/**
	 * Stores the player ID of the player, which began the last time after
	 */
	private int roundStartPlayer=-1;

	/**
	 * Init all card stacks, playerstacks and playedstack will be empty
	 */
	public CardManager(){
		globalStack = new CardStack();
		playedStack = new CardStack();
		playerStacks = new ArrayList<CardStack>();
		for(int i=0; i<playerCount; ++i){
			playerStacks.add(new CardStack());
		}

		activePlayer = 0;

		listeners = new ArrayList<CardActionListener>();

		exchangeCards = new SparseArray<Card>();

		fillStartCards();
	}

	/**
	 * Restarts a game, makes the first handing out of cards.
	 */
	public synchronized void restart(){
		activePlayer = 0;
		roundStartPlayer = -1;
		fillStartCards();
		handOut();
	}

	/**
	 * Add a new listener for any card actions
	 * @param listener
	 */
	public synchronized void addCardListener(CardActionListener listener){
		listeners.add(listener);
	}

	/**
	 * Notify all listeners about a new event
	 */
	public synchronized void notifyListener(CardEventType type){
		for(CardActionListener listener: listeners){
			listener.onCardAction(type);
		}
	}

	/**
	 * Fill the card stack with the default cards.
	 */
	protected synchronized void fillStartCards(){
		clearAllStacks();

		totalCards = 0;

		cardnameToNumber = new HashMap<String, Integer>();
		cardnameToNumber.put("1", 9);
		cardnameToNumber.put("2", 7);
		cardnameToNumber.put("3", 7);
		cardnameToNumber.put("4", 7);
		cardnameToNumber.put("5", 7);
		cardnameToNumber.put("6", 7);
		cardnameToNumber.put("7", 8);
		cardnameToNumber.put("8", 7);
		cardnameToNumber.put("9", 7);
		cardnameToNumber.put("10", 7);
		cardnameToNumber.put("12", 7);
		cardnameToNumber.put("13", 9);

		cardnameToNumber.put("tac", 4);
		cardnameToNumber.put("trickser", 7);

		cardnameToNumber.put("engel", 1);
		cardnameToNumber.put("teufel", 1);
		cardnameToNumber.put("krieger", 1);
		cardnameToNumber.put("narr", 1);

		int uniqueID = 0;
		for(String cardName: cardnameToNumber.keySet()){
			int number = cardnameToNumber.get(cardName);
			totalCards += number;
			for(int i=0; i< cardnameToNumber.get(cardName); ++i){
				Card nCard = new Card(cardName);
				nCard.setId(uniqueID);
				uniqueID++;
				globalStack.addCard(nCard);
			}
		}

		globalStack.mix();

		notifyListener(CardEventType.OTHER);
	}

	/**
	 * Remove all cards from all stacks
	 */
	protected synchronized void clearAllStacks(){
		globalStack.clear();
		playedStack.clear();
		for(int i=0; i<playerCount; ++i){
			playerStacks.get(i).clear();
		}
	}

	/**
	 * @return true, if no player has any card left, false otherwise
	 */
	public synchronized boolean allHandsClear(){
		for(int i=0; i<playerCount; ++i){
			if(playerStacks.get(i).getSize() > 0){
				return false;
			}
		}

		return true;
	}

	/**
	 * @return true, if the last larger round is played currently
	 */
	public synchronized boolean isLastRound(){
		if(globalStack.getSize() <= 24 ){
			return true;
		}
		return false;
	}

	/**
	 * @return total number of cards in the entire game
	 */
	public synchronized int getTotalCards(){
		return totalCards;
	}

	/**
	 * Number of cards, which are handed out in default rounds (not the last round)
	 */
	private static final int normalCardNumber = 5;

	/**
	 * Hand out cards to each player
	 */
	public synchronized void handOut(){
		//Do not hand out new cards, if there are still cards left.
		if(!allHandsClear()){
			return;
		}

		if(globalStack.getSize() == 0){
			//Refill the global stack
			fillStartCards();
		}

		int rounds = normalCardNumber;
		if(isLastRound()){
			rounds++;
		}

		for(int i=0; i<rounds; i++){
			for(int j=0; j<playerCount; j++){
				if(globalStack.getSize() > 0){
					Card card = globalStack.getCards().get(0);
					playerStacks.get(j).addCard(card);
					globalStack.removeCard(card);
				}
			}
		}

		exchangeCards.clear();
		roundStartPlayer++;
		if(roundStartPlayer >= playerCount ){
			roundStartPlayer = 0;
		}
		activePlayer = roundStartPlayer;

		notifyListener(CardEventType.OTHER);
	}

	/**
	 * Make next player the active one
	 */
	public synchronized void nextPlayer(){
		activePlayer++;
		if(activePlayer >= playerCount ){
			activePlayer = 0;
		}
	}
	
	/**
	 * Go one player back, needed for the narr.
	 */
	public synchronized void prevPlayer(){
		activePlayer--;
		if(activePlayer < 0 ){
			activePlayer = playerCount-1;;
		}
	}

	/**
	 * @return id of currently active player, 0..playerCount
	 */
	public synchronized int getActivePlayer(){
		return activePlayer;
	}

	/**
	 * Determine the team mate of the given player ID.
	 * @param player the player ID for which a team mate is searched
	 * @return the ID of the player's team mate or -1, if there is no team mate for this ID
	 */
	public int getTeamMate(int player){
		switch(player){
		case 0: return 2;
		case 1: return 3;
		case 2: return 0;
		case 3: return 1;
		default: return -1;
		}
	}
	
	/**
	 * @return true, if players still need to exchange cards on round's beginning
	 */
	protected boolean isExchanging(){
		return exchangeCards.size() < playerCount;
	}
	
	/**
	 * Player intends to play a card.
	 * Check , if that player is active.
	 * Move the card to the playedStack.
	 * @param player player, who wants to play a card in range [0..playerCount-1]
	 * @param cardId the card, which is intended to be played
	 */
	public synchronized void playCard(int player, int cardId){
		//Check player id
		if(player<0 || player >= playerCount){
			return;
		}

		//Check if he has the card
		if(cardId < 0 || cardId >= playerStacks.get(player).getCards().size() ){
			return;
		}
		Card pCard = playerStacks.get(player).getCards().get(cardId);
		
		if(player != activePlayer || isExchanging()){//Is this step a card exchange?
			if(exchangeCards.get(player) == null ){
				exchangeCards.put(player, pCard);
				playerStacks.get(player).removeCard(pCard);
				//Exchange cards, if team mate has pushed his card
				int mate = getTeamMate(player);
				Card other = exchangeCards.get(mate); 
				if(other != null){//The second of a team will go into this branch
					playerStacks.get(mate).addCard(pCard);//Give mate my card
					playerStacks.get(player).addCard(other);//Give me mate's card
				}
				notifyListener(CardEventType.ADDED);
			}
			return;
		}
		
		playedStack.addCard(pCard);
		playerStacks.get(player).removeCard(pCard);

		nextPlayer();

		if(allHandsClear() ){
			//New player becomes the first active player of this round
			nextPlayer();
			handOut();
		}

		notifyListener(CardEventType.OTHER);
	}

	/**
	 * @param player the id of the player
	 * @return the cards for that player
	 */
	public synchronized CardStack getPlayerCards(int player){
		if(player<0 || player >= playerCount){
			return null;
		}

		return playerStacks.get(player);
	}

	/**
	 * @return a card stack holding all cards played so far.
	 */
	public synchronized CardStack getPlayedCards(){
		return playedStack;
	}
	
	/**
	 * Execute the Narr card.
	 * Player one gets the cards of player 2 and so forth.
	 */
	public synchronized void playNarr(){
		CardStack tmpOne = playerStacks.get(0);
		for(int i=0; i<= playerCount-2; i++){
			playerStacks.set(i, playerStacks.get(i+1) );
		}
		playerStacks.set(playerCount-1, tmpOne);
		
		prevPlayer();
		
		notifyListener(CardEventType.OTHER);
	}
}
