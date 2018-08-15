package com.guvery.notifyme;

/**
 * Created by Aaron on 10/27/2014.
 */
public class Notif {
    private int id;
    private String title;
    private String body;
    private boolean ongoing;
    private boolean bigTextStyle;
    private int priority;
    private int imageId;
    private int[] time;

    public Notif() {
        id = -1;
        title = "Empty notif";
        body = "Empty notif";
        ongoing = false;
        bigTextStyle = false;
        priority = 0;
        imageId = -1;
        time = new int[]{0, 0};
    }

    public Notif(String title, String body, boolean ongoing,
                 boolean bigTextStyle, int priority, int imageId) {
        this.title = title;
        this.body = body;
        this.ongoing = ongoing;
        this.bigTextStyle = bigTextStyle;
        this.priority = priority;
        this.imageId = imageId;
    }

    public Notif(String title, String body, boolean ongoing,
                 boolean bigTextStyle, int priority, int imageId, int[] time) {
        this.title = title;
        this.body = body;
        this.ongoing = ongoing;
        this.bigTextStyle = bigTextStyle;
        this.priority = priority;
        this.imageId = imageId;
        this.time = time;
    }

    public Notif(int id, String title, String body, boolean ongoing, boolean bigTextStyle,
                 int priority, int imageId, int[] time) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.ongoing = ongoing;
        this.bigTextStyle = bigTextStyle;
        this.priority = priority;
        this.imageId = imageId;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public Notif setId(int id) {
        this.id = id;
        return this;
    }

    public int getImageId() {
        return imageId;
    }

    public Notif setImageId(int imageId) {
        this.imageId = imageId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Notif setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Notif setBody(String body) {
        this.body = body;
        return this;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public Notif setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
        return this;
    }

    public boolean isBigTextStyle() {
        return bigTextStyle;
    }

    public Notif setBigTextStyle(boolean bigTextStyleBox) {
        this.bigTextStyle = bigTextStyleBox;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public Notif setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int[] getTime() {
        return time;
    }

    public Notif setTime(int[] time) {
        this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Notif other = (Notif) obj;
        if (bigTextStyle != other.bigTextStyle)
            return false;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (ongoing != other.ongoing)
            return false;
        if (imageId != other.imageId)
            return false;
        if (priority != other.priority)
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }

    public String toString() {
        return id + " " + title;
    }
}
