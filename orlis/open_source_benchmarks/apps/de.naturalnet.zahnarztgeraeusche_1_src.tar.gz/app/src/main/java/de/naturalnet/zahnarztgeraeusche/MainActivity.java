package de.naturalnet.zahnarztgeraeusche;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    private class SoundListAdapter extends BaseAdapter {
        private ArrayList<String> items = new ArrayList<String>();
        private LayoutInflater inflater;

        public SoundListAdapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            items.add(item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int pos) {
            return items.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View v, ViewGroup p) {
            ViewHolder holder = null;
            if (v == null) {
                v = inflater.inflate(R.layout.atom_item, null);
                holder = new ViewHolder();
                holder.btn = (ToggleButton) v.findViewById(R.id.toggleButton);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            holder.btn.setTextOn(items.get(pos));
            holder.btn.setTextOff(items.get(pos));
            holder.btn.setText(items.get(pos));
            return v;
        }

        public class ViewHolder {
            public ToggleButton btn;
        }
    }

    SoundListAdapter adapter;
    Hashtable<String, MediaPlayer> players;

    ListView lstSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = new SoundListAdapter();
        players = new Hashtable<String, MediaPlayer>();

        for (Field f : R.raw.class.getFields()) {
            try {
                players.put(f.getName(), MediaPlayer.create(this, f.getInt(f)));
                players.get(f.getName()).setLooping(true);
                adapter.addItem(f.getName());
            } catch (IllegalAccessException e) {
                // FIXME Do something useful
            }
        }

        lstSounds = (ListView) findViewById(R.id.lstSounds);
        lstSounds.setAdapter(adapter);
    }

    public void btnSoundOnClickHandler(View v) {
        ToggleButton btn = (ToggleButton) v.findViewById(R.id.toggleButton);
        toggleSound(btn.getTextOn().toString());
    }

    public void toggleSound(String name) {
        MediaPlayer player = players.get(name);

        if (! player.isPlaying()) {
            player.start();
        } else {
            player.pause();
            player.seekTo(0);
        }
    }
}
