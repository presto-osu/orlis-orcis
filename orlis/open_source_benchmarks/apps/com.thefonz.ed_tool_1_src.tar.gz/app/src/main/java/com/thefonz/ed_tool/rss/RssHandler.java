package com.thefonz.ed_tool.rss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thefonz on 26/03/15.
 */
public class RssHandler extends DefaultHandler {
    private List<RssItem> rssItemList;
    private RssItem currentItem;
    private boolean parsingTitle;
    private boolean parsingDate;
    private boolean parsingLink;
    private boolean parsingDescription;

    public RssHandler() {
        //Initializes a new ArrayList that will hold all the generated RSS items.
        rssItemList = new ArrayList<RssItem>();
    }

    public List<RssItem> getRssItemList() {
        return rssItemList;
    }


    //Called when an opening tag is reached, such as <item> or <title>
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("item"))
            currentItem = new RssItem();
        else if (qName.equals("title"))
            parsingTitle = true;
        else if (qName.equals("pubDate"))
            parsingDate = true;
        else if (qName.equals("link"))
            parsingLink = true;
        else if (qName.equals("description"))
            parsingDescription = true;
        else if (qName.equals("media:thumbnail") || qName.equals("media:content") || qName.equals("image")) {
            if (attributes.getValue("url") != null)
                currentItem.setImageUrl(attributes.getValue("url"));
        }
    }

    //Called when a closing tag is reached, such as </item> or </title>
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("item")) {
            //End of an item so add the currentItem to the list of items.
            rssItemList.add(currentItem);
            currentItem = null;
        } else if (qName.equals("title"))
            parsingTitle = false;
        else if (qName.equals("pubDate"))
            parsingDate = false;
        else if (qName.equals("link"))
            parsingLink = false;
        else if (qName.equals("description"))
            parsingDescription = false;
    }

    //Goes through character by character when parsing whats inside of a tag.
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentItem != null) {
            //If parsingTitle is true, then that means we are inside a <title> tag so the text is the title of an item.
            if (parsingTitle)
                currentItem.setTitle(new String(ch, start, length));
                //If parsingLink is true, then that means we are inside a <link> tag so the text is the link of an item.
            else if (parsingDate)
                currentItem.setDate(new String(ch, start, length));
            else if (parsingLink)
                currentItem.setLink(new String(ch, start, length));
                //If parsingDescription is true, then that means we are inside a <description> tag so the text is the description of an item.
            else if (parsingDescription)
                currentItem.setDescription(new String(ch, start, length));
        }
    }
}
