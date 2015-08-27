package kr.baggum.awesomemusic.Service;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by Administrator on 2015-08-26.
 */
public class VolumeControl extends Activity {

    // Audio
    AudioManager am = null;

    int StreamType = AudioManager.STREAM_MUSIC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void VolumUP(){
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currVol = am.getStreamVolume(StreamType);
        int maxVol = am.getStreamMaxVolume(StreamType);
        //Log.d("VOL",  "CURRENT VOL = " + currVol + ", MAX VOL = " + maxVol);
        if(currVol < maxVol)
        {
            am.setStreamVolume(StreamType, currVol + 1, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    public void VolumnDOWN()
    {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currVol = am.getStreamVolume(StreamType);
        Log.d("VOL", "CURRENT VOL = " + currVol);
        if(currVol > 0)
        {
            am.setStreamVolume(StreamType, currVol -1, AudioManager.FLAG_PLAY_SOUND);
        }
    }
}
