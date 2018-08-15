/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;

import android.os.Build;

import com.gabm.fancyplaces.data.FancyPlace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by gabm on 08/06/15.
 */
public class GPXExporter implements IExporter {
    /**
     * XML header.
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /**
     * GPX opening tag
     */
    private static final String TAG_GPX = "<gpx"
            + " xmlns=\"http://www.topografix.com/GPX/1/1\""
            + " version=\"1.1\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd \">";

    /**
     * Date format for a point timestamp.
     */
    private static final SimpleDateFormat POINT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    public void writeGpxFile(List<FancyPlace> fpList, File target) throws IOException {
        FileWriter fw = new FileWriter(target);
        String folder = target.getParent();

        fw.write(XML_HEADER + "\n");
        fw.write(TAG_GPX + "\n");
        fw.write(getMetaData() + "\n");

        writeWayPoints(fw, folder, fpList);

        fw.write("</gpx>");

        fw.close();
    }

    public String getMetaData() {
        return "<metadata>\n" +
                "\t<author>\n" +
                "\t\t<name>" + Build.MANUFACTURER + " " + Build.MODEL + "</name>\n" +
                "\t</author>\n" +
                "</metadata>";
    }


    public void writeWayPoints(FileWriter fw, String folder, List<FancyPlace> fpList) throws IOException {

        POINT_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));


        for (int i = 0; i < fpList.size(); i++) {
            FancyPlace curFancyPlace = fpList.get(i);

            StringBuffer out = new StringBuffer();
            out.append("\t"
                    + "<wpt lat=\"" + curFancyPlace.getLocationLat() + "\" "
                    + "lon=\"" + curFancyPlace.getLocationLong() + "\">" + "\n");

            out.append("\t\t<time>" + POINT_DATE_FORMATTER.format(new Date()) + "</time>\n");
            out.append("\t\t<name>" + escapeXML(curFancyPlace.getTitle()) + "</name>\n");

            if (!curFancyPlace.getNotes().equals(""))
                out.append("\t\t<desc>" + escapeXML(curFancyPlace.getNotes()) + "</desc>\n");

            if (curFancyPlace.getImage().exists()) {
                String fileName = i + ".png";
                curFancyPlace.getImage().copy(folder + File.separator + fileName);
                out.append("\t\t<link  href=\"file:" + fileName + "\" />\n");
            }


            out.append("\t" + "</wpt>" + "\n");

            fw.write(out.toString());
        }
    }

    @Override
    public boolean WriteToFile(List<FancyPlace> fpList, File targetName, Object userData) {

        boolean success = false;
        try {
            writeGpxFile(fpList, targetName);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    @Override
    public boolean WriteToFile(FancyPlace fancyPlace, File target, Object userData) {

        List<FancyPlace> fpList = new ArrayList<>();
        fpList.add(fancyPlace);


        boolean success = false;
        try {
            writeGpxFile(fpList, target);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    protected String escapeXML(String input) {
        StringBuilder escapedXML = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':
                    escapedXML.append("&lt;");
                    break;
                case '>':
                    escapedXML.append("&gt;");
                    break;
                case '\"':
                    escapedXML.append("&quot;");
                    break;
                case '&':
                    escapedXML.append("&amp;");
                    break;
                case '\'':
                    escapedXML.append("&apos;");
                    break;
                default:
                    if (c > 0x7e) {
                        escapedXML.append("&#" + ((int) c) + ";");
                    } else
                        escapedXML.append(c);
            }
        }
        return escapedXML.toString();
    }
}


/**
 * Iterates on track points and write them.
 *
 * @param trackName Name of the track (metadata).
 * @param fw Writer to the target file.
 * @param fpList List of FancyPlaces
 * @throws IOException
 */

    /*
    public static void writeTrackPoints(String trackName, FileWriter fw, List<FancyPlace> fpList) throws IOException {
        fw.write("\t" + "<trk>");
        fw.write("\t\t" + "<name>" + trackName + "</name>" + "\n");

        fw.write("\t\t" + "<trkseg>" + "\n");

        for (int i=0; i<fpList.size();i++)
        {
            FancyPlace curFancyPlace = fpList.get(i);

            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\""
                    + curFancyPlace.getLocationLat() + "\" "
                    + "lon=\"" + curFancyPlace.getLocationLong() + "\">");
            Date date = new Date();
            out.append("<time>" + POINT_DATE_FORMATTER.format(date) + "</time>");


            out.append("</trkpt>" + "\n");
            fw.write(out.toString());
        }

        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }*/