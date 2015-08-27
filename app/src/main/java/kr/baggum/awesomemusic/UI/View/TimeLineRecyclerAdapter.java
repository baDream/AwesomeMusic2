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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by user on 15. 8. 18.
 */
public class TimeLineRecyclerAdapter extends RecyclerView.Adapter<TimeLineRecyclerAdapter.ViewHolder> {

    private Context mContext;
    public ArrayList<IDTag> list;
    private LinearLayoutManager linearLayoutManager;
    private String exDate;

    public ArrayList<IDTag> getSongList(){
        return list;
    }

    public TimeLineRecyclerAdapter(Context context, ArrayList<IDTag> _dataSet, LinearLayoutManager linearLayoutManager) {
        mContext = context;
        list = _dataSet;
        this.linearLayoutManager = linearLayoutManager;
        exDate = "";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.timeline_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, list.get(position).albumId);

        Picasso.with(mContext).load(sAlbumArtUri).placeholder(R.drawable.ic_no_album_sm).error(R.drawable.ic_no_album_sm).into(holder.albumArt);

        holder.playDay.setText(list.get(position).year +"-"+ list.get(position).month+"-"+list.get(position).day+
                    "/"+list.get(position).listenTime);


        holder.songTitle.setText(list.get(position).title);
        holder.artistName.setText(list.get(position).artist);
        if( list.get(position).startPoint == null ){
            holder.listenSong.setText( "0:00 ~ " + list.get(position).endPoint);
        }else{
            holder.listenSong.setText( list.get(position).startPoint + " ~ " + list.get(position).endPoint);
        }
        if( list.get(position).skipFlag ) {
            holder.isSkipped.setText("Skipped");
        }else{
            holder.isSkipped.setText("Listened");
        }
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
        public TextView playDay;

        public ImageView albumArt;
        public TextView songTitle;
        public TextView artistName;

        public TextView listenSong;
        public TextView isSkipped;

        public ViewHolder(View itemView) {
            super(itemView);
            playDay = (TextView) itemView.findViewById(R.id.playDay);

            albumArt = (ImageView) itemView.findViewById(R.id.album_art3);
            songTitle = (TextView) itemView.findViewById(R.id.song_title3);
            artistName = (TextView) itemView.findViewById(R.id.artist_name3);

            listenSong = (TextView) itemView.findViewById(R.id.listenSong);
            isSkipped = (TextView) itemView.findViewById(R.id.skipped);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

}



