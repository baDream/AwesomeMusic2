package kr.baggum.awesomemusic.Service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.ListGenerator;
import kr.baggum.awesomemusic.UI.View.TestFragment;

import java.util.ArrayList;

/**
 * Created by user on 15. 8. 14.
 */
public class EarPhoneReceiver extends BroadcastReceiver {

    private boolean isEarPhoneOn;
    private static boolean isIt=false;
    private ServiceConnection musicConnection;

    @Override
    public void onReceive(Context context, Intent intent) {

        if( intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            if( !isIt ){ isIt = true; return ; }
            isEarPhoneOn = (intent.getIntExtra("state", 0) > 0) ? true : false;
            if (isEarPhoneOn && AwesomePlayer.instance.isPaused) {
                AwesomePlayer.instance.start();
            } else if (!isEarPhoneOn && AwesomePlayer.instance.isPlaying()) {
                AwesomePlayer.instance.pausePlayer();
            }
            AwesomePlayer.instance.updateUIActivity();
        }
        if( Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if( keyEvent == null ) return ;

            int action = keyEvent.getAction();

            if( action == KeyEvent.ACTION_DOWN){    //When Clicked HeadSet Button
                if( AwesomePlayer.instance != null ) {
                    if (AwesomePlayer.instance.isPlaying()) { //if Is Playing Song stop playing
                        AwesomePlayer.instance.pausePlayer();
                    } else if (AwesomePlayer.instance.isPaused) {
                        AwesomePlayer.instance.start();
                    }
                    AwesomePlayer.instance.updateUIActivity();
                }else{
                    Intent playIntent = new Intent(context, AwesomePlayer.class);

                    ComponentName startResult = context.startService(playIntent);
                    //boolean bindResult = context.bindService(playIntent, musicConnection, context.BIND_AUTO_CREATE);

                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(context);

                    //TODO change tabIndex to tab ID (genre, artist)
                    //TODO song index have a weakness. if user change some song(add, delete, move,..)
                    int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
                    int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

                    if(tabIndex != -1 && songIndex != -1) {
                        //get song list from DB

                        ArrayList<IDTag> songList = null;

                        switch (tabIndex) {
                            case TestFragment.TITLE:
                                songList = ListGenerator.getAllSongList(context);
                                break;
                            case TestFragment.SKIP:
                                songList = ListGenerator.getSkipSongList(context);
                                break;
                            case TestFragment.FOLDER:
                                String lastPath = appSharedPrefs.getString("LASTSONG_PATH", null);
                                songList = ListGenerator.getAllSongsInPathWithoutChild(context, lastPath);

                                break;
                            case TestFragment.RECENT:
                                songList = ListGenerator.getRecentlyAddedList(context);
                                break;
                            default:
                                songList = null;
                        }
                        AwesomePlayer.instance.setSongs(songList);
                        AwesomePlayer.instance.setSongIndex(songIndex);
                        AwesomePlayer.instance.playSong();
                    }
                }
            }else if( action == KeyEvent.ACTION_MULTIPLE ){
                if( AwesomePlayer.instance.isPlaying() ){
                    AwesomePlayer.instance.playNext();
                }
            }

            abortBroadcast();
        }
    }
}
