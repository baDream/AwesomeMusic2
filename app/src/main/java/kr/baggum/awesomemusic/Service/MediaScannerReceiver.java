package kr.baggum.awesomemusic.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by user on 15. 7. 8.
 */
//on MediaScan.class onCreate
public class MediaScannerReceiver extends BroadcastReceiver {
    public static boolean mediaSync = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.i("aaa", "scanning finished : " + intent.getData().getPath());

            Intent intent1 = new Intent(context.getApplicationContext(), MediaScan.class);
            if( mediaSync ){
                context.getApplicationContext().stopService(intent1);
            }
            mediaSync = true;
            context.getApplicationContext().startService(intent1);
        }
    }
}