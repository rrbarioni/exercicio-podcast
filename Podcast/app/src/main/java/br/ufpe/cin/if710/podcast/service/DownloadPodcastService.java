package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;

/**
 * Created by Ricardo R Barioni on 04/10/2017.
 */

public class DownloadPodcastService extends IntentService {
    public static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.services.action.DOWNLOAD_COMPLETE";

    public DownloadPodcastService() {
        super("DownloadPodcastService");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onHandleIntent(Intent i) {
        ItemFeed item = (ItemFeed) i.getSerializableExtra("item");

        try {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File root = new File(Environment.getExternalStorageDirectory() + "/Podcasts");

                root.mkdirs();
                File file_output = new File(root, i.getData().getLastPathSegment());

                if (file_output.exists()) {
                    file_output.delete();
                }

                Log.d("file path", i.getData().toString());
                URL url = new URL(i.getData().toString());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                Log.d("output path", file_output.getPath());
                FileOutputStream fos = new FileOutputStream(file_output.getPath());
                BufferedOutputStream out = new BufferedOutputStream(fos);
                try {
                    Log.d("começando a baixar", "s");
                    InputStream in = c.getInputStream();
                    byte[] buffer = new byte[8192];
                    int len = 0;
                    int count = 0;
                    while ((len = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, len);
                        if (count % 100 == 0) {
                            Log.d("baixando", "" + count);
                        }
                        count++;
                    }
                    out.flush();
                }
                finally {
                    Log.d("download terminou", "s");
                    item.setUri(file_output.getPath());

                    ContentValues cv = new ContentValues();
                    cv.put(PodcastDBHelper.EPISODE_FILE_URI, item.getUri());

                    String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                    String[] selection_args = new String[]{item.getLink()};

                    getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);

                    fos.getFD().sync();
                    out.close();
                    c.disconnect();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Conceda as premissões de armazenamento!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Log.e(getClass().getName(), "Exception durante download", e);
        }
    }
}