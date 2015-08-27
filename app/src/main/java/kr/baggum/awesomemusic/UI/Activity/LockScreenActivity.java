package kr.baggum.awesomemusic.UI.Activity;

import android.app.Activity;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.Service.AwesomePlayer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import jp.wasabeef.picasso.transformations.BlurTransformation;

/**
 * Created by user on 15. 8. 3.
 */
public class LockScreenActivity extends Activity {

    public static boolean isCreate = false;

    AwesomePlayer musicSrv;
    public ImageView albumArtBlur;
    public ImageView albumArtBox;
    public ImageView lockImg;
    public TextView lockTitle;

    public ImageButton prev;
    public ImageButton next;
    public ImageButton play;

    private boolean isActivityVisible;

    private boolean isSongChanged;

    private int x;
    private int y;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.screen_activity);

        musicSrv = AwesomePlayer.instance;
        musicSrv.setLockScreenActivity(this);
        isActivityVisible = true;

        albumArtBlur = (ImageView) findViewById(R.id.albumArtBlur);
        albumArtBox = (ImageView) findViewById(R.id.albumArtBox);
        lockImg = (ImageView) findViewById(R.id.lockImg);
        lockTitle = (TextView) findViewById(R.id.lockTitle);

        play = (ImageButton) findViewById(R.id.lockPlay);
        prev = (ImageButton) findViewById(R.id.lockPrev);
        next = (ImageButton) findViewById(R.id.lockNext);

        setButton();
        setSongData(musicSrv.getCurrentSong());

        isCreate = true;
    }

    private void setSongData(IDTag songIDTag){
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, songIDTag.albumId);

        String artist = songIDTag.artist == null ? "unknown" : songIDTag.artist;
        String title = songIDTag.title == null ? "unknown" : songIDTag.title;

        lockTitle.setText(artist + " - " + title);
        lockTitle.setSelected(true);

        Transformation transformation = new BlurTransformation(getApplicationContext(), 25, 4);

        Picasso.with(getApplicationContext()).load(sAlbumArtUri).
                placeholder(albumArtBlur.getDrawable()).
                error(albumArtBlur.getDrawable()).
                transform(transformation).
                into(albumArtBlur);

        Picasso.with(getApplicationContext()).load(sAlbumArtUri)
                .placeholder(albumArtBox.getDrawable()).
                error(R.drawable.ic_no_album_hd)
                .into(albumArtBox);


    }

    private void setButton(){
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicSrv.isPlaying()) {
                    musicSrv.pausePlayer();
                    play.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
                } else {
                    if( isSongChanged ){
                        musicSrv.playSong();
                        isSongChanged=false;
                    }else {
                        musicSrv.start();
                    }
                    play.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicSrv.isPlaying()){
                    musicSrv.playPrev();
                }else{
                    musicSrv.prevSong();
                    setSongData(musicSrv.getCurrentSong());
                    isSongChanged=true;
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicSrv.isPlaying()){
                    musicSrv.playNext();
                }else{
                    musicSrv.nextSong();
                    setSongData(musicSrv.getCurrentSong());
                    isSongChanged=true;
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        isActivityVisible = true;

        //assumption : music is playing
        setSongData(musicSrv.getCurrentSong());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActivityVisible = false;
        isCreate = false;
    }

    @Override
    protected void onPause() {
        isActivityVisible = false;
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint(){
        isCreate = false;
        finish();
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed() {
        isCreate = false;
        finish();
    } // in MyActivit

    @Override
    public boolean onTouchEvent(MotionEvent event){

        super.onTouchEvent(event);

        int x1,y1;

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            lockImg.setVisibility(ImageView.VISIBLE);
            x = (int) event.getX();
            y = (int) event.getY();
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            x1 = (int) event.getX();
            y1 = (int) event.getY();

            if( x-25 <= x1 && x1 <= x+25 || y-25 <= y1 && y1 <= y+25){
                lockImg.setImageResource(R.drawable.ic_lock_outline_white_24dp);
            }else{
                lockImg.setImageResource(R.drawable.ic_lock_open_white_24dp);
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            x1 = (int) event.getX();
            y1 = (int) event.getY();

            if( x-25 <= x1 && x1 <= x+25 || y-25 <= y1 && y1 <= y+25){
                lockImg.setVisibility(ImageView.INVISIBLE);
            }else{
                isCreate = false;
                finish();
            }
        }
        return true;
    }

    public boolean isActivityVisible() {
        return isActivityVisible;
    }
    public void stateChangeMessageFromMP() {
        setSongData(musicSrv.getCurrentSong());
    }
    public void changePlayButton(boolean bool){
        if( bool )
            play.setImageResource(R.drawable.ic_play_circle_outline_white_24dp);
        else{
            play.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
        }
    }
}
