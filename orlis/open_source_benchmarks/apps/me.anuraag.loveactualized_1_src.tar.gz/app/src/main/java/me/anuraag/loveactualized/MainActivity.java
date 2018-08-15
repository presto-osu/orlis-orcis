package me.anuraag.loveactualized;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {
    private ViewPager mPager;
    private TextView pageNumber;
    private static final int NUM_PAGES = 41;
    private ArrayList<String> sentences;
    private String[] words;
    private ArrayList<Integer> colors;
    private RelativeLayout background;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sentences = new ArrayList<String>();
        pageNumber = (TextView)findViewById(R.id.textView2);
        colors = new ArrayList<>();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
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
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
    public static class ScreenSlidePageFragment extends android.support.v4.app.Fragment {
        private int position;
        private TextView pageNumber,tview,time;
        private Button myb;
        private RelativeLayout mylayout;
        private String[] words = new String[]{"Set I","1.Given the choice of anyone in the world, whom would you want as a dinner guest?",
                "2. Would you like to be famous? In what way?","3. Before making a telephone call, do you ever rehearse what you are going to say? Why?",
                "4. What would constitute a “perfect” day for you?","5. When did you last sing to yourself? To someone else?","6. If you were able to live to the age of 90 and retain either the mind or body of a 30-year-old for the last 60 years of your life, which would you want?","7. Do you have a secret hunch about how you will die?",
                "8. Name three things you and your partner appear to have in common.","9. For what in your life do you feel most grateful?","10. If you could change anything about the way you were raised, what would it be?",
                "11. Take four minutes and tell your partner your life story in as much detail as possible.", "12. If you could wake up tomorrow having gained any one quality or ability, what would it be?", "Set II","13. If a crystal ball could tell you the truth about yourself, your life, the future or anything else, what would you want to know?",
                "14. Is there something that you’ve dreamed of doing for a long time? Why haven’t you done it?","15. What is the greatest accomplishment of your life?",
                "16. What do you value most in a friendship?","17. What is your most treasured memory?","18. What is your most terrible memory?","19. If you knew that in one year you would die suddenly, would you change anything about the way you are now living? Why?",
                "20. What does friendship mean to you?","21. What roles do love and affection play in your life?","22. Alternate sharing something you consider a positive characteristic of your partner. Share a total of five items.","23. How close and warm is your family? Do you feel your childhood was happier than most other people’s?",
                "24. How do you feel about your relationship with your mother?","Set III","25. Make three true “we” statements each. For instance, “We are both in this room feeling ... “",
                "26. Complete this sentence: “I wish I had someone with whom I could share ... “","27. If you were going to become a close friend with your partner, please share what would be important for him or her to know.","28. Tell your partner what you like about them; be very honest this time, saying things that you might not say to someone you’ve just met.",
                "29. Share with your partner an embarrassing moment in your life.","30. When did you last cry in front of another person? By yourself?","31. Tell your partner something that you like about them already.",
                "32. What, if anything, is too serious to be joked about?","33. If you were to die this evening with no opportunity to communicate with anyone, what would you most regret not having told someone? Why haven’t you told them yet?","34. Your house, containing everything you own, catches fire. After saving your loved ones and pets, you have time to safely make a final dash to save any one item. What would it be? Why?",
                "35. Of all the people in your family, whose death would you find most disturbing? Why?","36. Share a personal problem and ask your partner’s advice on how he or she might handle it. Also, ask your partner to reflect back to you how you seem to be feeling about the problem you have chosen.","Done? Now look into the eyes of your partner silently for 4 minutes"};
        ;

        public static ScreenSlidePageFragment newInstance(int position) {

            ScreenSlidePageFragment f = new ScreenSlidePageFragment();
            Bundle b = new Bundle();
            b.putInt("position", position);

            f.setArguments(b);

            return f;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.viwepager_layout, container, false);
            TextView mytext = (TextView)rootView.findViewById(R.id.textView);
            tview = (TextView)rootView.findViewById(R.id.textView3);
            time = (TextView)rootView.findViewById(R.id.time);
            myb = (Button)rootView.findViewById(R.id.button);
            myb.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CountDownTimer mytimer = new CountDownTimer(240000,1000) {
                        @Override
                        public void onTick(long milliseconds) {
                            int seconds = (int) (milliseconds / 1000) % 60 ;
                            String secs = seconds + "";
                            int minutes = (int) ((milliseconds / (1000*60)) % 60);
                            if(seconds < 10){
                                secs = "0" + secs;
                            }
                            time.setText(minutes + " minutes " + secs + " seconds");
                        }

                        @Override
                        public void onFinish() {
                            AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
                            mydialog.setTitle("Time Up");
                            mydialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                        }
                    };
                    mytimer.start();
                    myb.setVisibility(View.GONE);
                    time.setVisibility(View.GONE);
                }
            });
            pageNumber = (TextView)getActivity().findViewById(R.id.textView2);
            mylayout = (RelativeLayout)rootView.findViewById(R.id.background);
           boolean a =  getArguments().getInt("position") == 0;
            if(a){
                tview.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.nytimes.com/2015/01/11/fashion/modern-love-to-fall-in-love-with-anyone-do-this.html")));
                        return true;
                    }
                });
                myb.setVisibility(View.INVISIBLE);
                time.setVisibility(View.INVISIBLE);

            }else{
                tview.setVisibility(View.GONE);
                mytext.setText(words[getArguments().getInt("position") -1]);
                int random = (int)(Math.random() * 4 + 1);
                switch (random){
                    case 1:  mylayout.setBackgroundColor(0xFF2ecc71);
                        break;
                    case 2:  mylayout.setBackgroundColor(0xFF2c3e50);
                        break;
                    case 3:  mylayout.setBackgroundColor(0xFF9b59b6);
                        break;
                    case 4:  mylayout.setBackgroundColor(0xFF3498db);
                        break;
                    case 5:  mylayout.setBackgroundColor(0xFFe67e22);

                }
                if(getArguments().getInt("position") == 40){
                    myb.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                }else{
                    myb.setVisibility(View.INVISIBLE);
                    time.setVisibility(View.INVISIBLE);

                }
            }

            return rootView;
        }
    }
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            ScreenSlidePageFragment myfrag = ScreenSlidePageFragment.newInstance(position);
            return myfrag;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
