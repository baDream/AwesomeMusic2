package kr.baggum.awesomemusic.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import kr.baggum.awesomemusic.Data.UserDB;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.Service.MediaScan;
import kr.baggum.awesomemusic.Service.MediaScannerReceiver;


public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initDB();

        //start a service (lock screen detector)
        //Intent service = new Intent(this, ScreenOnService.class);
        //startService(service);

        //for animation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);

                // 뒤로가기 했을경우 안나오도록 없애주기 >> finish!!
                finish();
            }
        }, 500);
    }

    private void initDB(){
        SQLiteDatabase db = UserDB.getInstance(getApplicationContext()).getDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_TABLE_NAME, null);
        Log.i("aaa", "initDB - # of media files : " + cursor.getCount());

//        if( cursor.getCount() <= 0 ) {
//            MediaScannerReceiver.mediaSync =true;
//            Intent intent = new Intent(this, MediaScan.class);
//            startService(intent);
//        }

        if(MediaScan.instance == null){ //prevent redundant MediaScan thread
            MediaScannerReceiver.mediaSync =true;
            Intent intent = new Intent(this, MediaScan.class);
            startService(intent);
        }

    }

}


