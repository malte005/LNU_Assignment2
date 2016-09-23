package assignment2.md222pv.dv606.assignment2.mp3;

/**
 * A simple class representing MP3 songs (tracks) in a playlist.
 *
 * Created by Oleksandr Shpak in 2013.
 * Ported to Android Studio by Kostiantyn Kucher in 2015.
 * Last modified by Kostiantyn Kucher on 10/09/2015.
 */
public class Song {

    private final String artist;
    private final String album;
    private final String name;
    private final String path;

    private Song previous = null;
    private Song next = null;

    public Song(String artist, String album, String name, String path)
    {
        this.artist = artist;
        this.album = album;
        this.name = name;
        this.path = path;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getName()
    {
        return name;
    }

    public String getPath()
    {
        return path;
    }

    public void setNext(Song song)
    {
        next = song;
    }

    public Song getNext()
    {
        return next;
    }

    public Song getPrevious() {
        return previous;
    }

    public void setPrevious(Song previous) {
        this.previous = previous;
    }

    @Override
    public String toString()
    {
        String result = null;
        if (path != null)
            result = path;

        if (name != null)
            result = name;

        if (artist != null)
            result = artist + " - " + result;

        if (album != null)
            result = result + " (" + album + ")";

        return result;
    }
}
