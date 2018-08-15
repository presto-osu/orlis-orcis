package org.itishka.pointim.model.point;

import java.util.List;

public class Post extends PointResult {
    public boolean recommended;//server bug: only in feed
    public boolean editable;//server bug: only in feed
    public boolean subscribed;//server bug: only in feed
    public boolean bookmarked;//server bug: only in feed

    public long uid;//server bug: only in feed

    public PostData post;
    public List<Comment> comments;//only when requesting single post

    public RecommendData rec;//only in feed
    public String comment_id;//if rec!=null
}
