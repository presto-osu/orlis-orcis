package de.phoenixstudios.pc_dimmer;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private Map<Main.PCD_Scene, List<Main.PCD_Scene>> scenelist_collection;
    private List<Main.PCD_Scene> scenes;

    public ExpandableListAdapter(Activity context, List<Main.PCD_Scene> scenes,
                                 Map<Main.PCD_Scene, List<Main.PCD_Scene>> scenelist_collection) {
        this.context = context;
        this.scenelist_collection = scenelist_collection;
        this.scenes = scenes;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return scenelist_collection.get(scenes.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Main.PCD_Scene scene = (Main.PCD_Scene) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.scenelist_child_item, null);
        }

        TextView item = (TextView) convertView.findViewById(R.id.scenelist_child_item);
        if (item!=null) item.setText(scene.Name);

        ImageView startbtn = (ImageView) convertView.findViewById(R.id.startbtn);
        startbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               List<Main.PCD_Scene> child = scenelist_collection.get(scenes.get(groupPosition));
               Main.start_scene(child.get(childPosition).ID);
            }
        });
        ImageView stopbtn = (ImageView) convertView.findViewById(R.id.stopbtn);
        stopbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                List<Main.PCD_Scene> child = scenelist_collection.get(scenes.get(groupPosition));
                Main.stop_scene(child.get(childPosition).ID);
            }

/*
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to remove?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                List<Main.PCD_Scene> child =
                                        scenelist_collection.get(scenes.get(groupPosition));
                                child.remove(childPosition);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
*/
        });

        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        if (scenelist_collection == null) {
            return 0;
        } else {
            if (scenes == null) {
                return 0;
            }else {
                if (scenelist_collection.get(scenes.get(groupPosition))==null) {
                    return 0;
                }else {
                    return scenelist_collection.get(scenes.get(groupPosition)).size();
                }
            }
        }
    }

    public Object getGroup(int groupPosition) {
        return scenes.get(groupPosition);
    }

    public int getGroupCount() {
        return scenes.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Main.PCD_Scene groupitem = (Main.PCD_Scene) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.scenelist_group_item, null);
        }
        TextView itemtext = (TextView) convertView.findViewById(R.id.scenelist_group_itemtext);
        itemtext.setTypeface(null, Typeface.BOLD);
        itemtext.setText(groupitem.Name);

        ImageView itemimage = (ImageView) convertView.findViewById(R.id.scenelist_group_itemicon);

        if (groupitem.ID.equals("1")) itemimage.setImageResource(R.drawable.ic_scene1);
        if (groupitem.ID.equals("2")) itemimage.setImageResource(R.drawable.ic_scene2);
        if (groupitem.ID.equals("3")) itemimage.setImageResource(R.drawable.ic_scene3);
        if (groupitem.ID.equals("4")) itemimage.setImageResource(R.drawable.ic_scene4);
        if (groupitem.ID.equals("5")) itemimage.setImageResource(R.drawable.ic_scene5);
        if (groupitem.ID.equals("6")) itemimage.setImageResource(R.drawable.ic_scene6);
        if (groupitem.ID.equals("7")) itemimage.setImageResource(R.drawable.ic_scene7);
        if (groupitem.ID.equals("8")) itemimage.setImageResource(R.drawable.ic_scene8);
        if (groupitem.ID.equals("9")) itemimage.setImageResource(R.drawable.ic_scene9);
        if (groupitem.ID.equals("10")) itemimage.setImageResource(R.drawable.ic_scene10);
        if (groupitem.ID.equals("11")) itemimage.setImageResource(R.drawable.ic_scene11);
        if (groupitem.ID.equals("12")) itemimage.setImageResource(R.drawable.ic_scene12);

        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
