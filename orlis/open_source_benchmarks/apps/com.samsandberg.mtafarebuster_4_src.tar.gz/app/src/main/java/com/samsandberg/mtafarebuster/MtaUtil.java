package com.samsandberg.mtafarebuster;

import java.util.ArrayList;


public class MtaUtil {

    // $2.75 to ride the sub
    public static final int COST_PER_TRIP = 275;

    // $5.50 min for 5% bonus
    public static final int MIN_AMOUNT_TO_GET_BONUS = 550;
    public static final float BONUS_PCT = 0.11f;

    // 5 cent steps
    public static final int TO_ADD_STEP = 5;

    public MtaUtil() {}

    public class MtaUtilResult {
        public int toAdd;
        public int bonus = 0;
        public int amountOnCardAfter;
        public int numTrips;
        public boolean receivedBonus;
        public boolean hasLeftover;

        public MtaUtilResult(int amountOnCard, int toAdd) {
            this.toAdd = toAdd;
            receivedBonus = toAdd >= MIN_AMOUNT_TO_GET_BONUS;
            if (receivedBonus) {
                bonus = Math.round(BONUS_PCT * toAdd);
            }
            amountOnCardAfter = amountOnCard + toAdd + bonus;
            numTrips = amountOnCardAfter / COST_PER_TRIP;
            hasLeftover = amountOnCardAfter - (numTrips * COST_PER_TRIP) > 0;
        }

        public String toAddPretty() {
            return "$" + String.format("%.2f", (float) toAdd / 100);
        }

        public String bonusPretty() {
            return "$" + String.format("%.2f", (float) bonus / 100);
        }

        public String amountOnCardAfterPretty() {
            return "$" + String.format("%.2f", (float) amountOnCardAfter / 100);
        }

        @Override
        public String toString() {
            return "To Add: " + toAddPretty() + "\nBonus: " + bonusPretty() + "\nAmount On Card: " + amountOnCardAfterPretty() + "\nNum Trips: " + numTrips;
        }
    }

    public static ArrayList<MtaUtilResult> amountToAdd(float amountOnCard) {
        MtaUtil mtaUtil = new MtaUtil();
        int originalAmountOnCard = (int)(100 * amountOnCard);
        ArrayList<MtaUtilResult> results = new ArrayList<MtaUtilResult>();
        for (int toAdd = 0; results.size() < 5; toAdd += TO_ADD_STEP) {
            MtaUtilResult result = mtaUtil.new MtaUtilResult(originalAmountOnCard, toAdd);
            if (result.amountOnCardAfter > 0 && ! result.hasLeftover) {
                results.add(result);
            }
        }
        return results;
    }
}