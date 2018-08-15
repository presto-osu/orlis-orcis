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

package de.karbach.tac;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.Window;
import de.karbach.tac.ui.fragments.CardHolder;
import de.karbach.tac.ui.fragments.LocalBoard;

/**
 * Digital TAC board for a local board.
 * 
 * @author Carsten Karbach
 *
 */
public class TAC extends FragmentActivity {

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		//Remove title for app
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setContentView(R.layout.tac_frame);

		if(savedInstanceState == null){
			LocalBoard localboard = new LocalBoard();
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.add(R.id.fragment_container, localboard);
			transaction.commit();
		}
	}


	/**
	 * @return the main fragment currently shown by this activity, e.g. NetworkBoard or LocalBoard
	 */
	protected Fragment getMainFragment(){
		return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	public boolean onTouchEvent(MotionEvent event){
		boolean result = super.onTouchEvent(event);
		
		//Touch events are forwarded here to let all required fragments know about their motion events
		Fragment mainFragment = getMainFragment();
		if(mainFragment != null){
			if(mainFragment instanceof LocalBoard){
				LocalBoard local = (LocalBoard) mainFragment;
				return local.onTouchEvent(local.getView(), event);
			}
			if(mainFragment instanceof CardHolder){
				CardHolder cardHolder = (CardHolder) mainFragment;
				return cardHolder.onTouchEvent(event);
			}
		}

		return result;
	}

}
