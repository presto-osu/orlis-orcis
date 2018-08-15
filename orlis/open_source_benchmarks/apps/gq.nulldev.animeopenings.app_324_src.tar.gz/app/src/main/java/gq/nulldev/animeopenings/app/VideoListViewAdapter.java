package gq.nulldev.animeopenings.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Project: AnimeOpenings
 * Created: 15/11/15
 * Author: nulldev
 */
public class VideoListViewAdapter extends BaseExpandableListAdapter {

    private static final class ViewHolder {
        TextView textLabel;
    }

    private final List<Video.ListSeriesItem> itemList;
    private final LayoutInflater inflater;

    public VideoListViewAdapter(Context context, List<Video.ListSeriesItem> itemList) {
        this.inflater = LayoutInflater.from(context);
        this.itemList = itemList;
    }

    @Override
    public Video.ListVideoItem getChild(int groupPosition, int childPosition) {

        return itemList.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).getChildren().size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;


        if (resultView == null) {

            resultView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(android.R.id.text1);
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        final Video.ListVideoItem item = getChild(groupPosition, childPosition);

        holder.textLabel.setText(item.getVideo().getName());

        if(item.isSelected()) {
            holder.textLabel.setBackgroundColor(Color.GREEN);
        } else {
            holder.textLabel.setBackgroundColor(Color.TRANSPARENT);
        }

        return resultView;
    }

    @Override
    public Video.ListSeriesItem getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
        View resultView = theConvertView;
        ViewHolder holder;

        if (resultView == null) {
            resultView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(android.R.id.text1);
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        final Video.ListSeriesItem item = getGroup(groupPosition);

        holder.textLabel.setText(item.getName());

        int counter = 0;

        for(Video.ListVideoItem item1 : item.getChildren()) {
            if(item1.isSelected()) {
                counter++;
            }
        }

        if(counter == item.getChildren().size()) {
            holder.textLabel.setBackgroundColor(Color.GREEN);
        } else if(counter == 0) {
            holder.textLabel.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.textLabel.setBackgroundColor(Color.YELLOW);
        }

        return resultView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
