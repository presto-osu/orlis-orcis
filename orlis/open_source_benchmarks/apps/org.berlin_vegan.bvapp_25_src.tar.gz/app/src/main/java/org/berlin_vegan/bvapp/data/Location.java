/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.data;


import android.support.annotation.NonNull;

import org.berlin_vegan.bvapp.helpers.DateUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class Location implements Comparable<Location>, Serializable {
    public final static int OMNIVORE = 1;
    final public static int OMNIVORE_VEGAN_DECLARED = 2;
    final public static int VEGETARIAN = 3;
    final public static int VEGETARIAN_VEGAN_DECLARED = 4;
    final public static int VEGAN = 5;
    protected String id;
    protected String name;
    protected String street;
    protected Integer cityCode;
    protected String district;
    protected Double latCoord;
    protected Double longCoord;
    protected List<String> tags = new ArrayList<>();
    private String city;
    private Float distToCurLoc = -1.0f;
    private String telephone;
    private String website;
    private String otMon;
    private String otTue;
    private String otWed;
    private String otThu;
    private String otFri;
    private String otSat;
    private String otSun;
    private Integer vegan;
    private String comment;

    // getter & setter
    public String getId() {
        if (id == null) {
            return "";
        }
        return id.trim();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        if (street == null) {
            return "";
        }
        return street.trim();
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }

    public String getCity() {
        if (city == null) {
            return "";
        }
        return city.trim();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatCoord() {
        return latCoord;
    }

    public void setLatCoord(Double latCoord) {
        this.latCoord = latCoord;
    }

    public Double getLongCoord() {
        return longCoord;
    }

    public void setLongCoord(Double longCoord) {
        this.longCoord = longCoord;
    }

    public Float getDistToCurLoc() {
        return distToCurLoc;
    }

    public void setDistToCurLoc(Float distToCurLoc) {
        this.distToCurLoc = distToCurLoc;
    }

    public String getTelephone() {
        if (telephone == null) {
            return "";
        }
        return telephone.trim();
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWebsite() {
        if (website == null) {
            return "";
        }
        return website.trim();
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getWebsiteWithProtocolPrefix() {
        final String http = "http://";
        final String https = "https://";
        if (!website.startsWith(http) || !website.startsWith(https)) {
            return http + website.trim();
        }
        return website.trim();
    }

    /**
     * Method to present a nicer formatted website string to a user in the view.
     *
     * @return formatted website string
     */
    public String getWebsiteFormatted() {
        if (website == null) {
            return "";
        }

        final String http = "http://";
        final String https = "https://";
        final char slash = '/';
        String out = website;

        out = out.replaceAll(http, "");
        out = out.replaceAll(https, "");

        final int lastCharacter = out.length() - 1;
        if (out.charAt(lastCharacter) == slash) {
            out = out.substring(0, lastCharacter);
        }

        return out.trim();
    }

    public String getOtMon() {
        if (otMon == null) {
            return "";
        }
        return otMon.trim();
    }

    public void setOtMon(String otMon) {
        this.otMon = otMon;
    }

    public String getOtTue() {
        if (otTue == null) {
            return "";
        }
        return otTue.trim();
    }

    public void setOtTue(String otTue) {
        this.otTue = otTue;
    }

    public String getOtWed() {
        if (otWed == null) {
            return "";
        }
        return otWed.trim();
    }

    public void setOtWed(String otWed) {
        this.otWed = otWed;
    }

    public String getOtThu() {
        if (otThu == null) {
            return "";
        }
        return otThu.trim();
    }

    public void setOtThu(String otThu) {
        this.otThu = otThu;
    }

    public String getOtFri() {
        if (otFri == null) {
            return "";
        }
        return otFri.trim();
    }

    public void setOtFri(String otFri) {
        this.otFri = otFri;
    }

    public String getOtSat() {
        if (otSat == null) {
            return "";
        }
        return otSat.trim();
    }

    public void setOtSat(String otSat) {
        this.otSat = otSat;
    }

    public String getOtSun() {
        if (otSun == null) {
            return "";
        }
        return otSun.trim();
    }

    public void setOtSun(String otSun) {
        this.otSun = otSun;
    }

    public List<OpeningHoursInterval> getCondensedOpeningHours() {
        final ArrayList<OpeningHoursInterval> result = new ArrayList<>();
        String[] openingHours = getOpeningHoursAsArray();

        int equalIndex = -1;
        for (int day = 0; day <= 6; day++) {
            if (day < 6 && openingHours[day].equalsIgnoreCase(openingHours[day + 1])) {
                // successor has equal opening hours, so remember current day and continue
                if (equalIndex == -1) {
                    equalIndex = day;
                }
            } else {
                if (equalIndex == -1) {
                    // current day (opening hours) is unique, so create new entry
                    if (openingHours[day].isEmpty()) {
                        // closed
                        result.add(new OpeningHoursInterval(day, OpeningHoursInterval.CLOSED));
                    } else {
                        result.add(new OpeningHoursInterval(day, openingHours[day]));
                    }
                } else {
                    // there are consecutive days
                    String openTimesText = openingHours[day].isEmpty() ? OpeningHoursInterval.CLOSED : openingHours[day];
                    result.add(new OpeningHoursInterval(equalIndex, day, openTimesText));
                    equalIndex = -1;
                }
            }
        }
        return result;
    }

    public boolean isOpen(Date date) {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = DateUtil.inMinutes(currentHour, calendar.get(Calendar.MINUTE));
        if (isAfterMidnight(date)) {
            currentMinute = currentMinute + DateUtil.MINUTES_PER_DAY; // add a complete day
        }
        final OpeningHours hours = getOpeningHours(date);
        return hours.isInRange(currentMinute);

    }

    @NonNull
    private OpeningHours getOpeningHours(Date date) {
        final int sundayIndex = 6;
        final String[] openingHours = getOpeningHoursAsArray();

        int dayOfWeek = DateUtil.getDayOfWeek(date);
        if (isAfterMidnight(date)) {
            dayOfWeek = dayOfWeek - 1; // its short after midnight, so we use the opening hour from the day before
            if (dayOfWeek == -1) {
                dayOfWeek = sundayIndex; // sunday
            }
        }
        if (DateUtil.isPublicHoliday(date)) { // it is a holiday so take the opening hours from sunday
            dayOfWeek = sundayIndex;
        }
        return new OpeningHours(openingHours[dayOfWeek]);
    }

    /**
     * returns true if the date is short after midnight
     */
    private boolean isAfterMidnight(Date date) {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        return currentHour >= 0 && currentHour <= 6;
    }

    public String getFormattedClosingTime(Date date) {
        final OpeningHours hours = getOpeningHours(date);
        return hours.getFormattedClosingTime();
    }

    public Integer getVegan() {
        return vegan;
    }

    public void setVegan(Integer vegan) {
        this.vegan = vegan;
    }

    private String getComment() {
        if (comment == null) {
            return "";
        }
        return comment.trim();
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentWithoutSoftHyphens() {
        // unfortunately soft hyphen (&shy;) is only partially working with fromHtml(): the word gets
        // split at the correct place, but the hyphen (dash) is not shown. this might be very annoying
        // to the user, because it just does not look right. as a workaround we do not split words at
        // all.
        // a web view solves the hyphen problem, but does not integrate into our current layout very well
        return getComment().replace("&shy;", "").trim();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    private String[] getOpeningHoursAsArray() {
        String[] openingHours = new String[7];
        openingHours[0] = getOtMon();
        openingHours[1] = getOtTue();
        openingHours[2] = getOtWed();
        openingHours[3] = getOtThu();
        openingHours[4] = getOtFri();
        openingHours[5] = getOtSat();
        openingHours[6] = getOtSun();
        return openingHours;
    }

    // idea for equals(...) and hashCode() are taken from:
    // http://java67.blogspot.de/2013/04/example-of-overriding-equals-hashcode-compareTo-java-method.html
    //
    // note: not all member variables are taken into account for calculation. adapt, if needed!
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || (getClass() != object.getClass())) {
            return false;
        }
        Location other = (Location) object;
        return (id != null && id.equals(other.id)) &&
                (name != null && name.equals(other.name)) &&
                (street != null && street.equals(other.street)) &&
                (cityCode != null && cityCode.equals(other.cityCode)) &&
                (latCoord != null && latCoord.equals(other.latCoord)) &&
                (longCoord != null && longCoord.equals(other.longCoord));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (cityCode != null ? cityCode.hashCode() : 0);
        result = 31 * result + (latCoord != null ? latCoord.hashCode() : 0);
        result = 31 * result + (longCoord != null ? longCoord.hashCode() : 0);
        return result;
    }
    // --------------------------------------------------------------------
    // implement comparable interface

    public int compareTo(@NonNull Location other) {
        if (getDistToCurLoc() == null && other.getDistToCurLoc() == null) {
            return 0;
        }
        if (getDistToCurLoc() == null) {
            return 1;
        }
        if (other.getDistToCurLoc() == null) {
            return -1;
        }
        return getDistToCurLoc().compareTo(other.getDistToCurLoc());
    }
}
