package com.sevag.unrealtracker;

import android.app.Activity;
import android.os.*;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.sevag.unrealtracker.parser.BlogParser;
import com.sevag.unrealtracker.parser.LiveBroadcastParser;
import com.sevag.unrealtracker.parser.MarketplaceParser;

import java.util.ArrayList;

public class ContentActivity extends Activity {

    private TextView subtitleText, mainText;
    private int whatToDisplay, theme;

    private ArrayList<String> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            whatToDisplay = extras.getInt("display");
            theme = extras.getInt("theme");
        }

        switch (theme) {
            case 0:
                setContentView(R.layout.activity_content1);
                break;
            case 1:
                setContentView(R.layout.activity_content2);
                break;
            case 2:
                setContentView(R.layout.activity_content3);
                break;
            default:
                setContentView(R.layout.activity_content1);
                break;
        }

        mainText = (TextView) findViewById(R.id.mainTextView);
        subtitleText = (TextView) findViewById(R.id.subtitle);
    }

    @Override
    protected void onResume() {
        displayItems();
        super.onResume();
    }

    private void displayItems() {
        switch (whatToDisplay) {
            case 0:
                ImageView title1 = (ImageView) findViewById(R.id.title1);
                title1.setVisibility(View.VISIBLE);
                subtitleText.setText(Html.fromHtml("<a href=" + BlogParser.unrealEngine4BlogUrl + ">blog</a>"));
                subtitleText.setMovementMethod(LinkMovementMethod.getInstance());
                displayList = BlogParser.getUnrealEngine4BlogPosts();
                break;
            case 1:
                ImageView title2 = (ImageView) findViewById(R.id.title2);
                title2.setVisibility(View.VISIBLE);
                subtitleText.setText(Html.fromHtml("<a href=" + BlogParser.unrealTournamentBlogUrl + ">blog</a>"));
                subtitleText.setMovementMethod(LinkMovementMethod.getInstance());
                displayList = BlogParser.getUnrealTournamentBlogPosts();
                break;
            case 2:
                ImageView title3 = (ImageView) findViewById(R.id.title3);
                title3.setVisibility(View.VISIBLE);
                subtitleText.setText(Html.fromHtml("<a href=" + MarketplaceParser.unrealEngine4MarketplaceUrl + ">marketplace</a>"));
                subtitleText.setMovementMethod(LinkMovementMethod.getInstance());
                displayList = MarketplaceParser.getUnrealEngine4MarketplacePosts();
                break;
            case 3:
                ImageView title4 = (ImageView) findViewById(R.id.title4);
                title4.setVisibility(View.VISIBLE);
                subtitleText.setText(Html.fromHtml("<a href=" + LiveBroadcastParser.liveStreamUrl + ">twitch</a>"));
                subtitleText.setMovementMethod(LinkMovementMethod.getInstance());
                displayList = LiveBroadcastParser.getUnrealTournamentBroadcastSchedule();
                break;
            case 4:
                ImageView title5 = (ImageView) findViewById(R.id.title5);
                title5.setVisibility(View.VISIBLE);
                mainText.setText(Html.fromHtml("<a href=http://sevagh.github.io>my github page</a>"));
                mainText.setMovementMethod(LinkMovementMethod.getInstance());
                break;
            default:
                break;
        }

        if (displayList != null) {
            for (String blogPost : displayList) {
                mainText.append(blogPost);
                mainText.append("\n\n");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
