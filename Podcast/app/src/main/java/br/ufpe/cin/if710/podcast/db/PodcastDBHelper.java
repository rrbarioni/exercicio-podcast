package br.ufpe.cin.if710.podcast.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;

public class PodcastDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "podcasts";
    public static final String DATABASE_TABLE = "episodes";
    private static final int DB_VERSION = 1;

    private PodcastDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    private static PodcastDBHelper db;

    public static PodcastDBHelper getInstance(Context c) {
        if (db == null) {
            db = new PodcastDBHelper(c.getApplicationContext());
        }
        return db;
    }

    public final static String _ID = "_id";
    public final static String EPISODE_TITLE = "title";
    public final static String EPISODE_DATE = "pubDate";
    public final static String EPISODE_LINK = "link";
    public final static String EPISODE_DESC = "description";
    public final static String EPISODE_DOWNLOAD_LINK = "downloadLink";
    public final static String EPISODE_FILE_URI = "downloadUri";
    public final static String EPISODE_CURRENT_TIME = "currentTime";

    public final static String[] columns = {
            _ID, EPISODE_TITLE, EPISODE_DATE, EPISODE_LINK,
            EPISODE_DESC, EPISODE_DOWNLOAD_LINK, EPISODE_FILE_URI
    };

    // Usar link do item como chave primária
    final private static String CREATE_CMD =
            "CREATE TABLE "+DATABASE_TABLE+" (" + _ID
                    + " INTEGER, "
                    + EPISODE_TITLE + " TEXT NOT NULL, "
                    + EPISODE_DATE + " TEXT NOT NULL, "
                    + EPISODE_LINK + " TEXT NOT NULL PRIMARY KEY, "
                    + EPISODE_DESC + " TEXT NOT NULL, "
                    + EPISODE_DOWNLOAD_LINK + " TEXT NOT NULL, "
                    + EPISODE_FILE_URI + " TEXT NOT NULL, "
                    + EPISODE_CURRENT_TIME + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new RuntimeException("inutilizado");
    }

}
