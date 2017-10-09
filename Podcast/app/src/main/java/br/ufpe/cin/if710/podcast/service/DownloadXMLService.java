package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

/**
 * Created by Ricardo R Barioni on 08/10/2017.
 */

public class DownloadXMLService extends IntentService {
    public static final String DOWNLOAD_AND_PERSIST_XML_COMPLETE = "br.ufpe.cin.if710.services.action.DOWNLOAD_AND_PERSIST_XML_COMPLETE";

    public DownloadXMLService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String RSS_FEED = intent.getStringExtra("rss_feed");
        List<ItemFeed> itemList = new ArrayList<>();
        try {
            itemList = XmlFeedParser.parse(getRssFeed(RSS_FEED));

            } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (ItemFeed item : itemList) {
            ContentValues cv = new ContentValues();

            cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
            cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
            cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
            cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
            cv.put(PodcastDBHelper.EPISODE_TITLE, item.getTitle());
    //        cv.put(PodcastDBHelper.EPISODE_FILE_URI, item.getUri());

            getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
        }

        Intent downloadAndPersistComplete = new Intent(DOWNLOAD_AND_PERSIST_XML_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(downloadAndPersistComplete);
    }

    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}
