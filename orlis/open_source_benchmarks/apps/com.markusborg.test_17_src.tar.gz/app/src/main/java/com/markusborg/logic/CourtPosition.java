package com.markusborg.logic;

/**
 * Represents one out of six positions on the court.
 *
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class CourtPosition {

    public static final int L_FRONT = 10;
    public static final int R_FRONT = 11;
    public static final int L_BACK = 12;
    public static final int R_BACK = 13;
    public static final int L_MID = 14;
    public static final int R_MID = 15;

    private int position;

    public CourtPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Check if the current position is in one of the corners.
     * @return true if it is a corner
     */
    public boolean isCornerPos() {
        return (position == L_FRONT || position == R_FRONT
                || position == L_BACK || position == R_BACK);
    }

    @Override
    public String toString() {
        String temp = null;
        switch (position) {
            case 10: temp = "L_FRONT";
                break;
            case 11: temp = "R_FRONT";
                break;
            case 12: temp = "L_BACK";
                break;
            case 13: temp = "R_BACK";
                break;
            case 14: temp = "L_MID";
                break;
            case 15: temp = "R_MID";
                break;
        }
        return temp;
    }

}
