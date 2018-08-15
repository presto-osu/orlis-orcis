package fr.renzo.wikipoff;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

public class WikiXMLParser {
	
	private static final String ns = null; // We don't use namespaces
	private static final String TAG = "WikiXMLParser";

	public static ArrayList<Wiki> loadAvailableDBFromXML(Context context, InputStream in) throws IOException {
		XmlPullParser parser = Xml.newPullParser();
		ArrayList<Wiki> wikis = new ArrayList<Wiki>();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

			parser.setInput(in, null);
			parser.nextTag();
			wikis = readAsset(parser,context);
		} catch (XmlPullParserException e) {
			Toast.makeText(context, "Problem parsing available databases file: "+e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		Collections.sort(wikis, new Comparator<Wiki>() {
			public int compare(Wiki w1, Wiki w2) {
				if (w1.getLangcode().equals(w2.getLangcode())) {
					return w1.getGendateAsDate().compareTo(w2.getGendateAsDate());
				} else {
					return w1.getLangcode().compareToIgnoreCase(w2.getLangcode());
				}
			}
		}); 

		return wikis;
	}


	private static ArrayList<Wiki> readAsset(XmlPullParser parser,Context c) throws XmlPullParserException, IOException {
		ArrayList<Wiki> wikis = new ArrayList<Wiki>();
		parser.require(XmlPullParser.START_TAG, ns, "root");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("wiki")) {
				wikis.add(readWiki(parser,c));
				//		        } else {
				//		            skip(parser);
			}
		}
		return wikis;
	}

	private static Wiki readWiki(XmlPullParser parser,Context c) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "wiki");
		Wiki wiki = new Wiki(c);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equalsIgnoreCase("type")) {
				wiki.setType(readType(parser));
			} else if (name.equalsIgnoreCase("lang-code")) {
				wiki.setLangcode(readLangcode(parser));
			} else if (name.equalsIgnoreCase("lang-english")) {
				wiki.setLangenglish(readLangenglish(parser));
			} else if (name.equalsIgnoreCase("lang-local")) {
				wiki.setLanglocal(readLanglocal(parser));
			} else if (name.equalsIgnoreCase("dbfiles")) {
				wiki.setDBFiles(readDBFiles(parser));
			} else if (name.equalsIgnoreCase("version")) {
				wiki.setVersion(readVersion(parser));
			} else if (name.equalsIgnoreCase("source")) {
				wiki.setSource(readSource(parser));
			} else if (name.equalsIgnoreCase("author")) {
				wiki.setAuthor(readAuthor(parser));
			} else if (name.equalsIgnoreCase("iconUrl")) {
					wiki.setAuthor(readIconURL(parser));
			} else {
				Log.d(TAG,"WTF "+name);
				skip(parser);
			}
		}
		return wiki;
	}


	private static String readType(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "type");
		String type = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "type");
		return type;
	}
	private static String readLangcode(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "lang-code");
		String langcode = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "lang-code");
		return langcode;
	}
	private static String readLangenglish(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "lang-english");
		String langenglish = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "lang-english");
		return langenglish;
	}
	private static String readLanglocal(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "lang-local");
		String langlocal = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "lang-local");
		return langlocal;
	}
	private static String readVersion(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "version");
		String version = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "version");
		return version;
	}
	private static String readSource(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "source");
		String langlocal = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "source");
		return langlocal;
	}
	private static String readAuthor(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "author");
		String version = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "author");
		return version;
	}
	private static String readIconURL(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "iconUrl");
		String version = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "iconUrl");
		return version;
	}

	private static WikiDBFile readDBFile(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "dbfile");
		WikiDBFile wdbf = new WikiDBFile();
		wdbf.setFilename(parser.getAttributeValue(null, "filename")); 
		wdbf.setGendate(parser.getAttributeValue(null, "gendate"));
		wdbf.setSize(Long.parseLong(parser.getAttributeValue(null, "size")));
		wdbf.setUrl(parser.getAttributeValue(null, "url"));	    
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, ns, "dbfile");
		return wdbf;
	}

	private static ArrayList<WikiDBFile> readDBFiles(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<WikiDBFile> dbfiles =new ArrayList<WikiDBFile>();
		parser.require(XmlPullParser.START_TAG, ns, "dbfiles");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("dbfile")) {
				dbfiles.add(readDBFile(parser));
				//	        } else {
				//	            skip(parser);
			}
		}  
		return dbfiles;

	}

	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
