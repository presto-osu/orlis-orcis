package org.toulibre.cdl.api;

import java.util.Locale;

/**
 * This class contains all CDL Urls
 *
 * @author Christophe Beyls
 */
public class Urls {

    private static final String SCHEDULE_URL = "https://2015.capitoledulibre.org/schedule/xml";

    private static final String EVENT_URL_FORMAT = "https://2015.capitoledulibre.org/programme/presentation/%d/";

    private static final String PERSON_URL_FORMAT = "https://2015.capitoledulibre.org/speaker/profile/%d/";

    public static String getSchedule() {
        return SCHEDULE_URL;
    }

    public static String getEvent(long id) {
        return String.format(Locale.US, EVENT_URL_FORMAT, id);
    }

    public static String getPerson(long id) {
        return String.format(Locale.US, PERSON_URL_FORMAT, id);
    }
}
