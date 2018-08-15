package com.saladdressing.veterondo.adapters;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.saladdressing.veterondo.R;
import com.saladdressing.veterondo.pojos.Dot;
import com.saladdressing.veterondo.utils.Constants;
import com.saladdressing.veterondo.utils.DrawableTinter;
import com.saladdressing.veterondo.utils.SPS;

import java.util.ArrayList;
import java.util.Random;


public class GridDotAdapter extends BaseAdapter {

    private final static Handler handler = new Handler();

    ArrayList<Dot> mDots;
    Context mContext;
    DrawableTinter tinter = new DrawableTinter();
    Random rand = new Random();
    SPS sps;

    LayoutInflater inflater;

    public GridDotAdapter(Context context, ArrayList<Dot> dots) {
        mContext = context;
        mDots = dots;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sps = new SPS(mContext);
    }


    @Override
    public int getCount() {
        return mDots.size();
    }

    @Override
    public Object getItem(int position) {
        return mDots.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position * 3 - 11;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        /*
        You will notice that the inflation of views if unconditional. HAVE MERCY ON MY SOUL!

        Doesn't make sense for it to be conditional anyway, since all of the views in the GridView
        will be visible on the screen at the same time.

        Please someone correct me if I'm wrong!
         */
        final View v = inflater.inflate(R.layout.dot_cell, parent, false);

        final ImageView dot = (ImageView) v.findViewById(R.id.dot_element);

        dot.setImageDrawable(tinter.setDrawableColor(mContext, dot, mDots.get(position).getColor()));

        // Set animator type according to data sent from MainActivity

        if (sps.getPrefs().getBoolean(Constants.IS_RAINY, false)) {
            rainAnimator(dot);
        } else if (sps.getPrefs().getBoolean(Constants.IS_WINDY, false)) {
            windAnimator(dot);
        } else {
            long randTime = rand.nextInt(5000);


            if (randTime > 2000) {
                dotPulseAnimator(dot, 300, 200, randTime);
            }

            if (randTime < 2000) {
                dotPulseAnimator2(dot, 100, 200, randTime);
            }
        }


        return v;

    }

    // Give activity a way to remove Handler callbacks!
    public void removeHandlerCallbacks() {
        handler.removeCallbacksAndMessages(null);
    }

    // When it rains
    public void rainAnimator(final ImageView iv) {
        handler.post(new MyRunnableThree(iv));
    }

    // When it's windy
    public void windAnimator(final ImageView iv) {
        handler.post(new MyRunnableFour(iv));
    }

    // When it's... anything else.
    public void dotPulseAnimator(final ImageView iv, final long expansion, final long contraction, final long pause) {


        handler.post(new MyRunnableOne(iv, expansion, contraction, pause));

    }

    // Ditto.
    public void dotPulseAnimator2(final ImageView iv, final long expansion, final long contraction, final long pause) {


        handler.post(new MyRunnableTwo(iv, expansion, contraction, pause));

    }

    private static final class MyRunnableOne implements Runnable {

        final ImageView iv;
        long expansion;
        long contraction;
        long pause;

        public MyRunnableOne(final ImageView iv, final long expansion, final long contraction, final long pause) {
            this.iv = iv;
            this.expansion = expansion;
            this.contraction = contraction;
            this.pause = pause;
        }

        public void run() {

            Log.i("Pulse Runnable ONE ", "running...");

            iv.animate().setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    iv.animate().setDuration(expansion).scaleX(1.0f).scaleY(1.0f).setListener(null).start();
                    iv.clearAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).scaleX(0.5f).scaleY(0.5f).setDuration(contraction).start();

            handler.removeCallbacksAndMessages(this);

            handler.postDelayed(this, pause);

        }
    }

    private static final class MyRunnableTwo implements Runnable {

        final ImageView iv;
        long expansion;
        long contraction;
        long pause;

        public MyRunnableTwo(final ImageView iv, final long expansion, final long contraction, final long pause) {
            this.iv = iv;
            this.expansion = expansion;
            this.contraction = contraction;
            this.pause = pause;
        }

        public void run() {

            Log.i("Pulse runnable TWO ", "running...");

            iv.animate().setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    iv.animate().setDuration(expansion).scaleX(1.0f).scaleY(1.0f).setListener(null).start();
                    iv.clearAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).scaleX(1.2f).scaleY(1.2f).setDuration(contraction).start();

            handler.removeCallbacksAndMessages(this);
            handler.postDelayed(this, 700);

        }
    }

    private static final class MyRunnableThree implements Runnable {

        final ImageView iv;


        public MyRunnableThree(final ImageView iv) {
            this.iv = iv;

        }

        public void run() {

            Log.i("Runnable THREE ", "running...");

            Random random = new Random();
            iv.animate().setDuration(400).alpha(0.0f).setStartDelay(random.nextInt(4000)).scaleY(1.7f).translationY(100).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    iv.animate().setDuration(0).translationY(0).alpha(1.0f).scaleY(1.0f).setListener(null).start();
                    iv.clearAnimation();


                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();

            handler.postDelayed(this, 4000);

        }
    }

    private static final class MyRunnableFour implements Runnable {

        final ImageView iv;


        public MyRunnableFour(final ImageView iv) {
            this.iv = iv;

        }

        public void run() {

            Log.i("Runnable FOUR ", "running...");


            Random random = new Random();
            iv.animate().setDuration(900).alpha(0.0f).setStartDelay(random.nextInt(800)).scaleY(1.0f).translationX(100).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    iv.animate().setDuration(0).alpha(1.0f).translationX(0).scaleY(1.0f).setListener(null).start();
                    iv.clearAnimation();


                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();

            handler.postDelayed(this, 2000);

        }
    }


}
