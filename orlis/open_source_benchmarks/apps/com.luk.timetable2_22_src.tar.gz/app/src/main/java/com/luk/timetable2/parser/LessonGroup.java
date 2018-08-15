package com.luk.timetable2.parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by luk on 4/23/16.
 */
public class LessonGroup extends HashMap<Integer, ArrayList<Lesson>> {

    public ArrayList<Lesson> getLessons(Integer day) {
        if (get(day) == null) {
            return new ArrayList<>();
        }

        return get(day);
    }

}
