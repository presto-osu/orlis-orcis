package com.guvery.notifyme;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class NotificationHistory {

    private static NotificationHistory instance = null;
    private static File notificationFolder;
    private static ArrayList<Notif> history;

    private NotificationHistory(File f, String folderName) {
        notificationFolder = new File(f, folderName);
        notificationFolder.mkdir();
        history = getHistoryFromFile();
    }

    public static NotificationHistory getInstance(File f, String folderName) {
        if (instance == null) {
            instance = new NotificationHistory(f, folderName);
        }
        return instance;
    }

    private static ArrayList<Notif> getHistoryFromFile() {
        BufferedReader br = null;
        File[] files = notificationFolder.listFiles();
        String filePath = notificationFolder.getAbsoluteFile() + "/", line;

        ArrayList<Notif> notifications = new ArrayList<Notif>();

        for (int i = 0; i < files.length; i++) {
            Notif n = new Notif();

            try {
                br = new BufferedReader(new FileReader(filePath + files[i].getName()));
                line = br.readLine();
                br.close();
            } catch (Exception e) {
                break;
            }
            n.setId(Integer.parseInt(
                    files[i].getName()));
            n.setTitle(line.substring(
                    line.indexOf("<T>") + 3, line.indexOf("</T>")));

            String message = line.substring(line.indexOf("<M>") + 3,
                    line.indexOf("</M>"));
            n.setBody(message.replace("LINE_BREAK", "\n"));

            n.setOngoing(Boolean.parseBoolean(
                    line.substring(line.indexOf("<O>") + 3, line.indexOf("</O>"))));
            n.setBigTextStyle(Boolean.parseBoolean(
                    line.substring(line.indexOf("<B>") + 3, line.indexOf("</B>"))));
            n.setPriority(Integer.parseInt(
                    line.substring(line.indexOf("<P>") + 3, line.indexOf("</P>"))));
            try {
                int[] time = new int[2];
                time[0] = Integer.parseInt(
                        line.substring(line.indexOf("<H>") + 3, line.indexOf("</H>")));
                time[1] = Integer.parseInt(
                        line.substring(line.indexOf("<m>") + 3, line.indexOf("</m>")));
                n.setTime(time);
            } catch (StringIndexOutOfBoundsException e) {
                // catch the exception when upgrading from older versions that
                // did not store a time variable
                int[] time = {-1, -1};
                n.setTime(time);
            }
            try {
                ImageAdapter ia = new ImageAdapter(null);
                int imageId = Integer.parseInt(
                        line.substring(line.indexOf("<I>") + 3, line.indexOf("</I>")));
                if (ia.isAnIcon(imageId)) {
                    n.setImageId(imageId);
                } else {
                    n.setImageId(n.getImageId());
                }
            } catch (StringIndexOutOfBoundsException e) {
                // catch the exception when upgrading from older versions that
                // did not store an image id variable
                n.setImageId(n.getImageId());
            }
            notifications.add(n);
        }

        return notifications;
    }

    public boolean add(Notif n) {
        // Check for duplicates first
        for (int i = 0; i < history.size(); i++)
            if (n.equals(history.get(i)))
                return true;

        // write to file
        if (!writeToFile(n))
            return false;

        // add to history array
        history.add(n);

        return true;
    }

    public boolean remove(Notif n) {
        File[] files = notificationFolder.listFiles();

        // Search through files for our bundle
        for (File file : files) {
            if (file.getName().equals(Integer.toString(n.getId()))) {
                file.delete();
                history.remove(n);
                return true;
            }
        }

        return false;
    }

    public void clear() {
        File[] files = notificationFolder.listFiles();
        for (int i = 0; i < files.length; i++)
            files[i].delete();
        history.clear();
    }

    public ArrayList<Notif> getHistory() {
        //history = getHistoryFromFile();
        //Collections.reverse(history);
        return history;
    }

    public Notif findNotifById(int mId) {
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getId() == mId) {
                return history.get(i);
            }
        }
        return null;
    }

    /**
     * Private methods for reading/writing to files
     */

    private boolean writeToFile(Notif n) {
        String file = notificationFolder.getAbsoluteFile() + "/" + n.getId();
        String tmp = n.getBody().replace("\n", "LINE_BREAK");

        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            int[] time = n.getTime();
            if (notificationFolder.canWrite()) {
                bw.write("<T>" + n.getTitle() + "</T>" +
                        "<M>" + tmp + "</M>" +
                        "<O>" + n.isOngoing() + "</O>" +
                        "<B>" + n.isBigTextStyle() + "</B>" +
                        "<P>" + n.getPriority() + "</P>" +
                        "<I>" + n.getImageId() + "</I>" +
                        "<H>" + time[0] + "</H>" +
                        "<m>" + time[1] + "</m>");
                bw.close();
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}