package com.gk.datacontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

public class DataPointParcel  implements Parcelable {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    private ArrayList<DataPoint> dataPoints;

    public DataPointParcel() {
        if ( DEBUG_FLAG ) Log.d( APP_NAME, "DataPointParcel :: DataPointParcel");
        dataPoints = new ArrayList<DataPoint>();
    }

    public ArrayList<DataPoint> restoreData()
    {
        Log.d( APP_NAME, "DataPointParcel :: restoreData");
        return dataPoints;
    }

    public void addPoint( DataPoint dp )
    {
        Log.d( APP_NAME, "DataPointParcel :: addPoint");
        dataPoints.add( dp );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d( APP_NAME, "DataPointParcel :: writeToParcel");
        for ( DataPoint dp : dataPoints)
        {
            dest.writeDouble( dp.getX() );
            dest.writeDouble( dp.getY() );
        }

    }


}
