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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    public TextView titleEdit;
    public TextView artistEdit;
    public TextView albumEdit;

    public IDTag idTag;

    public EditDialog(Context context){
        mContext = context;
        view = LayoutInflater.from(mContext).inflate (R.layout.bottom_sheet, null);
        title = (TextView)view.findViewById( R.id.titleEdit);
        lyric = (TextView)view.findViewById( R.id.lyricEdit);

        mBottomSheetDialog = new Dialog (mContext, R.style.MaterialDialogSheet);

        mMaterialDialog = new MaterialDialog(mContext)
                .setTitle("MaterialDialog")
                .setMessage("Hello world!")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("aaa", "title : "+ idTag.title);
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

    public void openDialog(IDTag tag){
        mBottomSheetDialog.setContentView (view);
        mBottomSheetDialog.setCancelable (true);
        mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
        mBottomSheetDialog.show ();

        idTag = tag;

        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                View contentView = LayoutInflater.from(mContext).inflate (R.layout.dialog_layout, null);

                titleEdit = (TextView)contentView.findViewById(R.id.titleEdit);
                artistEdit = (TextView)contentView.findViewById(R.id.artistEdit);
                albumEdit = (TextView)contentView.findViewById(R.id.albumEdit);

                try{
                    AudioFile f = AudioFileIO.read(new File(idTag.path));
                    Tag tag1 = f.getTag();

                    if( idTag==null) return ;
                    String ti = tag1.getFirst(FieldKey.TITLE);
                    String ar = tag1.getFirst(FieldKey.ARTIST);
                    String al = tag1.getFirst(FieldKey.ALBUM);

                }catch(Exception ecr){
                    ecr.printStackTrace();
                }

                titleEdit.setText(idTag.title);
                artistEdit.setText(idTag.artist);
                albumEdit.setText(idTag.album);

                mMaterialDialog.setView(contentView);
                mMaterialDialog.show();
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
}
