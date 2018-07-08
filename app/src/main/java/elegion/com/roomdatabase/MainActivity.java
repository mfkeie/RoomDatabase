package elegion.com.roomdatabase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import elegion.com.roomdatabase.database.Album;
import elegion.com.roomdatabase.database.AlbumSong;
import elegion.com.roomdatabase.database.MusicDao;
import elegion.com.roomdatabase.database.Song;

public class MainActivity extends AppCompatActivity {
    private Button mAddBtn;
    private Button mGetBtn;

    // добавить базу данных Room ----
    // вставить данные / извлечь данные ---
    // добавить контент провайдер над Room ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MusicDao musicDao = ((AppDelegate) getApplicationContext()).getMusicDatabase().getMusicDao();

        mAddBtn = (findViewById(R.id.add));
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicDao.deleteAllAlbumSong();
                musicDao.deleteAllAlbum();
                musicDao.deleteAllSong();

               List<Album> albums = createAlbums();
               musicDao.insertAlbums(albums);

                List<Song> songs = createSongs();
               musicDao.insertSongs(songs);

                List<AlbumSong> albumSongs = createLinksAlbumSongs(albums, songs);
                musicDao.setLinksAlbumSongs(albumSongs);
            }
        });

        mGetBtn = findViewById(R.id.get);
        mGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(musicDao.getAlbums(), musicDao.getSongs(), musicDao.getAlbumSongs());
            }
        });

    }

    private List<Album> createAlbums() {
        List<Album> albums = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            albums.add(new Album(i, "album " + i, "release" + System.currentTimeMillis()));
        return albums;
    }

    private List<Song> createSongs() {
        Random random = new Random();
        List<Song> songs = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            songs.add(new Song(i, "song " + i, "duration " + random.nextInt(11) + 10));
        return songs;
    }

    private List<AlbumSong> createLinksAlbumSongs(List<Album> albums, List<Song> songs) {
        List<AlbumSong> albumSongs = new ArrayList<>(3);
        for (int i = 0; i < 3; i++)
            albumSongs.add(new AlbumSong(i, i));
        return albumSongs;
    }

    private void showToast(List<Album> albums, List<Song> songs, List<AlbumSong> albumSongs) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = albums.size(); i < size; i++) {
            builder.append(albums.get(i).toString()).append("\n");
        }
        for (int i = 0, size = songs.size(); i < size; i++) {
            builder.append(songs.get(i).toString()).append("\n");
        }
        for (int i = 0, size = albumSongs.size(); i < size; i++) {
            builder.append(albumSongs.get(i).toString()).append("\n");
        }

        Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();

    }
}
