package org.itishka.pointim.listeners;

import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.Post;

/**
 * Created by Tishka17 on 08.05.2016.
 */
public interface OnCommentChangedListener {

    void onCommentChanged(Post post, Comment comment);

    void onCommentDeleted(Post post, Comment comment);
}
