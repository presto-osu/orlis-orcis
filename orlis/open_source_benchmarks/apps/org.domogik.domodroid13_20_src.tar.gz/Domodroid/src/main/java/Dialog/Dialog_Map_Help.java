package Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import org.domogik.domodroid13.R;

public class Dialog_Map_Help extends Dialog implements View.OnClickListener {
    private final Button okButton;

    public Dialog_Map_Help(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /** Design the dialog in main.xml file */
        setContentView(R.layout.dialog_help);
        okButton = (Button) findViewById(R.id.OkButton);
        okButton.setOnClickListener(this);
    }


    public void onClick(View v) {
        /** When OK Button is clicked, dismiss the dialog */
        if (v == okButton)
            dismiss();
    }
}