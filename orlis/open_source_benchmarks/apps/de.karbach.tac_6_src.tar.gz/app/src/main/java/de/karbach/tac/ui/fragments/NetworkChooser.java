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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import de.karbach.tac.R;

/**
 * Chooses the role in a network game (board or card holder).
 * 
 * @author Carsten Karbach
 *
 */
public class NetworkChooser extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.multiplayer_menu, container, false);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Now the view is accessible via getView
        initButtonActions();
	}
	
	/**
     * Define the actions of the buttons for choosing the network mode.
     */
    protected void initButtonActions(){
    	View rootView = getView();
        if(rootView == null){
            return;
        }
    	//Go to network board with additional buttons and bluetooth support
    	Button boardButton = (Button)rootView.findViewById(R.id.boardButton);
    	boardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Replace this fragment with the new network board fragment
				FragmentManager fragMgr = getActivity().getSupportFragmentManager();
				FragmentTransaction fragTrans = fragMgr.beginTransaction();

				NetworkBoard board = new NetworkBoard();

				fragTrans.replace(R.id.fragment_container, board);
				fragTrans.addToBackStack("ToNetworkBoard");
				fragTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragTrans.commit();
			}
		});
    	
    	//Go to card holder for client
    	Button cardButton = (Button)rootView.findViewById(R.id.cardHolderButton);
    	cardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Replace this fragment with the new network board fragment
				FragmentManager fragMgr = getActivity().getSupportFragmentManager();
				FragmentTransaction fragTrans = fragMgr.beginTransaction();

				CardHolder holder = new CardHolder();

				fragTrans.replace(R.id.fragment_container, holder);
				fragTrans.addToBackStack("ToCardHolder");
				fragTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragTrans.commit();
			}
		});
    }
}
