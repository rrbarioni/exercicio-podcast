package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {

    PodcastDBHelper db;

    public PodcastProvider() {}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Responsável por deletar itens do banco de dados
        // Retorna a quantidade de itens deletados
        if (isEpisodeUri(uri)) {
            return db.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE, selection, selectionArgs);
        }
        else {
            return 0;
        }
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        // Responsável por inserir itens no banco de dados
        if (isEpisodeUri(uri)) {
            long id = db.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE, null, cv);
//            int id = (int) db.getWritableDatabase().insertWithOnConflict(PodcastDBHelper.DATABASE_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
//            if (id == -1) {
//                if (!(cv.getAsString(PodcastDBHelper.EPISODE_FILE_URI)).equals("NONE")) {
//                String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
//                String[] selection_args = new String[]{cv.getAsString(PodcastDBHelper.EPISODE_LINK)};
//                db.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, cv, selection, selection_args);
//            }
            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }
        else {
            return null;
        }
    }

    @Override
    public boolean onCreate() {
        db = PodcastDBHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Responsável por fazer consultas ao banco de dados
        // Retorna um cursor com os itens que satisfazem a consulta
        Cursor cursor = null;
        if (isEpisodeUri(uri)) {
            cursor = db.getReadableDatabase().query(PodcastDBHelper.DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Responsável por atualizar itens já existentes no banco de dados
        // Retorna a quantidade de itens atualizados
        if (isEpisodeUri(uri)) {
            return db.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        }
        else {
            return 0;
        }
    }

    public boolean isEpisodeUri(Uri uri) {
        return uri.getLastPathSegment().equals(PodcastProviderContract.EPISODE_TABLE);
    }
}
