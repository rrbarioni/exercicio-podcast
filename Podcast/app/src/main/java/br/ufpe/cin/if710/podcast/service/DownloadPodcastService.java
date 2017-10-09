package br.ufpe.cin.if710.podcast.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

import javax.net.ssl.HttpsURLConnection;

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
                // Diretório de download é o root
                File root = new File(Environment.getExternalStorageDirectory() + "/Podcasts");

                root.mkdirs();
                // Arquivo baixado é file_output
                File file_output = new File(root, i.getData().getLastPathSegment());

                // Caso o arquivo já exista, ele é deletado para ser baixado novamente
                if (file_output.exists()) {
                    file_output.delete();
                }

                // Conexão
                URL url = new URL(i.getData().toString());
//                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
                FileOutputStream fos = new FileOutputStream(file_output.getPath());
                BufferedOutputStream out = new BufferedOutputStream(fos);
                Log.d("start podcast service", "FileOutputStream");
                try {
                    Log.d("Começando download", "Issae");
                    InputStream in = c.getInputStream();
                    byte[] buffer = new byte[8192];
                    int len = 0;
                    int count = 0;
                    while ((len = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, len);
                        if (count % 100 == 0) {
                            Log.d("Downloading podcast", "Buffer " + count);
                        }
                        count++;
                    }
                    out.flush();
                }
                finally {
                    Log.d("Fim de download", "Issae");
                    item.setUri(file_output.getPath());

                    // Atualizar URI do podcast baixado (no banco de dados)
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
                Log.d("s", "Conceda as permissões de armazenamento!");
            }

        } catch (IOException e) {
            Log.e(getClass().getName(), "Exception durante download", e);
        }
    }
}