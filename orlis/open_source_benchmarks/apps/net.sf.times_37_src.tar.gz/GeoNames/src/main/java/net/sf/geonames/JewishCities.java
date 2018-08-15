package net.sf.geonames;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

public class JewishCities extends Cities {

    /**
     * List of Jewish cities' GeoName IDs.
     */
    private static final long[] JEWISH_CITIES = {
            // Addis Ababa, Ethiopia
            344979,
            // Aizawl, India
            1279186,
            // Amsterdam, Netherlands
            2759794,
            // Ariel, Israel
            8199394,
            // Ashdod, Israel
            295629,
            // Ashkelon / Ashqelon, Israel
            295620,
            // Atlanta, United States
            4180439,
            // Auckland, New Zealand
            2193733,
            // Baltimore, United States
            4347778,
            // Be'er Sheva, Israel
            295530,
            // Beit Shemesh, Israel
            295432,
            // Berlin, Germany
            2950159,
            // Bogota, Colombia
            3688689,
            // Boston, United States
            4930956,
            // Budapest, Hungary
            3054643,
            // Buenos Aires, Argentina
            3435910,
            // Brussels, Belgium
            2800866,
            // Cape Town, South Africa
            3369157,
            // Casablanca, Morocco
            2553604,
            // Chicago, United States
            4887398,
            // Cleveland, United States
            4055815,
            // Dallas, United States
            4684888,
            // Denver, United States
            5419384,
            // Detroit, United States
            4990729,
            // Durban, South Africa
            1007311,
            // Eilat, Israel
            295277,
            // Miami, United States
            4164138,
            // Geneve, Switzerland
            2660646,
            // Hadera, Israel
            294946,
            // Haifa, Israel
            294801,
            // Helsinki, Finland
            658225,
            // Houston, United States
            4699066,
            // Istanbul, Turkey
            745044,
            // Jerusalem, Israel
            281184,
            // Johannesburg, South Africa
            993800,
            // Kiev, Ukraine
            703448,
            // Las Vegas, United States
            5475433,
            // London, United Kingdom
            2643741,
            // Los Angeles, United States
            3882428,
            // Lyon, France
            2996944,
            // Madrid, Spain
            3117735,
            // Manchester, United Kingdom
            2643123,
            // Marseille, France
            2995469,
            // Melbourne, Australia
            2158177,
            // Mexico City, Mexico
            3530597,
            // Montreal, Canada
            6077243,
            // Moscow, Russia
            524901,
            // Mumbai, India
            1275339,
            // Munich, Germany
            2867714,
            // Nahariya, Israel
            294117,
            // Nazareth, Israel
            294098,
            // New York, United States
            5128581,
            // Nice, France
            2990440,
            // Paris, France
            2988507,
            // Perth, Australia
            2063523,
            // Philadelphia, United States
            4560349,
            // Phoenix, United States
            5308655,
            // Pittsburgh, United States
            5206379,
            // Portland, United States
            4975802,
            // Rio de Janeiro, Brazil
            3451190,
            // Rome, Italy
            3169070,
            // Sarajevo, Bosnia and Herzegovina
            3191281,
            // Seattle, United States
            5809844,
            // St Louis, United States
            4407066,
            // St Petersburg, Russia
            498817,
            // San Diego, United States
            5391811,
            // San Francisco, United States
            5391959,
            // Sao Paulo, Brazil
            3448439,
            // Stockholm, Sweden
            2673730,
            // Sydney, Australia
            2147714,
            // Tel Aviv, Israel
            293397,
            // Tiberias, Israel
            293322,
            // Toronto, Canada
            6167865,
            // Safed / Zefat, Israel
            293100,
            // Vancouver, Canada
            6173331,
            // Vienna, Austria
            2761369,
            // Washington D.C., United States
            4140963,
            // Zurich, Switzerland
            2657896};

    public JewishCities() {
        super();
        Arrays.sort(JEWISH_CITIES);
    }

    public static void main(String[] args) {
        String path = "GeoNames/res/cities1000.txt";
        File res = new File(path);
        JewishCities cities = new JewishCities();
        Collection<GeoName> names;
        Collection<GeoName> jewish;
        try {
            names = cities.loadNames(res);
            jewish = cities.filterJewishCities(names);
            cities.toAndroidXML(jewish, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<GeoName> filterJewishCities(Collection<GeoName> names) {
        Collection<GeoName> cities = new ArrayList<GeoName>();

        long nameId;
        for (GeoName name : names) {
            nameId = name.getGeoNameId();

            for (long id : JEWISH_CITIES) {
                if (id == nameId) {
                    cities.add(name);
                    continue;
                }
            }
        }

        return cities;
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
    @Override
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
            file = new File(APP_RES, "/values/cities.xml");
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
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_countries");
        countriesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(countriesElement);
        Element latitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_latitudes");
        latitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(latitudesElement);
        Element longitudesElement = doc.createElement(ANDROID_ELEMENT_STRING_ARRAY);
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_NAME, "cities_longitudes");
        longitudesElement.setAttribute(ANDROID_ATTRIBUTE_TRANSLATABLE, "false");
        if (language == null)
            resources.appendChild(longitudesElement);

        Element city, country, latitude, longitude;

        for (GeoName name : sorted) {
            city = doc.createElement(ANDROID_ELEMENT_ITEM);
            city.setTextContent(name.getName());
            country = doc.createElement(ANDROID_ELEMENT_ITEM);
            country.setTextContent(name.getCountryCode());
            latitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            latitude.setTextContent(String.valueOf(name.getLatitude()));
            longitude = doc.createElement(ANDROID_ELEMENT_ITEM);
            longitude.setTextContent(String.valueOf(name.getLongitude()));

            citiesElement.appendChild(city);
            countriesElement.appendChild(country);
            latitudesElement.appendChild(latitude);
            longitudesElement.appendChild(longitude);
        }

        Source src = new DOMSource(doc);
        Result result = new StreamResult(file);
        xformer.transform(src, result);
    }
}
