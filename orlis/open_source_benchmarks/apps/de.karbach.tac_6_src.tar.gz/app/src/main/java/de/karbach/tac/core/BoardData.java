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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;

import de.karbach.tac.core.DataChangeEvent.ChangeType;
import de.karbach.tac.ui.Board;

/**
 * Stores logical data for one board
 * 
 * @author Carsten Karbach
 *
 */
public class BoardData implements Serializable{

	/**
	 * Version for serialization
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Filename of where to save the history of this board
	 */
	private static final String filename="boarddata.obj";

	/**
	 * Number of fields on the big circle
	 */
	private final int numCircleFields=64;

	/**
	 * Save all points
	 */
	private ArrayList<Point> points;

	/**
	 * Saves a snapshot for each move executed on this board
	 */
	private ArrayList<ArrayList<Point>> history;

	/**
	 * Saves index in history of the current position
	 */
	private int historyPosition=0;
	
	/**
	 * Stores the cards played by the actions directly on the board.
	 * Is updated with every action on the board.
	 * It stores its own history.
	 */
	private CardStack playedCards;
	
	/**
	 * Number of cards stored in the above stack.
	 * The maximum number of history steps stored, too.
	 */
	private static final int maximumStepsStored = 101;
	
	/**
	 * Colors for the player balls
	 * List of colors assigned to the different players, 
	 * the first color is assigned to player 0, the second to player 1 and so forth 
	 */
	private List<Integer> colors;
	
	/**
	 * List of registered listeners for data changes
	 */
	private transient List<DataChangeListener> listeners;
	
	/**
	 * Store for each player ID, which target fields are allowed for him
	 */
	private transient SparseArray<List<Point>> playerIdToTargetFields;
	
	/**
	 * The ID of the currently active action card
	 */
	private String actionCardName = null;
	
	/**
	 * If the last ball move can be assigned to a player, this variable will store the player's ID.
	 * Otherwise, it will be set to -1.
	 */
	private int lastCardPlayedByID = -1;

	/**
	 * Init standard board
	 */
	public BoardData(){
		history = new ArrayList<ArrayList<Point>>();

		points = getStartPoints();
		
		playedCards = new CardStack();
		
		listeners = new ArrayList<DataChangeListener>();

		initColors();
		
		initAfterLoading();

		saveSnapshot();
	}

    /**
     * Make a copy of any serializable object.
     *
     * @param toCopy the object, which is copied
     *
     * @return deep copy of toCopy
     */
    public static <T extends Serializable> T copy(T toCopy){
        //Serialize this object to bytearray
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oOut;
        byte[] byteOutput;
        try {
            oOut = new ObjectOutputStream(bos);
            oOut.writeObject(toCopy);
            oOut.close();
            byteOutput = bos.toByteArray();

        } catch (IOException e) {
            return null;
        }
        //Unserialize it and return the unserialized object
        try {
            ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(byteOutput));
            T loaded = (T) oIn.readObject();

            return loaded;
        } catch (IOException e1) {
            return null;
        } catch (ClassNotFoundException e2) {
            return null;
        }
    }

    /**
     * Make a deep copy of this boarddata.
     * Do not copy the transient members.
     * @return deep copy of this boarddata or null, if error occurred
     */
    public BoardData copy(){
        BoardData result = BoardData.copy(this);
        if(result != null){
            result.initAfterLoading();
        }
        return result;
    }

    /**
     * Use the start point setting. Init the fields only with the balls given
     * as parameter. Set all other point's ball ID to -1. This allows to generate
     * the entire point field by saving only those fields, which actually have a
     * ball on them.
     *
     * @param ballsOnly list of all Points, which do have balls on
     * @return point field inited with the ballsOnly parameter
     */
    protected ArrayList<Point> getPointFieldInitedWithBalls(ArrayList<Point> ballsOnly){
        ArrayList<Point> result = getStartPoints();
        for(Point resPoint: result){
            resPoint.setBallID(-1);
            for(Point ballPoint: ballsOnly){
                double distance = (resPoint.getPosX()-ballPoint.getPosX())*(resPoint.getPosX()-ballPoint.getPosX())+(resPoint.getPosY()-ballPoint.getPosY())*(resPoint.getPosY()-ballPoint.getPosY());
                if(distance < 0.00001){//Are these identical points?
                    resPoint.setBallID( ballPoint.getBallID() );
                }
            }
        }
        return result;
    }

    /**
     * Get a list of all points and their initial ball locations
     * when a new game is started.
     *
     * @return list of all points of the field
     */
    protected ArrayList<Point> getStartPoints(){
        ArrayList<Point> result = new ArrayList<Point>();

        double radiusFactor = 0.9;

        //Generate outer circle
        double part = 360.0/numCircleFields;
        for(int i=0; i<numCircleFields; i++){
            double radian = part*i/360.0*2*Math.PI;
            float posx = (float)(0.5+Math.cos(radian)/2*radiusFactor);
            float posy = (float)(0.5+Math.sin(radian)/2*radiusFactor);

            Point p = new Point(posx, posy);
            result.add(p);
        }

        //Generate start fields in corners
        float middle = 0.08f;//Middle of rectabngle for start fields
        float diff = 0.02f;
        //Upper left rectangle
        result.add(new Point(middle-diff,middle-diff, 0));
        result.add(new Point(middle+diff,middle-diff, 1));
        result.add(new Point(middle-diff,middle+diff, 2));
        result.add( new Point(middle+diff,middle+diff, 3));
        //Upper right
        result.add(new Point(1-(middle-diff),middle-diff, 4 ));
        result.add(new Point(1-(middle+diff),middle-diff, 5));
        result.add(new Point(1-(middle-diff),middle+diff, 6));
        result.add(new Point(1-(middle+diff),middle+diff, 7));
        //Lower right
        result.add(new Point(1-(middle-diff),1-(middle-diff), 8));
        result.add(new Point(1-(middle+diff),1-(middle-diff), 9));
        result.add(new Point(1-(middle-diff),1-(middle+diff), 10));
        result.add(new Point(1-(middle+diff),1-(middle+diff), 11));
        //Lower left
        result.add(new Point(middle-diff,1-(middle-diff), 12));
        result.add(new Point(middle+diff,1-(middle-diff), 13));
        result.add(new Point(middle-diff,1-(middle+diff), 14));
        result.add(new Point(middle+diff,1-(middle+diff), 15));

        //Generate target fields
        //Upper left
        Point target = new Point(0.5f, 0.18f);
        target.setTargetField(true);
        result.add(target );//80
        target = new Point(0.57f, 0.29f);
        target.setTargetField(true);
        result.add(target );//81
        target = new Point(0.5f, 0.33f);
        target.setTargetField(true);
        result.add(target );//82
        target = new Point(0.43f, 0.29f);
        target.setTargetField(true);
        result.add(target );//83

        //Lower right
        target = new Point(0.5f, 1-0.18f);
        target.setTargetField(true);
        result.add(target );//84
        target = new Point(0.43f, 0.702f);
        target.setTargetField(true);
        result.add(target );//85
        target = new Point(0.5f, 0.665f);
        target.setTargetField(true);
        result.add(target );//86
        target = new Point(0.57f, 0.702f);
        target.setTargetField(true);
        result.add(target );//87
        //Lower left
        target = new Point(0.218f, 0.418f);
        target.setTargetField(true);
        result.add(target );//88
        target = new Point(0.2845f, 0.456f);
        target.setTargetField(true);
        result.add(target );//89
        target = new Point(0.2845f, 0.536f);
        target.setTargetField(true);
        result.add(target );//90
        target = new Point(0.218f, 0.577f);
        target.setTargetField(true);
        result.add(target );//91
        //Upper right
        target = new Point(1-0.218f-0.004f, 0.577f);
        target.setTargetField(true);
        result.add(target );//92
        target = new Point(1-0.2845f-0.004f, 0.536f);
        target.setTargetField(true);
        result.add(target );//93
        target = new Point(1-0.2845f-0.004f, 0.456f);
        target.setTargetField(true);
        result.add(target );//94
        target = new Point(1-0.218f-0.004f, 0.418f);
        target.setTargetField(true);
        result.add(target );//95

        return result;
    }
	
	/**
	 * 
	 * @return the player's ID of the last active player or -1, if the last move cannot be assigned to a player
	 */
	public int getPlayerIDForLastMove(){
		return lastCardPlayedByID;
	}
	
	/**
	 * Check, if there is any move allowed for the starting ball to a point 
	 * in the given distance.
	 * 
	 * @param distance the distance, in which to check
	 * @param start starting point for a possible move
	 * @return true, if there is a possible move, false if there is no move allowed in this distance
	 */
	public boolean isMoveInDistanceAllowed(int distance, Point start){
		List<Point> targets = new ArrayList<Point>();
		if(distance == 4){
			targets.addAll( start.getPointsInDistance(4, false) );
		}
		else{
			if(start.isStartField()){//From startfield only distance 1 is allowed
				if(distance == 1 || distance == 13){
					targets.addAll( start.getPointsInDistance(1, true) );
				}//No targets added for any other distance
			}
			else{
				targets.addAll( start.getPointsInDistance(distance, true) );
			}
		}
		
		for(Point target: targets){
			int checkDistance = distance;
			if(start.isStartField()){
				checkDistance = 1;
			}
			if(isMoveAllowed(start, target, checkDistance)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @return list of points in the big ring, where balls are placed 
	 */
	public List<Point> getBallsInRing(){
		
		List<Point> result = new ArrayList<Point>();
		for(Point p: this.points){
			if(p.getBallID() != -1 && ! p.isStartField() && ! p.isTargetField() ){
				result.add(p);
			}
		}
		
		return result;
	}
	
	/**
	 * Check, whether a move is allowed according to the TAC rules.
	 * 
	 * @param ball, the starting point with the ball, which is moving
	 * @param target, the destination of the ball
	 * @return true, if the move is allowed, false otherwise
	 */
	public boolean isMoveAllowed(Point ball, Point target, int distance){
		if(ball.getBallID() <= -1){
			return false;
		}
		List<Point> way = new ArrayList<Point>();//Here all points from ball to target are stored
		ball.getDistance(target, way);
		
		if(distance >= 40){
			return false;
		}

		//No movement is allowed
		if(distance == 0){
			return true;
		}
		
		if(distance == 11){
			return false;
		}
		
		int id1 = points.indexOf(ball);
		int id2 = points.indexOf(target);
		int playerID = ball.getBallID()/4;
		if(ball.isTargetField() && target.isTargetField()){
			
			//Check whether the ball is at its final position (locked), do not allow any move from there
			List<Point> targetFields = playerIdToTargetFields.get(playerID);
			if(targetFields != null){
				int ballPointID = this.points.indexOf(ball);
				boolean balllocked = true;
				for(Point t: targetFields){
					int targetID = points.indexOf(t);
					if(targetID > ballPointID){
						if(t.getBallID() == -1){
							balllocked = false;
							break;
						}
					}
				}
				
				if(balllocked){
					return false;
				}
			}
			
			
			if(distance != 7 && id1 > id2){
				return false;
			}
		}
		//Balls from startfield can only use 1 and 13 to get out
		if(ball.isStartField()){
			if(distance == 1 || distance == 13){
				return true;
			}
			else{
				return false;
			}
		}
		
		//Make sure, that each player only uses his own target fields
		if(target.isTargetField()){
			List<Point> allowedFields = playerIdToTargetFields.get(playerID);
			if(! allowedFields.contains(target)){
				return false;
			}
		}
		
		boolean forward;
		Point next = way.get(1);
		forward = ball.getForwardNeighbours().contains(next);
		boolean backward = ball.getBackwardNeighbours().contains(next);
		
		if(distance != 4 && !forward){
			return false;
		}
		if(distance == 4 && !backward){
			return false;
		}
		//These moves are beating all balls in between, no check on the way necessary
		if(distance == 7 || distance ==1){
			return true;
		}
		
		//Is there a ball on the way?
		boolean onWay = false;
		for(int i=1; i< way.size()-1; i++){
			if(way.get(i).getBallID() != -1 ){
				onWay = true;
				break;
			}
		}
		
		if(onWay ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Beat all balls on the way given by the start and end point.
	 * Does not affect the start and end point. Only balls in between are beaten.
	 * 
	 * @param from starting point of the move
	 * @param to end point of the move
	 */
	public void beatAllOnTheWay(Point from, Point to){
		List<Point> way = new ArrayList<Point>();
		from.getDistance(to, way);
		
		for(int i=1; i<way.size()-1; i++ ){
			Point p = way.get(i);
			
			Point startField = getStartField(p.getBallID());
			if(startField != null){
				startField.setBallID(p.getBallID());
				p.clear();
			}
		}
	}
	
	/**
	 * This function must be called, if this instance was deserialized.
	 * It restores all required transient data such as field connections
	 * or the limit for stored cards. It can also savely be called from the
	 * constructor as part of that initialization.
	 */
	private void initAfterLoading(){
		connectNeighbours();
		playedCards.setMaximumCardsStored(maximumStepsStored-1);
		mapPlayerIdsToTargetFields();
		lastCardPlayedByID = -1;
	}
	
	/**
	 * Fill the attribute playerIdToTargetFields
	 */
	private void mapPlayerIdsToTargetFields(){
		playerIdToTargetFields = new SparseArray<List<Point>>();
		for(int i=0; i<4; i++){
			playerIdToTargetFields.put(i, new ArrayList<Point>());
		}
		
		for(int i=80; i<=83; i++){
			playerIdToTargetFields.get(0).add(points.get(i));
		}
		for(int i=84; i<=87; i++){
			playerIdToTargetFields.get(2).add(points.get(i));
		}
		for(int i=88; i<=91; i++){
			playerIdToTargetFields.get(3).add(points.get(i));
		}
		for(int i=92; i<=95; i++){
			playerIdToTargetFields.get(1).add(points.get(i));
		}
	}
	
	/**
	 * Add a new listener waiting for data changes.
	 * @param listener the new listener, which is registered with this function
	 */
	public void addListener(DataChangeListener listener){
		getListeners().add(listener);
	}
	
	/**
	 * Stop a listener from listening.
	 * @param listener the unnecessary listener
	 */
	public void removeListener(DataChangeListener listener){
		getListeners().remove(listener);
	}
	
	/**
	 * @return list of listeners, avoids return of null
	 */
	protected List<DataChangeListener> getListeners(){
		if(listeners == null){
			listeners = new ArrayList<DataChangeListener>();
		}
		return listeners;
	}
	
	/**
	 * Notify all listeners about a change event.
	 * 
	 * @param changeType the type of change
	 */
	protected void notifyChange(ChangeType changeType){
		
		DataChangeEvent event = new DataChangeEvent(changeType, this);
		
		for(DataChangeListener listener: getListeners()){
			listener.onDataChanged(event);
		}
	}
	
	/**
	 * @return list of colors encoded as integers for the player colors
	 */
	public List<Integer> getColors(){
		if(colors == null){
			initColors();
		}
		return colors;
	}
	
	/**
	 * Set default colors for the four players
	 */
	protected void initColors(){
		colors = new ArrayList<Integer>();
		colors.add(Color.rgb(255, 0, 0));
		colors.add(Color.rgb(0, 255, 0));
		colors.add(Color.rgb(0, 0, 255));
		colors.add(Color.rgb(0, 0, 0));
		
		notifyChange(ChangeType.colorInit);
	}
	
	/**
	 * Switch the colors of two players.
	 * @param player1 id of player 1
	 * @param player2 id of player 2
	 */
	public void switchColors(int player1, int player2){
		int tmp = colors.get(player1);
		colors.set(player1, colors.get(player2));
		colors.set(player2, tmp);
		
		notifyChange(ChangeType.colorSwitch);
	}

	/**
	 * Connect the points to each other
	 */
	private void connectNeighbours(){
		
		for(Point p: points){
			p.removeNeighbours();
		}
		
		//Connect outer circle
		for(int i=0; i<numCircleFields; i++){
			Point p=points.get(i);
			if(i>0){
				points.get(i-1).addNeighbour(p, true);
				p.addNeighbour(points.get(i-1), false);
			}
		}
		points.get(numCircleFields-1).addNeighbour(points.get(0), true);
		points.get(0).addNeighbour(points.get(numCircleFields-1), false);
		//Upper left
		for(int i=64; i <=67; ++i){
			points.get(i).addNeighbour(points.get(48), true);
		}
		//Upper right
		for(int i=68; i <=71; ++i){
			points.get(i).addNeighbour(points.get(0), true);
		}
		//Lower right
		for(int i=72; i <=75; ++i){
			points.get(i).addNeighbour(points.get(16), true);
		}
		//Lower left
		for(int i=76; i <=79; ++i){
			points.get(i).addNeighbour(points.get(32), true);
		}

		//Connect Target fields
		//Upper left
		//Targets can be reached with forward and backward moves
		points.get(48).addNeighbour(points.get(80), true);
		points.get(48).addNeighbour(points.get(80), false);
		for(int i=81; i<=83; i++){
			points.get(i).addNeighbour(points.get(i-1), true);
			points.get(i).addNeighbour(points.get(i-1), false);
			points.get(i-1).addNeighbour(points.get(i), true);
			points.get(i-1).addNeighbour(points.get(i), false);
		}
		//Lower right
		points.get(16).addNeighbour(points.get(84), true);
		points.get(16).addNeighbour(points.get(84), false);
		for(int i=85; i<=87; i++){
			//Same as calling addNeighbours with direction both for true and false
			points.get(i).addNeighbour(points.get(i-1));
			points.get(i-1).addNeighbour(points.get(i));
		}
		//Lower left
		points.get(32).addNeighbour(points.get(88), true);
		points.get(32).addNeighbour(points.get(88), false);
		for(int i=89; i<=91; i++){
			points.get(i).addNeighbour(points.get(i-1));
			points.get(i-1).addNeighbour(points.get(i));
		}
		//Upper right
		points.get(0).addNeighbour(points.get(92), true);
		points.get(0).addNeighbour(points.get(92), false);
		for(int i=93; i<=95; i++){
			points.get(i).addNeighbour(points.get(i-1));
			points.get(i-1).addNeighbour(points.get(i));
		}
	}

	/**
	 * @return current point states
	 */
	public ArrayList<Point> getPoints(){
		return points;
	}
	
	/**
	 * @return stack for the last played cards on this board
	 */
	public CardStack getPlayedCards(){
		return playedCards;
	}

	/**
	 * @param posX x-coordinate of click point
	 * @param posY y-coordinate of click point
	 * @return the point on the board closest to the given position, which is an action target and not an action starter
	 */
	public Point getClosestTarget(float posX, float posY){
		double abs = -1;
		Point result = null;
		for(Point p: points){
			if(! p.isActionField()){
				continue;
			}
			if(result == null){
				result = p;
				abs = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
			}
			else{
				double dist = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
				if(dist < abs){
					abs = dist;
					result = p;
				}
			}
		}

		return result;
	}
	
	/**
	 * @param posX x-coordinate of click point
	 * @param posY y-coordinate of click point
	 * @return the point on the board closest to the given position having a ball placed on it
	 */
	public Point getClosestBall(float posX, float posY){
		double abs = -1;
		Point result = null;
		for(Point p: points){
			if(p.getBallID() == -1){
				continue;
			}
			if(result == null){
				result = p;
				abs = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
			}
			else{
				double dist = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
				if(dist < abs){
					abs = dist;
					result = p;
				}
			}
		}

		return result;
	}

	/**
	 * @param posX x-coordinate of click point
	 * @param posY y-coordinate of click point
	 * @return the point on the board closest to the given position
	 */
	public Point getClosestPoint(float posX, float posY){
		double abs = -1;
		Point result = null;
		for(Point p: points){
			if(result == null){
				result = p;
				abs = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
			}
			else{
				double dist = (p.getPosX()-posX)*(p.getPosX()-posX)+(p.getPosY()-posY)*(p.getPosY()-posY);
				if(dist < abs){
					abs = dist;
					result = p;
				}
			}
		}

		return result;
	}

    /**
     *
     * @return the position in the history of stored moves, this is between 0 and maximumStepsStored-1
     */
    public int getHistoryPosition(){
        return historyPosition;
    }

    /**
     *
     * @return the number of steps stored in the history, which can be traversed by going back and forth
     */
    public int getCurrentHistorySize(){
        return history.size();
    }

    /**
     *
     * @return the number of steps, which can be stored in history
     */
    public int getMaximumHistorySize(){
        return maximumStepsStored;
    }

	/**
	 * Copies current state into history.
	 */
	protected void saveSnapshot(){
		//Remove all future events
		while(history.size() > historyPosition+1){
			history.remove(history.size()-1);
		}
		history.add(getSnapshot());//Store only inited fields with balls on
		//Remove those steps, which are too much
		while(history.size() > maximumStepsStored){
			history.remove(0);
		}
		historyPosition = history.size()-1;
		
		notifyChange(ChangeType.snapshotSaved);
	}

	/**
	 * Makes snapshot of current situation of the board.
     * Only returns the points, which have balls located on.
     *
	 * @return deep copy of current points
	 */
	protected ArrayList<Point> getSnapshot(){
		ArrayList<Point> res = new ArrayList<Point>();

		for( Point p: points){
            if(p.getBallID() != -1) {
                res.add(p.copy());
            }
		}
		return res;
	}

    /**
     * Convert a list given by getSnapshot into a full grown field with all points.
     *
     * @param snapPoints the points, which house balls, empty fields need to be refilled here
     * @return full set of points inited with the snapshot points
     */
    protected ArrayList<Point> getAllPointsFromSnapshot(ArrayList<Point> snapPoints){
        if(snapPoints.size() > 4*4){
            return snapPoints;
        }
        ArrayList<Point> result = getPointFieldInitedWithBalls(snapPoints);
        return result;
    }

	/**
	 * Searches for the startfield among all points
	 * @param ballID the id of the ball, fior which its start field is searched
	 * @return the point or null, if there is no start field for this ball
	 */
	public Point getStartField(int ballID){
		if(ballID == -1){
			return null;
		}

		for(Point point: points){
			if(point.getStartFieldID() == ballID){
				return point;
			}
		}

		return null;
	}
	

	/**
	 * Move the ball sitting on start point to the target point.
	 * Beats a possibly on the target sitting ball.
	 * If available the actioncard ID is used as the played card.
	 * @param start position, where the ball starts
	 * @param target position, where the ball is move to
	 */
	public void moveBall(Point start, Point target){
		if(start.getBallID() == -1){
			return;
		}
		//Dont beat your own ball. If requested, move it to the start field
		if(start == target){
			return;
		}
		//Do not move a ball onto another ball's start field
		if(target.isStartField() && target.getStartFieldID() != start.getBallID()){
			return;
		}
		lastCardPlayedByID = start.getBallID()/4;
		//Beat possible target ball
		if(target.getBallID() != -1){
			Point startField = getStartField(target.getBallID());
			if(startField != null){
				startField.setBallID(target.getBallID());
				target.clear();
			}
		}
		//Place moving ball to target
		target.setBallID(start.getBallID());
		start.clear();
		
		int distance = start.getDistance(target);
		String cardName = getActionCardName();
		if(cardName == null){
			cardName = Card.getCardNameForDistance(distance);
		}
		else{
			distance = Card.getDistanceForCardName(cardName);
		}
		
		Card card;
		if(cardName != null){
			card = new Card( cardName );
		}
		else{
			 card = new Card( Card.getCardNameForDistance(distance) );
		}
		card.setDistance(distance);
		card.setPlayedById(lastCardPlayedByID);
		playedCards.addCard(card);
		
		if(distance == 7){
			beatAllOnTheWay(start, target);
		}

		saveSnapshot();
		notifyChange(DataChangeEvent.ChangeType.moveBalls);
	}

	/**
	 * Switches the balls, which are placed on the positions a and b
	 * @param a first position
	 * @param b second position
	 */
	public void switchBalls(Point a, Point b){
		if(a.getBallID() == -1 || b.getBallID() == -1){
			return;
		}
		if(a.isStartField() || b.isStartField()){
			return;
		}
		lastCardPlayedByID = -1;//Can not decide, which player played the trickser
		int tmp = b.getBallID();
		b.setBallID(a.getBallID());
		a.setBallID(tmp);

		Card card = new Card("trickser");
		card.setInvolvedBallIDs(new int[]{a.getBallID(), b.getBallID()});

		playedCards.addCard(card);

		saveSnapshot();
		notifyChange(ChangeType.ballsSwitched);
	}

	/**
	 * Go one step back in history
	 */
	public void goBack(){
		if(historyPosition > 0){
			historyPosition--;
		}
		else{
			return;
		}
		points = history.get(historyPosition);
		//Copy points
		points = getAllPointsFromSnapshot(getSnapshot());
		connectNeighbours();
		mapPlayerIdsToTargetFields();
		
		playedCards.goBack();
		
		lastCardPlayedByID = -1;
		notifyChange(ChangeType.stepBack);
	}

	/**
	 * Go one step forward in history
	 */
	public void goForward(){
		if(historyPosition < history.size()-1){
			historyPosition++;
		}
		else{
			return;
		}
		points = history.get(historyPosition);
		//Copy points
		points = getAllPointsFromSnapshot(getSnapshot());
		connectNeighbours();
		mapPlayerIdsToTargetFields();
		
		playedCards.goForward();
		
		lastCardPlayedByID = -1;
		notifyChange(ChangeType.stepForward);
	}

	/**
	 * @return true, if board can go forward
	 */
	public boolean canGoForward(){
		return historyPosition < history.size()-1;
	}

	/**
	 * @return true, if board can go forward
	 */
	public boolean canGoBack(){
		return historyPosition > 0;
	}

	/**
	 * Save this board data to file
	 * @param activity needed activity to save data.
	 */
	public void save(Activity activity){
		try {
			ObjectOutputStream oOut = new ObjectOutputStream(activity.openFileOutput(filename, Context.MODE_PRIVATE));
			oOut.writeObject(this);
			oOut.close();

			notifyChange(ChangeType.dataSaved);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load last saved board from file.
	 * Save board into this instance
	 * @param activity needed activity to load data from.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws StreamCorruptedException 
	 * @throws ClassNotFoundException 
	 */
	public void load(Activity activity) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream oIn = new ObjectInputStream(activity.openFileInput(filename));
		BoardData loaded = (BoardData) oIn.readObject();

		points = loaded.points;
		history = loaded.history;
		historyPosition = loaded.historyPosition;
		playedCards = loaded.playedCards;
		initAfterLoading();
		
		
		notifyChange(ChangeType.dataLoaded);
	}
	
	/**
	 * Switch all fields back to non-action mode.
	 */
	public void clearActionMarkers(){
		for(Point p: this.points){
			p.setActionField(false);
			p.setActionStarter(false);
		}
		this.actionCardName = null;
		notifyChange(ChangeType.setActionPoint);
	}
	
	
	/**
	 * 
	 * @return all points marked as action points, but not the action starter
	 */
	public List<Point> getActionTargets(){
		List<Point> res = new ArrayList<Point>();
		
		for(Point p: this.points){
			if(p.isActionField() && !p.isActionStarter()){
				res.add(p);
			}
		}
		
		return res;
	}
	
	/**
	 * 
	 * @return the point, which started the current action, or null if there is no action going
	 */
	public Point getActionStarter(){
		for(Point p: this.points){
			if(p.isActionStarter()){
				return p;
			}
		}
		
		return null;
	}
	
	/**
	 * Set the card name for the card type of the card, which is used to make
	 * show the action markers.
	 * @param cardName identifies the card type played, e.g. "trickser", "krieger", "tac", "teufel", "engel", "narr", "1", "2", ..., "10", "12", "13"
	 */
	public void setActionCard(String cardName){
		this.actionCardName = cardName;
	}
	
	/**
	 * 
	 * @return the drawable ID of the card used for the current action or -1 if there is no action
	 */
	public String getActionCardName(){
		return this.actionCardName;
	}

	/**
	 * Allows static loading of a boarddata instance.
	 * Avoids initialising just to load afterwards on the instance.
	 * 
	 * @param activity
	 * @return last saved instance of boarddata
	 * @throws StreamCorruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static BoardData loadInstance(Activity activity) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream oIn = new ObjectInputStream(activity.openFileInput(filename));
		BoardData loaded = (BoardData) oIn.readObject();
		loaded.initAfterLoading();

		return loaded;
	}
	
	/**
	 * Move a ball from its start field to the first position in the large circle.
	 * @param ball the point, where the ball resides, which should be started
	 * 
	 */
	public void startBall(Point ball){
		if(! ball.isStartField()){
			return;
		}
		if(ball.getBallID() == -1){
			return;
		}
		
		List<Point> neighbours = ball.getNeighbours();
		if(neighbours.size() != 1){
			return;
		}
		
		moveBall(ball, neighbours.get(0));
		notifyChange(ChangeType.ballStarted);
	}
}
