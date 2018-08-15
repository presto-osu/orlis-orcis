package ca.farrelltonsolar.classic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Me on 5/14/2016.
 */
public class RecordEntry implements Parcelable {

    public RecordEntry(float v, float sc, float bc, int s, long t) {
        volt = v;
        supplyCurrent = sc;
        batteryCurrent = bc;
        state = s;
        time = t;
    }

    private RecordEntry(Parcel in) {
        volt = in.readFloat();
        supplyCurrent = in.readFloat();
        batteryCurrent = in.readFloat();
        state = in.readInt();
        time = in.readLong();
    }

    public float volt;
    public float supplyCurrent;
    public float batteryCurrent;
    public int state;
    public long time;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(volt);
        dest.writeFloat(supplyCurrent);
        dest.writeFloat(batteryCurrent);
        dest.writeInt(state);
        dest.writeLong(time);
    }

    public static final Parcelable.Creator<RecordEntry> CREATOR = new Parcelable.Creator<RecordEntry>() {
        public RecordEntry createFromParcel(Parcel in) {
            return new RecordEntry(in);
        }

        public RecordEntry[] newArray(int size) {
            return new RecordEntry[size];
        }
    };
}
