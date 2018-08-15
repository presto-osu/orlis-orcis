package de.phoenixstudios.pc_dimmer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

public class ControlpanelButtonAdapter extends BaseAdapter {
    private Context mContext;

    public ControlpanelButtonAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        int ButtonCount = 16;
        if (Main.mPCD!=null) {
            if (Main.mPCD.ControlpanelButtons != null) {
                ButtonCount = Main.mPCD.ControlpanelButtons.length*Main.mPCD.ControlpanelButtons[0].length;
            }
        }
        return ButtonCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Button b;
        if (convertView == null) {
            b = new Button(mContext);
            b.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, 100));
            b.setPadding(8, 8, 8, 8);
            b.setTextSize(10f); // 8f
        } else {
            b = (Button) convertView;
        }

        if (Main.mPCD!=null) {
            if (Main.mPCD.ControlpanelButtons!=null) {
                double CountX=Main.mPCD.ControlpanelButtons[0].length;
                int Y=(int)Math.round(Math.floor(position/CountX));
                int X=(int)Math.round(((position/CountX)-Y)*CountX);


                int StartColor = Color.rgb(Main.mPCD.ControlpanelButtons[Y][X].R, Main.mPCD.ControlpanelButtons[Y][X].G, Main.mPCD.ControlpanelButtons[Y][X].B);
                int EndColor = Color.rgb(Main.mPCD.ControlpanelButtons[Y][X].R-10, Main.mPCD.ControlpanelButtons[Y][X].G-10, Main.mPCD.ControlpanelButtons[Y][X].B-10);
                final GradientColor gd = new GradientColor(StartColor, EndColor);
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    b.setBackgroundDrawable(Main.NewGradient(gd));
                }else{
                    b.setBackground(Main.NewGradient(gd));
                }
                b.setText(Main.mPCD.ControlpanelButtons[Y][X].Name);

                b.setTag(Main.mPCD.ControlpanelButtons[Y][X]);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Main.click_button(((Main.PCD_ControlpanelButton) view.getTag()).X+1, ((Main.PCD_ControlpanelButton) view.getTag()).Y+1);
                    }
                });
            }
        }else{
            int StartColor = Color.rgb(255, 255, 255);
            int EndColor = Color.rgb(128, 128, 128);
            final GradientColor gd = new GradientColor(StartColor, EndColor);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                b.setBackgroundDrawable(Main.NewGradient(gd));
            }else{
                b.setBackground(Main.NewGradient(gd));
            }
            b.setText("Button " + Integer.toString(position + 1));
        }
        return b;
    }
}