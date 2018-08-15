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


package org.berlin_vegan.bvapp.fragments.LocationDetails;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.berlin_vegan.bvapp.BuildConfig;
import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.activities.LocationDetailActivity;
import org.berlin_vegan.bvapp.data.GastroLocation;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.data.OpeningHoursInterval;
import org.berlin_vegan.bvapp.helpers.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Holds content for the details tab in {@link LocationDetailActivity}.
 */
public class LocationDetailsFragment extends LocationBaseFragment {

    private Location location;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.location_details_fragment, container, false);
        location = initLocation(savedInstanceState);

        addOpeningHours(v);
        addAddress(v);
        addTelephone(v);
        addWebsite(v);
        addMiscellaneous(v);
        addEditing(v);

        return v;
    }

    private void addAddress(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.address);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_place_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final String text;
        final String street = location.getStreet();
        final int cityCode = location.getCityCode();
        final String city = location.getCity();
        if (!street.isEmpty()) {
            text = "<a href=\"http://maps.google.com/maps?q="
                    // google maps
                    + street + ", " + cityCode + ", " + city + "\">"
                    // view
                    + street + ", " + cityCode + " " + city + "</a>";
        } else {
            text = getString(R.string.gastro_details_contact_address);
        }
        final TextView content = (TextView) item.findViewById(R.id.content);
        content.setText(Html.fromHtml(text));
        content.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(content);
    }

    private void addOpeningHours(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.opening_hours);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_schedule_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final LinearLayout content = (LinearLayout) item.findViewById(R.id.content);

        final List<OpeningHoursInterval> openingHours = location.getCondensedOpeningHours();

        final HashMap<Integer, String> dayTranslation = new HashMap<>();
        dayTranslation.put(0, getString(R.string.gastro_details_opening_hours_content_monday));
        dayTranslation.put(1, getString(R.string.gastro_details_opening_hours_content_tuesday));
        dayTranslation.put(2, getString(R.string.gastro_details_opening_hours_content_wednesday));
        dayTranslation.put(3, getString(R.string.gastro_details_opening_hours_content_thursday));
        dayTranslation.put(4, getString(R.string.gastro_details_opening_hours_content_friday));
        dayTranslation.put(5, getString(R.string.gastro_details_opening_hours_content_saturday));
        dayTranslation.put(6, getString(R.string.gastro_details_opening_hours_content_sunday));

        for (OpeningHoursInterval openingHoursInterval : openingHours) {
            final LinearLayout dateLayout = new LinearLayout(v.getContext());
            dateLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dateLayout.setPadding(0, 0, 0, 16);
            dateLayout.setOrientation(LinearLayout.HORIZONTAL);

            final TextView key = new TextView(v.getContext());
            key.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.65f));
            String text;
            if (openingHoursInterval.getNumberOfDays() == 1) {
                text = dayTranslation.get(openingHoursInterval.getStartDay());
            } else {
                text = dayTranslation.get(openingHoursInterval.getStartDay()) + " - " + dayTranslation.get(openingHoursInterval.getEndDay());
            }
            final boolean todayInInterval = openingHoursInterval.isDateInInterval(Calendar.getInstance().getTime());
            if (todayInInterval) {
                key.setTypeface(key.getTypeface(), Typeface.BOLD);
            }
            key.setText(text);
            dateLayout.addView(key);

            final TextView value = new TextView(getActivity());
            value.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.35f));

            if (openingHoursInterval.getOpeningHours().equals(OpeningHoursInterval.CLOSED)) {
                text = getString(R.string.gastro_details_opening_hours_content_closed);
            } else {
                text = openingHoursInterval.getOpeningHours() + " " + getString(R.string.gastro_details_opening_hours_content_clock);
            }
            if (todayInInterval) {
                value.setTypeface(value.getTypeface(), Typeface.BOLD);
            }
            value.setText(text);

            dateLayout.addView(value);
            content.addView(dateLayout);
        }
        // add warning to the opening hours if its a holiday
        // TODO fix layout and style, at the moment its italic and below the opening hours @Robin-siebzehn3
        if (DateUtil.isPublicHoliday(GregorianCalendar.getInstance().getTime())) {
            final TextView holidayWarning = new TextView(v.getContext());
            holidayWarning.setTypeface(holidayWarning.getTypeface(), Typeface.ITALIC);
            holidayWarning.setText(getString(R.string.gastro_details_opening_hours_content_holiday_warning));
            content.addView(holidayWarning);
        }


    }

    private void addTelephone(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.telephone);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_phone_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final String text;
        final String telephone = location.getTelephone();
        if (!telephone.isEmpty()) {
            text = "<a href=\"tel:"
                    + telephone + "\">"
                    + telephone + "</a>";
        } else {
            text = getString(R.string.gastro_details_contact_telephone);
        }
        final TextView content = (TextView) item.findViewById(R.id.content);
        content.setText(Html.fromHtml(text));
        content.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(content);
    }

    private void addWebsite(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.website);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_public_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final String text;
        final String website = location.getWebsite();
        if (!website.isEmpty()) {
            final String websiteWithProtocolPrefix = location.getWebsiteWithProtocolPrefix();
            final String websiteFormatted = location.getWebsiteFormatted();
            text = "<a href="
                    + websiteWithProtocolPrefix + ">"
                    + websiteFormatted + "</a>";
        } else {
            text = getString(R.string.gastro_details_contact_website);
        }
        final TextView content = (TextView) item.findViewById(R.id.content);
        content.setText(Html.fromHtml(text));
        content.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(content);
    }

    private void addMiscellaneous(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.miscellaneous);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_more_vert_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final LinearLayout content = (LinearLayout) item.findViewById(R.id.content);

        final List<List<String>> dates = new ArrayList<>();
        dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_vegan), getVeganContentString(location.getVegan())));
        if (location instanceof GastroLocation) {
            GastroLocation gastroLocation = (GastroLocation) location;
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_organic), getMiscellaneousContentString(gastroLocation.getOrganic())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_gluten_free), getMiscellaneousContentString(gastroLocation.getGlutenFree())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_wlan), getMiscellaneousContentString(gastroLocation.getWlan())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_handicapped_accessible), getMiscellaneousContentString(gastroLocation.getHandicappedAccessible())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_handicapped_accessible_wc), getMiscellaneousContentString(gastroLocation.getHandicappedAccessibleWc())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_child_chair), getMiscellaneousContentString(gastroLocation.getChildChair())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_dog), getMiscellaneousContentString(gastroLocation.getDog())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_seats_indoor), getMiscellaneousContentString(gastroLocation.getSeatsIndoor())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_seats_outdoor), getMiscellaneousContentString(gastroLocation.getSeatsOutdoor())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_delivery), getMiscellaneousContentString(gastroLocation.getDelivery())));
            dates.add(Arrays.asList(getString(R.string.gastro_details_miscellaneous_content_catering), getMiscellaneousContentString(gastroLocation.getCatering())));
        }


        for (List<String> date : dates) {
            final LinearLayout dateLayout = new LinearLayout(v.getContext());
            dateLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            dateLayout.setPadding(0, 0, 0, 16);
            dateLayout.setOrientation(LinearLayout.HORIZONTAL);

            final TextView key = new TextView(v.getContext());
            key.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.65f));
            key.setText(date.get(0));

            dateLayout.addView(key);

            final TextView value = new TextView(getActivity());
            value.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 0.35f));
            value.setText(date.get(1));

            dateLayout.addView(value);

            content.addView(dateLayout);
        }
    }

    private String getMiscellaneousContentString(Integer i) {
        if (i == null || (i != 0 && i != 1)) {
            return getString(R.string.gastro_details_miscellaneous_content_unknown);
        }
        if (i == 1) {
            return getString(R.string.gastro_details_miscellaneous_content_yes);
        } else {
            return getString(R.string.gastro_details_miscellaneous_content_no);
        }
    }


    private String getVeganContentString(int i) {
        if (i == GastroLocation.OMNIVORE) {
            return getString(R.string.gastro_details_miscellaneous_content_omnivore);
        } else if (i == GastroLocation.OMNIVORE_VEGAN_DECLARED) {
            return getString(R.string.gastro_details_miscellaneous_content_omnivore_vegan_declared);
        } else if (i == GastroLocation.VEGETARIAN) {
            return getString(R.string.gastro_details_miscellaneous_content_vegetarian);
        } else if (i == GastroLocation.VEGETARIAN_VEGAN_DECLARED) {
            return getString(R.string.gastro_details_miscellaneous_content_vegetarian_vegan_declared);
        } else if (i == GastroLocation.VEGAN) {
            return getString(R.string.gastro_details_miscellaneous_content_completely_vegan);
        }
        return getString(R.string.gastro_details_miscellaneous_content_unknown);
    }

    private void addEditing(final View v) {
        final RelativeLayout item = (RelativeLayout) v.findViewById(R.id.editing);

        final ImageView icon = (ImageView) item.findViewById(R.id.icon);
        icon.setImageDrawable(getResources(

        ).getDrawable(R.mipmap.ic_edit_white_24dp));
        icon.setColorFilter(getResources().getColor(R.color.theme_primary));

        final String text = getString(R.string.gastro_details_editing);
        final TextView content = (TextView) item.findViewById(R.id.content);
        content.setText(Html.fromHtml(text));
        content.setTypeface(null, Typeface.ITALIC);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        content.setOnClickListener(new EditingOnClickListener());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(LocationDetailActivity.EXTRA_LOCATION, location);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * see: http://stackoverflow.com/questions/4096851/
     */
    private void stripUnderlines(final TextView textView) {
        Spannable s = (Spannable) textView.getText();
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    /**
     * see: http://stackoverflow.com/questions/4096851/
     */
    public class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(final String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    private class EditingOnClickListener implements View.OnClickListener {
        private static final int NUM_STARS = 25;

        @Override
        public void onClick(View v) {
            final Intent report = new Intent(Intent.ACTION_SENDTO);
            final String uriText = ""
                    + "mailto:" + Uri.encode("bvapp@berlin-vegan.org")
                    + "?subject=" + Uri.encode(getMessageSubject())
                    + "&body=" + Uri.encode(getMessageBody());
            final Uri uri = Uri.parse(uriText);
            report.setData(uri);
            startActivity(Intent.createChooser(report, getString(R.string.gastro_details_editing)));
        }

        private String getMessageSubject() {
            return getString(R.string.error) + ": "
                    + location.getName() + ", "
                    + location.getStreet();
        }

        private String getMessageBody() {
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < NUM_STARS; i++) {
                stars.append("*");
            }
            return stars
                    + "\nApp Version: " + BuildConfig.VERSION_GIT_DESCRIPTION
                    + "\nApp Flavor: " + BuildConfig.FLAVOR
                    + "\nDevice Name: " + Build.MODEL
                    + "\nPlatform: Android"
                    + "\nDevice Version: " + Build.VERSION.RELEASE
                    + "\n" + stars
                    + "\n\n" + getString(R.string.insert_error_report)
                    + "\n\n";
        }
    }
}
