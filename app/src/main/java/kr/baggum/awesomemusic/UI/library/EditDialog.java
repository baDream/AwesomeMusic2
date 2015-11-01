package kr.baggum.awesomemusic.UI.library;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kr.baggum.awesomemusic.R;

/**
 * Created by user on 15. 10. 30.
 */
public class EditDialog {
    private View view;
    private TextView title;
    private TextView artist;
    private TextView album;
    private TextView lyric;

    private Context mContext;

    private Dialog mBottomSheetDialog;

    public EditDialog(Context context){
        View view = LayoutInflater.from(mContext).inflate (R.layout.bottom_sheet, null);
        title = (TextView)view.findViewById( R.id.titleEdit);
        artist = (TextView)view.findViewById( R.id.artistEdit);
        album = (TextView)view.findViewById( R.id.albumEdit);
        lyric = (TextView)view.findViewById( R.id.lyricEdit);

        mBottomSheetDialog = new Dialog (mContext, R.style.MaterialDialogSheet);

        mContext = context;
    }

    public void openDialog(){
        mBottomSheetDialog.setContentView (view);
        mBottomSheetDialog.setCancelable (true);
        mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
        mBottomSheetDialog.show ();


        title.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Clicked Backup", Toast.LENGTH_SHORT).show();
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
}
