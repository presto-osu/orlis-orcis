package com.alexcruz.papuhwalls.Live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alexcruz.papuhwalls.R;

import java.util.ArrayList;

/**
 * Created by Daniel Huber on 22.12.2015.
 */

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<WallItem> walls;

    public GridAdapter(Context context, ArrayList<WallItem> walls){
        super();
        this.mContext = context;
        this.walls = walls;
    }

    @Override
    public int getCount() {
        return walls.size();
    }

    @Override
    public WallItem getItem(int position) {
        return walls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemChecked(int position, boolean checked){
        walls.get(position).setChecked(checked);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View squareItem = convertView;

        final WallsHolder holder;
        if (squareItem == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            squareItem = inflater.inflate(R.layout.wallpaper_item_manager, parent, false);
            holder = new WallsHolder(squareItem);
            squareItem.setTag(holder);
        } else {
            holder = (WallsHolder) squareItem.getTag();
        }

        boolean isChecked = walls.get(position).isChecked();
        holder.selection.setChecked(isChecked);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        final int imageWidth = (int) (width / 3);

        if(walls.size() > 0) {

            final WallsHolder finalHolder = holder;
            new AsyncTask<Void, Void, Void>() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bmp = null;

                @Override
                protected Void doInBackground(Void... params) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(walls.get(position).getPath(), options);
                    options.inSampleSize = calculateInSampleSize(options, imageWidth, imageWidth);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    if(position < walls.size()) {
                        bmp = BitmapFactory.decodeFile(walls.get(position).getPath(), options);
                        bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 2, bmp.getHeight() / 2, false);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    finalHolder.wall.setImageBitmap(bmp);
                }
            }.execute();
        }

        return squareItem;
    }

    public ArrayList<WallItem> getUncheckedItems(){
        ArrayList<WallItem> uncheckedItems = new ArrayList<>();

        for(WallItem wall : walls)
            if(!wall.isChecked())
                uncheckedItems.add(wall);

        return uncheckedItems;
    }

    class WallsHolder {
        ImageView wall;
        CheckBox selection;

        WallsHolder(View v) {
            wall = (ImageView) v.findViewById(R.id.wall);
            selection = (CheckBox) v.findViewById(R.id.selectionCb);
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
