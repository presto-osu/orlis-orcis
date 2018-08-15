package org.ligi.scr;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.ligi.axt.AXT;
import org.ligi.scr.model.Event;
import org.ligi.scr.model.decorated.EventDecorator;

import java.util.Set;
import java.util.TreeSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.metadude.java.library.halfnarp.model.GetTalksResponse;

public class EventViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.titleTV)
    TextView titleText;

    @Bind(R.id.abstractTV)
    TextView abstractText;

    @Bind(R.id.speaker)
    TextView speakerText;

    @Bind(R.id.track)
    TextView trackText;

    @Bind(R.id.talkSwitch)
    SwitchCompat talkSwitch;

    @Bind(R.id.shareView)
    View shareView;

    private CardView root;

    public EventViewHolder(CardView itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.root = itemView;
    }

    public void apply(final Event response) {

        final EventDecorator eventDecorator = new EventDecorator(response);

        titleText.setText(response.title + response.room);
        speakerText.setText("" + response.duration);
        abstractText.setText(eventDecorator.getStart().toString(DateTimeFormat.shortTime()) + " " + eventDecorator.getEnd().toString(DateTimeFormat.shortTime()) + " " + response.abstractText);

        final long main = 5 * eventDecorator.getDuration().getStandardMinutes();

        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (main)));
        root.requestLayout();
    }


    public void apply(final GetTalksResponse response) {

        titleText.setText(response.getTitle());
        abstractText.setText(response.getAbstract());
        speakerText.setText(response.getSpeakers());
        trackText.setText(response.getTrackName());

        talkSwitch.setOnCheckedChangeListener(null);

        talkSwitch.setChecked(App.talkIds.getTalkIds().contains(response.getEventId()));

        talkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Set<Integer> talkIds = new TreeSet<>(App.talkIds.getTalkIds());
                if (isChecked) {
                    talkIds.add(response.getEventId());
                } else {
                    talkIds.remove(response.getEventId());
                }
                App.talkIds.clear();
                App.talkIds.add(talkIds);
                App.talkIds.save();
                App.bus.post(new TalkIdsChangeEvent());
            }
        });


        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, response.getTitle());
                intent.putExtra(Intent.EXTRA_TEXT, response.getAbstract());
                intent.setType("text/plain");
                root.getContext().startActivity(intent);
            }
        });

    }

}
