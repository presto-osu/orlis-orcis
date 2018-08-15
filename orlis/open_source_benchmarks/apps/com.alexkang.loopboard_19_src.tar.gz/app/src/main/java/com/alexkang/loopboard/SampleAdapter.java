package com.alexkang.loopboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class SampleAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Sample> samples;

    public SampleAdapter(Context context, ArrayList<Sample> samples) {
        this.context = context;
        this.samples = samples;
    }

    @Override
    public int getCount() {
        return samples.size();
    }

    @Override
    public Sample getItem(int position) {
        return samples.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Sample sample = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sound_clip_row, parent, false);
        }

        Button record = (Button) convertView.findViewById(R.id.rerecord);
        final Button loopButton = (Button) convertView.findViewById(R.id.loop);
        Button playButton = (Button) convertView.findViewById(R.id.play);

        if (sample.isImported()) {
            record.setText("STOP");
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("normal");
                    sample.stop();
                    loopButton.setSelected(false);
                }
            });
            record.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
        } else {
            record.setText("REC");
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });
            record.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction();

                    if (action == MotionEvent.ACTION_DOWN) {
                        view.setPressed(true);

                        if (((System.nanoTime() - ((MainActivity) context).lastKnownTime) / 1e6) < 300) {
                            ((MainActivity) context).lastKnownTime = System.nanoTime();
                            return false;
                        }

                        ((MainActivity) context).lastKnownTime = System.nanoTime();
                        ((MainActivity) context).startRecording(position);
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        view.setPressed(false);
                        ((MainActivity) context).stopRecording();
                        sample.stop();

                        loopButton.setSelected(false);
                    }

                    return true;
                }
            });
        }

        playButton.setText(sample.getName());
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                if (sample.isLooping()) {
                    loopButton.setSelected(false);
                }
                sample.play(false);
            }
        });

        if (!sample.isLooping()) {
            loopButton.setSelected(false);
        } else {
            loopButton.setSelected(true);
        }
        loopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
                if (!sample.isLooping()) {
                    v.setSelected(true);
                    sample.play(true);
                } else {
                    v.setSelected(false);
                    sample.stop();
                }
            }

        });

        return convertView;
    }

}
