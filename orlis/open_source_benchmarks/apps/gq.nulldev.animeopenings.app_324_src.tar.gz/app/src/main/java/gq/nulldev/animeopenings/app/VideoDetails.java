package gq.nulldev.animeopenings.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Project: AnimeOpenings
 * Created: 06/03/16
 * Author: nulldev
 */
public class VideoDetails {
    String subtitles = null;

    public static VideoDetails fromJSON(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (!object.getBoolean("success")) {
                return null;
            }
            VideoDetails details = new VideoDetails();
            try {
                String subs = object.getString("subtitles");
                if(subs != null && !subs.equals("0")) {
                    details.setSubtitles(subs);
                }
            } catch(JSONException ignored) {}
            return details;
        } catch(JSONException e) {
            return null;
        }
    }

    public String getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(String subtitles) {
        this.subtitles = subtitles;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoDetails that = (VideoDetails) o;

        return !(subtitles != null ? !subtitles.equals(that.subtitles) : that.subtitles != null);
    }

    @Override public int hashCode() {
        return subtitles != null ? subtitles.hashCode() : 0;
    }
}
