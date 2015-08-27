package kr.baggum.awesomemusic.Service;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import kr.baggum.awesomemusic.UI.Activity.LockScreenActivity;

/**
 * Created by user on 15. 8. 4.
 */
public class ScreenOnService extends IntentService {
/*
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){

        intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        receiver = new MediaScannerReceiver();

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("aaa", "onStartCommand()");
        super.onStartCommand(intent, flags, startId);

        if(intent != null){
            if(intent.getAction()==null){
                if(receiver==null){
                    intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                    intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
                    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                    intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
                    receiver = new MediaScannerReceiver();

                    registerReceiver(receiver, intentFilter);
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d("aaa", "onDestroy()");
        super.onDestroy();

        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }
*/
    //lock screen fields

    public static boolean isRunning=false;
    public static boolean isLock=false;
    PowerManager powermanager;

    private KeyguardManager km = null;
    private KeyguardManager.KeyguardLock keyLock = null;

    private TelephonyManager telephonyManager = null;
    private boolean isPhoneIdle = true;
    private boolean isPhoneCalled = false;


    public ScreenOnService() {
        super("ScreenOnService");
        isRunning=true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if( !isRunning ){
            isRunning = true;
        }
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        powermanager = (PowerManager) getSystemService(POWER_SERVICE);

        km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        keyLock = km.newKeyguardLock(KEYGUARD_SERVICE);

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        while( isRunning ) {
            //if Phone Call STATE -> music pause

            if(!isPhoneIdle){
                if( AwesomePlayer.instance.isPlaying() ){
                    AwesomePlayer.instance.pausePlayer();
                    isPhoneCalled = true;
                }
            }else if(isPhoneCalled && AwesomePlayer.instance.isPaused){
                AwesomePlayer.instance.start();
                isPhoneCalled=false;
            }

            if (powermanager.isScreenOn()) {
                isLock=false;
            } else {
                if( AwesomePlayer.instance != null && AwesomePlayer.instance.isPlaying() && !LockScreenActivity.isCreate && !isLock){
                    Log.i("bbb", "screen lock!");

                    if(isPhoneIdle) {
                        disableKeyguard();
                        Intent intent2 = new Intent(this, LockScreenActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);
                    }
                    isLock=true;
                }
            }
        }
    }

    public void reenableKeyguard() {
        keyLock.reenableKeyguard();
    }

    public void disableKeyguard() {
        keyLock.disableKeyguard();
    }

    private PhoneStateListener phoneListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE :
                    isPhoneIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING :
                    isPhoneIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK :
                    isPhoneIdle = false;
                    break;
            }
        }
    };


    @Override
    public void onDestroy(){
        isRunning = false;
    }
}

