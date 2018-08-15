package com.saladdressing.veterondo.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.widget.ImageView;


public class DrawableTinter {

    public DrawableTinter() {

    }

    /**
     * @param context Context
     * @param imageView ImageView to be received and returned as a Drawable
     * @param color Desired color
     * @return A Drawable implementation that could be a ColorDrawable, a ShapeDrawable or a GradientDrawable
     */
    public Drawable setDrawableColor(Context context, ImageView imageView, String color) {


        if (imageView.getDrawable() instanceof ShapeDrawable) {

            Log.e("TAG", " shape");
            ShapeDrawable shapeDrawable;
            shapeDrawable = (ShapeDrawable) imageView.getDrawable();
            shapeDrawable.getPaint().setColor(Color.parseColor(color));
            return shapeDrawable;

        } else if (imageView.getDrawable() instanceof ColorDrawable) {

            Log.e("TAG", " color");

            ColorDrawable colorDrawable;
            colorDrawable = (ColorDrawable) imageView.getDrawable();
            colorDrawable.setColor(Color.parseColor(color));
            colorDrawable.setAlpha(1);
            return colorDrawable;
        } else if (imageView.getDrawable() instanceof GradientDrawable) {
            GradientDrawable gradientDrawable;
            gradientDrawable = (GradientDrawable) imageView.getDrawable();
            gradientDrawable.setColor(Color.parseColor(color));
            return gradientDrawable;
        } else {
            Log.e(DrawableTinter.class.getSimpleName(), imageView.getDrawable().getClass().getSimpleName());

            return null;
        }

    }

}
