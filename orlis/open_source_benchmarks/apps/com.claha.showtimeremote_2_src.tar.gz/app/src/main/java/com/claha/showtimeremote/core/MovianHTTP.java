package com.claha.showtimeremote.core;

import android.content.Context;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Class that handles the communication with Movian.
 * This class is used to send actions and search queries to Movian.
 *
 * @author Claes Hallstrom
 * @version 1.1.0
 */
public class MovianHTTP {

    /**
     * Base URL used for communication.
     */
    private static final String URL_BASE = "http://%s:%s/showtime/";

    /**
     * URL used for sending actions.
     */
    private static final String URL_ACTION = URL_BASE + "input/action/%s";

    /**
     * URL used for sending search queries.
     */
    private static final String URL_SEARCH = URL_BASE + "open?url=search:%s";

    /**
     * ShowtimeSettings instance to access current IP address and port.
     */
    private final MovianRemoteSettings settings;

    /**
     * Create a Movian HTTP object.
     *
     * @param context The context of the current activity.
     */
    public MovianHTTP(Context context) {
        settings = new MovianRemoteSettings(context);
    }

    /**
     * Send an action to Movian.
     *
     * @param action The action to be sent.
     */
    public void sendAction(String action) {
        if (action != null && !action.equals("")) {
            String url = String.format(URL_ACTION, getIPAddress(), getPort(), action);
            sendURL(url);
        }
    }

    /**
     * Send a search query to Movian.
     *
     * @param query The query to be sent.
     */
    public void search(String query) {
        query = query.replace(" ", "+");
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format(URL_SEARCH, getIPAddress(), getPort(), query);
        sendURL(url);
    }

    /**
     * Send an url to Movian.
     *
     * @param url The url to be sent.
     */
    private void sendURL(final String url) {
        Thread thread = new Thread((new Runnable() {
            @Override
            public void run() {
                URLConnection connection;
                try {
                    connection = new URL(url).openConnection();
                    connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        thread.start();
    }

    /**
     * Get the IP address of Movian.
     *
     * @return The IP address.
     */
    private String getIPAddress() {
        return settings.getIPAddress();
    }

    /**
     * Get the port of Movian.
     *
     * @return The port.
     */
    private String getPort() {
        return settings.PORT;
    }
}
