package gq.nulldev.animeopenings.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

/**
 * Project: AnimeOpenings
 * Created: 15/11/15
 * Author: nulldev
 */
public class ActivityPlaylistBuilder extends Activity {

    VideoListViewAdapter videoListViewAdapter;
    ExpandableListView expandableListView;
    List<Video.ListSeriesItem> sortedVideos = null;
    List<Video.ListSeriesItem> displayingVideos = null;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlistbuilder);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        expandableListView = (ExpandableListView) findViewById(R.id.vidList);
    }

    @Override
    protected void onPause() {
        super.onPause();

        HashSet<String> toPut = new HashSet<>();
        for(Video.ListSeriesItem listSeriesItem : sortedVideos) {
            for(Video.ListVideoItem videoItem : listSeriesItem.getChildren()) {
                if(videoItem.isSelected()) {
                    toPut.add(videoItem.getVideo().getFile());
                }
            }
        }

        sharedPreferences.edit().remove("playlist").putStringSet("playlist", toPut).apply();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        sortedVideos = Video.sortVideos(ActivityNewVideo.INSTANCE.videos);
        displayingVideos = new ArrayList<>();
        displayingVideos.addAll(sortedVideos);

        videoListViewAdapter = new VideoListViewAdapter(this, displayingVideos);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Video.ListVideoItem item = videoListViewAdapter.getChild(groupPosition, childPosition);
                item.setIsSelected(!item.isSelected());
                updateUI();
                return true;
            }
        });
        expandableListView.setAdapter(videoListViewAdapter);

        findViewById(R.id.btnSelNone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Video.ListSeriesItem video : sortedVideos) {
                    for(Video.ListVideoItem videoItem : video.getChildren()) {
                        videoItem.setIsSelected(false);
                    }
                }
                updateUI();
            }
        });

        findViewById(R.id.btnSelAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Video.ListSeriesItem video : sortedVideos) {
                    for(Video.ListVideoItem videoItem : video.getChildren()) {
                        videoItem.setIsSelected(true);
                    }
                }
                updateUI();
            }
        });

        ((EditText) findViewById(R.id.searchText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateShowingElements();
            }
        });

        findViewById(R.id.btnUseRegex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateShowingElements();
            }
        });

        ToggleButton playlistToggle = ((ToggleButton) findViewById(R.id.btnEnablePlaylist));

        playlistToggle.setChecked(sharedPreferences.getBoolean("enable_playlist", false));

        playlistToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean("enable_playlist", isChecked).apply();
                updateUIForPlaylistState(isChecked);
            }
        });

        //noinspection Convert2Diamond (doesn't work without it)
        Set<String> oldPlaylist = sharedPreferences.getStringSet("playlist", new HashSet<String>());

        for (Video.ListSeriesItem seriesItem : sortedVideos) {
            for (Video.ListVideoItem videoItem : seriesItem.getChildren()) {
                videoItem.setIsSelected(oldPlaylist.contains(videoItem.getVideo().getFile()));
            }
        }

        updateUIForPlaylistState(playlistToggle.isChecked());
    }

    void updateShowingElements() {
        String trim = "";
        if (((EditText) findViewById(R.id.searchText)).getText() != null) {
            trim = ((EditText) findViewById(R.id.searchText)).getText().toString().trim();
        }
        if (trim.isEmpty()) {
            displayingVideos.clear();
            displayingVideos.addAll(sortedVideos);
        } else {
            displayingVideos.clear();
            boolean useRegex = ((ToggleButton) findViewById(R.id.btnUseRegex)).isChecked();
            if (!useRegex) {
                String[] split = trim
                        .toLowerCase(ActivityNewVideo.LOCALE)
                        .replace("-", " ")
                        .replace("_", " ")
                        .replace("*", " ")
                        .replace("☆", " ") //:P
                        .replace("\n", " ")
                        .split(" ");
                displayingVideos.addAll(sortedVideos);
                for (Video.ListSeriesItem listSeriesItem : sortedVideos) {
                    for (String splitItem : split) {
                        if (!listSeriesItem.getName().toLowerCase(ActivityNewVideo.LOCALE).contains(splitItem)) {
                            displayingVideos.remove(listSeriesItem);
                        }
                    }
                }
            } else {
                try {
                    for (Video.ListSeriesItem listSeriesItem : sortedVideos) {
                        if (listSeriesItem.getName().matches(trim)) displayingVideos.add(listSeriesItem);
                    }
                } catch (PatternSyntaxException ignored) {} //If the user is being an idiot, just ignore him/her
            }
        }
        updateUI();
    }

    void updateUI() {
        videoListViewAdapter.notifyDataSetChanged();
        videoListViewAdapter.notifyDataSetInvalidated();
        expandableListView.invalidate();
    }

    void updateUIForPlaylistState(boolean isEnabled) {
        findViewById(R.id.btnSelNone).setEnabled(isEnabled);
        findViewById(R.id.btnSelAll).setEnabled(isEnabled);
        findViewById(R.id.btnUseRegex).setEnabled(isEnabled);
        findViewById(R.id.vidList).setEnabled(isEnabled);
        displayingVideos.clear();
        if(isEnabled) {
            displayingVideos.addAll(sortedVideos);
        }
        findViewById(R.id.searchText).setEnabled(isEnabled);
        updateUI();
    }
}
