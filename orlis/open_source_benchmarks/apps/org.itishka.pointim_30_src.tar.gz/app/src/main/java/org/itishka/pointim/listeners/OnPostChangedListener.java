package org.itishka.pointim.listeners;

import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.Post;

/**
 * Created by Tishka17 on 04.05.2016.
 */
public interface OnPostChangedListener {
    void onChanged(Post post);

    void onDeleted(Post post);
}
