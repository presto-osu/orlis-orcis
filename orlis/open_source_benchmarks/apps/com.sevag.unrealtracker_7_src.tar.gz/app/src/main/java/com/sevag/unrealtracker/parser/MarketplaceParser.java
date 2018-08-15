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
public class MarketplaceParser {

    public static final String unrealEngine4MarketplaceUrl = "https://www.unrealengine.com/new-content";

    private static ArrayList<String> unrealEngineMarketplacePosts;

    public static ArrayList<String> getUnrealEngine4MarketplacePosts() {
        return unrealEngineMarketplacePosts;
    }

    public MarketplaceParser() {
    }

    public void fetchUE4MarketplacePosts() {
        ArrayList<String> returnList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(unrealEngine4MarketplaceUrl).get();
            Elements listings = doc.select("div.asset-container");

            for (Element element : listings) {
                String title = "";
                String price = "";
                String creator = "";
                String category = "";

                Elements imageboxes = element.select("div.image-box");
                for (Element subelement : imageboxes) {
                    Elements prices = subelement.getElementsByTag("span");

                    for (Element priceElement : prices) {
                        price = priceElement.text();
                    }
                }

                Elements infoboxes = element.select("div.info");
                for (Element subelement : infoboxes) {
                    Elements titles = subelement.getElementsByTag("h3");

                    for (Element titleElement : titles) {
                        title = titleElement.text();
                    }

                    Elements creators = subelement.select("li.creator");

                    for (Element creatorElement : creators) {
                        creator = creatorElement.text();
                    }

                    Elements categories = subelement.select("ul.categories-full-view");

                    for (Element categoryElement : categories) {
                        Elements actualCategories = categoryElement.getElementsByTag("li");

                        for (Element actualCategoryElement : actualCategories) {
                            category = actualCategoryElement.text();
                        }
                    }
                }

                if ((title != null) && (!title.equals("")) && (price != null) && (!price.equals(""))
                        && (creator != null) && (!creator.equals("")) && (category != null) && (!category.equals(""))) {
                    returnList.add((new MarketplacePost(title, price, creator, category).toString()));
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
        unrealEngineMarketplacePosts = returnList;
    }

    private static class MarketplacePost {
        private String title = null;
        private String price = null;
        private String creator = null;
        private String category = null;

        public MarketplacePost(String title, String price, String creator, String category) {
            this.title = title;
            this.price = price;
            this.creator = creator;
            this.category = category;
        }

        @Override
        public String toString() {
            if ((title != null) && (price != null) && (creator != null) && (category != null)) {
                return title + " - " + price + " - by " + creator + " - in " + category;
            } else {
                return "";
            }
        }
    }
}
