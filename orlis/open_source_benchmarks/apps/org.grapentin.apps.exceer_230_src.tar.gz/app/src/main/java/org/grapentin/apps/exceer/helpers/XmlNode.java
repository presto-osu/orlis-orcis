/******************************************************************************
 *    This file is part of Exceer                                             *
 *                                                                            *
 *    Copyright (C) 2015  Andreas Grapentin                                   *
 *                                                                            *
 *    This program is free software: you can redistribute it and/or modify    *
 *    it under the terms of the GNU General Public License as published by    *
 *    the Free Software Foundation, either version 3 of the License, or       *
 *    (at your option) any later version.                                     *
 *                                                                            *
 *    This program is distributed in the hope that it will be useful,         *
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 *    GNU General Public License for more details.                            *
 *                                                                            *
 *    You should have received a copy of the GNU General Public License       *
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.   *
 ******************************************************************************/

package org.grapentin.apps.exceer.helpers;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class XmlNode implements Serializable
{

  private String name = null;
  private String value = "";

  private HashMap<String, String> attributes = new HashMap<>();

  private ArrayList<XmlNode> children = new ArrayList<>();

  public XmlNode (XmlResourceParser p) throws XmlPullParserException, XmlNodeMalformedException, IOException
    {
      int event = p.getEventType();
      switch (event)
        {
        case XmlPullParser.START_DOCUMENT:
          // this is the root node
          break;
        case XmlPullParser.START_TAG:
          this.name = p.getName();
          for (int i = 0; i < p.getAttributeCount(); ++i)
            this.attributes.put(p.getAttributeName(i), p.getAttributeValue(i));
          break;
        default:
          throw new XmlNodeMalformedException("unexpected event at node start: " + event);
        }

      // pull all START_DOCUMENT events (sometimes there seem to be several?!)
      do
        p.next();
      while (p.getEventType() == XmlPullParser.START_DOCUMENT);

      while (true)
        {
          event = p.getEventType();
          switch (event)
            {
            case XmlPullParser.END_DOCUMENT:
              // this is okay if this is the root node, otherwise throw
              if (name != null)
                throw new XmlNodeMalformedException("unexpected end of document");
              return;
            case XmlPullParser.START_TAG:
              // start of a child node
              children.add(new XmlNode(p));
              break;
            case XmlPullParser.END_TAG:
              // if the end tag matches the name, consume it and return, otherwise throw
              if (!p.getName().equals(name))
                throw new XmlNodeMalformedException("mismatch in close tag: " + name + " vs " + p.getName());
              p.next();
              return;
            case XmlPullParser.TEXT:
              this.value += p.getText();
              p.next();
              break;
            default:
              throw new XmlNodeMalformedException("unexpected event at node body: " + event);
            }
        }
    }

  public String getName ()
    {
      return this.name;
    }

  public String getValue ()
    {
      return this.value;
    }

  public String getAttribute (String key)
    {
      return attributes.get(key);
    }

  public ArrayList<XmlNode> getChildren (String name)
    {
      ArrayList<XmlNode> nodes = new ArrayList<>();
      for (XmlNode n : children)
        if (n.name.equals(name))
          nodes.add(n);
      return nodes;
    }

  public static class XmlNodeMalformedException extends Exception
  {
    public XmlNodeMalformedException (String msg)
      {
        super(msg);
      }
  }

}
