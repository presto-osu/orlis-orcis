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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * Countries.
 *
 * @author Moshe
 */
public class Countries extends Cities {

    /** The number of main vertices per region border. */
    private static final int VERTICES_COUNT = 8;

    /**
     * Constructs a new countries.
     */
    public Countries() {
        super();
    }

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        Countries countries = new Countries();
        Collection<GeoName> names;
        Collection<CountryRegion> regions;
        try {
            names = countries.loadNames(res);
            regions = countries.toRegions(names);
            countries.toAndroidXML(regions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Transform the list of names to a list of countries.
     *
     * @param names
     *         the list of places.
     * @return the list of regions.
     */
    public Collection<CountryRegion> toRegions(Collection<GeoName> names) {
        Map<String, CountryRegion> regions = new TreeMap<String, CountryRegion>();
        String countryCode;
        CountryRegion region;

        for (GeoName name : names) {
            countryCode = name.getCountryCode();

            if (!regions.containsKey(countryCode)) {
                region = new CountryRegion(countryCode);
                regions.put(countryCode, region);
            } else
                region = regions.get(countryCode);
            region.addLocation(name.getLatitude(), name.getLongitude());
        }

        return regions.values();
    }

    /**
     * Write the list of names as arrays in Android resource file format.
     *
     * @param countries
     *         the list of countries.
     * @throws ParserConfigurationException
     *         if a DOM error occurs.
     * @throws TransformerException
     *         if a DOM error occurs.
     */
    public void toAndroidXML(Collection<CountryRegion> countries) throws ParserConfigurationException, TransformerException {
        List<CountryRegion> sorted = null;
        if (countries instanceof List)
            sorted = (List<CountryRegion>) countries;
        else
            sorted = new ArrayList<CountryRegion>(countries);
        Collections.sort(sorted, new RegionComparator());

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        TransformerFactory xformerFactory = TransformerFactory.newInstance();
        Transformer xformer = xformerFactory.newTransformer();
        File file = new File(APP_RES, "values/countries.xml");
        file.getParentFile().mkdirs();

        Element resources = doc.createElement(ANDROID_ELEMENT_RESOURCES);
        doc.appendChild(doc.createComment("Generated from geonames.org data."));
        doc.appendChild(resources);

        Element countriesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(countriesElement);
        Element verticesCountElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "vertices_count");
        verticesCountElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(verticesCountElement);
        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(latitudesElement);
        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_INTEGER_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        resources.appendChild(longitudesElement);

        Element country, latitude, longitude, verticesCount;
        int[] pointIndexes;
        int pointIndex;
        int pointCount = 0;

        for (CountryRegion region : sorted) {
            pointIndexes = region.findMainVertices(VERTICES_COUNT);

            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(region.getCountryCode());
            countriesElement.appendChild(country);

            pointCount = 0;
            for (int i = 0; i < VERTICES_COUNT; i++) {
                pointIndex = pointIndexes[i];

                if (pointIndex < 0)
                    break;

                latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
                latitude.setTextContent(String.valueOf(region.ypoints[pointIndex]));
                latitudesElement.appendChild(latitude);
                longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
                longitude.setTextContent(String.valueOf(region.xpoints[pointIndex]));
                longitudesElement.appendChild(longitude);
                pointCount++;
            }

            verticesCount = doc.createElement(ANDROID_ELEMENT_ITEM);
            verticesCount.setTextContent(String.valueOf(pointCount));
            verticesCountElement.appendChild(verticesCount);

        }

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        xformer.transform(src, result);
    }
}
