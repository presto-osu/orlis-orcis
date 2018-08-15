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
import java.util.HashSet;
import java.util.List;

/**
 * A position on the game board.
 * Saves position and a possible ball.
 * Is Serializable in order to save all ball positions.
 * 
 * @author Carsten Karbach
 *
 */
public class Point implements Serializable{

	/**
	 * Stores a list of directly adjacent points in forward direction
	 */
	transient private ArrayList<Point> forwardNeighbours;

	/**
	 * Stores a list of directly adjacent points in backward direction
	 */
	transient private ArrayList<Point> backwardNeighbours;

	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The ID of the ball, which is placed on this point.
	 * Or -1, if there is no ball on this point.
	 */
	private int ball=-1;

	/**
	 * Saves, if this field is a start field
	 */
	private int startField;

	//If true, this field is a target field(haeusschen)
	private boolean targetField=false;

	//If true, this point is marked as an action field, it is correspondingly rendered
	private boolean actionField = false;

	//If true, this point is the origin of the action
	private boolean actionStarter = false;

	/**
	 * 
	 * @return true, if this field is a target field(haeusschen)
	 */
	public boolean isTargetField() {
		return targetField;
	}

	/**
	 * Set this field to a target field.
	 * 
	 * @param targetField true, if this field is a target field(haeusschen)
	 */
	public void setTargetField(boolean targetField) {
		this.targetField = targetField;
	}

	/**
	 * 
	 * @return true, if this point is the origin of the action
	 */
	public boolean isActionStarter() {
		return actionStarter;
	}

	/**
	 * Mark this point as action starter
	 * @param actionStarter true, if this point is the origin of the action
	 */
	public void setActionStarter(boolean actionStarter) {
		this.actionStarter = actionStarter;
	}

	/**
	 * 
	 * @return true, if this field is an action field
	 */
	public boolean isActionField() {
		return actionField;
	}

	/**
	 * Mark / Unmark this field as action field.
	 * @param actionField true, if this field is to be marked
	 */
	public void setActionField(boolean actionField) {
		this.actionField = actionField;
	}

	/**
	 * Position of this point.
	 * Coordinates between 0 and 1
	 * posx=0 => left border of board
	 * posx=1 => right border of board
	 * posy=0 => upper border of board
	 * posy=1 => lower border of board
	 */
	private float posx,posy;

	/**
	 * Allows to define this field as start field
	 * @param posx relative x-position of this point
	 * @param posy relative y-position of this point
	 * @param startField -1, if this is not a start field, otherwise ID of the ball, which starts here
	 */
	public Point(float posx, float posy, int startField ){
		this.posx=posx;
		this.posy=posy;
		this.startField = startField;
		ball = startField;

		forwardNeighbours = new ArrayList<Point>();
		backwardNeighbours = new ArrayList<Point>();
	}

	/**
	 * Standard constructor for non-start field
	 * @param posx relative x-position of this point
	 * @param posy relative y-position of this point
	 */
	public Point(float posx, float posy){
		this(posx, posy, -1);
	}
	
	/**
	 * Traverse the neighbours in forward direction.
	 * Search for the next ball within the big ring.
	 * Return null, if this point is a starting or target field.
	 * Return null, if no ball is found at all.
	 * 
	 * @return the next ball point in the big ring
	 */
	public Point getNextBallOnRing(){
		if(isTargetField() || isStartField()){
			return null;
		}
		
		List<Point> visited = new ArrayList<Point>();
		
		return getNextBallOnRing(visited);
	}
	
	/**
	 * Recursive function for searching the next ball on the ring
	 * 
	 * @param visited list of visited points, which are not visited again
	 * @return the next point with a ball on it in forward direction
	 */
	protected Point getNextBallOnRing(List<Point> visited){
		visited.add(this);
		
		List<Point> neighbours = getForwardNeighbours();
		for(Point n: neighbours){
			if(n.isTargetField() || n.isStartField() ){
				continue;
			}
			if(n.getBallID() != -1){
				return n;
			}
			
			if(! visited.contains(n)){
				Point found = n.getNextBallOnRing(visited);
				if(found != null){
					return found;
				}
			}
		}
		
		return null;
	}

	/**
	 * Add an adjacent point in both directions
	 * @param neigh the adjacent neighbour
	 */
	public void addNeighbour(Point neigh){
		addNeighbour(neigh, true);
		addNeighbour(neigh, false);
	}

	/**
	 * Adds a neighbor for a special direction.
	 * Always adds the neighbor to the default
	 * @param neigh
	 * @param forward
	 */
	public void addNeighbour(Point neigh, boolean forward){
		if(! forward){
			if(backwardNeighbours == null){
				backwardNeighbours = new ArrayList<Point>();
			}
			if(! backwardNeighbours.contains(neigh)){
				backwardNeighbours.add(neigh);
			}
		}
		else{
			if(forwardNeighbours == null){
				forwardNeighbours = new ArrayList<Point>();
			}
			if(! forwardNeighbours.contains(neigh)){
				forwardNeighbours.add(neigh);
			}
		}
	}


	/**
	 * @param point the searched point
	 * @return Number of steps to reach the passed point
	 */
	public int getDistance(Point point){
		return getDistance(point, null);
	}

	/**
	 * @param point the searched point
	 * @param way stores the way to go to that point
	 * @return Number of steps to reach the passed point
	 */
	public int getDistance(Point point, List<Point> way){
		if(way==null){
			way = new ArrayList<Point>();
		}
		//Store the way on how we have gotten to a given point
		List<List<Point>> ways = new ArrayList<List<Point>>();

		ArrayList<Point> queue = new ArrayList<Point>();
		HashSet<Point> visited = new HashSet<Point>();
		queue.add(this);
		List<Point> currentWay = new ArrayList<Point>();
		currentWay.add(this);
		ways.add(currentWay);
		int distance = 0;

		while(distance < 40){
			int toRemove = queue.size();
			while(toRemove > 0){
				Point current = queue.get(0);
				List<Point> myWay = ways.get(0);
				if(!visited.contains(current)){
					if(current == point){
						way.clear();
						way.addAll(myWay);
						return distance;
					}
					visited.add(current);
					if(current.getNeighbours() != null){
						for(Point cn: current.getNeighbours()){
							if(! visited.contains(cn)){
								queue.add(cn);
								List<Point> extendedWay = new ArrayList<Point>();
								extendedWay.addAll(myWay);
								extendedWay.add(cn);
								ways.add(extendedWay);
							}
						}
					}
				}
				queue.remove(0);
				ways.remove(0);
				toRemove--;
			}
			distance++;
		}

		return distance;
	}

	/**
	 * 
	 * @param dist the distance searched here.
	 * 
	 * @param forward, if true, this function only searches in forward direction, otherwise only in backward direction
	 * 
	 * @return a list of the points, which are in the given distance away from this point
	 */
	public List<Point> getPointsInDistance(int dist, boolean forward){

		List<Point> result = new ArrayList<Point>(); 

		List<Point> queue = new ArrayList<Point>();
		List<Integer> distanceQueue = new ArrayList<Integer>();
		distanceQueue.add(0);
		queue.add(this);

		List<Point> visited = new ArrayList<Point>();

		while(distanceQueue.size() > 0 && distanceQueue.get(0) <= dist){
			Point p = queue.get(0);
			queue.remove(0);
			int cdist = distanceQueue.get(0);
			distanceQueue.remove(0);
			if(visited.contains(p) && !(dist==7 && p.isTargetField()) ){
				continue;
			}

			if(cdist == dist && !result.contains(p)){
				result.add(p);
			}


			visited.add(p);
			List<Point> neighbours;
			if(forward){
				neighbours = p.getForwardNeighbours();
			}
			else{
				neighbours = p.getBackwardNeighbours();
			}
			for(Point neighbour: neighbours){
				//Do not beat balls in the target fields
				if(neighbour.isTargetField() && (neighbour.getBallID() != -1 && neighbour != this) ){
					continue;
				}
				queue.add(neighbour);
				distanceQueue.add(cdist+1);
			}
		}

		return result;
	}

	/**
	 * Makes deep copy of this point.
	 * @return copy of this point
	 */
	public Point copy(){
		Point result = new Point(posx,posy,startField);
		result.setBallID(ball);
		result.setTargetField(isTargetField());
		if(forwardNeighbours != null){
			for(Point neighbour: forwardNeighbours){
				result.forwardNeighbours.add(neighbour);
			}
		}
		if(backwardNeighbours != null){
			for(Point neighbour: backwardNeighbours){
				result.backwardNeighbours.add(neighbour);
			}
		}
		return result;
	}

	/**
	 * @return x relative position of this point
	 */
	public float getPosX(){
		return posx;
	}

	/**
	 * @return y relative position of this point
	 */
	public float getPosY(){
		return posy;
	}

	/**
	 * @return ID of the ball on this point or -1, if there is no ball
	 */
	public int getBallID(){
		return ball;
	}

	/**
	 * Places a new ball on this point
	 * @param id id of the new placed ball or -1, if point is cleared
	 */
	public void setBallID(int id){
		ball = id;
	}

	/**
	 * @return -1, if this is not a start field, otherwise ID of the ball, which starts here
	 */
	public int getStartFieldID(){
		return startField;
	}

	/**
	 * @return true, if this is a start field, false otherwise
	 */
	public boolean isStartField(){
		return startField != -1;
	}
	/**
	 * Clear the ball from this point
	 */
	public void clear(){
		setBallID(-1);
	}

	/**
	 * @return list of all adjacent points
	 */
	public List<Point> getNeighbours(){
		List<Point> result = new ArrayList<Point>();
		result.addAll(forwardNeighbours);
		for(Point p: backwardNeighbours){
			if(! result.contains(p)){
				result.add(p);
			}
		}
		return result;
	}

	/**
	 * 
	 * @return list of neighbours in backward direction
	 */
	public List<Point> getBackwardNeighbours(){
		return backwardNeighbours;
	}

	/**
	 * 
	 * @return list of neighbours in forward direction
	 */
	public List<Point> getForwardNeighbours(){
		return forwardNeighbours;
	}

	/**
	 * Remove all existing neighbours
	 */
	public void removeNeighbours(){
		forwardNeighbours = new ArrayList<Point>();
		backwardNeighbours = new ArrayList<Point>();
	}

}
