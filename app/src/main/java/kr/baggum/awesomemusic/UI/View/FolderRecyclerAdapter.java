package kr.baggum.awesomemusic.UI.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.SongDirectoryTree;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.UI.Activity.MainActivity;
import kr.baggum.awesomemusic.UI.library.EditDialog;

import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by user on 15. 7. 21.
 */
public class FolderRecyclerAdapter extends RecyclerView.Adapter<FolderRecyclerAdapter.ViewHolder> {

    public Context mContext;
    public ArrayList<SongDirectoryTree> backupTree;   //이전 폴더
    public SongDirectoryTree directoryTree;            //현재 폴더
    private SongDirectoryTree fullDirectoryTree;        //used for tree traverse
    private ArrayList<IDTag> songList;                  //현재 폴더의 노래목록
    private ArrayList<SongDirectoryTree> folderList;    //현재 폴더의 폴더 목록

    //현재 디렉토리의 노래와 하위 디렉토리의 노래들을 모두 뽑은 리스트를 반환한다..
    private ArrayList<IDTag> ExtractFolder( ArrayList<IDTag> list, SongDirectoryTree folder){
        if( folder.musicData != null ){
            list.addAll(folder.musicData);
        }

        for( int i=0; i<folder.nextTree.size(); i++){
            ExtractFolder(list, folder.nextTree.get(0).getTree(folder.nextTree.get(i)) );
        }
        return list;
    }
    //현재 디렉토리 하위의 모든 노래를 뽑아내는 메소드
    public ArrayList<IDTag> getSongList(){
        ArrayList<IDTag> list = new ArrayList<IDTag>();

        return ExtractFolder(list, directoryTree);
    }

    public FolderRecyclerAdapter(Context context, SongDirectoryTree dataSet) {
        mContext = context;
        fullDirectoryTree = dataSet;
        directoryTree = dataSet.getTree(dataSet);
        folderList = directoryTree.nextTree;
        songList = directoryTree.musicData;
        backupTree = new ArrayList<SongDirectoryTree>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.title_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if( folderList.size() <= position ){
            int pos = position-folderList.size();
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, songList.get(pos).albumId);

            Picasso.with(mContext).load(sAlbumArtUri).placeholder(R.drawable.ic_no_album_sm).error(R.drawable.ic_no_album_sm).into(holder.albumArt);

            holder.songTitle.setText(songList.get(pos).title);
            holder.artistName.setText(songList.get(pos).artist);
        }else{
            Picasso.with(mContext).load(R.drawable.awesome_folder).into(holder.albumArt);
            holder.songTitle.setText(folderList.get(position).folderName);
            holder.artistName.setText("");
        }
    }

    //folderList를 다 출력 후 노래들을 출력한다.
    @Override
    public int getItemCount() {
        if( songList == null)
            return folderList.size();
        else
            return folderList.size() + songList.size();
    }

    //상위 폴더로 올라간다.
    public void setUpdate(){
        directoryTree = backupTree.get( backupTree.size()-1 );
        folderList = directoryTree.nextTree;
        songList = directoryTree.musicData;
        backupTree.remove( backupTree.size()-1 );
        notifyDataSetChanged();
    }


    //TODO use filePath parameter only
    //last play song in folder
    public void moveIntoSpecificFolder(String folderName, String filePath){
        //folderName이 현재 디렉토리와 같으면 탐색 중지
        if( fullDirectoryTree.folderName != null && fullDirectoryTree.folderName.equals(folderName) ) {
            notifyDataSetChanged();
            return;
        } else{   //다르면 하위 디렉토리로 들어간다.
            int indexOfSlash = filePath.indexOf("/");

            String currentPath;
            String nextPath;

            if(indexOfSlash != -1){ // filePath contains one slash at least
                currentPath = filePath.substring(0, filePath.indexOf("/"));    //다음 디렉토리 추출
                nextPath = filePath.substring(filePath.indexOf("/")+1);  //다음 디렉토리의 하위 디렉토리 추출
            }else{ // filePath doesn't contain slash
                currentPath = filePath;
                nextPath = null;
            }

            for(int i=0; i<fullDirectoryTree.nextTree.size(); i++){
                SongDirectoryTree nextNode = fullDirectoryTree.nextTree.get(i);

                if( nextNode.folderName.equals(currentPath)){  //디렉토리가 같으면

                    if(fullDirectoryTree.isPrinting) // add current path into backupTree only it is printable
                        backupTree.add(fullDirectoryTree);

                    //set current tree to next tree
                    fullDirectoryTree = nextNode;
                    folderList = fullDirectoryTree.nextTree;
                    songList = fullDirectoryTree.musicData;

                    //recursive search
                    moveIntoSpecificFolder(folderName, nextPath);
                }
            }

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumArt;
        public TextView songTitle;
        public TextView artistName;
        private EditDialog dialog;

        public ViewHolder(View itemView) {
            super(itemView);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art1);
            songTitle = (TextView) itemView.findViewById(R.id.song_title1);
            artistName = (TextView) itemView.findViewById(R.id.artist_name1);

            //set click listener to each song item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedIndex = getAdapterPosition();
                    Log.d("aaa", "folder size : " + folderList.size());
                    if(songList != null)
                        Log.d("aaa,", ", songS : " + songList.size());

                    if(selectedIndex < folderList.size()){
                        // folder is selected
                        backupTree.add(directoryTree);
                        directoryTree = folderList.get(0).getTree(folderList.get(getAdapterPosition()));
                        folderList = directoryTree.nextTree;
                        songList = directoryTree.musicData;
                        notifyDataSetChanged();

                    }else{
                        // song is selected
                        int songIndex = selectedIndex - folderList.size();
                        ((MainActivity)mContext).songPicked(songList, songIndex);
                    }

                }
//                    Log.d("aaa",index + "th song is selected");
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int selectedIndex = getAdapterPosition();
                    Log.d("aaa", "folder size : " + folderList.size());
                    if (songList != null)
                        Log.d("aaa,", ", songS : " + songList.size());
                    if (selectedIndex < folderList.size()) {
                        // folder is selected
                        backupTree.add(directoryTree);
                        directoryTree = folderList.get(0).getTree(folderList.get(getAdapterPosition()));
                        folderList = directoryTree.nextTree;
                        songList = directoryTree.musicData;
                        notifyDataSetChanged();
                    } else {
                        // song is selected
                        dialog = new EditDialog(mContext);
                        dialog.openDialog(songList.get(selectedIndex));
                    }
                    return true;
                }
            });
        }
    }
}