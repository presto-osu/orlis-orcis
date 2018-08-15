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

package de.karbach.tac.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import de.karbach.tac.R;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.core.DataChangeEvent;
import de.karbach.tac.core.DataChangeListener;
import de.karbach.tac.core.DataChangeEvent.ChangeType;
import de.karbach.tac.ui.CardStackView;

/**
 * Grid of cards shown directly next to the board.
 * This fragment is always visible. 
 *
 */
public class CardGridFragment extends Fragment implements DataChangeListener{

	/**
	 * Number of columns of cards
	 */
	private static final int columnCount = 5;

	/**
	 * Detect touch events on this dialog 
	 *
	 */
	private class CardGridGestureListener extends SimpleOnGestureListener{

		public boolean onSingleTapConfirmed(MotionEvent e){

			boolean res = super.onSingleTapConfirmed(e);

			setSelectedCardFromLocation(e.getX(), e.getY());

			return res;
		}
	}

	/**
	 * Implement this listener to get event notifications for clicked cards
	 * 
	 *
	 */
	public static interface CardSelectedListener{

		/**
		 * This function is called, when the user selects a card.
		 * If no card was selected, null is provided
		 * @param selected the card selected by the user
		 */
		public void cardSelected(Card selected);
	}

	//Stack of cards shown here.
	private CardStack cards;
	//The card selected by the user
	private Card selectedCard;
	//Gesture detector for this dialog
	private GestureDetector gdt;

	//List of listeners for events of clicking on a card (selecting a card)
	private List<CardSelectedListener> selectListeners;

	/**
	 * Init grid with the cards shown in this view.
	 */
	public CardGridFragment(){
		cards = new CardStack();
		for(int i=1; i<=13; i++){
			if(i == 11){
				continue;
			}
			Card c = new Card(Card.getCardNameForDistance(i));
			c.setDistance(i);
			c.setEnabled(false);
			cards.addCard(c);
		}

		//Add trickser card
		Card trick = new Card("trickser");
		trick.setEnabled(false);
		cards.addCard(trick);
		//Add trickser card
		Card war = new Card("krieger");
		war.setEnabled(false);
		cards.addCard(war);

		selectListeners = new ArrayList<CardSelectedListener>();
	}

	/**
	 * Add a new listener to this fragment
	 * @param listener the added listener
	 */
	public void addCardSelectListener(CardSelectedListener listener){
		selectListeners.add(listener);
	}

	/**
	 * Update the view for this fragment
	 */
	public void updateView(){
		if(CardGridFragment.this.getView() == null){
			return;
		}
		CardStackView cardstackView = (CardStackView)CardGridFragment.this.getView().findViewById(R.id.cardgridstack);
		cardstackView.invalidate();
	}

	/**
	 * Deactivate all cards.
	 */
	public void deactivate(){
		for(Card c: this.cards.getCards()){
			c.setEnabled(false);
		}
	}

	/**
	 * Deselect the card.
	 */
	public void clearSelectedCard(){
		for(Card c: this.cards.getCards()){
			c.setSelected(false);
		}
	}

	/**
	 * Assuming a user clicked on the grid view at the given location,
	 * this function sets the corresponding selected card.
	 * 
	 * @param x x location of the click event
	 * @param y y location of the click event
	 */
	protected void setSelectedCardFromLocation(float x, float y){
		if(CardGridFragment.this.getView() == null){
			return;
		}
		CardStackView cardstackView = (CardStackView)CardGridFragment.this.getView().findViewById(R.id.cardgridstack);
		if(cardstackView == null){
			return;
		}
		int[] absPosition=new int[2];
		cardstackView.getLocationOnScreen(absPosition);
		int[] fragmentPos = new int[2];
		this.getView().getLocationOnScreen(fragmentPos);
		//x,y here is relative to the fragment
		//by adding the fragmentPos the absolute position on screen is achieved
		//by removing the cardstackview position the location on the cardstackview is calculated
		x = x+fragmentPos[0]-absPosition[0];
		y = y+fragmentPos[1]-absPosition[1];

		selectedCard = cardstackView.getCardAtLocation(x, y);

		if(selectedCard != null && !selectedCard.isEnabled()){
			selectedCard = null;
			return;
		}

		//Notify listeners, notify only for allowed cards
		for(CardSelectedListener listener: this.selectListeners){
			listener.cardSelected(selectedCard);
		}

		if(selectedCard != null ){
			selectedCard.setSelected(true);
			for(Card card: cards.getCards()){
				if(card != selectedCard){
					card.setSelected(false);
				}
			}
			cardstackView.invalidate();
		}
	}

	/**
	 * @return the card selected by the user
	 */
	public Card getSelectedCard(){
		return selectedCard;
	}

	/**
	 * 
	 * @return the cards shown by this view, they can be adjusted
	 */
	public CardStack getCards(){
		return this.cards;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View result = inflater.inflate(R.layout.cardgrid, container, false); 

		CardStackView cardStack = (CardStackView)result.findViewById(R.id.cardgridstack);
		cardStack.setCards(this.cards);
		cardStack.setCardsPerLine(columnCount);

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int lines = cards.getSize()/columnCount;
		if(cards.getSize() % columnCount != 0){
			lines++;
		}
		cardStack.suggestCardHeight( (int) Math.round(metrics.heightPixels / lines / 266.0 * 180.0 ) );

		cardStack.requestLayout();
		cardStack.invalidate();

		result.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(gdt != null ){
					setSelectedCardFromLocation(event.getX(), event.getY());
					gdt.onTouchEvent(event);
				}
				return false;
			}
		});

		return result;
	}

	public void onStart(){
		super.onStart();
		gdt = new GestureDetector(this.getActivity(), new CardGridGestureListener());
	}

	@Override
	public void onDataChanged(DataChangeEvent event) {
		//No markers no action
		if(event.getEventType() == ChangeType.markersCleared){
			this.deactivate();
		}
		this.updateView();
	}

}
