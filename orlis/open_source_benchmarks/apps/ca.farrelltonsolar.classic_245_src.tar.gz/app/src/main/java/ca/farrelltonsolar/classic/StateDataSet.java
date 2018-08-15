package ca.farrelltonsolar.classic;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * Created by Me on 5/14/2016.
 */
public class StateDataSet extends LineDataSet {

    public StateDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }
}
