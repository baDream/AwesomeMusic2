package kr.baggum.awesomemusic.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Helper.AwesomeDBHelper;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.UI.Activity.LockScreenActivity;
import kr.baggum.awesomemusic.UI.Activity.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class AwesomePlayer extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    // TODO 2.fade-in-out 4.volume 6.shuffle(wrt list) 7.EQ  9.audio focus -find more features
    public static final short NOREPLAY = 1;
    public static final short ALLREPLAY = 2;
    public static final short SINGLEREPLAY = 3;

    public static final int STOP=0;
    public static final int PLAYING=1;

    // this static variable will be initiated at outside (by binding service)
    public static AwesomePlayer instance = null;

    private static MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private ArrayList<IDTag> songs;
    private ArrayList<IDTag> tempSongs;
    private int songPos;


    public boolean shuffle;
    public short replay;

    private boolean isMusicBound = false;
    private MainActivity baseActivity;
    private LockScreenActivity lockScreenActivity;

    public boolean isPicked;

    //DB Helper
    private AwesomeDBHelper dbHelper;

    //notification Bar
    private NotificationManager nm;
    private Notification.Builder builder;
    private RemoteViews contentiew;
    private Notification noti;
    private boolean isSongChanged = false;
    private boolean isButtonClicked = false;
    private BroadcastReceiver notiBroadcastReceiver;
    private boolean broadRegister = false;

    //EarPhone
    private BroadcastReceiver earBroadcastReceiver;
    public boolean isPaused;
    ComponentName mediaButton;
    AudioManager am;
    public long lastPressTime;
    public long newPressTime;
    public final long DOUBLE_DELAY=500;

    @Override
    public void onCreate() {
        super.onCreate();

        songPos = 0;
        shuffle = false;
        replay = NOREPLAY;
        player = new MediaPlayer();

        initMusicPlayer();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(instance == null){
            songPos = 0;
            shuffle = false;
            replay = NOREPLAY;
            player = new MediaPlayer();

            initMusicPlayer();
        }

        return START_REDELIVER_INTENT;
    }
    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        //TODO remember last played song & list
    }
    public void setSongs(ArrayList<IDTag> songs) {
        this.songs = songs;
        tempSongs = new ArrayList<IDTag>();
    }
    public ArrayList<IDTag> getSongs(){return songs;}
    public int getSongPos(){
        return songPos;
    }
    public void removeSongPos(int idx){ songs.remove(idx); }
    public IDTag getCurrentSong(){
        // TODO may be need to check boundary
        if( songs == null || songs.size()<=0 || songPos >= songs.size()) return null;

        return songs.get(songPos);
    }
    public boolean isMusicBound() {
        return isMusicBound;
    }
    public void setIsMusicBound(boolean isMusicBound) {
        this.isMusicBound = isMusicBound;
    }
    public void setBaseActivity(MainActivity baseActivity) {
        this.baseActivity = baseActivity;
    }
    public void sendListChangeEvent(){
        if(baseActivity != null)
            baseActivity.listChangeEvent(baseActivity);
    }
    public boolean doesHasSongList() {
        return songs != null;
    }
    public void setLockScreenActivity(LockScreenActivity lockScreenActivity) {
        this.lockScreenActivity = lockScreenActivity;
    }
    public class MusicBinder extends Binder {
        public AwesomePlayer getService() {
            return AwesomePlayer.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        isMusicBound = true;
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent) {

        //connection to activity lost
        isMusicBound = false;
        baseActivity = null;
//        player.stop();
//        player.release();
        return false;
    }
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    /*Play Association*/
    public void playSong() {
        if(songs == null)
            return;
        try {
            if( isPicked && isPlaying() && dbHelper != null){
                dbHelper.countSkip(getCurrentPosition());
            }
            //play a song
            player.reset();

            IDTag playsong = songs.get(songPos);
            String songId = playsong.id;

            Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(songId));
            player.setDataSource(getApplicationContext(), trackUri);   //get song data from the source
            player.prepareAsync();          // onPrepared will be called when the music player is prepared to play
        } catch (IOException e) {
            Log.e("aaa", "Error : AWESOME PLAYER - setting data source");
            e.printStackTrace();
        }

        isSongChanged = false;
    }
    public void playPrev(){
        prevSong();
        if( songPos < 0 )   stopPlayer();
        else                playSong();
    }
    public void playNext(){
        if( isPlaying() && dbHelper != null ) dbHelper.countSkip(getCurrentPosition());
        nextSong();
        if( songs.size() <= 0 ) stopPlayer();
        else                    playSong();
    }
    public void nextSong(){
        songPos++;
        if (songPos >= songs.size()) songPos = 0;
        isSongChanged = true;

    }
    public void completeNextSong(){
        songPos++;
        if (songPos >= songs.size()) {
            songPos--;
            stopPlayer();
            updateUIActivity();
        }else{
            playSong();
        }
    }
    public void prevSong(){
        songPos--;
        if(songPos < 0) songPos=songs.size()-1;
        isSongChanged = true;
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("aaa", "ap - onCompletion");
        //TODO error: this method is called even if the songs instance is null -> NULLPOINTEREXCEPTION
        if(songs == null) return;

        if( dbHelper != null ) dbHelper.countSkip(getCurrentPosition());

        if( replay == SINGLEREPLAY ){
            playSong();
        }else if( replay == NOREPLAY){
            completeNextSong();
        }else{
            playNext();
        }
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        //when the Music player is prepared
        //start playback
        Log.i("ccc", "state replay : " + replay + ", shuffle : " + shuffle );
        mp.start();

        if( ScreenOnService.isRunning == false ) {
            Intent service = new Intent(this, ScreenOnService.class);
            startService(service);
        }

        //set notification
        setNotiBar();

        if( !broadRegister ){
            earBroadcastReceiver = new EarPhoneReceiver();
            notiBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO Auto-generated method stub
                    if( isButtonClicked ){
                        return;
                    }
                    isButtonClicked=true;
                    if( intent.getAction().equals("play") ){
                        if (isPlaying()) {
                            pausePlayer();
                        } else {
                            if( isSongChanged ){
                                playSong();
                                isSongChanged=false;
                            }else {
                                start();
                            }
                        }
                        updateUIActivity();
                    }else if( intent.getAction().equals("next") ){
                        if(isPlaying()){
                            playNext();
                        }else{
                            nextSong();
                            isSongChanged=true;
                        }
                        updateUIActivity();
                    }else if( intent.getAction().equals("prev") ){
                        if(isPlaying()){
                            playPrev();
                        }else{
                            prevSong();
                            isSongChanged=true;
                        }
                        updateUIActivity();
                    }
                    else{

                    }
                    isButtonClicked=false;
                    //AwesomePlayer.instance.playNext();
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("play");
            intentFilter.addAction("next");
            intentFilter.addAction("prev");
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction(Intent.ACTION_MEDIA_BUTTON);
            intentFilter2.addAction(Intent.ACTION_HEADSET_PLUG);
            intentFilter2.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY+999999999);

            mediaButton = new ComponentName(getPackageName(), EarPhoneReceiver.class.getName());
            am = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            am.registerMediaButtonEventReceiver(mediaButton);

            registerReceiver(notiBroadcastReceiver, intentFilter);
            registerReceiver(earBroadcastReceiver, intentFilter2);

            broadRegister = true;
        }

        dbHelper = new AwesomeDBHelper( getApplicationContext());
        dbHelper.inputSongData();

        // TODO combine events : AwesomePlayerStateChangeListener
        updateUIActivity();
    }


    public void updateUIActivity(){
        if(baseActivity != null && baseActivity.isActivityVisible())
            baseActivity.stateChangeMessageFromMP();
        if(lockScreenActivity != null && lockScreenActivity.isActivityVisible()){
            lockScreenActivity.stateChangeMessageFromMP();
        }
        setNotiBar();

        if( isPlaying() ) {
            if (baseActivity != null && baseActivity.isActivityVisible())
                baseActivity.changePlayButton(false);
            if (lockScreenActivity != null && lockScreenActivity.isActivityVisible())
                lockScreenActivity.changePlayButton(false);
        }else{
            if (baseActivity != null && baseActivity.isActivityVisible())
                baseActivity.changePlayButton(true);
            if (lockScreenActivity != null && lockScreenActivity.isActivityVisible())
                lockScreenActivity.changePlayButton(true);
        }
    }

    public void setNotiBar(){
        if( getCurrentSong() == null || songs.size()<=0 ) return ;
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_no_album_sm);
        builder.setTicker("Awesome Music");
        builder.setWhen(System.currentTimeMillis());
        builder.setPriority(Notification.PRIORITY_MAX);
        noti = builder.build();
        //noti.flags = Notification.FLAG_NO_CLEAR;

        Intent intent1 = new Intent("play");
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, 0);
        Intent intent2 = new Intent("next");
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);
        Intent intent3 = new Intent("prev");
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(this, 0, intent3, 0);
        Intent intent4 = new Intent("noti");
        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(this, 0, intent4, 0);

        contentiew = new RemoteViews(getPackageName(), R.layout.notification_layout);
        contentiew.setOnClickPendingIntent(R.id.notiPlay, pendingIntent1);
        contentiew.setOnClickPendingIntent(R.id.notiNext, pendingIntent2);
        contentiew.setOnClickPendingIntent(R.id.notiPrev, pendingIntent3);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        noti.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        contentiew.setOnClickPendingIntent(R.id.noti1, pendingIntent);
        contentiew.setOnClickPendingIntent(R.id.noti2, pendingIntent);

        if( !isPlaying() ) {
            contentiew.setImageViewResource(R.id.notiPlay, R.drawable.ic_play_circle_outline_white_24dp);
        }else{
            contentiew.setImageViewResource(R.id.notiPlay, R.drawable.ic_pause_circle_outline_white_24dp);
        }

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, getCurrentSong().albumId);

        contentiew.setTextViewText(R.id.song_title2, getCurrentSong().title);
        contentiew.setTextViewText(R.id.artist_name2, getCurrentSong().artist);
        contentiew.setImageViewUri(R.id.album_art2, sAlbumArtUri);

        noti.contentView = contentiew;
        //startForeground : to prevent the service from stopping at later
        startForeground(1, noti); // first parameter(notification ID) should not be 0
        nm.notify(1, noti);
    }

    public void updateUInotNoti(){
        if(baseActivity != null && baseActivity.isActivityVisible())
            baseActivity.stateChangeMessageFromMP();
        if(lockScreenActivity != null && lockScreenActivity.isActivityVisible()){
            lockScreenActivity.stateChangeMessageFromMP();
        }
        if( isPlaying() ) {
            if (baseActivity != null && baseActivity.isActivityVisible())
                baseActivity.changePlayButton(false);
            if (lockScreenActivity != null && lockScreenActivity.isActivityVisible())
                lockScreenActivity.changePlayButton(false);
        }else{
            if (baseActivity != null && baseActivity.isActivityVisible())
                baseActivity.changePlayButton(true);
            if (lockScreenActivity != null && lockScreenActivity.isActivityVisible())
                lockScreenActivity.changePlayButton(true);
        }
    }
    public void stopNoti(){
        player.pause();
        isPaused = true;
        player.stop();
        dbHelper=null;
        stopForeground(true);
        nm.cancelAll();
    }
    public void setSongIndex(int songindex) {
        this.songPos = songindex;
    }
    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }
    public int getDur(){
        return player.getDuration();
    }
    public void seek(int posn){
        player.seekTo(posn);
        if( dbHelper != null )dbHelper.setStartPoint(posn);
    }
    public boolean isPlaying(){
        return player.isPlaying();
    }
    public boolean isShuffling(){ return shuffle; }
    public void setShuffle(){
        ArrayList<IDTag> shuffleList = new ArrayList<IDTag>();
        Random rand = new Random(System.currentTimeMillis());
        int randNum1 = rand.nextInt(100);
        int randNum2;
        int size;
        int pos;
        tempSongs = new ArrayList<IDTag>();
        tempSongs.addAll(songs);
        IDTag tmp = AwesomePlayer.instance.getCurrentSong();

        if( shuffle ){
            Collections.sort(tempSongs, new Comparator<IDTag>() {
                public int compare(IDTag obj1, IDTag obj2){
                    return (obj1.score > obj2.score) ? -1 : (obj1.score > obj2.score) ? 1 : 0;
                }
            });

            while( tempSongs.size() > 0 ) {
                size = tempSongs.size()/5;
                size += tempSongs.size()%5;
                randNum1 = rand.nextInt(100);
                if(randNum1 > 60 )      {   randNum2 = (rand.nextInt(size));} // 0 ~ size
                else if( randNum1 > 30 ){   randNum2 = (rand.nextInt(size)+size)        % tempSongs.size(); } // size ~
                else if( randNum1 > 15 ){   randNum2 = (rand.nextInt(size)+(size*2))    % tempSongs.size(); }
                else if( randNum1 > 5 ) {   randNum2 = (rand.nextInt(size)+(size*3))    % tempSongs.size(); }
                else                    {   randNum2 = (rand.nextInt(size)+(size*4))    % tempSongs.size(); }

                shuffleList.add(tempSongs.get(randNum2));
                tempSongs.remove(randNum2);
            }
            songs = shuffleList;
        }else{
            if( baseActivity != null ){
                baseActivity.setList();
            }
        }
        int i;
        for (i = 0; i < songs.size(); i++) {
            if (tmp.title.equals(songs.get(i).title) && tmp.artist.equals(songs.get(i).artist) && tmp.album.equals(songs.get(i).album))
                break;
        }
        AwesomePlayer.instance.setSongIndex(i);
        updateUIActivity();
    }
    public boolean isSongChanged() {
        return isSongChanged;
    }
    public void start() {
        player.start();
        setNotiBar();
        isPaused = false;

    }
    public void pausePlayer(){
        player.pause();
        isPaused = true;
        setNotiBar();
        stopForeground(true);
    }
    public void stopPlayer(){
        player.stop();
        dbHelper=null;
        setNotiBar();
        stopForeground(true);
    }

    public void increasePlayCount(){
        songs.get(songPos).playCount++;
    }
    @Override
    public void onDestroy() {
        player.release();
        stopForeground(true);
        unregisterReceiver(notiBroadcastReceiver);
        unregisterReceiver(earBroadcastReceiver);
        broadRegister=false;
        am.unregisterMediaButtonEventReceiver(mediaButton);
        super.onDestroy();

//        if(this.instance != null) instance.player.release();
    }
}


/*
    private void countSkip(){
        SQLiteDatabase db = UserDB.getInstance(getApplicationContext()).getDatabase();
        IDTag tag = getCurrentSong();

        String whereClause = "title='"+tag.title.replaceAll("'", "''")+"' AND " +
                "artist='"+tag.artist.replaceAll("'", "''")+"' AND " +
                "album='"+tag.album.replaceAll("'", "''")+"'";

        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_TABLE_NAME + " " +
                "WHERE " + whereClause , null);
        cursor.moveToFirst();
        int skip = cursor.getInt(cursor.getColumnIndex("skipcount"));

        if( getCurrentPosition()/1000 <= 30 ){
            //increase skip count
            ContentValues listRow = new ContentValues();
            listRow.put("skipcount", skip+1);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);

            //apply change to list if the skip count exceed threshold
            if( skip+1 >= 3 ){
                listRow = new ContentValues();
                listRow.put("skipflag",1);
                db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

                songs.remove(songPos);  //playList delete
                //pass event to UI
                if(baseActivity != null)
                    baseActivity.listChangeEvent(baseActivity);
            }
        }else{
            ContentValues listRow = new ContentValues();
            listRow.put("skipcount", 0);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);

            songs.get(songPos).playCount++;

            listRow = new ContentValues();
            listRow.put("playcount", songs.get(songPos).playCount);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);
            db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

            if(songs.get(songPos).skipFlag) {
                listRow = new ContentValues();
                listRow.put("skipflag", 0);
                db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

                songs.remove(songPos);

                if(baseActivity != null)
                    baseActivity.listChangeEvent(baseActivity);
            }
        }
        boolean isSkipped = (getDur() - getCurrentPosition())>0;
        if(dbHelper != null ) dbHelper.setSkipPoint(isSkipped, getCurrentPosition());
    }
*/