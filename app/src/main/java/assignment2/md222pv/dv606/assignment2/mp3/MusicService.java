package assignment2.md222pv.dv606.assignment2.mp3;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import assignment2.md222pv.dv606.assignment2.R;

public class MusicService extends Service {

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private final IBinder binder = new MusicBinder();
    Runnable work = new Runnable() {
        public void run() {
        }
    };

    private Song currentTrack;

    @Override
    public void onCreate() {
        Thread musicThread = new Thread(null, work, "Play Music Background");
        musicThread.start();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Song getCurrentTrack() {
        return currentTrack;
    }

    private void goNotification(String songName) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MP3Player.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification not = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.not_title))
                .setTicker(getResources().getString(R.string.not_title))
                .setContentText(getResources().getString(R.string.not_songPlayed) + " " + songName)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        startForeground(1235, not);
    }

    public void playTrack(final Song song) {
        if (song == null) {
            return;
        } else {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop(); // stop the current song

                // reset the resource of player
                mediaPlayer.reset();
                // set the song to play
                mediaPlayer.setDataSource(this, Uri.parse(song.getPath()));
                // select the audio stream
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                // prepare the resource ASYNC as prepare() might take to long. Call start() in onPrepared-method
                mediaPlayer.prepareAsync();
                // make display stay awake
                mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                // handle the next track
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playTrack(song.getNext());
                    }
                });

                currentTrack = song;
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        goNotification(currentTrack.getName());
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}