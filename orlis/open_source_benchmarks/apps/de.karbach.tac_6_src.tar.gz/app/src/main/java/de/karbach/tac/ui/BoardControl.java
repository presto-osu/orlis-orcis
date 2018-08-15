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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import de.karbach.tac.Preferences;
import de.karbach.tac.R;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.BoardViewData;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.core.Move;
import de.karbach.tac.core.Point;
import de.karbach.tac.core.Point2D;
import de.karbach.tac.ui.fragments.CardGridDialog;
import de.karbach.tac.ui.fragments.CardGridDialog.OnDismissListener;
import de.karbach.tac.ui.fragments.CardGridFragment;
import de.karbach.tac.ui.fragments.CardGridFragment.CardSelectedListener;
import de.karbach.tac.ui.fragments.LocalBoard;

/**
 * Implements all actions, which can be executed on the board
 * 
 * @author Carsten Karbach
 *
 */
public class BoardControl extends SimpleOnGestureListener implements OnDismissListener,CardSelectedListener,ExportMovesTask.TaskFinishedCallback{

	/**
	 * The controlled board
	 */
	protected Board board;

	/**
	 * Data model, which is adjusted by this controller for updating the corresponding board
	 */
	protected BoardData data;

	/**
	 * Needed for storing and loading data
	 */
	private Fragment fragment;

	/**
	 * Data for the current view
	 */
	private BoardViewData viewdata;
	
	/**
	 * The key for preference to change the card layout
	 */
	private static final String layoutPrefKey = "card_select";

	/**
	 * Listens for preference changes
	 */
	private OnSharedPreferenceChangeListener prefListener;

	/**
	 * Multiply the length of the board with this factor to get the radius for the center circle
	 */
	public static float centerFactor = 0.125f;
	
	
	/**
	 * @param board the board controlled by this listener
	 */
	public BoardControl(Board board, Fragment fragment){
		this.board = board;
		try {
			data = BoardData.loadInstance(fragment.getActivity());
		} catch (Exception e) {
			//Could not load board for any reason
			// => init new board
			data = new BoardData();
		}

		try {
			viewdata = BoardViewData.loadInstance(fragment.getActivity());
		} catch (Exception e) {
			//Could not load board view data for any reason
			// => init new board
			viewdata = new BoardViewData();
		}
		viewdata.clearMarkers();
		viewdata.setDistance(-1);

		board.setData(data, viewdata);
		if(board instanceof BoardWithCards){
			BoardWithCards boardView = (BoardWithCards) board;
			boardView.setCardStack(data.getPlayedCards());
		}

		this.fragment = fragment;
		
		//Register listener for card layout preference change
		//This listener is automatically deleted, when the board control is re-created in onCreateView of the Localboard
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
		prefListener = new OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				if(BoardControl.this.fragment == null || BoardControl.this.fragment.getActivity()==null){
					return;
				}
				if(key.equals(layoutPrefKey)){
					String defaultLayout = BoardControl.this.fragment.getResources().getString(R.string.cardtypedefault);
					String chosen = prefs.getString(layoutPrefKey, defaultLayout);
					setCardTypeByChosenValue(chosen,defaultLayout);
				}
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(prefListener);
		
		String defaultLayout = BoardControl.this.fragment.getResources().getString(R.string.cardtypedefault);
		String chosen = prefs.getString(layoutPrefKey, defaultLayout);
		setCardTypeByChosenValue(chosen,defaultLayout);
	}
	
	/**
	 * Adjust the card type according to the chosen preference value.
	 * @param newValue the new value to be used for the card layout
	 * @param defaultValue the default string of the card layout property
	 */
	protected void setCardTypeByChosenValue(String newValue, String defaultValue){
		String defaultLayout = BoardControl.this.fragment.getResources().getString(R.string.cardtypedefault);
		if(newValue.equals(defaultLayout)){
			Card.setCardType(1);
		}
		else{
			Card.setCardType(2);
		}
	}

	/**
	 * Connect viewdata with the fragment if necessary
	 */
	protected void addCardGridFragmentListener(){
		//Add listener for cardgridfragment
		if(! useDialog()){
			CardGridFragment cardGridFragment = getCardGridFragment();
			if(cardGridFragment != null){
				cardGridFragment.addCardSelectListener(this);
				Point startPoint = data.getActionStarter();
				if(startPoint!=null){
					CardStack cards = cardGridFragment.getCards();

					updateCardStates(cards, startPoint);
				}
				viewdata.addListener(cardGridFragment);
			}
		}
	}

	/**
	 * 
	 * @return true, if a dialog is used for showing card options, false if the fragment in the activity is used
	 */
	protected boolean useDialog(){
		boolean useDialog = true;//If true a dialog is shown otherwise the fragment is searched in the activity
		if(fragment instanceof LocalBoard){
			LocalBoard locboard = (LocalBoard) fragment;
			if(locboard.isOrientationLandscape()){
				useDialog = false;
			}
		}
		return useDialog;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	//gridview allowing access to the selected card
	private CardGridDialog cardgrid;

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	public void onLongPress (MotionEvent e){

		int min = board.getWidth();
		if(board.getHeight() < min){
			min=board.getHeight();
		}

		int[] absPosition=new int[2];
		board.getLocationOnScreen(absPosition);
		float x = e.getX()-absPosition[0];
		float y = e.getY()-absPosition[1];
		Point2D rescaledPoint = viewdata.invScale((int)x, (int)y);
		if(rescaledPoint.x>min || rescaledPoint.y>min || rescaledPoint.x<0 || rescaledPoint.y<0){
			return;
		}

		Point startPoint = data.getClosestBall(((float)rescaledPoint.x)/min, ((float)rescaledPoint.y/min));
		if(startPoint == null){
			return;
		}

		startActionOnPoint(startPoint);
	}

	/**
	 * Set card states to enabled, if the user can play the card with the ball from
	 * startPoint. If the card is not allowed, disable it.
	 * @param cards the cards, whcih are checked
	 * @param startPoint the starting point of the ball
	 */
	protected void updateCardStates(CardStack cards, Point startPoint){
		//Change enabled state for allowed cards, this code can be used for both dialog and top fragment
		for(Card card: cards.getCards()){
			boolean cardAllowed = false;
			//Is trickser allowed
			if(card.getCardName().equals("trickser") ){
				List<Point> targets = data.getBallsInRing();
				if(! startPoint.isTargetField() && ! startPoint.isStartField() && targets.size() > 1){
					cardAllowed = true;
				}
			}
			else if(card.getCardName().equals("krieger") ){
				if(! startPoint.isTargetField() && !startPoint.isStartField()){//Allowed for all balls on the ring
					cardAllowed = true;
				}
			}
			else{
				int dist = card.getDistance();
				cardAllowed = data.isMoveInDistanceAllowed(dist, startPoint);
			}
			card.setEnabled(cardAllowed);
		}
	}

	/**
	 * The cardgridfragment stored, when using landscape mode
	 */
	private CardGridFragment cgFragment;
	
	/**
	 * Make the card grid fragment of the landscape board known to this class.
	 * Only when this is done, the controller can interact with the fragment
	 * @param cgFragment the fragment instance
	 */
	public void registerCardGridFragment(CardGridFragment cgFragment){
		if(this.cgFragment != null){
			viewdata.removeListener( this.cgFragment );
		}
		
		this.cgFragment = cgFragment;
		
		addCardGridFragmentListener();
	}
	
	/**
	 * 
	 * @return the fragment of class CardGridFragment or null if none exists (e.g. in portrait mode)
	 */
	protected CardGridFragment getCardGridFragment(){
		if(this.cgFragment != null){
			return cgFragment;
		}
		
		FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
		List<Fragment> frags = fm.getFragments();
		CardGridFragment cgFragment = null;
		for(Fragment f: frags){
			if(f instanceof CardGridFragment){
				cgFragment = (CardGridFragment)f;
				break;
			}
		}
		
		return cgFragment;
	}

	/**
	 * Show popup for selecting a card action or activate card grid on the right for
	 * landscape mode.
	 * @param startPoint the starting point for the action
	 */
	protected void startActionOnPoint(Point startPoint){
		data.clearActionMarkers();

		startPoint.setActionField(true);
		startPoint.setActionStarter(true);
		viewdata.actionStateChanged();

		boolean useDialog = true;//If true a dialog is shown otherwise the fragment is searched in the activity
		if(fragment instanceof LocalBoard){
			LocalBoard locboard = (LocalBoard) fragment;
			if(locboard.isOrientationLandscape()){
				useDialog = false;
			}
		}

		FragmentManager fm = fragment.getActivity().getSupportFragmentManager();

		CardStack cards;

		if(useDialog){

			boolean openDialog = true;
			if(fragment instanceof LocalBoard){
				LocalBoard lboard = (LocalBoard)fragment;
				if(lboard.isShowingDialog()){
					openDialog = false;
				}
			}
			
			if(openDialog){
				cardgrid = new CardGridDialog();

				cards = cardgrid.getCards();
				updateCardStates(cards, startPoint);

				cardgrid.addListener(this);

				cardgrid.show(fm, "Cards");
			}
		}
		else{
			//Cards are shown as CardGridFragment directly on top of the activity
			CardGridFragment cgFragment = getCardGridFragment();

			cards = cgFragment.getCards();

			updateCardStates(cards, startPoint);
			cgFragment.clearSelectedCard();
			cgFragment.updateView();
		}

	}

	@Override
	public boolean onDoubleTap (MotionEvent e){
		if(!viewdata.isMoving()){
			int[] absPosition=new int[2];
			board.getLocationOnScreen(absPosition);
			float x = e.getX()-absPosition[0];
			float y = e.getY()-absPosition[1];

			doubleTab(x, y);
		}

		return false;
	}


	/**
	 * Called on touch events on the board.
	 * @param event the touch event
	 * @return if the action was consumed by this listener
	 */
	public boolean onTouch(MotionEvent event) {
		int[] absPosition=new int[2];
		board.getLocationOnScreen(absPosition);

		if(event.getAction() == MotionEvent.ACTION_UP){
			move();
			checkButtonStates();
		}

		return false;
	}

	/**
	 * Expects the startPoint to have a ball on it.
	 * Executes the action moving the ball from startPoint to the given endPoint.
	 * Uses the data store in boarddata.getActionCardId() to run the corresponding action.
	 * The default action is to simply move the ball from the start point to the target.
	 * But this differs for instance for trickser or krieger cards.
	 * @param startPoint starting point for an action
	 * @param endPoint end point for an action
	 */
	protected void makeAction(Point startPoint, Point endPoint){
		
		String card = data.getActionCardName();
		
		//Check if action is inited with a trickser card
		if(card!=null && card.equals("trickser")){
			data.switchBalls(startPoint, endPoint);
		}
		else{
			//Allow to beat the ball itself for the krieger
			if(card!=null && card.equals("krieger") && startPoint == endPoint){
				if( data.getBallsInRing().size() == 1 ){//Kill the starting ball only if it is the only ball in the ring
					Point startField = data.getStartField(startPoint.getBallID());
					if(startField != null){
						data.moveBall(startPoint, startField);
					}
				}
			}
			else{
				data.moveBall(startPoint, endPoint);
			}
		}

		viewdata.setDistance( -1 );
		data.clearActionMarkers();
		viewdata.clearMarkers();
		checkButtonStates();
		
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e){

		int min = board.getWidth();
		if(board.getHeight() < min){
			min=board.getHeight();
		}

		int[] absPosition=new int[2];
		board.getLocationOnScreen(absPosition);
		float x = e.getX()-absPosition[0];
		float y = e.getY()-absPosition[1];
		Point2D rescaledPoint = viewdata.invScale((int)x, (int)y);
		if(rescaledPoint.x>min || rescaledPoint.y>min || rescaledPoint.x<0 || rescaledPoint.y<0){
			return false;
		}

		/**
		 * Check if center was clicked, then show list of moves
		 */
		double center = min*0.5;
		double centerDistance = Math.sqrt((rescaledPoint.x-center)*(rescaledPoint.x-center)+(rescaledPoint.y-center)*(rescaledPoint.y-center));
		if(centerDistance < centerFactor*min){ // If user clicked in the middle of the field:
			ArrayList<Move> moves = data.getPlayedCards().toMoveList();

			Activity activity = this.fragment.getActivity();
			Intent showMoveListIntent = new Intent(activity, MoveListActivity.class);
			showMoveListIntent.putExtra(MoveListActivity.MOVE_LIST, moves);
			ArrayList<Integer> colors = new ArrayList<Integer>(data.getColors());
			showMoveListIntent.putIntegerArrayListExtra(MoveListActivity.COLOR_LIST, colors);
			this.fragment.getActivity().startActivity(showMoveListIntent);
			return false;
		}

		Point startPoint = data.getActionStarter();
		if(startPoint == null){
			//No action running
			//Start a new one
			startPoint = data.getClosestBall(((float)rescaledPoint.x)/min, ((float)rescaledPoint.y/min));
			if(startPoint == null){
				return false;
			}

			startActionOnPoint(startPoint);

			return false;
		}
		//Action already running, move the ball
		Point endPoint = data.getClosestTarget( ((float)rescaledPoint.x)/min, ((float)rescaledPoint.y/min));
		if(endPoint == null){
			return false;
		}

		makeAction(startPoint, endPoint);

		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {

		if(!viewdata.isMoving()){
			int[] absPosition=new int[2];
			board.getLocationOnScreen(absPosition);
			int x1 = (int)(e1.getX()-absPosition[0]);
			int y1 = (int)(e1.getY()-absPosition[1]);
			int x2 = (int)(e2.getX()-absPosition[0]);
			int y2 = (int)(e2.getY()-absPosition[1]);

			moving(x1,y1,x2,y2);
		}
		else{
			//Move the board itself
			shiftX(-(int)distanceX);
			shiftY(-(int)distanceY);
		}

		return false;
	}

	/**
	 * Determine the state of all buttons.
	 * Enable/Disable them if necessary.
	 */
	public void checkButtonStates(){
		if(fragment.getView() == null){
			return;
		}

		EnDisImageButton leftButton = (EnDisImageButton)fragment.getView().findViewById(R.id.leftButton);
		EnDisImageButton rightButton = (EnDisImageButton)fragment.getView().findViewById(R.id.rightButton);
		EnDisImageButton moveButton = (EnDisImageButton)fragment.getView().findViewById(R.id.moveButton);
		EnDisImageButton colorButton = (EnDisImageButton)fragment.getView().findViewById(R.id.switchButton);
		EnDisImageButton zoominButton = (EnDisImageButton)fragment.getView().findViewById(R.id.plusButton);
		EnDisImageButton zoomoutButton = (EnDisImageButton)fragment.getView().findViewById(R.id.minusButton);

		leftButton.setActive(data.canGoBack());
		rightButton.setActive(data.canGoForward());
		moveButton.setActive(viewdata.isMoving());
		colorButton.setActive(viewdata.isColorSwitch());
		zoominButton.setActive(canZoomIn());
		zoomoutButton.setActive(canZoomOut());
	}

	/**
	 * Go one step backward in history
	 */
	public void stepLeft(){
		data.goBack();
		checkButtonStates();
	}

	/**
	 * Go one step forward in history.
	 */
	public void stepRight(){
		data.goForward();
		checkButtonStates();
	}

	/**
	 * This function is executed, if a real restart should be run.
	 * The simple restart function first asks the user, if a restart is
	 * really requested.
	 */
	protected void realRestart(){

		if(viewdata != null){
			viewdata.removeListener( getCardGridFragment() ); 
		}
		
		data = new BoardData();
		data.save(fragment.getActivity());
		viewdata = new BoardViewData();
		board.setData(data, viewdata);
		if(board instanceof BoardWithCards){
			BoardWithCards boardView = (BoardWithCards) board;
			boardView.setCardStack(data.getPlayedCards());
		}

		addCardGridFragmentListener();

		checkButtonStates();
	}

	/**
	 * Restart the game. Delete history.
	 */
	public void restart(){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					realRestart();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(board.getContext());
		builder.setMessage(R.string.reallyrestart).setPositiveButton(R.string.yes, dialogClickListener)
		.setNegativeButton(R.string.no, dialogClickListener).show();
	}

	/**
	 * Called on stop action of the activity.
	 * Save the board data.
	 */
	public void stopActivity(){
		data.save(fragment.getActivity());
		viewdata.save(fragment.getActivity());
	}

	/**
	 * Zoom into the board
	 */
	public void zoomIn(){
		scale(1.5f);
		checkButtonStates();
	}

	/**
	 * Zoom out action for the board
	 */
	public void zoomOut(){
		scale(1.0f / 1.5f);
		checkButtonStates();
	}

    /**
     * Make image of the current board and show it in an image viewer activity
     *
     * @param onlyUpdateTask if true, the existing task is only updated, otherwise a new task can also be started
     */
    public void makeAndShowBoardImage(boolean onlyUpdateTask){
        View fragmentview = fragment.getView();
        ProgressBar progressbar = null;
        if(fragmentview != null) {
            progressbar = (ProgressBar) fragmentview.findViewById(R.id.progressBar);
            if (progressbar != null) {
                progressbar.setProgress(0);
            }
        }

        ExportMovesTask exporter = ExportMovesTask.getInstance();//Might return null
        if(exporter != null){//Needed tio check, whether an instance existed before
            ExportMovesTask.createInstance(data.copy(), viewdata.copy(), fragment.getActivity(), this, progressbar);
            if(progressbar != null){
                progressbar.setVisibility(ProgressBar.VISIBLE);
            }
            //No need to call execution here
            return;
        }
        if(onlyUpdateTask){
            return;
        }

        exporter = ExportMovesTask.createInstance(data.copy(), viewdata.copy(), fragment.getActivity(), this, progressbar);
        exporter.execute();
        if(progressbar != null){
            progressbar.setVisibility(ProgressBar.VISIBLE);
        }
    }

    /**
     * Show a list of all exported games
     */
    public void showExports(){
        List<String> exports = ExportMovesTask.getStoredImages(fragment.getActivity(), true);
        if(exports != null) {
            //Sort first by date, then by game id, then reverse ordered by part id
            Collections.sort(exports, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    String ldate = ExportMovesTask.getDateFromFilename(lhs, false);
                    String rdate = ExportMovesTask.getDateFromFilename(rhs, false);

                    int cmp = rdate.compareTo(ldate);
                    if (cmp != 0) {
                        return cmp;
                    }

                    int lgame = Integer.parseInt(ExportMovesTask.getGameIDFromFilename(lhs));
                    int rgame = Integer.parseInt(ExportMovesTask.getGameIDFromFilename(rhs));

                    if (lgame < rgame) {
                        return 1;
                    }
                    if (lgame > rgame) {
                        return -1;
                    }

                    Integer lpart = Integer.parseInt(ExportMovesTask.getPartIDFromFilename(lhs));
                    Integer rpart = Integer.parseInt(ExportMovesTask.getPartIDFromFilename(rhs));
                    //Show smaller part number first
                    return lpart.compareTo(rpart);
                }
            });
        }

        Intent intent = new Intent(fragment.getActivity(), ExportedImagesActivity.class);
        ArrayList<String> files;
        if(exports == null){
            files = new ArrayList<String>();
        }
        else{
            files = new ArrayList<String>(exports);
        }
        intent.putStringArrayListExtra(ExportedImagesActivity.FILE_LIST, files);
        fragment.getActivity().startActivity(intent);
    }

    @Override
    public void taskIsFinished(){
        if(fragment == null){
            return;
        }
        View fragmentview = fragment.getView();
        if(fragmentview == null){
            return;
        }
        ProgressBar progressbar = (ProgressBar) fragmentview.findViewById(R.id.progressBar);
        if(progressbar != null){
            progressbar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

	/**
	 * Switch from current tricksing mode to the opposite.
	 */
	public void switchTricksingMode(){
		setTricksingMode(!viewdata.isTricksing());
		if(viewdata.isTricksing()){
			viewdata.clearMarkers();
			data.clearActionMarkers();
		}
	}

	/**
	 * Switch from current colo swicth mode to the opposite.
	 */
	public void switchColorSwitchMode(){
		viewdata.setColorSwitch(!viewdata.isColorSwitch());
		checkButtonStates();
	}

	/**
	 * Switch from current moving mode to the opposite.
	 */
	public void switchMoveMode(){
		setMovingMode(!viewdata.isMoving());
	}

	/**
	 * Enable/Disable tricksing mode.
	 * @param tricksing true, if tricksing mode should be enabled
	 */
	protected void setTricksingMode(boolean tricksing){
		viewdata.setTricksing(tricksing);
		checkButtonStates();
	}

	/**
	 * Enable/Disable moving mode. Allows for scroll the board.
	 * @param moving true, if moving mode is activated.
	 */
	protected void setMovingMode(boolean moving){
		viewdata.setMoving(moving);
		checkButtonStates();
	}

	/**
	 * Handle double tab on the board.
	 * Start a ball from its home field to the first field on the outer circle.
	 * 
	 * @param x x-position on the board, where double tab took place
	 * @param y y-position on the board, where double tab took place
	 */
	public void doubleTab(float x, float y){
		int min = board.getWidth();
		if(board.getHeight() < min){
			min=board.getHeight();
		}

		Point2D rescaledPoint = viewdata.invScale((int)x, (int)y);
		if(rescaledPoint.x>min || rescaledPoint.y>min || rescaledPoint.x<0 || rescaledPoint.y<0){
			return;
		}

		Point startPoint = data.getClosestBall(((float)rescaledPoint.x)/min, ((float)rescaledPoint.y/min));

		data.startBall(startPoint);

		data.clearActionMarkers();
		viewdata.clearMarkers();
	}

	/**
	 * Called when moving from one point to another
	 * @param x1 x coordinate of first point
	 * @param y1 y coordinate of first point
	 * @param x2 x coordinate of second point
	 * @param y2 y coordinate of second point
	 */
	public void moving(float x1, float y1, float x2, float y2){
		data.clearActionMarkers();

		int min = board.getWidth();
		if(board.getHeight() < min){
			min=board.getHeight();
		}

		Point2D p1 = viewdata.invScale((int)x1, (int)y1);
		Point2D p2 = viewdata.invScale((int)x2, (int)y2);

		viewdata.setStartPoint(data.getClosestBall(((float)p1.x)/min, ((float)p1.y/min)));
		Point endPointBefore = viewdata.getEndPoint();
		if(!viewdata.isTricksing() && !viewdata.isColorSwitch()){
			viewdata.setEndPoint( data.getClosestPoint(((float)p2.x/min), ((float)p2.y/min)) );
			viewdata.setDistance( viewdata.getStartPoint().getDistance(viewdata.getEndPoint()) );
		}
		else{
			viewdata.setEndPoint( data.getClosestBall(((float)p2.x/min), ((float)p2.y/min)) );
		}
		Point endPointAfter = viewdata.getEndPoint();
		
		if(endPointBefore!=null && endPointBefore != endPointAfter){
			board.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
		
		//Dont do anything, if there is no ball selected
		if(viewdata.getStartPoint().getBallID() == -1){
			data.clearActionMarkers();
			viewdata.clearMarkers();
		}
		else{
			viewdata.setCx( p2.x );
			viewdata.setCy( p2.y );
		}
	}

	/**
	 * Move the ball from the last startPoint to last endPoint
	 */
	public void move(){
		if(viewdata.getStartPoint() == null || viewdata.getEndPoint()==null){
			return;
		}

		if(viewdata.isTricksing()){
			data.switchBalls(viewdata.getStartPoint(), viewdata.getEndPoint());
		}
		else if(viewdata.isColorSwitch()){
			data.switchColors(viewdata.getStartPoint().getBallID()/4, viewdata.getEndPoint().getBallID()/4);//Ballid / 4 = playerId
		}
		else{
			data.moveBall(viewdata.getStartPoint(), viewdata.getEndPoint());
		}

		viewdata.setDistance( -1 );
		data.clearActionMarkers();
		viewdata.clearMarkers();
	}

	/**
	 * Shift map diffx pixels to the right
	 * @param diffx pixels of translation in x-direction
	 */
	public void shiftX(int diffx){
		viewdata.setVx( viewdata.getVx()+diffx);
		viewdata.checkBoardBorders(board.getWidth(), board.getHeight());
	}

	/**
	 * Shift map diffy pixels to the bottom
	 * @param diffy pixels of translation in x-direction
	 */
	public void shiftY(int diffy){
		viewdata.setVy( viewdata.getVy()+diffy);
		viewdata.checkBoardBorders(board.getWidth(), board.getHeight());
	}

	/**
	 * Allows to set scalex and scaley with an absolute value.
	 * The map is exactly scaled with the given factor.
	 * 
	 * @param scale the new scale factor
	 */
	public void setScale(float scale){
		if(scale < BoardViewData.minscale ){
			scale = BoardViewData.minscale;
		}
		if(scale > BoardViewData.maxscale){
			scale = BoardViewData.maxscale;
		}

		float factor = scale/viewdata.getScalex();
		scale(factor);
	}

	//Epsilon to compare float values
	protected final double eps = 1e-5;
	
	/**
	 * 
	 * @return true, if zoomout function can be called
	 */
	public boolean canZoomOut(){
		if( Math.abs(viewdata.getScalex()-BoardViewData.minscale) < eps || viewdata.getScalex()<BoardViewData.minscale ){
			return false;
		}
		else{
			return true;
		}
	}
	
	/**
	 * 
	 * @return true, if zoomin function can be called
	 */
	public boolean canZoomIn(){
		if( Math.abs(viewdata.getScalex()-BoardViewData.maxscale) < eps || viewdata.getScalex()>BoardViewData.maxscale ){
			return false;
		}
		else{
			return true;
		}
	}
	
	/**
	 * Scale the board with the given factor.
	 * E.g. factor 0.5 means zooming out or showing the board smaller
	 * @param factor
	 */
	public void scale(float factor){
		if(viewdata.getScalex()*factor < BoardViewData.minscale){
			factor = BoardViewData.minscale/viewdata.getScalex();
		}
		if(viewdata.getScaley()*factor < BoardViewData.minscale){
			factor = BoardViewData.minscale/viewdata.getScaley();
		}
		if(viewdata.getScalex()*factor > BoardViewData.maxscale){
			factor = BoardViewData.maxscale/viewdata.getScalex();
		}
		if(viewdata.getScaley()*factor > BoardViewData.maxscale){
			factor = BoardViewData.maxscale/viewdata.getScaley();
		}

		viewdata.setScalex( viewdata.getScalex()*factor);
		viewdata.setScaley( viewdata.getScaley()*factor);

		viewdata.setVx( Math.round( viewdata.getVx() * factor ) );
		viewdata.setVy( Math.round(viewdata.getVy() * factor ) );

		viewdata.checkBoardBorders(board.getWidth(), board.getHeight());
	}

	/**
	 * 
	 * @return the larger of the two scale factors.
	 */
	public float getScaleFactor(){
		float scalex = viewdata.getScalex();
		float scaley = viewdata.getScaley();

		if(scalex > scaley){
			return scalex;
		}
		else{
			return scaley;
		}
	}

	/**
	 * This function allows access on the board data.
	 * It is only allowed to read the data.
	 * @return read only access on the board data.
	 */
	public BoardData getBoardData(){
		return this.data;
	}

	/**
	 * This function allows access on the board view data.
	 * It is only allowed to read the data.
	 * @return read only access on the board view data.
	 */
	public BoardViewData getBoardViewData(){
		return this.viewdata;
	}

	@Override 
	public void onDismiss() {
		if(fragment == null || fragment.getActivity() == null){
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
		boolean runActionDirectly = prefs.getBoolean(Preferences.CARDDIRECTPLAY_KEY, true);
		
		//cardgrid dialog was closed
		if(cardgrid != null){

			Card card = cardgrid.getSelectedCard();

			if(card==null){
				data.clearActionMarkers();
				return;
			}

			//Handle trickser or krieger
			if(card.getCardName().equals("trickser") || card.getCardName().equals("krieger")){
				handleKriegerAndTrickser(card, runActionDirectly);
				return;
			}
			else{
				//Handle normal move cards

				if(Card.getDistanceForDrawable(card.getDrawableId()) == -1 ){

					return;
				}

				int distance = Card.getDistanceForDrawable(card.getDrawableId());

				data.setActionCard(card.getCardName());

				Point startPoint = data.getActionStarter();
				if(startPoint == null){
					return;
				}

				boolean forward = true;
				if(distance == 4){
					forward = false;
				}

				int realDistance = distance;
				if(startPoint.isStartField()){
					realDistance = 1;
				}
				List<Point> targets = startPoint.getPointsInDistance(realDistance, forward);
				List<Point> allowedTargets = new ArrayList<Point>();
				for(Point t: targets){
					if(data.isMoveAllowed(startPoint, t, realDistance)){
						t.setActionField(true);
						allowedTargets.add(t);
					}
				}
				viewdata.actionStateChanged();
				
				//Run the action directly, if there is only one possible target point
				if(runActionDirectly && allowedTargets.size() == 1){
					makeAction(startPoint, allowedTargets.get(0));
				}
			}
		}
	}

	/**
	 * Check if card is a trickser or krieger.
	 * If so, set the corresponding target fields.
	 * @param card the selected card
	 * @param runActionDirectly if true, run an action immediately, if there is only one target field
	 * @return true, if target fields were set successfully, false if anything went wrong or the card is not krieger and not trickser
	 */
	protected boolean handleKriegerAndTrickser(Card card, boolean runActionDirectly){
		if(card.getCardName().equals("trickser")){
			Point startPoint = data.getActionStarter();

			if(startPoint == null){
				return false;
			}

			data.clearActionMarkers();
			data.setActionCard("trickser");

			startPoint.setActionStarter(true);
			startPoint.setActionField(true);

			List<Point> targets = data.getBallsInRing();
			if(targets.contains(startPoint)){
				targets.remove(startPoint);
			}
			for(Point t: targets){
				t.setActionField(true);
			}
			viewdata.actionStateChanged();
			
			//Run the action directly, if there is only one possible target point
			if(runActionDirectly && targets.size() == 1){
				makeAction(startPoint, targets.get(0));
			}

			return true;
		}
		else if(card.getCardName().equals("krieger")){//Handle krieger
			Point startPoint = data.getActionStarter();

			if(startPoint == null){
				return false;
			}

			data.clearActionMarkers();
			data.setActionCard(card.getCardName());
			
			startPoint.setActionStarter(true);
			startPoint.setActionField(true);

			Point next = startPoint.getNextBallOnRing();
			if(next != null){
				next.setActionField(true);
			}

			viewdata.actionStateChanged();

			//Run the action directly, if there is only one possible target point
			if(runActionDirectly && next!= null){
				makeAction(startPoint, next);
			}
			
			return true;
		}

		return false;
	}

	@Override
	public void cardSelected(Card card) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
		boolean runActionDirectly = prefs.getBoolean(Preferences.CARDDIRECTPLAY_KEY, true);
		
		if(card==null){
			return;
		}

		//Handle trickser or krieger
		if(card.getCardName().equals("trickser") || card.getCardName().equals("krieger")){
			handleKriegerAndTrickser(card, runActionDirectly);
			return;
		}
		else{

			if(Card.getDistanceForDrawable(card.getDrawableId()) == -1 ){
				data.clearActionMarkers();
				return;
			}

			int distance = Card.getDistanceForDrawable(card.getDrawableId());

			Point startPoint = data.getActionStarter();

			if(startPoint == null){
				return;
			}

			data.clearActionMarkers();

			data.setActionCard(card.getCardName());

			startPoint.setActionStarter(true);
			startPoint.setActionField(true);

			boolean forward = true;
			if(distance == 4){
				forward = false;
			}

			int realDistance = distance;
			if(startPoint.isStartField()){
				realDistance = 1;
			}
			List<Point> targets = startPoint.getPointsInDistance(realDistance, forward);
			List<Point> allowedTargets = new ArrayList<Point>();
			for(Point t: targets){
				if(data.isMoveAllowed(startPoint, t, realDistance)){
					t.setActionField(true);
					allowedTargets.add(t);
				}
			}
			viewdata.actionStateChanged();
			//Run the action directly, if there is only one possible target point
			if(runActionDirectly && allowedTargets.size() == 1){
				makeAction(startPoint, allowedTargets.get(0));
			}
		}
	}

}
