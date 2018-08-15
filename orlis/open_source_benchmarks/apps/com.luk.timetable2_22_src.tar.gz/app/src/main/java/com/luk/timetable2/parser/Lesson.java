package com.luk.timetable2.parser;

/**
 * Created by luk on 10/10/15.
 */
public class Lesson {
    private String mName;
    private String mRoom;
    private String mHour;
    private String mGroup;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
         mName = name;
    }

    public String getRoom() {
        return mRoom;
    }

    public void setRoom(String room) {
        mRoom = room;
    }

    public String getHour() {
        return mHour;
    }

    public void setHour(String hour) {
        mHour = hour;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        mGroup = group;
    }
}
