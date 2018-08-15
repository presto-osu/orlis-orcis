package koeln.mop.elpeefpe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andreas Streichardt on 19.06.2016.
 */
public class ElpeEfpeTableView extends TableLayout {
    private ImageView[] mImageViews;
    private int[] mIndices;
    private Value mValue;

    private HashMap<DamageType, Drawable> mImages;
    private Drawable mTransparent;

    public ElpeEfpeTableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mImages = new HashMap<>();
        mImages.put(DamageType.KANALISIERT, ResourcesCompat.getDrawable(context.getResources(), R.drawable.dmg_kanalisiert, null));
        mImages.put(DamageType.ERSCHOEPFT, ResourcesCompat.getDrawable(context.getResources(), R.drawable.dmg_erschoepft, null));
        mImages.put(DamageType.VERZEHRT, ResourcesCompat.getDrawable(context.getResources(), R.drawable.dmg_verzehrt, null));
        mTransparent = ResourcesCompat.getDrawable(context.getResources(), R.drawable.dmg_placeholder, null);

        resetTable(1, 1);
    }

    public void setValue(Value value) {
        mValue = value;
        if (value.multiplier == 1) {
            resetTable(10, value.value * value.multiplier);
        } else {
            resetTable(value.value, value.value * value.multiplier);
        }
    }

    public void setDamage(Map<DamageType, Integer> damageMap) {
        for (Map.Entry<DamageType, Integer> entry : damageMap.entrySet()) {
            for (int i=0;i<entry.getValue();i++) {
                addDamage(entry.getKey());
            }
        }
    }

    public void addDamage(DamageType type) {
        // mop: more damage than we can display :S
        if (mIndices[0] >= mImageViews.length) {
            return;
        }
        int index = Arrays.asList(DamageType.ordered).indexOf(type);

        // mop: add a gap
        for (int i=mIndices[0];i>mIndices[index];i--) {
            mImageViews[i].setImageDrawable(mImageViews[i-1].getDrawable());
        }
        mImageViews[mIndices[index]].setImageDrawable(mImages.get(type));
        for (int i=index;i>=0;i--) {
            mIndices[i]++;
        }
    }

    public void removeDamage(DamageType type) {
        int index = Arrays.asList(DamageType.ordered).indexOf(type);
        // aufruecken
        for (int i=mIndices[index] - 1;i<mIndices[0] - 1;i++) {
            mImageViews[i].setImageDrawable(mImageViews[i+1].getDrawable());
        }
        // mop: last image in row => transparent
        mImageViews[mIndices[0] - 1].setImageDrawable(mTransparent);
        for (int i=index;i>=0;i--) {
            mIndices[i]--;
        }
    }

    public void resetTable(int columns, int total) {
        removeAllViews();

        mImageViews = new ImageView[total];
        Log.v("image", "Total " + total + " " + columns + " " + Math.ceil((double)total/(double)columns));
        int current = 0;
        for (int i=0;i<Math.ceil((double)total/(double)columns);i++) {
            TableRow row = new TableRow(getContext());

            for (int j=0;j<columns;j++) {
                RelativeLayout cell = new RelativeLayout(getContext());
                cell.setPadding(1, 1, 1, 1);
                cell.setBackgroundColor(0xff000000);

                ImageView imageView = new ImageView(getContext());
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(layoutParams);

                imageView.setAdjustViewBounds(true);
                imageView.setMaxHeight(60);

                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setPadding(1, 1, 1, 1);
                imageView.setBackgroundColor(0xffffffff);
                imageView.setImageDrawable(mTransparent);

                imageView.requestLayout();
                mImageViews[current++] = imageView;
                cell.addView(imageView);

                row.addView(cell);
                if (current == total) {
                    break;
                }
            }
            this.addView(row);
        }
        mIndices = new int[3];
        mIndices[0] = 0;
        mIndices[1] = 0;
        mIndices[2] = 0;
    }

}
