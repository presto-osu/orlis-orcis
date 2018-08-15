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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import de.karbach.tac.R;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.ui.CardStackView;

/**
 * Grid of cards shown in a dialog. Allows to select one card
 * and store the selected card. 
 *
 */
public class CardGridDialog extends DialogFragment{

	/**
	 * Number of columns of cards
	 */
	private static final int columnCount = 5;
	
	/**
	 * Interface to implement for listening for dismiss events of the dialog. 
	 *
	 */
	public interface OnDismissListener{
		
		/**
		 * Called, when the dialog is dismissed
		 */
		public void onDismiss();
	}
	
	/**
	 * Detect touch events on this dialog 
	 *
	 */
	private class CardGridGestureListener extends SimpleOnGestureListener{
		
		public boolean onSingleTapConfirmed(MotionEvent e){
			
			boolean res = super.onSingleTapConfirmed(e);
			
			setSelectedCardFromLocation(e.getX(), e.getY());
			
			CardGridDialog.this.dismiss();
			
			return res;
		}
	}
	
	//Stack of cards shown here.
	private CardStack cards;
	//The card selected by the user
	private Card selectedCard;
	//Gesture detector for this dialog
	private GestureDetector gdt;
	//Listeners for this dialog
	private List<OnDismissListener> listeners;
	
	/**
	 * Init grid with the cards shown in this view.
	 */
	public CardGridDialog(){
		cards = new CardStack();
		for(int i=1; i<=13; i++){
			if(i == 11){
				continue;
			}
			Card c = new Card(Card.getCardNameForDistance(i));
			c.setDistance(i);
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
		
		listeners = new ArrayList<CardGridDialog.OnDismissListener>();
	}
	
	/**
	 * Add a new listener to dismiss events of this dialog.
	 * 
	 * @param listener the added listener
	 */
	public void addListener(OnDismissListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Assuming a user clicked on the grid view at the given location,
	 * this function sets the corresponding selected card.
	 * 
	 * @param x x location of the click event
	 * @param y y location of the click event
	 */
	protected void setSelectedCardFromLocation(float x, float y){
		if(CardGridDialog.this.getView() == null){
			return;
		}
		CardStackView cardstackView = (CardStackView)CardGridDialog.this.getView().findViewById(R.id.cardgridstack);
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
	public void onPause(){
		super.onPause();
		this.dismiss();
	}

	@Override
	public void onResume(){
		super.onResume();
	}
	
	@NonNull
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);

	  // request a window without the title
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  
	  return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		
		for(OnDismissListener listener: listeners){
			listener.onDismiss();
		}
		
		listeners.clear();
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
	
}
