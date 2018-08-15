package org.itishka.pointim.model.point;

import java.util.Date;
import java.util.List;

public class Comment {
    public boolean recommended;//server bug field missed
    public User author;
    public TextWithImages text;
    public Date created;
    public long id;
    public String post_id;
    public String to_comment_id;
    public List<String> files;
    public boolean is_rec;
}
