package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast

    private ListView items;
    private Button selectedButton;
    private PodcastProvider pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);
        pp = new PodcastProvider();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (internetConnection(getApplicationContext())) {
            new DownloadXmlTask().execute(RSS_FEED);
        }
        else {
            new GetFromDatabaseTask().execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    public boolean internetConnection(Context c) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null) && netInfo.isConnectedOrConnecting();
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando... (temos internet!)", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));
                for (ItemFeed item : itemList) {
                    ContentValues cv = new ContentValues();

                    cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
                    cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
                    cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                    cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
                    cv.put(PodcastDBHelper.EPISODE_TITLE, item.getTitle());
                    cv.put(PodcastDBHelper.EPISODE_FILE_URI, "");

                    Uri uri = getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
                }

                // print for checking items inside database
                Cursor queryCursor = getContentResolver().query(
                        PodcastProviderContract.EPISODE_LIST_URI,
                        null, "", null, null
                );
                int n = 0;
                while (queryCursor.moveToNext()) {
                    n++;
                    //Log.d("item " + n, queryCursor.getString(1));
                }
                Log.d("number of items", "" + n);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
//            setItemsListeners();
        }
    }

    private class GetFromDatabaseTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando... (não temos internet!)", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();

            Cursor queryCursor = getContentResolver().query(
                    PodcastProviderContract.EPISODE_LIST_URI,
                    null, "", null, null
            );
            while (queryCursor.moveToNext()) {
                String item_title = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.TITLE));
                String item_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
                String item_date = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DATE));
                String item_description = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DESCRIPTION));
                String item_download_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DOWNLOAD_LINK));
                itemList.add(new ItemFeed(item_title, item_link, item_date, item_description, item_download_link));
            }

            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
//            setItemsListeners();
        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
