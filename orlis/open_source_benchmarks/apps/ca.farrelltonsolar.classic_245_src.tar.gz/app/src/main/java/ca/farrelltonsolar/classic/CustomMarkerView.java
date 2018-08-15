package ca.farrelltonsolar.classic;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by Me on 5/14/2016.
 */
public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e.getData() != null ) {
            RegisterName reg = RegisterName.valueOf(e.getData().toString());
            if (RegisterName.ChargeState == reg) {
                float undcoddedState = e.getVal() * 10;
                tvContent.setText(MonitorApplication.getChargeStateTitleText((int) undcoddedState));
            }
            else if (RegisterName.BatVoltage == reg){
                tvContent.setText("" + e.getVal()+"V"); // set the entry-value as the display text
            }
            else {
                tvContent.setText("" + e.getVal()+"A"); // set the entry-value as the display text
            }
        }
        else {
            tvContent.setText("" + e.getVal()); // set the entry-value as the display text
        }
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }
}