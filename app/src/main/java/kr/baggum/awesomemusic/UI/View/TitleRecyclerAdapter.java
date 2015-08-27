package kr.baggum.awesomemusic.UI.View;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.UI.Activity.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by user on 15. 7. 12.
 */
public class TitleRecyclerAdapter extends RecyclerView.Adapter<TitleRecyclerAdapter.ViewHolder> {

    private Context mContext;
    public ArrayList<IDTag> list;
    private LinearLayoutManager linearLayoutManager;

    public ArrayList<IDTag> getSongList(){
        return list;
    }

    public TitleRecyclerAdapter(Context context, ArrayList<IDTag> _dataSet, LinearLayoutManager linearLayoutManager) {
        mContext = context;
        list = _dataSet;
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.title_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, list.get(position).albumId);

        Picasso.with(mContext).load(sAlbumArtUri).placeholder(R.drawable.ic_no_album_sm).error(R.drawable.ic_no_album_sm).into(holder.albumArt);

        holder.songTitle.setText(list.get(position).title);
        holder.artistName.setText(list.get(position).artist);
    }

    @Override
    public int getItemCount() {
        if( list == null ) return 0;
        return list.size();
    }

    public void remove(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void add(IDTag song, int position) {
        list.add(position, song);
        notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView albumArt;
        public TextView songTitle;
        public TextView artistName;

        public ViewHolder(View itemView) {
            super(itemView);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art1);
            songTitle = (TextView) itemView.findViewById(R.id.song_title1);
            artistName = (TextView) itemView.findViewById(R.id.artist_name1);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            //set click listener to each song item

            //pass this event to UI (activity)
            ((MainActivity)mContext).songPicked(list,getAdapterPosition());

            //TODO Error happened..
            //java.lang.ClassCastException: android.app.Application cannot be cast to com.example.user.awesomemusic.UI.Activity.MainActivity
        }
    }

}


