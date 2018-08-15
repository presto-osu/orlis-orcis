package activities;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

public class Gradients_Manager {

    public static Drawable LoadDrawable(final String profile, final int height) {
        final int[] gradient_white = {
                Color.parseColor("#88999999"),
                Color.parseColor("#88999999"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#DDDDDD")};
        final int[] gradient_black = {
                Color.parseColor("#88999999"),
                Color.parseColor("#88999999"),
                Color.parseColor("#1A1A1A"),
                Color.parseColor("#222222"),
                Color.parseColor("#171717"),
                Color.parseColor("#000000")};
        final int[] gradient_ltblack = {
                Color.parseColor("#88999999"),
                Color.parseColor("#88999999"),
                Color.parseColor("#2A2A2A"),
                Color.parseColor("#333333"),
                Color.parseColor("#272727"),
                Color.parseColor("#111111")};
        final int[] gradiant_title = {
                Color.parseColor("#222222"),
                Color.parseColor("#000000"),
                Color.parseColor("#FF222222"),
                Color.parseColor("#00222222")};


        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {

            @Override
            public Shader resize(int width, int height) {
                final float[] gradient_white_params = {0, ((float) 5 / (float) height), ((float) 5 / (float) height), 1};
                final float[] gradient_black_params = {0, ((float) 5 / (float) height), ((float) 5 / (float) height), 0.5f, 0.5f, 1};
                final float[] gradient_title_params = {0, 0.85f, 0.85f, 1};

                LinearGradient lg = null;
                switch (profile) {
                    case "white":
                        lg = new LinearGradient(0, 0, 0, height, gradient_white, gradient_white_params, Shader.TileMode.REPEAT);
                        break;
                    case "black":
                        lg = new LinearGradient(0, 0, 0, height, gradient_black, gradient_black_params, Shader.TileMode.REPEAT);
                        break;
                    case "ltblack":
                        lg = new LinearGradient(0, 0, 0, height, gradient_ltblack, gradient_black_params, Shader.TileMode.REPEAT);
                        break;
                    case "title":
                        lg = new LinearGradient(0, 0, 0, height, gradiant_title, gradient_title_params, Shader.TileMode.REPEAT);
                        break;
                }
                return lg;
            }
        };

        PaintDrawable p = new PaintDrawable();
        p.setDither(true);
        p.setShape(new RectShape());
        if (profile.equals("white") || profile.equals("black") || profile.equals("ltblack"))
            p.setCornerRadius(5);
        p.setShaderFactory(sf);
        return p;
    }
}
