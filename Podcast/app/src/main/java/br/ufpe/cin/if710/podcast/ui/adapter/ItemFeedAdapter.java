package br.ufpe.cin.if710.podcast.ui.adapter;

/**
 * Created by leopoldomt on 9/19/17.
 */

import java.io.File;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.service.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class ItemFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;
    PodcastProvider pp;

    public boolean internetConnection(Context c) {
        // Checa se há conexão com a internet (não há teste de ping)
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null) && netInfo.isConnectedOrConnecting();
    }

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

            if ((currentItem.getUri()).equals("DOWNLOADING")) {
                holder.itemButton.setEnabled(false);
                holder.itemButton.setText("Baixando");
                holder.itemButton.setBackgroundColor(Color.BLUE);
            }

            else if (!(currentItem.getUri()).equals("NONE")) {
                holder.itemButton.setEnabled(true);
                if (currentItem.getPodcastCurrentTime() == 0) {
                    holder.itemButton.setText("Ouvir");
                }
                else {
                    holder.itemButton.setText("Continuar");
                }
                holder.itemButton.setBackgroundColor(Color.GREEN);
            }
            else {
                holder.itemButton.setText("Baixar");
                holder.itemButton.setBackgroundColor(Color.RED);
            }

            holder.itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Context context = getContext();
                    // Realizar download do item, caso não tenha sido feito ainda (indicado pelo URI)
                    if ((currentItem.getUri()).equals("NONE")) {
                        String item_download_link = currentItem.getDownloadLink();
                        if (internetConnection(context)) {
                            Intent download_podcast_service = new Intent(context, DownloadPodcastService.class);
                            download_podcast_service.putExtra("item", currentItem);
                            download_podcast_service.setData(Uri.parse(item_download_link));
                            ((Button)view).setEnabled(false);
                            ((Button)view).setText("Baixando");
                            ((Button)view).setBackgroundColor(Color.BLUE);

                            // Deixar salvo no banco de dados que tal podcast está sendo baixado
                            ContentValues cv = new ContentValues();
                            cv.put(PodcastDBHelper.EPISODE_FILE_URI, "DOWNLOADING");
                            String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                            String[] selection_args = new String[]{currentItem.getLink()};
                            context.getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);

                            context.startService(download_podcast_service);
                        }
                        else {
                            Toast.makeText(context, "Não há conexão com a internet.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Podcast já está na base de dados, pronto pra ser tocado (ou pausado)
                    else {
                        Uri item_uri = Uri.parse(currentItem.getUri());

                        // Caso podcast não esteja tocando
                        if (((Button)view).getText() == "Ouvir") {

                            // Caso podcast não tenha começado
                            if (holder.media_player == null) {
                                holder.media_player = MediaPlayer.create(getContext(), item_uri);
                                holder.media_player.setLooping(false);
                                holder.media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mp) {
//                                        ((Button)view).setText("Ouvir");
//                                        ((Button)view).setBackgroundColor(Color.GREEN);
                                        ((Button)view).setText("Baixar");
                                        ((Button)view).setBackgroundColor(Color.RED);

                                        // Deletar arquivo de podcast do banco
                                        new File(currentItem.getUri()).delete();

                                        // Zerar tempo do áudio do podcast baixado (no banco de dados)
                                        ContentValues cv = new ContentValues();
                                        cv.put(PodcastDBHelper.EPISODE_CURRENT_TIME, "0");
                                        cv.put(PodcastDBHelper.EPISODE_FILE_URI, "NONE");
                                        String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                                        String[] selection_args = new String[]{currentItem.getLink()};
                                        context.getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);
                                    }
                                });
                            }
                            // Tocar podcast
                            holder.media_player.start();
                            ((Button)view).setText("Pausar");
                            ((Button)view).setBackgroundColor(Color.GRAY);
                        }

                        else if (((Button)view).getText() == "Continuar") {
                            // Caso podcast não tenha começado
                            if (holder.media_player == null) {
                                holder.media_player = MediaPlayer.create(getContext(), item_uri);
                                holder.media_player.setLooping(false);
                                holder.media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mp) {
//                                        ((Button)view).setText("Ouvir");
//                                        ((Button)view).setBackgroundColor(Color.GREEN);
                                        ((Button)view).setText("Baixar");
                                        ((Button)view).setBackgroundColor(Color.RED);

                                        // Deletar arquivo de podcast do banco
                                        new File(currentItem.getUri()).delete();

                                        // Zerar tempo do áudio do podcast baixado (no banco de dados)
                                        ContentValues cv = new ContentValues();
                                        cv.put(PodcastDBHelper.EPISODE_CURRENT_TIME, "0");
                                        cv.put(PodcastDBHelper.EPISODE_FILE_URI, "NONE");
                                        String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                                        String[] selection_args = new String[]{currentItem.getLink()};
                                        context.getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);
                                    }
                                });
                            }
                            // Tocar podcast
                            holder.media_player.seekTo(currentItem.getPodcastCurrentTime());
                            holder.media_player.start();
                            ((Button)view).setText("Pausar");
                            ((Button)view).setBackgroundColor(Color.GRAY);
                        }

                        else if (((Button)view).getText() == "Pausar") {
                            // Pausar podcast
                            holder.media_player.pause();
                            ((Button)view).setText("Continuar");
                            ((Button)view).setBackgroundColor(Color.GREEN);

                            // Salvar no banco de dados o momento do podcast pausado
                            currentItem.setPodcastCurrentTime(holder.media_player.getCurrentPosition());

                            // Atualizar tempo do áudio do podcast baixado (no banco de dados)
                            ContentValues cv = new ContentValues();
                            cv.put(PodcastDBHelper.EPISODE_CURRENT_TIME, "" + currentItem.getPodcastCurrentTime());
                            String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
                            String[] selection_args = new String[]{currentItem.getLink()};
                            context.getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selection_args);
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