package elegion.com.roomdatabase;

import android.arch.persistence.room.Room;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import elegion.com.roomdatabase.database.Album;
import elegion.com.roomdatabase.database.AlbumSong;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.MusicDatabase;
import elegion.com.roomdatabase.database.Song;

public class MusicProvider extends ContentProvider {

    private static final String TAG = MusicProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.elegion.roomdatabase.musicprovider";

    private static final String TABLE_ALBUM = "album";
    private static final String TABLE_SONG = "song";
    private static final String TABLE_ALBUM_SONG = "albumsong";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ALBUM_TABLE_CODE = 100;
    private static final int ALBUM_ROW_CODE = 101;

    private static final int SONG_TABLE_CODE = 200;
    private static final int SONG_ROW_CODE = 201;

    private static final int ALBUM_SONG_TABLE_CODE = 300;
    private static final int ALBUM_SONG_ROW_CODE = 301;

    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM, ALBUM_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM + "/*", ALBUM_ROW_CODE);

        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG, SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_SONG + "/*", SONG_ROW_CODE);

        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG, ALBUM_SONG_TABLE_CODE);
        URI_MATCHER.addURI(AUTHORITY, TABLE_ALBUM_SONG + "/*", ALBUM_SONG_ROW_CODE);
    }

    private MusicDao mMusicDao;

    public MusicProvider() {
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mMusicDao = Room.databaseBuilder(getContext().getApplicationContext(), MusicDatabase.class, "music_database")
                    .build()
                    .getMusicDao();
            return true;
        }

        return false;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ALBUM_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_ALBUM;
            case ALBUM_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_ALBUM;

            case SONG_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_SONG;
            case SONG_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_SONG;

            case ALBUM_SONG_TABLE_CODE:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + TABLE_ALBUM_SONG;
            case ALBUM_SONG_ROW_CODE:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + TABLE_ALBUM_SONG;

            default:
                throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int code = URI_MATCHER.match(uri);

        if (code != ALBUM_ROW_CODE && code != ALBUM_TABLE_CODE
                && code != SONG_ROW_CODE && code != SONG_TABLE_CODE
                && code != ALBUM_SONG_ROW_CODE && code != ALBUM_SONG_TABLE_CODE) return null;


        switch (code) {
            case ALBUM_TABLE_CODE:
                return mMusicDao.getAlbumsCursor();
            case ALBUM_ROW_CODE:
                return mMusicDao.getAlbumWithIdCursor((int) ContentUris.parseId(uri));

            case SONG_TABLE_CODE:
                return mMusicDao.getSongsCursor();
            case SONG_ROW_CODE:
                return mMusicDao.getSongsCursorWithIdCursor((int) ContentUris.parseId(uri));

            case ALBUM_SONG_TABLE_CODE:
                return mMusicDao.getAlbumSongsCursor();
            case ALBUM_SONG_ROW_CODE:
                return mMusicDao.getAlbumSongsCursorWithIdCursor((int) ContentUris.parseId(uri));

            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        switch (URI_MATCHER.match(uri)) {
            case ALBUM_TABLE_CODE:
                if (isInsertAlbumValuesValid(values)) {
                    Album album = new Album();
                    Integer idAlbum = values.getAsInteger("id");
                    album.setId(idAlbum);
                    album.setName(values.getAsString("name"));
                    album.setReleaseDate(values.getAsString("release"));
                    mMusicDao.insertAlbum(album);
                    return ContentUris.withAppendedId(uri, idAlbum);
                } else throw new IllegalArgumentException("cant insert albums");

            case SONG_TABLE_CODE:
                if (isInsertSongValuesValid(values)) {
                    Song song = new Song();
                    Integer idSong = values.getAsInteger("id");
                    song.setId(idSong);
                    song.setName(values.getAsString("name"));
                    song.setDuration(values.getAsString("duration"));
                    mMusicDao.insertSong(song);
                    return ContentUris.withAppendedId(uri, idSong);
                } else throw new IllegalArgumentException("cant insert songs");

            case ALBUM_SONG_TABLE_CODE:
                if (isInsertAlbumSongValuesValid(values)) {
                    AlbumSong albumSong = new AlbumSong();
                    //Integer idAlbumSong = values.getAsInteger("id");
                    //albumSong.setId(idAlbumSong);
                    albumSong.setAlbumId(values.getAsInteger("album_id"));
                    albumSong.setSongId(values.getAsInteger("song_id"));
                    long idAlbumSong = mMusicDao.setLinkAlbumSongs(albumSong);
                    return ContentUris.withAppendedId(uri, idAlbumSong);
                } else throw new IllegalArgumentException("cant insert albumsongs");

            default:
                throw new IllegalArgumentException("cant update items");
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        switch (URI_MATCHER.match(uri)) {
            case ALBUM_ROW_CODE:
                if(isUpdateAlbumValuesValid(values)) {
                    Album album = new Album();
                    int idAlbum = (int) ContentUris.parseId(uri);
                    album.setId(idAlbum);
                    album.setName(values.getAsString("name"));
                    album.setReleaseDate(values.getAsString("release"));
                    return mMusicDao.updateAlbumInfo(album);
                } else throw new IllegalArgumentException("cant update albums");

            case SONG_ROW_CODE:
                if(isUpdateSongValuesValid(values)) {
                    Song song = new Song();
                    int id = (int) ContentUris.parseId(uri);
                    song.setId(id);
                    song.setName(values.getAsString("name"));
                    song.setDuration(values.getAsString("duration"));
                    return mMusicDao.updateSongInfo(song);
                } else throw new IllegalArgumentException("cant update songs");

            case ALBUM_SONG_ROW_CODE:
                if(isUpdateAlbumSongValuesValid(values)) {
                    AlbumSong albumSong = new AlbumSong();
                    int idAlbumSong = (int) ContentUris.parseId(uri);
                    albumSong.setId(idAlbumSong);
                    albumSong.setAlbumId(values.getAsInteger("album_id"));
                    albumSong.setSongId(values.getAsInteger("song_id"));
                    return mMusicDao.updateAlbumSongInfo(albumSong);
                } else throw new IllegalArgumentException("cant update albumSongs");

            default:
                throw new IllegalArgumentException("cant update items");
        }


    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (URI_MATCHER.match(uri)) {
            case ALBUM_ROW_CODE:
                int idAlbum = (int) ContentUris.parseId(uri);
                return mMusicDao.deleteAlbumById(idAlbum);

            case SONG_ROW_CODE:
                int idSong = (int) ContentUris.parseId(uri);
                return mMusicDao.deleteSongById(idSong);

            case ALBUM_SONG_ROW_CODE:
                int idAlbumSong = (int) ContentUris.parseId(uri);
                return mMusicDao.deleteAlbumSongById(idAlbumSong);

            default:
                throw new IllegalArgumentException("cant add multiple items");
        }
    }

    private boolean isInsertAlbumValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("release");
    }
    private boolean isInsertSongValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("name") && values.containsKey("duration");
    }
    private boolean isInsertAlbumSongValuesValid(ContentValues values) {
        return values.containsKey("id") && values.containsKey("album_id") && values.containsKey("song_id");
    }
    private boolean isUpdateAlbumValuesValid(ContentValues values) {
        return values.containsKey("name") && values.containsKey("release");
    }
    private boolean isUpdateSongValuesValid(ContentValues values) {
        return values.containsKey("name") && values.containsKey("duration");
    }
    private boolean isUpdateAlbumSongValuesValid(ContentValues values) {
        return values.containsKey("album_id") && values.containsKey("song_id");
    }

}
