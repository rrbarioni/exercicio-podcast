package br.ufpe.cin.if710.podcast.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.service.DownloadXMLService;
import br.ufpe.cin.if710.podcast.ui.adapter.ItemFeedAdapter;

public class MainActivity extends Activity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

//    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast
    private final String RSS_FEED = "https://hpbl.github.io/hub42_APS/audio/xml_reduzido.xml";

    private ListView items;
    private Button selectedButton;
    private PodcastProvider pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);
        pp = new PodcastProvider();

        Stetho.initializeWithDefaults(this);

        IntentFilter download_xml = new IntentFilter(DownloadXMLService.DOWNLOAD_AND_PERSIST_XML_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(onDownloadXMLEvent, download_xml);
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

        checkDownloadPodcastsPermissions(this);
        while(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {}

        // Caso haja internet, pegar itens do XML da internet (conteúdo mais atualizado)
        // Caso não haja internet, pegar itens do banco de dados
        // Agora é feito pelo DownloadXMLService
        if (internetConnection(getApplicationContext())) {
//            new DownloadXmlTask().execute(RSS_FEED);
            Intent download_xml_service = new Intent(this.getApplicationContext(), DownloadXMLService.class);
            download_xml_service.putExtra("rss_feed", RSS_FEED);
            this.getApplicationContext().startService(download_xml_service);
        }
        else {
            new GetFromDatabaseTask().execute();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ItemFeedAdapter adapter = (ItemFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    public boolean internetConnection(Context c) {
        // Checa se há conexão com a internet (não há teste de ping)
        ConnectivityManager cm = (ConnectivityManager) getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null) && netInfo.isConnectedOrConnecting();
    }

    public static void checkDownloadPodcastsPermissions(Activity activity) {
        // Solicitar permissões para salvar arquivos no dispositivo
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

//    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            Toast.makeText(getApplicationContext(), "Pegando lista de itens da internet", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            // Usar parser para extrair itens provenientes do XML e salvá-los no banco de dados
//            try {
//                List<ItemFeed> itemList = XmlFeedParser.parse(getRssFeed(params[0]));
//                for (ItemFeed item : itemList) {
//                    ContentValues cv = new ContentValues();
//
//                    cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
//                    cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
//                    cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
//                    cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
//                    cv.put(PodcastDBHelper.EPISODE_TITLE, item.getTitle());
////                    cv.put(PodcastDBHelper.EPISODE_FILE_URI, item.getUri());
//
//                    getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void v) {
//            // Para listar elementos ao usuário, sempre pegar itens do banco de dados
//            new GetFromDatabaseTask().execute();
//        }
//    }

    private class GetFromDatabaseTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Pegando itens do banco...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            // Extrair todos os itens existentes no banco de dados
            List<ItemFeed> itemList = new ArrayList<>();

            Cursor queryCursor = getContentResolver().query(
                    PodcastProviderContract.EPISODE_LIST_URI,
                    null, "", null, null
            );
            int count = 0;
            while (queryCursor.moveToNext()) {
                String item_title = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.TITLE));
                String item_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
                String item_date = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DATE));
                String item_description = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DESCRIPTION));
                String item_download_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DOWNLOAD_LINK));
                String item_uri = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.EPISODE_URI));
                count++;
                itemList.add(new ItemFeed(item_title, item_link, item_date, item_description, item_download_link, item_uri));
            }
            Log.d("count", "" + count);

            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            // Usar adapter para colocar lista de itens na view para o usuário
            Toast.makeText(getApplicationContext(), "Itens pegos do banco", Toast.LENGTH_SHORT).show();

            ItemFeedAdapter adapter = new ItemFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
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

    private BroadcastReceiver onDownloadXMLEvent = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // Chamado quando o DownloadXMLService retorna com o XML baixado e colocado no banco de dados
            Toast.makeText(getApplicationContext(), "Itens carregados do XML pelo service", Toast.LENGTH_SHORT).show();

            // Carregar view com os dados do banco
            new GetFromDatabaseTask().execute();
        }
    };
}
