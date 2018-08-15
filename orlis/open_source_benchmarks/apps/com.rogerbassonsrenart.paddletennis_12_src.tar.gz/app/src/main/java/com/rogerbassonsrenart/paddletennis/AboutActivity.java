package com.rogerbassonsrenart.paddletennis;



import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView t = (TextView) findViewById(R.id.twitter);
        t.setMovementMethod(LinkMovementMethod.getInstance());

        t = (TextView) findViewById(R.id.sourceCode);
        t.setMovementMethod(LinkMovementMethod.getInstance());

        String version = BuildConfig.VERSION_NAME;
        t = (TextView) findViewById(R.id.version);
        t.setText(version);
    }


}
