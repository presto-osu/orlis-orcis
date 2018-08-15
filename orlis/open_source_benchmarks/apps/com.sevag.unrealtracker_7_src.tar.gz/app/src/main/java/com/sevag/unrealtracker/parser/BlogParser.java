package com.sevag.unrealtracker.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by sevag on 3/25/15.
 */
public class BlogParser {

    public static final String unrealEngine4BlogUrl = "https://www.unrealengine.com/blog";
    public static final String unrealTournamentBlogUrl = "http://www.unrealtournament.com/blog/";

    private static ArrayList<String> unrealEngineBlogPosts, unrealTournamentBlogPosts;

    public static ArrayList<String> getUnrealEngine4BlogPosts() {
        return unrealEngineBlogPosts;
    }

    public static ArrayList<String> getUnrealTournamentBlogPosts() {
        return unrealTournamentBlogPosts;
    }

    public BlogParser() {
    }

    public void fetchUE4BlogPosts() {
        ArrayList<String> returnList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(unrealEngine4BlogUrl).get();
            Elements articles = doc.select("div.article");

            for (Element element : articles) {
                String title = "";
                String authorAndDate = "";

                Elements headers = element.getElementsByTag("h1");
                for (Element subelement : headers) {
                    title = subelement.text();
                }

                Elements dates = element.getElementsByTag("span");
                for (Element subelement : dates) {
                    authorAndDate =subelement.text().split("\\|", 2)[0];
                }

                if ((title != null) && (!title.equals("")) && (authorAndDate != null) && (!authorAndDate.equals(""))) {
                    returnList.add((new BlogPost(title, authorAndDate).toString()));
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
        unrealEngineBlogPosts = returnList;
    }

    public void fetchUT5BlogPosts() {
        ArrayList<String> returnList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(unrealTournamentBlogUrl).get();
            Elements articles = doc.select("div.entry-header");

            for (Element element : articles) {
                String title = "";

                Elements headers = element.getElementsByTag("h2");
                for (Element subelement : headers) {
                    title = subelement.text();
                }

                if ((title != null) && (!title.equals(""))) {
                    returnList.add((new BlogPost(title).toString()));
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
        unrealTournamentBlogPosts = returnList;
    }

    private static class BlogPost {
        private String title = null;
        private String authorAndDate = null;

        public BlogPost(String title, String authorAndDate) {
            this.title = title;
            this.authorAndDate = authorAndDate;
        }

        public BlogPost(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            if ((title != null) && (authorAndDate != null)) {
                return title + "\n" + authorAndDate;
            } else if (title != null) {
                return title;
            } else {
                return "";
            }
        }
    }
}
