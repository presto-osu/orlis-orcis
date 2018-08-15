/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.geonames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Cities.
 *
 * @author Moshe
 */
public class Cities {

    public static final String ANDROID_ATTRIBUTE_NAME = "name";
    public static final String ANDROID_ATTRIBUTE_TRANSLATABLE = "translatable";
    public static final String ANDROID_ELEMENT_RESOURCES = "resources";
    public static final String ANDROID_ELEMENT_STRING_ARRAY = "string-array";
    public static final String ANDROID_ELEMENT_INTEGER_ARRAY = "integer-array";
    public static final String ANDROID_ELEMENT_ITEM = "item";

    protected static final String APP_RES = "app/src/main/res";

    /**
     * Constructs a new cities.
     */
    public Cities() {
        super();
    }

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        System.out.println(res);
        System.out.println(res.getAbsolutePath());
        try {
            System.out.println(res.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cities cities = new Cities();
        Collection<GeoName> names;
        Collection<GeoName> cityNames;
        Collection<GeoName> capitals;
        try {
            names = cities.loadNames(res);
            cityNames = cities.filterCity(names);
            capitals = cities.filterCapitals(cityNames);
            cities.toAndroidXML(capitals, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the list of names.
     *
     * @param file
     *         the geonames file.
     * @return the sorted list of records.
     * @throws IOException
     *         if an I/O error occurs.
     */
    public Collection<GeoName> loadNames(File file) throws IOException {
        GeoNames parser = new GeoNames();
        return parser.parse(file);
    }

    /**
     * Filter the list of names to find only cities.
     *
     * @param names
     *         the list of all names.
     * @return the list of cities.
     */
    public Collection<GeoName> filterCity(Collection<GeoName> names) {
        Collection<GeoName> cities = new ArrayList<GeoName>();

        for (GeoName name : names) {
            if (GeoName.FEATURE_P.equals(name.getFeatureClass())) {
                cities.add(name);
            }
        }

        return cities;
    }

    /**
     * Filter the list of names to find only capital cities.
     *
     * @param names
     *         the list of all names.
     * @return the list of capitals.
     */
    public Collection<GeoName> filterCapitals(Collection<GeoName> names) {
        Collection<GeoName> capitals = new ArrayList<GeoName>();
        Collection<String> countries = getCountries();

        for (GeoName name : names) {
            if (GeoName.FEATURE_PPLC.equals(name.getFeatureCode())) {
                capitals.add(name);
                countries.remove(name.getCountryCode());
            }
        }

        // For all countries without capitals, find the next best matching city
        // type.
        if (!countries.isEmpty()) {
            Map<String, GeoName> best = new TreeMap<String, GeoName>();
            GeoName place;
            String cc;
            for (GeoName name : names) {
                cc = name.getCountryCode();

                if (countries.contains(cc)) {
                    place = best.get(cc);
                    if (place == null) {
                        best.put(cc, name);
                        continue;
                    }
                    place = betterPlace(name, place);
                    best.put(cc, place);
                }
            }
            capitals.addAll(best.values());
        }

        return capitals;
    }

    /**
     * Get the better place by comparing its feature type as being more
     * populated.
     *
     * @param name1
     *         a name.
     * @param name2
     *         a name.
     * @return the better name.
     */
    private GeoName betterPlace(GeoName name1, GeoName name2) {
        String feature1 = name1.getFeatureCode();
        String feature2 = name2.getFeatureCode();
        int rank1 = getFeatureCodeRank(feature1);
        int rank2 = getFeatureCodeRank(feature2);

        // Compare features.
        if (rank1 < 0)
            return name2;
        if (rank2 < 0)
            return (rank1 < rank2) ? name2 : name1;
        // if (rank2 > rank1)
        // return name2;

        // Compare populations.
        // if (rank1 == rank2) {
        long pop1 = name1.getPopulation();
        long pop2 = name2.getPopulation();
        if (pop2 > pop1)
            return name2;
        // }

        return name1;
    }

    private Map<String, Integer> ranks;

    /**
     * Get the rank of the feature code.
     *
     * @param code
     *         the feature code.
     * @return the rank.
     */
    private int getFeatureCodeRank(String code) {
        if (ranks == null) {
            ranks = new TreeMap<String, Integer>();
            int rank = -2;
            ranks.put(GeoName.FEATURE_PPLW, rank++);
            ranks.put(GeoName.FEATURE_PPLQ, rank++);
            ranks.put(GeoName.FEATURE_P, rank++);
            ranks.put(GeoName.FEATURE_PPLX, rank++);
            ranks.put(GeoName.FEATURE_PPL, rank++);
            ranks.put(GeoName.FEATURE_PPLS, rank++);
            ranks.put(GeoName.FEATURE_PPLL, rank++);
            ranks.put(GeoName.FEATURE_PPLF, rank++);
            ranks.put(GeoName.FEATURE_PPLR, rank++);
            ranks.put(GeoName.FEATURE_STLMT, rank++);
            ranks.put(GeoName.FEATURE_PPLA4, rank++);
            ranks.put(GeoName.FEATURE_PPLA3, rank++);
            ranks.put(GeoName.FEATURE_PPLA2, rank++);
            ranks.put(GeoName.FEATURE_PPLA, rank++);
            ranks.put(GeoName.FEATURE_PPLG, rank++);
            ranks.put(GeoName.FEATURE_PPLC, rank++);
        }
        return ranks.get(code);
    }

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param names
     *         the list of names.
     * @param language
     *         the language code.
     * @throws ParserConfigurationException
     *         if a DOM error occurs.
     * @throws TransformerException
     *         if a DOM error occurs.
     */
    public void toAndroidXML(Collection<GeoName> names, String language) throws ParserConfigurationException, TransformerException {
        List<GeoName> sorted = null;
        if (names instanceof List)
            sorted = (List<GeoName>) names;
        else
            sorted = new ArrayList<GeoName>(names);
        Collections.sort(sorted, new LocationComparator());

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        TransformerFactory xformerFactory = TransformerFactory.newInstance();
        Transformer xformer = xformerFactory.newTransformer();
        File file;
        if (language == null)
            file = new File(APP_RES, "values/cities.xml");
        else
            file = new File(APP_RES, "values-" + language + "/cities.xml");
        file.getParentFile().mkdirs();

        Element resources = doc.createElement(ANDROID_ELEMENT_RESOURCES);
        doc.appendChild(doc.createComment("Generated from geonames.org data."));
        doc.appendChild(resources);

        Element citiesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        citiesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities");
        resources.appendChild(citiesElement);
        Element countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);
        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);
        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(longitudesElement);
        Element zonesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "timezones");
        zonesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(zonesElement);

        Element country, latitude, longitude, zone;

        for (GeoName capital : sorted) {
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(capital.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(String.valueOf(capital.getLatitude()));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(String.valueOf(capital.getLongitude()));
            zone = doc.createElement(ANDROID_ELEMENT_ITEM);
            zone.setTextContent(capital.getTimeZone());

            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
            zonesElement.appendChild(zone);
        }

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        xformer.transform(src, result);
    }

    /**
     * Get the list of country codes.
     *
     * @return the list of countries.
     */
    protected Collection<String> getCountries() {
        Collection<String> countries = new TreeSet<String>();
        for (String country : Locale.getISOCountries())
            countries.add(country);
        return countries;
    }
}
