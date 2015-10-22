package kr.baggum.awesomemusic.Service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.ListGenerator;
import kr.baggum.awesomemusic.UI.View.TestFragment;

import java.util.ArrayList;

/**
 * Created by user on 15. 8. 14.
 */
public class EarPhoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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

                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(context);

                    //TODO change tabIndex to tab ID (genre, artist)
                    //TODO song index have a weakness. if user change some song(add, delete, move,..)
                    int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
                    int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

                    if(tabIndex != -1 && songIndex != -1) {
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
                                songList = ListGenerator.getAllSongList(context);
                        }
                        AwesomePlayer.instance.initMusicPlayer();
                        AwesomePlayer.instance.setSongs(songList);
                        AwesomePlayer.instance.setSongIndex(songIndex);
                        AwesomePlayer.instance.playSong();
                    }
                }
            }else if(action == KeyEvent.ACTION_UP){
                AwesomePlayer.instance.lastPressTime = AwesomePlayer.instance.newPressTime;
                AwesomePlayer.instance.newPressTime = System.currentTimeMillis();
                long delta = AwesomePlayer.instance.newPressTime - AwesomePlayer.instance.lastPressTime;

                // Case for double click
                if(delta < AwesomePlayer.instance.DOUBLE_DELAY){
                    if( AwesomePlayer.instance.isPlaying() ){
                        AwesomePlayer.instance.playNext();
                    }else{
                        AwesomePlayer.instance.nextSong();
                    }
                }
            }
        }
        abortBroadcast();
    }
}
