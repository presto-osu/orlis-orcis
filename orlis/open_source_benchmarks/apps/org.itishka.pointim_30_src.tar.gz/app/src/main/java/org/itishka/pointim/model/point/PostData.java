package org.itishka.pointim.model.point;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class PostData {
    public List<String> tags;
    public List<String> files;
    public boolean pinned;
    public int comments_count;
    public User author;
    public TextWithImages text;
    public Date created;
    public Type type;
    public String id;
    @SerializedName("private")
    public boolean isPrivate;

    enum Type {
        post,
        comment
    }
}
