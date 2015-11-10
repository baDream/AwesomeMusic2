package kr.baggum.awesomemusic.UI.library;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.R;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by user on 15. 10. 30.
 */
public class EditDialog {
    private View view;
    private TextView title;
    private TextView lyric;

    private Context mContext;

    private Dialog mBottomSheetDialog;
    private MaterialDialog mMaterialDialog;


    private TextView titleEdit;
    private TextView artistEdit;
    private TextView albumEdit;


    public EditDialog(Context context){
        mContext = context;
        view = LayoutInflater.from(mContext).inflate (R.layout.bottom_sheet, null);
        title = (TextView)view.findViewById( R.id.titleEdit);
        artist = (TextView)view.findViewById( R.id.artistEdit);
        album = (TextView)view.findViewById( R.id.albumEdit);
        lyric = (TextView)view.findViewById( R.id.lyricEdit);

        mBottomSheetDialog = new Dialog (mContext, R.style.MaterialDialogSheet);

        mMaterialDialog = new MaterialDialog(mContext)
                .setTitle("MaterialDialog")
                .setMessage("Hello world!")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
    }

    public void openDialog(String path){
        mBottomSheetDialog.setContentView (view);
        mBottomSheetDialog.setCancelable (true);
        mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
        mBottomSheetDialog.show ();


        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                View contentView = LayoutInflater.from(mContext).inflate (R.layout.dialog_layout, null);
                mMaterialDialog.setView(contentView);
                mMaterialDialog.show();
                mBottomSheetDialog.dismiss();
            }
        });

        artist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked Detail", Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });

        album.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked Open", Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });

        lyric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked Uninstall", Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });
    }


    private IDTag setMetadataFromFileName(String path){
        File file = new File(path);         //path를 읽어 파일을 받아온다.
        String fileName = path.substring(path.lastIndexOf('/') + 1);   //path경로에서 파일 제목만 가져온다.(ex. storage/Music/aaa.mp3 -> aaa.mp3 )
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));    //음원 확장자에서 음원 제목만 가져온다(ex. aaa.mp3 -> aaa )
        MusicMetadataSet srcSet = null;

        try {
            srcSet = new MyID3().read(file);  //file에서 ID3 Tag(Metadata)를 읽어온다.
        }catch(IOException io) {
            Log.i("aaa", "IOException");
            return new IDTag();
        }

        //ID3 Tag가 없을 경우 종료한다.
        if(srcSet == null ) return new IDTag();

        MusicMetadata meta = srcSet.merged;  //메타데이터 정보를 받아온다.
        meta = setMetadata(meta,fileName);   //메타데이터 정보를 수정한다.

        try{
            new MyID3().update(file,srcSet,meta);   //파일에 수정한 정보를 적용한다.
        }catch(UnsupportedEncodingException e){
            Log.i("ERROR", "UnsupportedEncodingException");
            e.printStackTrace();
        }catch(ID3WriteException e){
            Log.i("ERROR", "ID3WriteException");
            e.printStackTrace();
        }catch(IOException e){
            Log.i("ERROR", "IOException");
            e.printStackTrace();
        }

        return new IDTag(meta.getSongTitle(),meta.getArtist(),meta.getAlbum(),meta.getGenre());
    }

    /********************************************************************/
    /*  setMetadataFromFileName에서 호출되는 메소드로 타이틀, 가수, 앨범명이    */
    /*          비어있을 경우 Default인 unknown으로 초기화 시켜주거나          */
    /*    (가수 - 제목) 형식의 파일명일 경우 그걸 뽑아와 메타데이터를 저장시켜준다. */
    /*******************************************************************/
    private MusicMetadata setMetadata(MusicMetadata iMusicMetadata, String fileName){
        MusicMetadata meta = new MusicMetadata(fileName);
        int idx = fileName.lastIndexOf("-");  //파일명에서 마지막으로 등장하는 -의 index를 찾는다.

        String title = iMusicMetadata.getSongTitle();
        String artist = iMusicMetadata.getArtist();
        String album = iMusicMetadata.getAlbum();

        //파일명에 -가 존재할 경우 (가수 - 제목) 형식으로 값을 뽑아와서 각 Metadata가 null일때 초기화해준다.
        if( idx >= 0 ) {
            if ( title == null) {
                title = fileName.substring(idx+1);
            }
            if( artist == null){
                artist = fileName.substring(0,idx);
            }
            if( album == null){
                album = "unknown";
            }
        }else{ // (가수 - 제목) 형식이 아닐 경우 Metadata가 null이면 unknown으로 초기화해준다.
            if ( title == null) {
                title = fileName;
            }
            if( artist == null){
                artist = "unknown";
            }
            if( album == null){
                album = "unknown";
            }
        }
        meta.setSongTitle(title);
        meta.setArtist(artist);
        meta.setAlbum(album);

        return meta;
    }
}
