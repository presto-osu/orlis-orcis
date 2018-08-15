package org.pixmob.freemobile.netstat.content;

import android.support.annotation.NonNull;

import org.pixmob.freemobile.netstat.Event;
import org.pixmob.freemobile.netstat.MobileOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Store statistics.
 * @author Pixmob
 */
public class Statistics {
    public Event[] events = new Event[0];
    public long orange2GTime;
    public long orange3GTime;
    public long orangeTime;
    public long freeMobile3GTime;
    public long freeMobile4GTime;
    public long freeMobileTime;
    public int orange2GUsePercent;
    public int orange3GUsePercent;
    public int orangeUsePercent;
    public int freeMobile3GUsePercent;
    public int freeMobileFemtocellUsePercent;
    public int freeMobile4GUsePercent;
    public int freeMobileUsePercent;
    public MobileOperator mobileOperator;
    public String mobileOperatorCode;
    public long connectionTime;
    public long screenOnTime;
    public long wifiOnTime;
    public long femtocellTime;
    public int battery;

    @Override
    public String toString() {
        return "Statistics[events=" + events.length + "; orange=" + orangeUsePercent + "%; free="
                + freeMobileUsePercent + "%]";
    }

    public static void roundPercentagesUpTo100(double[] percents) {
        roundPercentagesUpToN(percents, 100);
    }

    public static void roundPercentagesUpToN(double[] percents, int sum) {

        class Pair <K, V extends Comparable<? super V>> implements Comparable<Pair<K, V>> {
            public final K left;
            public final V right;
            Pair(K left, V right) { this.left = left; this.right = right; }

            @Override
            public int compareTo(@NonNull Pair<K, V> another) {
                return right.compareTo(another.right);
            }
        }

        final double[] integerParts = new double[percents.length];
        final List<Pair<Integer, Double>> sortedDecimalParts = new ArrayList<>();
        int integerSum = 0;
        for (int i = 0; i < percents.length; i++) {
            integerParts[i] = (int) percents[i];
            sortedDecimalParts.add(new Pair<>(i, percents[i] - (int)percents[i]));
            integerSum += integerParts[i];
        }
        if (integerSum == sum) {
            System.arraycopy(integerParts, 0, percents, 0, integerParts.length);
            return;
        }
        if (integerSum < sum - percents.length) // Check if we can one day reach the sum
            return;

        Collections.sort(sortedDecimalParts, Collections.reverseOrder());

        for (int i = 0, end = sum - integerSum; i < end; ++i)
            ++integerParts[sortedDecimalParts.get(i).left];

        System.arraycopy(integerParts, 0, percents, 0, integerParts.length);
    }
}