package org.itishka.pointim.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import org.itishka.pointim.model.point.Tag;
import org.itishka.pointim.model.point.TagList;
import org.itishka.pointim.network.PointIm;

/**
 * Created by Tishka17 on 06.08.2015.
 */
public class TagsRequest extends RetrofitSpiceRequest<TagList, PointIm> {

    private final String mUserName;

    public TagsRequest(String userName) {
        super(TagList.class, PointIm.class);
        mUserName = userName;
    }

    @Override
    final public TagList loadDataFromNetwork() throws Exception {
        return removeDuplicates(getService().getTags(mUserName));
    }

    public static TagList removeDuplicates(TagList tags) {
        TagList filteredTags = new TagList();
        for (Tag tag : tags) {
            tag.tag = tag.tag.toLowerCase();
            boolean found = false;
            for (Tag t : filteredTags) {
                if (tag.tag.equals(t.tag)) {
                    found = true;
                    t.count += tag.count;
                    break;
                }
            }
            if (!found) {
                filteredTags.add(tag);
            }
        }
        return filteredTags;
    }

    public String getCacheName() {
        return getClass().getCanonicalName() + "-" + mUserName;
    }
}
