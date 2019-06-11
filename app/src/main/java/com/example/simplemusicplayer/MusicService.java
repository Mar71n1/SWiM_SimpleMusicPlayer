package com.example.simplemusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int songPosition;
    private String songTitle = "";
    private final IBinder musicBind = new MusicBinder();
    private boolean shuffle = false;
    private Random random;
    private static final int NOTIFY_ID = 1;

    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        mediaPlayer = new MediaPlayer();
        initializeMusicPlayer();
        random = new Random();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private void initializeMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songs) { this.songs = songs; }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(0 < mediaPlayer.getCurrentPosition()) {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        /*Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);*/
    }

    public void setSong(int songIndex) { songPosition = songIndex; }

    public void playSong() {
        mediaPlayer.reset();
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch(Exception ex) {
            Log.e("MUSIC SERVICE", "Error setting data source", ex);
        }
        mediaPlayer.prepareAsync();
    }

    public void playPrev() {
        songPosition--;
        if(songPosition < 0) songPosition = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        if(shuffle) {
            int newSong = songPosition;
            while(newSong == songPosition) newSong = random.nextInt(songs.size());
            songPosition = newSong;
        } else {
            songPosition++;
            if(songs.size() == songPosition) songPosition = 0;
        }
        playSong();
    }

    public int getPosition() { return mediaPlayer.getCurrentPosition(); }
    public int getDuration() { return mediaPlayer.getDuration(); }
    public boolean isPng() { return mediaPlayer.isPlaying(); }
    public void pausePlayer() { mediaPlayer.pause(); }
    public void seek(int position) { mediaPlayer.seekTo(position); }
    public void go() { mediaPlayer.start(); }

    public void setShuffle() {
        if(shuffle) shuffle = false;
        else shuffle = true;
    }
}
