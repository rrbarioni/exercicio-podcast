package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ricardo R Barioni on 04/10/2017.
 */

public class DownloadPodcastService extends IntentService {
    public static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.services.action.DOWNLOAD_COMPLETE";

    public DownloadPodcastService() {
        super("DownloadPodcastService");
    }

    @Override
    public void onHandleIntent(Intent i) {
        try {
            //checar se tem permissao... Android 6.0+
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();
            File output = new File(root, i.getData().getLastPathSegment());
            if (output.exists()) {
                output.delete();
            }
            Log.d("file path", i.getData().toString());
            URL url = new URL(i.getData().toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            Log.d("output path", output.getPath());
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                Log.d("download terminou", "");
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_COMPLETE));

        } catch (IOException e) {
            Log.e(getClass().getName(), "Exception durante download", e);
        }
    }
}