package de.karbach.tac.core;

import java.io.Serializable;

/**
 * Stores a card and a possible color, which played it.
 * The color can be left blank as it is not always known (e.g. for trickser and tac).
 */
public class Move implements Serializable{

    /**
     * The played card
     */
    protected Card card;

    /**
     * The IDs of the balls, which were moved or made the move
     */
    protected int[] ballIDs;

    /**
     * The ID of this move
     */
    protected int id;

    /**
     *
     * @return -1 if not set, otherwise the number for the distance of the move
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Set the distance crossed by the move, if available
     * @param distance the number of fields spanned by the move
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Set this number, if an invalid number of fields is spanned by the move (e.g. more than 13)
     */
    protected int distance = -1;

    /**
     * Used for ID generation
     */
    protected static int idcounter=0;

    /**
     * Create empty move
     */
    public Move(){
        this(null, null);
    }

    /**
     * Create a move entry.
     * @param c the played card or null if unknown
     * @param ballIDs the players ball or -1 if unknown
     */
    public Move(Card c, int[] ballIDs){
        setCard(c);
        setBallIDs(ballIDs);
        idcounter++;
        setId(idcounter);
    }

    /**
     * Call this function on game restart.
     */
    public static void resetIDCounter(){
        idcounter=0;
    }

    /**
     * Set the ID for this move in the game
     * @param id the new ID for this move
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return the ID of the move in the game
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param c the played card
     */
    public void setCard(Card c){
        this.card=c;
    }

    /**
     *
     * @return the played card or null, if none was played
     */
    public Card getCard() {
        return card;
    }

    /**
     *
     * @return the IDs of the player balls or null if unknown
     */
    public int[] getBallIDs() {
        return ballIDs;
    }

    public void setBallIDs(int[] ballIDs) {
        this.ballIDs = ballIDs;
    }
}
