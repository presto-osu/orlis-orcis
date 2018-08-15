package com.thefonz.ed_tool.rss;

/**
 * Created by thefonz on 26/03/15.
 */
public class RssItem {
    String title;
    String date;
    String description;
    String link;
    String imageUrl;

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String removeLastChar(String str) {
        return str.substring(0,str.length()-9);
    }

    public String getContent() {
        return ">> INCOMING TRANSMISSION... \n\n"
                + "Signal Date : " + removeLastChar(date) + "\n"
                + "Signal Source : GALNET Central\n\n"
                + title + "\n\n"
                + "                ( click to view full story )";
    }

    public String getDetails() {
        return getContent() + "\n\n"
                + description + "\n";
    }
}
