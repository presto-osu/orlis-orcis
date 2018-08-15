package gq.nulldev.animeopenings.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import gq.nulldev.animeopenings.app.util.SubtitleSeeker;

/**
 * Project: AnimeOpenings
 * Created: 19/11/15
 * Author: nulldev
 */
public class MediaService extends Service {

    public static final String ACTION_PREV = "gq.nulldev.animeopenings.app.ACTION_PREV";
    public static final String ACTION_PLAYPAUSE = "gq.nulldev.animeopenings.app.ACTION_PLAYPAUSE";
    public static final String ACTION_NEXT = "gq.nulldev.animeopenings.app.ACTION_NEXT";
    public static final String ACTION_EXIT = "gq.nulldev.animeopenings.app.ACTION_EXIT";

    static HttpProxyCacheServer proxyCacheServer;
    static long previousProxyCacheSize = -1;

    MediaPlayer player;
    ArrayList<Video> videos;
    Stack<Video> playedVideos = new Stack<>();
    Video currentVideo = null;
    SubtitleSeeker subtitleSeeker = null;
    SharedPreferences preferences;
    OnMediaPlayerBuiltListener onMediaPlayerBuiltListener;
    Runnable onStopListener = null;
    int playlistIndex = 0;
    boolean paused = false;

    //MediaNotification
    MediaNotification notification;

    public void setupService(ArrayList<Video> videos, SubtitleSeeker subtitleSeeker, SharedPreferences preferences) {
        this.videos = videos;
        this.subtitleSeeker = subtitleSeeker;
        this.preferences = preferences;
    }

    public static HttpProxyCacheServer getProxy(Context context, long size) {
        if(proxyCacheServer == null
                || previousProxyCacheSize == -1
                || size != previousProxyCacheSize) {
            proxyCacheServer = new HttpProxyCacheServer.Builder(context)
                    .maxCacheSize(size)
                    .build();
            previousProxyCacheSize = size;
        }
        return proxyCacheServer;
    }

    public static String proxyURL(Context context, SharedPreferences preferences, String url) {
        if(preferences.getBoolean("prefCacheVideos", false)) {
            HttpProxyCacheServer proxy = getProxy(context,
                    preferences.getInt("prefCacheLimit", 512)*1000000);
            return proxy.getProxyUrl(url);
        } else {
            return url;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if(!action.isEmpty()) {
                switch(action) {
                    case ACTION_PREV:
                        doPrev();
                        break;
                    case ACTION_PLAYPAUSE:
                        doPlayPause();
                        break;
                    case ACTION_NEXT:
                        doNext();
                        break;
                    case ACTION_EXIT:
                        if(getPlayer() != null) {
                            getPlayer().stop();
                            getPlayer().release();
                            player = null;
                            if(onStopListener != null)
                                onStopListener.run();
                            stopSelf();
                        }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    boolean doPrev() {
        boolean result = playPrevVideo();
        updateNotification();
        return result;
    }
    void doPlayPause() {
        if (player != null) {
            if (paused) {
                player.start();
                paused = false;
            } else {
                try {
                    player.pause();
                } catch(IllegalStateException e) {
                    //Cannot pause while player is buffering, just stop the player entirely
                    player.stop();
                }
                paused = true;
            }
        }
        updateNotification();
    }
    void doNext() {
        playNextVideo();
        updateNotification();
    }

    public boolean playPrevVideo() {
        if(playedVideos.size() > 0) {
            playedVideos.pop(); //Pop the current video
            playVideo(playedVideos.peek());
            return true;
        } else {
            return false;
        }
    }

    public void playVideo(final Video vid) {
        subtitleSeeker.deSync(); //Desync the subtitle seeker
        currentVideo = vid;
        //Clear subtitles
        if(player != null) {
            player.reset();
        }
        String url = vid.getFileURL();
        if(preferences.getBoolean("prefAudioOnly", false)) {
            url = "http://omam.nulldev.xyz/" + vid.getFile() + ".ogg";
        }
        Log.i(ActivityNewVideo.TAG, "Playing media: " + url);

        paused = false;

        try {
            if(player == null)
                buildNewMediaPlayer();
            player.setDataSource(this, Uri.parse(proxyURL(this, preferences, url)));
            player.prepareAsync();
        } catch(Exception ignored) {}
        updateNotification();
    }

    public void playNextVideo() {
        Video vid = null;
        boolean handledByPlaylist = false;
        if(preferences.getBoolean("enable_playlist", false)) {
            boolean found = false;
            while(!found) {
                //Disable empty playlist
                //noinspection Convert2Diamond (Doesn't work without it)
                if (preferences.getStringSet("playlist", new HashSet<String>()).size() < 1) {
                    preferences.edit().putBoolean("enable_playlist", false).apply();
                    break;
                } else {
                    //noinspection Convert2Diamond (Doesn't work without it)
                    Set<String> playlistSet = preferences.getStringSet("playlist", new HashSet<String>());
                    String[] playlist = playlistSet.toArray(new String[playlistSet.size()]);
                    if (playlistIndex >= playlist.length) {
                        playlistIndex = 0;
                    }
                    String target = playlist[playlistIndex];
                    playlistIndex++;
                    for (Video video : videos) {
                        if (video.getFile().equals(target)) {
                            vid = video;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        playlistSet.remove(target);
                        preferences
                                .edit()
                                .remove("playlist")
                                .putStringSet("playlist", playlistSet)
                                .apply();
                    }
                }
            }
            if(found) {
                handledByPlaylist = true;
            }
        }
        if(!handledByPlaylist) {
            vid = Video.getRandomVideo(videos);
            switch(preferences.getString("prefVideoType", "all")) {
                case "openings":
                    //Loop till we get an opening
                    while (!vid.getName().toUpperCase(ActivityNewVideo.LOCALE).contains("OPENING")) {
                        vid = Video.getRandomVideo(videos);
                    }
                    break;
                case "endings":
                    //Loop till we get an ending
                    while (!vid.getName().toUpperCase(ActivityNewVideo.LOCALE).contains("ENDING")) {
                        vid = Video.getRandomVideo(videos);
                    }
                    break;
                default:
                    //Do nothing really
                    break;
            }
        }
        playedVideos.push(vid);
        playVideo(vid);
    }

    public void updateNotification() {
//        if(notification != null) {
//            notification.cancel();
//        }
        if(getCurrentVideo() != null) {
            notification = new MediaNotification(this,
                    getCurrentVideo().getName(),
                    getCurrentVideo().getSource(),
                    isPaused());
        }
    }

    MediaPlayer buildNewMediaPlayer() {
        player = new MediaPlayer();
        if(subtitleSeeker != null)
            subtitleSeeker.setPlayer(player);

        if(onMediaPlayerBuiltListener != null) {
            onMediaPlayerBuiltListener.onMediaPlayerBuilt(player);
        }
        updateNotification();
        return player;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public Stack<Video> getPlayedVideos() {
        return playedVideos;
    }

    public void setPlayedVideos(Stack<Video> playedVideos) {
        this.playedVideos = playedVideos;
    }

    public Video getCurrentVideo() {
        return currentVideo;
    }

    public void setCurrentVideo(Video currentVideo) {
        this.currentVideo = currentVideo;
    }

    public boolean isPaused() {
        return paused;
    }

    public OnMediaPlayerBuiltListener getOnMediaPlayerBuiltListener() {
        return onMediaPlayerBuiltListener;
    }

    public void setOnMediaPlayerBuiltListener(OnMediaPlayerBuiltListener onMediaPlayerBuiltListener) {
        this.onMediaPlayerBuiltListener = onMediaPlayerBuiltListener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MediaBinder();
    }

    public interface OnMediaPlayerBuiltListener {
        void onMediaPlayerBuilt(MediaPlayer mediaPlayer);
    }

    public class MediaBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }

    public SubtitleSeeker getSubtitleSeeker() {
        return subtitleSeeker;
    }

    public void setSubtitleSeeker(SubtitleSeeker subtitleSeeker) {
        this.subtitleSeeker = subtitleSeeker;
    }

    public Runnable getOnStopListener() {
        return onStopListener;
    }

    public void setOnStopListener(Runnable onStopListener) {
        this.onStopListener = onStopListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(notification != null) {
            notification.cancel();
        }
    }
}
