package org.ligi.scr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity {

    @Bind(R.id.helpText)
    TextView helpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setIcon(R.drawable.logo);

        getSupportActionBar().setTitle("Help");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        helpText.setText(Html.fromHtml(getString(R.string.help_html)));
        helpText.setMovementMethod(LinkMovementMethod.getInstance());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
