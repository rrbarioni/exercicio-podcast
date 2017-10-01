package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;

public class EpisodeDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        ItemFeed item = (ItemFeed) getIntent().getSerializableExtra("item");
        Log.d("item title", item.getTitle());

        TextView item_title = findViewById(R.id.item_title);
        TextView item_link = findViewById(R.id.item_link);
        TextView item_date = findViewById(R.id.item_date);
        TextView item_description = findViewById(R.id.item_description);
        TextView item_download_link = findViewById(R.id.item_download_link);

        String item_title_s = "";
        String item_link_s = "";
        String item_date_s = "";
        String item_description_s = "";
        String item_download_link_s = "";
        if (item.getTitle() != null)        { item_title_s = item.getTitle(); }
        if (item.getLink() != null)         { item_link_s = item.getLink(); }
        if (item.getPubDate() != null)      { item_date_s = item.getPubDate(); }
        if (item.getDescription() != null)  { item_description_s = item.getDescription(); }
        if (item.getDownloadLink() != null) { item_download_link_s = item.getDownloadLink(); }

        item_title.setText(item_title_s);
        item_link.setText(item_link_s);
        item_date.setText(item_date_s);
        item_description.setText(item_description_s);
        item_download_link.setText(item_download_link_s);
    }
}
