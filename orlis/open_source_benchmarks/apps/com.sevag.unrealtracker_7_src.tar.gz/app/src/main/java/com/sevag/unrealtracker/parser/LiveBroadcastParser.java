package com.sevag.unrealtracker.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sevag on 3/25/15.
 */
public class LiveBroadcastParser {

    public static final String liveStreamUrl = "http://www.twitch.tv/unrealtournament/";

    private static ArrayList<String> unrealTournamentBroadcastSchedule;

    public static ArrayList<String> getUnrealTournamentBroadcastSchedule() {
        return unrealTournamentBroadcastSchedule;
    }

    public LiveBroadcastParser() {
    }

    public void fetchUTBroadcastSchedule() {
        ArrayList<String> returnList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(BlogParser.unrealTournamentBlogUrl).get();
            Elements broadcast = doc.select("div#counter");
            long seconds = -1;

            Elements all = doc.getElementsByTag("div");
            for (Element subelement : all) {
                String s = subelement.data();
                String result = getStringBetweenTwoStrings(s, "shortly.setSeconds(\"", "\");");
                if (result != null) {
                    seconds = Long.parseLong(result);
                    break;
                }
            }

            String date = "";

            for (Element element : broadcast) {
                Elements dates = element.select("div#date");
                for (Element subelement : dates) {
                    date = subelement.text();
                }
            }

            if ((date != null) && (!date.equals("")) && (seconds != -1)) {
                returnList.add(getTimeString(seconds) + "\n\n" +  "Date: " + date);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
        unrealTournamentBroadcastSchedule = returnList;
    }

    private String getStringBetweenTwoStrings(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    private String getTimeString(long secondsParam) {
        int day = (int) TimeUnit.SECONDS.toDays(secondsParam);
        long hours = TimeUnit.SECONDS.toHours(secondsParam) -
                TimeUnit.DAYS.toHours(day);
        long minutes = TimeUnit.SECONDS.toMinutes(secondsParam) -
                TimeUnit.DAYS.toMinutes(day) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.SECONDS.toSeconds(secondsParam) -
                TimeUnit.DAYS.toSeconds(day) -
                TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return "Countdown: " + day + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
    }
}
