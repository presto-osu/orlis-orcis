package com.github.dfa.diaspora_android.data;

import com.github.dfa.diaspora_android.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by gregor on 05.06.16.
 */
public class PodAspect {
    public long id;
    public String name;
    public boolean selected;

    public PodAspect(long id, String name, boolean selected) {
        this.id = id;
        this.name = name;
        this.selected = selected;
    }


    public PodAspect(String shareabletext) {
        // fromShareAbleText
        String[] str = shareabletext.split("%");
        selected = Integer.parseInt(str[0]) == 1;
        id = Long.parseLong(str[1]);
        name = shareabletext.substring(shareabletext.indexOf(str[1]) + str[1].length() + 1);
    }

    public PodAspect(JSONObject json) throws JSONException {
        if (json.has("id")) {
            id = json.getLong("id");
        }
        if (json.has("name")) {
            name = json.getString("name");
        }
        if (json.has("selected")) {
            selected = json.getBoolean("selected");
        }
    }

    public String toJsonString() {
        JSONObject j = new JSONObject();
        try {
            j.put("id", id);
            j.put("name", name);
            j.put("selected", selected);
        } catch (JSONException e) {/*Nothing*/}
        return j.toString();
    }

    public String toHtmlLink(final App app) {
        final AppSettings appSettings = app.getSettings();
        return String.format(Locale.getDefault(),
                "<a href='https://%s/aspects?a_ids[]=%d' style='color: #000000; text-decoration: none;'>%s</a>",
                appSettings.getPodDomain(), id, name);
    }

    @Override
    public String toString() {
        return toShareAbleText();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PodAspect && ((PodAspect) o).id == id;
    }

    public String toShareAbleText() {
        return String.format(Locale.getDefault(), "%d%%%d%%%s", selected ? 1 : 0, id, name);
    }
}
