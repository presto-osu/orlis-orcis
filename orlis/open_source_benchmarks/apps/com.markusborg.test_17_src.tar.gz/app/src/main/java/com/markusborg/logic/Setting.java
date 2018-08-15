package com.markusborg.logic;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class Setting {

    private String date;

    private boolean squash;
    private int sets;
    private int reps;
    private int interval; // in ms
    private int breakTime;
    private boolean sixPoints;
    private boolean audio;

    public Setting(boolean squash, int sets, int reps, int interval, int breakTime, boolean sixPoints, boolean audio) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.format(new Date());

        this.squash = squash;
        this.sets = sets;
        this.reps = reps;
        this.interval = interval;
        this.breakTime = breakTime;
        this.sixPoints = sixPoints;
        this.audio = audio;
    }

    /**
     * Create a Setting object from the string stored on file.
     * @param string The string from file.
     */
    public Setting(String string) {
        // Multiple checks needed to take care of old app versions
        boolean validString = setSettingFromString(string);
        // if the string is invalid, set dummy data
        if (!validString) {
            sets = -1;
            reps = -1;
            interval = -1;
            breakTime = -1;
        }
    }

    @Override
    public String toString() {
        String type = "(SQ)";
        if (!squash) {
            type = "(BA)";
        }
        StringBuffer sb = new StringBuffer(getDate() + " " + type + ": " + getSets() + "; " +
                getReps() + "; " + getInterval() + "; " + getBreakTime());
        return sb.toString();
    }

    /**
     * Return a string without the "(SQ)/(BA)" type.
     * @return The string.
     */
    public String getRestrictedString() {
        StringBuffer sb = new StringBuffer(getDate() + " - " + getSets() + "; " + getReps() +
                "; " + ((float) getInterval() / 1000) + "; " + getBreakTime());
        return sb.toString();
    }

    public String getDate() { return date; }

    public boolean isSquash() { return squash; }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public boolean isSixPoints() {
        return sixPoints;
    }

    public void setSixPoints(boolean sixPoints) {
        this.sixPoints = sixPoints;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    /**
     * Validate the string, return false at earliest possible failed check. For valid string,
     * set all important settings.
     * @param input The string to validate and use.
     * @return True for a valid string, otherwise false.
     */
    private boolean setSettingFromString(String input) {
        // Example of valid string
        // 2015-11-24 (SQ): 1; 1; 1; 1

        // shortest possible string is 27 characters
        if (input.length() < 27) {
            return false;
        }
        // start with a date
        String date = input.substring(0, 10);
        if (!Character.isDigit(date.charAt(0)) ||
                !Character.isDigit(date.charAt(1)) ||
                !Character.isDigit(date.charAt(2)) ||
                !Character.isDigit(date.charAt(3)) ||
                date.charAt(4) != '-' ||
                !Character.isDigit(date.charAt(5)) ||
                !Character.isDigit(date.charAt(6)) ||
                date.charAt(7) != '-' ||
                !Character.isDigit(date.charAt(8)) ||
                !Character.isDigit(date.charAt(9))) {
            return false;
        }
        this.date = date;
        // sport within the parantheses
        if ((!(input.charAt(12) == 'B') && !(input.charAt(12) == 'S')) ||
                (!(input.charAt(13) == 'A') && !(input.charAt(13) == 'Q'))) {
            return false;
        }
        // if badminton, set squash to false
        this.squash = true;
        if (input.charAt(12) == 'B' && input.charAt(13) == 'A') {
            this.squash = false;
        }

        // four components separated by ; in the end
        String substring = input.substring(16, input.length());
        String[] ints = substring.split(";");
        if (ints.length != 4) {
            return false;
        }
        // the components should be integers
        try {
            this.sets = Integer.parseInt(ints[0].trim());
            this.reps = Integer.parseInt(ints[1].trim());
            this.interval = Integer.parseInt(ints[2].trim());
            this.breakTime = Integer.parseInt(ints[3].trim());
        } catch (NumberFormatException e) {
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

        // everything ok, the string is valid
        return true;
    }

}
