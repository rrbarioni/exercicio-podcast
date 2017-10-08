package br.ufpe.cin.if710.podcast.ui.adapter;

/**
 * Created by leopoldomt on 9/19/17.
 */

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.service.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class ItemFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;

    public ItemFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button itemButton;
        MediaPlayer media_player;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemFeed currentItem = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            convertView.setTag(holder);

            // Ao clicar no botão de download de um item, realizar download do mesmo (usando DownloadPodcastService para tal)
            // Caso tal item já tenha sido baixado, dar o play
            holder.itemButton = convertView.findViewById(R.id.item_action);

            Log.d("getUri", currentItem.getUri());
            if (!(currentItem.getUri()).equals("NONE")) {
                holder.itemButton.setEnabled(true);
                holder.itemButton.setText("Ouvir");
                holder.itemButton.setBackgroundColor(Color.GREEN);
            }
            else {
                holder.itemButton.setText("Baixar");
                holder.itemButton.setBackgroundColor(Color.RED);
            }

            holder.itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Realizar download do item, caso não tenha sido feito ainda (indicado pelo URI)
                    if ((currentItem.getUri()).equals("NONE")) {
                        String item_download_link = currentItem.getDownloadLink();
                        Context context = getContext();
                        Intent download_podcast_service = new Intent(context, DownloadPodcastService.class);
                        download_podcast_service.putExtra("item", currentItem);
                        download_podcast_service.setData(Uri.parse(item_download_link));
                        ((Button)view).setEnabled(false);
                        ((Button)view).setText("Baixando");
                        ((Button)view).setBackgroundColor(Color.BLUE);
                        context.startService(download_podcast_service);
                    }

                    // Podcast já está na base de dados, pronto pra ser tocado (ou pausado)
                    else {
                        Uri item_uri = Uri.parse(currentItem.getUri());

                        // Caso podcast esteja pausado
                        if (((Button)view).getText() == "Ouvir") {

                            // Caso podcast não tenha começado
                            if (holder.media_player == null) {
                                holder.media_player = MediaPlayer.create(getContext(), item_uri);
                                holder.media_player.setLooping(false);
                            }
                            // Tocar podcast
                            holder.media_player.start();
                            ((Button)view).setText("Pausar");
                            ((Button)view).setBackgroundColor(Color.GRAY);
                        }

                        else if (((Button)view).getText() == "Pausar") {
                            // Pausar podcast
                            holder.media_player.pause();
                            ((Button)view).setText("Ouvir");
                            ((Button)view).setBackgroundColor(Color.GREEN);
                        }
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_title.setText(getItem(position).getTitle());
        holder.item_date.setText(getItem(position).getPubDate());

        // Ao clicar no título de um item, exibir outras informações do item correspondente (EpisodeDetailActivity)
        holder.item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                Intent episode_detail_intent = new Intent(context, EpisodeDetailActivity.class);
                episode_detail_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                episode_detail_intent.putExtra("item", currentItem);
                context.startActivity(episode_detail_intent);
            }
        });

        return convertView;
    }
}