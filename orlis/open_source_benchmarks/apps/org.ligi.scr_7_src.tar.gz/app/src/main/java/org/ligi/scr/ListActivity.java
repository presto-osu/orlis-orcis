package org.ligi.scr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ligi.scr.model.Day;
import org.ligi.scr.model.Event;
import org.ligi.scr.model.decorated.EventDecorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {

    @Bind(R.id.list_host)
    ViewGroup list_host;

    private List<RecyclerView> recyclers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setIcon(R.drawable.logo);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(new DaySelector(this));

        getSupportActionBar().setSubtitle("Schedule Conflict Resolver");
        ButterKnife.bind(this);

        DateTime earliestEventTime = DateTime.parse(App.conference.days.get(0).date).plusDays(1);
        DateTime latestEventTime = DateTime.parse(App.conference.days.get(0).date);

        for (Day day : App.conference.days) {
            for (ArrayList<Event> events : day.rooms.values()) {
                for (Event event : events) {
                    final DateTime dateTime = new DateTime(event.date);
                    if (dateTime.isBefore(earliestEventTime)) {
                        earliestEventTime = dateTime;
                    }
                    if (dateTime.isAfter(latestEventTime)) {
                        latestEventTime = dateTime;
                    }
                }
            }
        }

        final HashMap<String, ArrayList<Event>> roomToAllEvents = new HashMap<>();

        final Set<String> rooms = App.conference.days.get(0).rooms.keySet();
        for (String room : rooms) {
            DateTime act_time = earliestEventTime;
            final ArrayList<Event> newEventList = new ArrayList<>();
            for (Day day : App.conference.days) {

                for (Event event : day.rooms.get(room)) {
                    final EventDecorator eventDecorator = new EventDecorator(event);

                    if (act_time.isBefore(eventDecorator.getStart())) {
                        final Event breakEvent = new Event();
                        breakEvent.title = "break";
                        breakEvent.date = act_time.toString(ISODateTimeFormat.dateTime());

                        final EventDecorator eventDecorator1 = new EventDecorator(breakEvent);

                        eventDecorator1.setEnd(eventDecorator.getStart());
                        newEventList.add(breakEvent);
                    }
                    act_time = eventDecorator.getEnd();
                    newEventList.add(event);
                }
            }

            if (DateTime.parse(Iterables.getLast(newEventList).date).isBefore(latestEventTime)) {
                final Event event = new Event();
                event.title = "end";
                event.date = DateTime.parse(Iterables.getLast(newEventList).date).toString(ISODateTimeFormat.dateTime());
                new EventDecorator(event).setEnd(latestEventTime);
                newEventList.add(event);
            }
            roomToAllEvents.put(room, newEventList);
        }


        recyclers.clear();
        for (int i = 0; i < rooms.size(); i++) {
            final GridLayoutManager layoutManager1 = new GridLayoutManager(this, 1);
            RecyclerView recycler = (RecyclerView) getLayoutInflater().inflate(R.layout.recycler, list_host, false);
            recycler.setLayoutManager(layoutManager1);
            recycler.setAdapter((new EventAdapter(Iterables.get(roomToAllEvents.values(), i))));

            recyclers.add(recycler);
            recycler.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (recyclerView.getTag(R.id.tag_scroll_sync) != null) {
                        return;
                    }

                    for (RecyclerView recyclerView1 : recyclers) {
                        if (!recyclerView1.equals(recyclerView)) {
                            recyclerView1.setTag(R.id.tag_scroll_sync, true);
                            recyclerView1.scrollBy(dx, dy);
                            recyclerView1.setTag(R.id.tag_scroll_sync, null);
                        }
                    }
                }
            });
            list_host.addView(recycler);
        }

    }

}