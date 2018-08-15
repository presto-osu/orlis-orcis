package com.example.tobiastrumm.freifunkautoconnect;

import android.os.Parcel;
import android.os.Parcelable;

public class Node implements Parcelable {
    String name;
    double lat;
    double lon;
    boolean online;
    double distance;

    public Node(String name, double lat, double lon, boolean online){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.online = online;
        distance = Double.MAX_VALUE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        double[] doubleArray = {lat, lon, distance};
        parcel.writeDoubleArray(doubleArray);
        boolean[] booleanArray = {online};
        parcel.writeBooleanArray(booleanArray);
    }

    public final static Parcelable.Creator<Node>CREATOR =
            new Parcelable.Creator<Node>(){

        @Override
        public Node createFromParcel(Parcel parcel) {
            double[] doubleArray = new double[3];
            parcel.readDoubleArray(doubleArray);
            boolean[] booleanArray = new boolean[1];
            parcel.readBooleanArray(booleanArray);
            Node n = new Node(parcel.readString(), doubleArray[0], doubleArray[1], booleanArray[0]);
            n.distance = doubleArray[2];
            return n;
        }

        @Override
        public Node[] newArray(int i) {
            return new Node[i];
        }
    };
}
