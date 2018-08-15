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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * List of geoname records.
 *
 * @author Moshe
 */
public class GeoNames {

    public GeoNames() {
        super();
    }

    public Collection<GeoName> parse(File file) throws IOException {
        Collection<GeoName> records = null;
        Reader reader = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            reader = new InputStreamReader(in, "UTF-8");
            in = null;
            records = parse(reader);
        } finally {
            if (in != null)
                in.close();
            if (reader != null)
                reader.close();
        }
        return records;
    }

    public Collection<GeoName> parse(Reader reader) throws IOException {
        Collection<GeoName> records = new ArrayList<GeoName>();
        GeoName record;
        String line;
        BufferedReader buf = new BufferedReader(reader);
        String[] fields;
        int column;
        String field;

        while (true) {
            line = buf.readLine();
            if (line == null)
                break;
            fields = line.split("\t");
            record = new GeoName();

            column = 0;
            field = fields[column++];
            record.setGeoNameId(Long.parseLong(field));
            field = fields[column++];
            record.setName(field);
            field = fields[column++];
            record.setAsciiName(field);
            field = fields[column++];
            record.setAlternateNames(field);
            field = fields[column++];
            record.setLatitude(Double.parseDouble(field));
            field = fields[column++];
            record.setLongitude(Double.parseDouble(field));
            field = fields[column++];
            record.setFeatureClass(field);
            field = fields[column++];
            record.setFeatureCode(field);
            field = fields[column++];
            record.setCountryCode(field);
            field = fields[column++];
            record.setCc2(field);
            field = fields[column++];
            record.setAdmin1(field);
            field = fields[column++];
            record.setAdmin2(field);
            field = fields[column++];
            record.setAdmin3(field);
            field = fields[column++];
            record.setAdmin4(field);
            field = fields[column++];
            record.setPopulation(Long.parseLong(field));
            field = fields[column++];
            if (field.length() > 0)
                record.setElevation(Integer.parseInt(field));
            field = fields[column++];
            if (field.length() > 0)
                record.setDem(Integer.parseInt(field));
            field = fields[column++];
            if (field.length() == 0) {
                // throw new NullPointerException("time zone required for " +
                // record.getGeoNameId());
                System.err.println("time zone required for " + record.getGeoNameId());
                System.err.println(line);
                continue;
            }
            record.setTimeZone(field);
            field = fields[column++];
            record.setModification(field);

            records.add(record);
        }

        return records;
    }
}
