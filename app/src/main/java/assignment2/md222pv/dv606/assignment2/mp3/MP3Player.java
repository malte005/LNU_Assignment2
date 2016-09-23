package assignment2.md222pv.dv606.assignment2.mp3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import assignment2.md222pv.dv606.assignment2.R;

public class MP3Player extends AppCompatActivity {

    private ImageButton btn_play, btn_previous, btn_next;
    private MusicService music_service = null;
    private View.OnClickListener MusicButtonsListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_play:
                    if (music_service.getMediaPlayer().isPlaying()) {
                        pause();
                    } else if (music_service.getCurrentTrack() == null) {
                        Toast.makeText(MP3Player.this, R.string.nothing_played, Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            resume();
                        } catch (Exception e) {
                        }
                    }
                    break;
                case R.id.btn_previous:
                    if (music_service.getCurrentTrack() != null && music_service.getCurrentTrack().getPrevious() != null) {
                        playPrevious();
                    } else if (music_service.getCurrentTrack() == null) {
                        Toast.makeText(MP3Player.this, R.string.nothing_played, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_next:
                    if (music_service.getCurrentTrack() != null && music_service.getCurrentTrack().getNext() != null) {
                        playNext();
                    } else if (music_service.getCurrentTrack() == null) {
                        Toast.makeText(MP3Player.this, R.string.nothing_played, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the layout
        setContentView(R.layout.activity_mp3_player);

        // Initialize the service
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        this.bindService(intent, s_connection, Context.BIND_AUTO_CREATE);

        // Initialize the list of tracks
        final ListView listView = (ListView) findViewById(R.id.list_view);
        final ArrayList<Song> tracks = trackList();

        listView.setAdapter(new PlayListAdapter(this, tracks));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
                music_service.playTrack(tracks.get(pos));
                changePlayBtn(false);
            }
        });

        btn_play = (ImageButton) findViewById(R.id.btn_play);
        btn_play.setOnClickListener(MusicButtonsListener);
        btn_previous = (ImageButton) findViewById(R.id.btn_previous);
        btn_previous.setOnClickListener(MusicButtonsListener);
        btn_next = (ImageButton) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(MusicButtonsListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_mp3_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_player:
                stop();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ServiceConnection s_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName cName, IBinder binder) {
            music_service = ((MusicService.MusicBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName cName) {
            music_service = null;
        }
    };

    /* Unbinding from service */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unbindService(s_connection);
    }


    private class PlayListAdapter extends ArrayAdapter<Song> {
        public PlayListAdapter(Context context, ArrayList<Song> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            Song data = getItem(position);

            row = getLayoutInflater().inflate(R.layout.mp3_row, parent, false);

            TextView name = (TextView) row.findViewById(R.id.name);
            name.setText(String.valueOf(data));
            row.setTag(data);

            return row;
        }
    }

    /**
     * Checks the state of media storage. True if mounted;
     *
     * @return
     */
    private boolean isStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Reads song list from media storage.
     *
     * @return
     */
    private ArrayList<Song> trackList() {
        ArrayList<Song> songs = new ArrayList<Song>();

        if (!isStorageAvailable()) // Check for media storage
        {
            Toast.makeText(this, R.string.nosd, Toast.LENGTH_SHORT).show();
            return songs;
        }

        Cursor music = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.IS_MUSIC + " > 0 ",
                null, null
        );

        if (music.getCount() > 0) {
            music.moveToFirst();
            Song prev = null;
            do {
                Song track = new Song(music.getString(0), music.getString(1), music.getString(2), music.getString(3));

                if (prev != null) {
                    prev.setNext(track);
                    track.setPrevious(prev);
                }

                prev = track;
                songs.add(track);
            }
            while (music.moveToNext());

            prev.setNext(songs.get(0)); // startSong in loop
        }
        music.close();

        return songs;
    }

    private void changePlayBtn(boolean toPlay) {
        if (toPlay) {
            // Change Image of btn to play
            btn_play.setImageResource(R.drawable.ic_play);
        } else {
            // Change Image of btn to pause
            btn_play.setImageResource(R.drawable.ic_pause);
        }
    }

    private void stop() {
        try {
            if (music_service.getMediaPlayer().isPlaying()) {
                music_service.getMediaPlayer().stop(); // stop the current song
                music_service.getMediaPlayer().reset();
                changePlayBtn(true);
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        music_service.stopForeground(true);
        music_service.stopSelf();
    }

    private void pause() {
        System.out.println("IN PAUSE");
        music_service.getMediaPlayer().pause();
        changePlayBtn(true);
    }

    private void resume() {
        System.out.println("IN RESUME");
        music_service.getMediaPlayer().start();
        changePlayBtn(false);
    }

    private void playPrevious() {
        System.out.println("IN PREVIOUS");
        music_service.playTrack(music_service.getCurrentTrack().getPrevious());
        changePlayBtn(false);
    }

    private void playNext() {
        System.out.println("IN NEXT");
        music_service.playTrack(music_service.getCurrentTrack().getNext());
        changePlayBtn(false);
    }

}

