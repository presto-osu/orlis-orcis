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

/**
 * <code>
 * The main 'geoname' table has the following fields :<br>
 * ---------------------------------------------------<br>
 * geonameid         : integer id of record in geonames database<br>
 * name              : name of geographical point (utf8) varchar(200)<br>
 * asciiname         : name of geographical point in plain ascii characters, varchar(200)<br>
 * alternatenames    : alternatenames, comma separated varchar(5000)<br>
 * latitude          : latitude in decimal degrees (wgs84)<br>
 * longitude         : longitude in decimal degrees (wgs84)<br>
 * feature class     : see http://www.geonames.org/export/codes.html, char(1)<br>
 * feature code      : see http://www.geonames.org/export/codes.html, varchar(10)<br>
 * country code      : ISO-3166 2-letter country code, 2 characters<br>
 * cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters<br>
 * admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)<br>
 * admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)<br>
 * admin3 code       : code for third level administrative division, varchar(20)<br>
 * admin4 code       : code for fourth level administrative division, varchar(20)<br>
 * population        : bigint (8 byte int)<br>
 * elevation         : in meters, integer<br>
 * dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.<br>
 * timezone          : the timezone id (see file timeZone.txt) varchar(40)<br>
 * modification date : date of last modification in yyyy-MM-dd format<br>
 * </code>
 *
 * @author Moshe
 */
public class GeoName {

    /** Feature class for a populated city. */
    public static final String FEATURE_P = "P";
    /** Feature code for a populated place. */
    public static final String FEATURE_PPL = "PPL";
    /** Feature code for a seat of a first-order administrative division. */
    public static final String FEATURE_PPLA = "PPLA";
    /** Feature code for a seat of a second-order administrative division. */
    public static final String FEATURE_PPLA2 = "PPLA2";
    /** Feature code for a seat of a third-order administrative division. */
    public static final String FEATURE_PPLA3 = "PPLA3";
    /** Feature code for a seat of a fourth-order administrative division. */
    public static final String FEATURE_PPLA4 = "PPLA4";
    /** Feature code for a capital of a political entity. */
    public static final String FEATURE_PPLC = "PPLC";
    /** Feature code for a farm village. */
    public static final String FEATURE_PPLF = "PPLF";
    /** Feature code for a seat of government of a political entity. */
    public static final String FEATURE_PPLG = "PPLG";
    /** Feature code for a populated locality. */
    public static final String FEATURE_PPLL = "PPLL";
    /** Feature code for an abandoned populated place. */
    public static final String FEATURE_PPLQ = "PPLQ";
    /** Feature code for a religious populated place. */
    public static final String FEATURE_PPLR = "PPLR";
    /** Feature code for populated places. */
    public static final String FEATURE_PPLS = "PPLS";
    /** Feature code for a destroyed populated place. */
    public static final String FEATURE_PPLW = "PPLW";
    /** Feature code for a section of populated place. */
    public static final String FEATURE_PPLX = "PPLX";
    /** Feature code for an Israeli settlement. */
    public static final String FEATURE_STLMT = "STLMT";

    private long geonameid;
    private String name;
    private String asciiname;
    private String alternatenames;
    private double latitude;
    private double longitude;
    private String featureClass;
    private String featureCode;
    private String countryCode;
    private String cc2;
    private String admin1;
    private String admin2;
    private String admin3;
    private String admin4;
    private long population;
    private int elevation;
    private int dem;
    private String timezone;
    private String modification;

    public GeoName() {
        super();
    }

    /**
     * Get the geonameid.
     *
     * @return the geonameid
     */
    public long getGeoNameId() {
        return geonameid;
    }

    /**
     * Set the geonameid.
     *
     * @param geonameid
     *         the geonameid.
     */
    public void setGeoNameId(long geonameid) {
        this.geonameid = geonameid;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name
     *         the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the asciiname.
     *
     * @return the asciiname
     */
    public String getAsciiName() {
        return asciiname;
    }

    /**
     * Set the asciiname.
     *
     * @param asciiname
     *         the asciiname.
     */
    public void setAsciiName(String asciiname) {
        this.asciiname = asciiname;
    }

    /**
     * Get the alternatenames.
     *
     * @return the alternatenames
     */
    public String getAlternateNames() {
        return alternatenames;
    }

    /**
     * Set the alternatenames.
     *
     * @param alternatenames
     *         the alternatenames.
     */
    public void setAlternateNames(String alternatenames) {
        this.alternatenames = alternatenames;
    }

    /**
     * Get the latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude.
     *
     * @param latitude
     *         the latitude.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude.
     *
     * @param longitude
     *         the longitude.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the featureClass.
     *
     * @return the featureClass
     */
    public String getFeatureClass() {
        return featureClass;
    }

    /**
     * Set the featureClass.
     *
     * @param featureClass
     *         the featureClass.
     */
    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    /**
     * Get the featureCode.
     *
     * @return the featureCode
     */
    public String getFeatureCode() {
        return featureCode;
    }

    /**
     * Set the featureCode.
     *
     * @param featureCode
     *         the featureCode.
     */
    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    /**
     * Get the countryCode.
     *
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Set the countryCode.
     *
     * @param countryCode
     *         the countryCode.
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Get the cc2.
     *
     * @return the cc2
     */
    public String getCc2() {
        return cc2;
    }

    /**
     * Set the cc2.
     *
     * @param cc2
     *         the cc2.
     */
    public void setCc2(String cc2) {
        this.cc2 = cc2;
    }

    /**
     * Get the admin1.
     *
     * @return the admin1
     */
    public String getAdmin1() {
        return admin1;
    }

    /**
     * Set the admin1.
     *
     * @param admin1
     *         the admin1.
     */
    public void setAdmin1(String admin1) {
        this.admin1 = admin1;
    }

    /**
     * Get the admin2.
     *
     * @return the admin2
     */
    public String getAdmin2() {
        return admin2;
    }

    /**
     * Set the admin2.
     *
     * @param admin2
     *         the admin2.
     */
    public void setAdmin2(String admin2) {
        this.admin2 = admin2;
    }

    /**
     * Get the admin3.
     *
     * @return the admin3
     */
    public String getAdmin3() {
        return admin3;
    }

    /**
     * Set the admin3.
     *
     * @param admin3
     *         the admin3.
     */
    public void setAdmin3(String admin3) {
        this.admin3 = admin3;
    }

    /**
     * Get the admin4.
     *
     * @return the admin4
     */
    public String getAdmin4() {
        return admin4;
    }

    /**
     * Set the admin4.
     *
     * @param admin4
     *         the admin4.
     */
    public void setAdmin4(String admin4) {
        this.admin4 = admin4;
    }

    /**
     * Get the population.
     *
     * @return the population
     */
    public long getPopulation() {
        return population;
    }

    /**
     * Set the population.
     *
     * @param population
     *         the population.
     */
    public void setPopulation(long population) {
        this.population = population;
    }

    /**
     * Get the elevation.
     *
     * @return the elevation
     */
    public int getElevation() {
        return elevation;
    }

    /**
     * Set the elevation.
     *
     * @param elevation
     *         the elevation.
     */
    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    /**
     * Get the dem.
     *
     * @return the dem
     */
    public int getDem() {
        return dem;
    }

    /**
     * Set the dem.
     *
     * @param dem
     *         the dem.
     */
    public void setDem(int dem) {
        this.dem = dem;
    }

    /**
     * Get the timezone.
     *
     * @return the timezone
     */
    public String getTimeZone() {
        return timezone;
    }

    /**
     * Set the timezone.
     *
     * @param timezone
     *         the timezone.
     */
    public void setTimeZone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Get the modification.
     *
     * @return the modification
     */
    public String getModification() {
        return modification;
    }

    /**
     * Set the modification.
     *
     * @param modification
     *         the modification.
     */
    public void setModification(String modification) {
        this.modification = modification;
    }

    @Override
    public int hashCode() {
        return (int) getGeoNameId();
    }
}
