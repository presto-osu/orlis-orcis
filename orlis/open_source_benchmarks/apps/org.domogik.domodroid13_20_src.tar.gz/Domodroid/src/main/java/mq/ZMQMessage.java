package mq;

/**
 * Created by mpunie on 12/05/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class ZMQMessage extends MQMessage {
    public ZMQMessage() {
    }

    public int describeContents() {
        return 0;
    }

    // Write Message data to the passed in Parcel
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(super.getId());
        dest.writeString(super.getMessage());
    }

    // Regenerate object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<MQMessage> CREATOR = new Parcelable.Creator<MQMessage>() {
        public ZMQMessage createFromParcel(Parcel in) {
            return new ZMQMessage(in);
        }

        public MQMessage[] newArray(int size) {
            return new MQMessage[size];
        }
    };

    // Constructor takes a Parcel and returns message populated with it's values
    private ZMQMessage(Parcel in) {
        super.setId(in.readString());
        super.setMessage(in.readString());
    }
}

