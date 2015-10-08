package kr.baggum.awesomemusic.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 15. 10. 8.
 */
public class HeadSetPlugReceiver extends BroadcastReceiver {

        private boolean isEarPhoneOn;
        private static boolean isIt=false;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                Log.i("aaa", "headPlug");
                if (!isIt) {
                    isIt = true;
                    return;
                }
                isEarPhoneOn = (intent.getIntExtra("state", 0) > 0) ? true : false;
                if (isEarPhoneOn && AwesomePlayer.instance.isPaused) {
                    AwesomePlayer.instance.start();
                } else if (!isEarPhoneOn && AwesomePlayer.instance.isPlaying()) {
                    AwesomePlayer.instance.stopNoti();
                    AwesomePlayer.instance.updateUInotNoti();
                }

            }

            abortBroadcast();
        }

}
