package kr.baggum.awesomemusic.UI.Activity;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.ListGenerator;
import kr.baggum.awesomemusic.Data.UserDB;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.Service.AwesomePlayer;
import kr.baggum.awesomemusic.Service.MediaScan;
import kr.baggum.awesomemusic.UI.View.FolderRecyclerAdapter;
import kr.baggum.awesomemusic.UI.View.IconPageIndicator;
import kr.baggum.awesomemusic.UI.View.PageIndicator;
import kr.baggum.awesomemusic.UI.View.TestFragment;
import kr.baggum.awesomemusic.UI.View.TestFragmentAdapter;
import kr.baggum.awesomemusic.UI.View.TimeLineRecyclerAdapter;
import kr.baggum.awesomemusic.UI.View.TitleRecyclerAdapter;
import kr.baggum.awesomemusic.UI.library.MarqueeText;
import kr.baggum.awesomemusic.UI.library.SlidingDownPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.text.NumberFormat;
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.BlurTransformation;


public class MainActivity extends ActionBarActivity {

    // UI fields
    ImageView ivAlbumArt;
    MarqueeText tvTitle;
    MarqueeText tvArtist;

    SeekBar seekBar;
    SeekBar mMainMusicSeekBar;
    SeekBar mSoundSeekBar;

    //Seek Bar pos
    int songSeek;
    Thread seekBarChecker;

    ImageView mPlayImgViewblur;
    ImageView mPlayImgViewMain;

    TestFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    SlidingDownPanelLayout slidingPanelLayout = null;

    // back, play, next button
    ImageView mPrevButton;
    ImageView mPlayPauseButton;
    ImageView mNextButton;

    ImageView mSoundButton;
    ImageView mNoSoundButton;

    //Replay and Shuffle
    ImageView mReplay;
    ImageView mShuffle;

    //main Acticity Text
    TextView mMainName;
    TextView mMainArtist;

    ImageView mUpButton;

    //seekbar Maxtime
    int mSeekMaxTime;
    int mSeekMaxTimeMin;
    int mSeekMaxTimeSec;

    int mSeekProTime;
    int mSeekProTimeMin;
    int mSeekProTimeSec;

    TextView SeekbarMaxTime;
    TextView SeekbarProcessTime;

    // Audio
    AudioManager am = null;
    int StreamType = AudioManager.STREAM_MUSIC;

    // music player fields
    private boolean isFirstPlay;
    private final int SEEKBAR_TIME_SLICE = 50;
    private ServiceConnection musicConnection;

    //activity fields
    private boolean isActivityVisible;


    //DB fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_icons);

        //actionbar hide(); you can change actionbar at here! by Jun
        ActionBar actionbar = getSupportActionBar();
        actionbar.hide();

//        //change the image color to transparency. by jun
//        Drawable alpha = ((ImageView)findViewById(R.id.mini_play_view_albumart)).getDrawable();
//        alpha.setAlpha(50);   this.g

        mAdapter = new TestFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mPager.setOffscreenPageLimit(9);

        mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        //sliding Layout
        slidingPanelLayout = (SlidingDownPanelLayout) findViewById(R.id.sliding_layout);

        //slidingPanelLayout.setSliderFadeColor(Color.argb(128, 0, 0, 0));
        //slidingPanelLayout.setParallaxDistance(100);

        slidingPanelLayout.setPanelSlideListener(new SlidingDownPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelOpened(View panel) {
                init();
                mPrevButton.setClickable(true);
                mPlayPauseButton.setClickable(true);
                mNextButton.setClickable(true);
                mMainMusicSeekBar.setEnabled(true);
                mSoundSeekBar.setEnabled(true);
            }

            @Override
            public void onPanelClosed(View panel) {
                mPrevButton.setClickable(false);
                mPlayPauseButton.setClickable(false);
                mNextButton.setClickable(false);
                mMainMusicSeekBar.setEnabled(false);
                mSoundSeekBar.setEnabled(false);
            }
        });

        //get UI components in mini play view
        ivAlbumArt = (ImageView) findViewById(R.id.mini_play_view_albumart);
        tvTitle = (MarqueeText) findViewById(R.id.mini_play_view_title);
        tvArtist = (MarqueeText) findViewById(R.id.mini_play_view_artist);
        seekBar = (SeekBar) findViewById(R.id.mini_play_view_seekbar);
        mMainMusicSeekBar = (SeekBar) findViewById(R.id.mini_play_view_seekbar2);
        mSoundSeekBar = (SeekBar) findViewById(R.id.mini_sound_seekBar);

        tvTitle.setSingleLine(true);
        tvTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTitle.setMarqueeRepeatLimit(-1);

        tvArtist.setSingleLine(true);
        tvArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvArtist.setMarqueeRepeatLimit(-1);

        mPlayImgViewblur = (ImageView) findViewById(R.id.play_imageView_blur);
        mPlayImgViewMain = (ImageView) findViewById(R.id.play_imageView_main);

        //Main Buttons
        mPrevButton = (ImageView) findViewById(R.id.ic_back_button);
        mPlayPauseButton = (ImageView) findViewById(R.id.ic_play_button);
        mNextButton = (ImageView) findViewById(R.id.ic_next_button);

        mSoundButton = (ImageView) findViewById(R.id.ic_sound);
        mNoSoundButton = (ImageView) findViewById(R.id.ic_nosound);

        mUpButton = (ImageView) findViewById(R.id.action_up);

        //replay and shuffle button
        mReplay = (ImageView) findViewById(R.id.ic_replay_button);
        mShuffle = (ImageView) findViewById(R.id.ic_shuffle_button);

        //Main Text
        mMainName = (TextView) findViewById(R.id.main_activity_music_name);
        mMainArtist = (TextView) findViewById(R.id.main_activity_music_artist);

        //Seekbar Time
        SeekbarMaxTime = (TextView) findViewById(R.id.SeekbarMaxTime);
        SeekbarProcessTime = (TextView) findViewById(R.id.SeekbarProcessTime);

        mMainName.setSingleLine(true);
        mMainName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mMainName.setMarqueeRepeatLimit(-1);

        mMainArtist.setSingleLine(true);
        mMainArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mMainArtist.setMarqueeRepeatLimit(-1);

        isFirstPlay = true;

        //init!!
        init();

        //set SeekbarChangeListener
        setSeekbarChangeListener();

        //set Buttons
        setPlayViewButton();

        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("aaa", "onServiceConnected");

                //get Service
                AwesomePlayer.MusicBinder binder = (AwesomePlayer.MusicBinder) service;
                AwesomePlayer.instance = binder.getService();

                //set context of this activity to take callback
                AwesomePlayer.instance.setBaseActivity(MainActivity.this);

                runSeekBarThread(SEEKBAR_TIME_SLICE);

                if (AwesomePlayer.instance != null && !AwesomePlayer.instance.doesHasSongList()) { // first running
                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());

                    //TODO change tabIndex to tab ID (genre, artist)
                    //TODO song index have a weakness. if user change some song(add, delete, move,..)
                    int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
                    int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

                    if (tabIndex != -1 && songIndex != -1) {
                        //get song list from DB

                        ArrayList<IDTag> songList = null;

                        switch (tabIndex) {
                            case TestFragment.TITLE:
                                songList = ListGenerator.getAllSongList(getApplicationContext());
                                break;
                            case TestFragment.SKIP:
                                songList = ListGenerator.getSkipSongList(getApplicationContext());
                                break;
                            case TestFragment.FOLDER:
                                String lastPath = appSharedPrefs.getString("LASTSONG_PATH", null);
                                songList = ListGenerator.getAllSongsInPathWithoutChild(getApplicationContext(), lastPath);

                                TestFragment lastFragment = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
                                lastFragment.folderAdapter.moveIntoSpecificFolder(lastPath.substring(lastPath.lastIndexOf("/") + 1), lastPath.substring(1, lastPath.length()));
                                break;
                            case TestFragment.RECENT:
                                songList = ListGenerator.getRecentlyAddedList(getApplicationContext());
                                break;
                            default:
                                songList = null;

                        }
                        //exception handling
                        if (songList != null && songList.size() > 0) {

                            if (songIndex >= songList.size())
                                songIndex = 0;

                            AwesomePlayer.instance.setSongIndex(songIndex);
                            AwesomePlayer.instance.setSongs(songList);
                        } else {
                            //no operation
                        }
                    }

                }

                //ToDo 옵션불러오기
                loadOption();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AwesomePlayer.instance.setIsMusicBound(false);
                Log.d("aaa", "onServiceDisconnected");
            }
        };

        if (MediaScan.instance != null) {
            MediaScan.instance.setMainActivity(this);
        }

        if (AwesomePlayer.instance != null) {
            runSeekBarThread(SEEKBAR_TIME_SLICE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityVisible = false;
//        stopService(playIntent);
        unbindService(musicConnection);
        if (MediaScan.instance != null) {
            MediaScan.instance.setMainActivity(null);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityVisible = true;

//        Log.d("aaa", "activity onStart");
        Intent playIntent = new Intent(this, AwesomePlayer.class);

        ComponentName startResult = startService(playIntent);
        boolean bindResult = bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;

        // when notification bar touched
        if (AwesomePlayer.instance != null && AwesomePlayer.instance.isPlaying()) {
            updateMiniPlayView(AwesomePlayer.instance.getCurrentSong());
            setPlayActivity(AwesomePlayer.instance.getCurrentSong());

            //make preview component to transparent
            tvTitle.setTextColor(Color.argb(1, 0, 0, 0));
            tvArtist.setTextColor(Color.argb(1, 0, 0, 0));
            ivAlbumArt.setImageAlpha(Color.argb(1, 0, 0, 0));

            slidingPanelLayout.openPane();

            //껏다켯을때 버튼 상태들
            stateButton();
        }
    }


    @Override
    public void onBackPressed() {
        //sliding panel up
        if (slidingPanelLayout.isOpen()) {
            slidingPanelLayout.closePane();
            return;
        }
        switch (mPager.getCurrentItem()) {
            case TestFragment.TITLE:
            case TestFragment.ALBUM:
            case TestFragment.ARTIST:
            case TestFragment.GRAPH:
            case TestFragment.RECENT:
            case TestFragment.MOSTPLAYED:
            case TestFragment.SKIP:
            case TestFragment.TIMELINE:
                super.onBackPressed();
                break;
            case TestFragment.FOLDER:
                TestFragment a = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());

                if (a.folderAdapter.backupTree.size() != 0) {
                    a.folderAdapter.setUpdate();
                } else {
                    super.onBackPressed();
                }
                break;
        }
    } // in MyActivit

    @Override
    protected void onUserLeaveHint() {
        finish();
        super.onUserLeaveHint();
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    public void updateSongInfo2UI() {
        //if no last song
        //if there is last song
        //if playing

        //TODO load song info in case of running service
        //load last song info
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        //TODO change tabIndex to tab ID (genre, artist)
        //TODO song index have a weakness. if user change some song(add, delete, move,..)
        int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
        int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

        if (tabIndex != -1 && songIndex != -1) {
            //init music player

            //get song list from pager
            TestFragment lastFragment = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
            ArrayList<IDTag> songList = lastFragment.titleAdapter.getSongList();

            AwesomePlayer.instance.setSongIndex(songIndex);
            AwesomePlayer.instance.setSongs(songList);

            //init UI(mini preview, tab focus
            updateMiniPlayView(songList.get(songIndex));
            mIndicator.onPageSelected(tabIndex);
        }
    }

    // when music player is ready to play
    public void stateChangeMessageFromMP() {

        IDTag currentSongTag = AwesomePlayer.instance.getCurrentSong();

        updateMiniPlayView(currentSongTag);
        setPlayActivity(currentSongTag);
        slidingPanelLayout.openPane();

        //버튼상태 변경
        stateButton();

//        //move scroll to position of currently playing song
//        TestFragment tf = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
//
//        if (tf != null && tf.isFinish()) {
//            LinearLayoutManager llm = (LinearLayoutManager) tf.recyclerView.getLayoutManager();
//
//            if (llm != null)
//                llm.scrollToPositionWithOffset(AwesomePlayer.instance.getSongPos(), 400); // TODO offset hardcoded
//        }

        //TODO save song as last played song
        //Things to save - list, index, tab ID
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

        //TODO change tabIndex to tab ID (genre, artist)
        int lastTabIndex = mPager.getCurrentItem();
        prefsEditor.putInt("LASTSONG_TAB_INDEX", lastTabIndex);

        //last tab is folder -> save path
        if (lastTabIndex == TestFragment.FOLDER) {
            String currentSongPath = currentSongTag.path;
            int lastSlashIndex = currentSongPath.lastIndexOf("/");
            // /storage/emulated/0/Download/a.mp3 -> // /storage/emulated/0/Download
            String lastSongPath = currentSongPath.substring(0, lastSlashIndex);

            prefsEditor.putString("LASTSONG_PATH", lastSongPath);
        }

        prefsEditor.putInt("LASTSONG_SONG_INDEX", AwesomePlayer.instance.getSongPos());
        prefsEditor.apply(); //commit -> foreground, apply -> background
    }

    public void songPicked(ArrayList<IDTag> list, int index) {
        if(mPager.getCurrentItem() == TestFragment.SKIP){
            //TODO restore skipped song into non-skipped song
            SQLiteDatabase db = UserDB.getInstance(getApplicationContext()).getDatabase();
            IDTag tag = list.get(index);

            String whereClause = "title='"+tag.title.replaceAll("'", "''")+"' AND " +
                    "artist='"+tag.artist.replaceAll("'", "''")+"' AND " +
                    "album='"+tag.album.replaceAll("'", "''")+"'";

            //set this song is NOT skipped
            ContentValues listRow = new ContentValues();
            listRow.put("skipflag", 0);
            db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

            //reset skip count
            listRow = new ContentValues();
            listRow.put("skipcount", 0);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);

            //refresh UI
            listChangeEvent(this);
            return;
        }

        AwesomePlayer.instance.setSongs(list);
        AwesomePlayer.instance.setSongIndex(index);
        AwesomePlayer.instance.isPicked = true;
        AwesomePlayer.instance.playSong();

        if (AwesomePlayer.instance.shuffle) {
            AwesomePlayer.instance.setShuffle();
            stateChangeMessageFromMP();
        }
    }

    public void updateMiniPlayView(IDTag currentSong) {
        tvTitle.setText(currentSong.title);
        tvArtist.setText(currentSong.artist);

        mMainName.setText(currentSong.title);
        mMainArtist.setText(currentSong.artist);

        //set album art
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.albumId);
        RequestCreator rc = Picasso.with(this).load(sAlbumArtUri);
        rc.placeholder(ivAlbumArt.getDrawable());
        rc.error(R.drawable.ic_no_album_sm);
        rc.into(ivAlbumArt);

        runSeekBarThread(SEEKBAR_TIME_SLICE);

//        //set seek bar
//        if(isFirstPlay){
//            runSeekBarThread(SEEKBAR_TIME_SLICE);
//            isFirstPlay = false;
//            Log.d("aaa","# of active thread : " + Thread.activeCount());
//        }
    }

    private void runSeekBarThread(final int timeSlice) {
        Runnable checkProgress = new Runnable() {
            @Override
            public void run() {

                int currentPos = 0;
                Log.d("aaa", "# of thread : " + Thread.activeCount());
//                Log.d("aaa","thread : " + Thread.currentThread());
                while (isActivityVisible) {
                    try {
                        if (AwesomePlayer.instance != null && AwesomePlayer.instance.isPlaying()) {
                            mMainMusicSeekBar.setMax(AwesomePlayer.instance.getDur());
                            seekBar.setMax(AwesomePlayer.instance.getDur());

                            currentPos = AwesomePlayer.instance.getCurrentPosition();
                            mMainMusicSeekBar.setProgress(currentPos);
                            seekBar.setProgress(currentPos);

                            mSeekProTimeMin = currentPos / 60000;
                            mSeekProTimeSec = (currentPos % 60000) / 1000;


                            //chage UI
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);

                            Thread.sleep(timeSlice);
                        } else
                            Thread.sleep(timeSlice);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
//                Log.d("aaa","seek thread dead : "+ Thread.currentThread());
            }
        };

//        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
//
//        for(Thread t : threadArray){
//            Log.d("aaa", String.valueOf(t));
//        }

        if (seekBarChecker == null) { // only one thread allowed to check seek bar
            seekBarChecker = new Thread(checkProgress);
            seekBarChecker.start();
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //자리수 맞추기
            NumberFormat numberFormat = NumberFormat.getIntegerInstance();
            numberFormat.setMinimumIntegerDigits(2);
            SeekbarProcessTime.setText(numberFormat.format(mSeekProTimeMin) + ":" + numberFormat.format(mSeekProTimeSec));

            mSeekMaxTime = seekBar.getMax();
            mSeekMaxTimeMin = mSeekMaxTime / 60000;
            mSeekMaxTimeSec = (mSeekMaxTime % 60000) / 1000;
            SeekbarMaxTime.setText(numberFormat.format(mSeekMaxTimeMin) + ":" + numberFormat.format(mSeekMaxTimeSec));

        }
    };

    public void setList() {
        TestFragment a;
        ArrayList<IDTag> list;

        switch (mPager.getCurrentItem()) {
            case TestFragment.TITLE:
            case TestFragment.ALBUM:
            case TestFragment.ARTIST:
            case TestFragment.SKIP:
            case TestFragment.RECENT:
            case TestFragment.MOSTPLAYED:
            case TestFragment.GRAPH:
                a = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                list = a.titleAdapter.getSongList();
                break;
            case TestFragment.FOLDER:
                a = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                list = a.folderAdapter.getSongList();
                break;
            default:
                list = ListGenerator.getAllSongList(getApplicationContext());
        }
        AwesomePlayer.instance.setSongs(list);
    }

    public void awesomeShuffle(View v) {
        AwesomePlayer.instance.shuffle = true;
        TestFragment a;
        ArrayList<IDTag> list;
        switch (mPager.getCurrentItem()) {
            case TestFragment.TITLE:
            case TestFragment.ALBUM:
            case TestFragment.ARTIST:
            case TestFragment.SKIP:
            case TestFragment.RECENT:
            case TestFragment.MOSTPLAYED:
            case TestFragment.GRAPH:
                a = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                list = a.titleAdapter.getSongList();
                break;
            case TestFragment.FOLDER:
                a = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                list = a.folderAdapter.getSongList();
                break;
            default:
                list = ListGenerator.getAllSongList(getApplicationContext());
        }

        AwesomePlayer.instance.setSongs(list);

        AwesomePlayer.instance.setShuffle();
        AwesomePlayer.instance.setSongIndex(0);
        AwesomePlayer.instance.playSong();
        stateChangeMessageFromMP();
    }

    //reload list
    public void listChangeEvent(final MainActivity baseActivity) {
        for (int i = 0; i < mPager.getChildCount(); i++) {
            final TestFragment tf = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + i);

            if (tf == null || !tf.isFinish()) return; //Fragment does not finish generating list

            final int finalI = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (finalI) {
                        case TestFragment.TITLE:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getAllSongList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());

                            tf.titleAdapter.notifyDataSetChanged();

                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;/*
                        case TestFragment.ARTIST:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getSkipSongList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;
                        case TestFragment.ALBUM:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getRecentlyAddedList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;*/
                        case TestFragment.GRAPH:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getRankingList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;

                        case TestFragment.SKIP:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getSkipSongList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;
                        case TestFragment.RECENT:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getRecentlyAddedList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;
                        case TestFragment.MOSTPLAYED:
                            tf.titleAdapter = new TitleRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                                    ListGenerator.getMostPlayedList(getApplicationContext()),
                                    (LinearLayoutManager) tf.recyclerView.getLayoutManager());
                            tf.titleAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.titleAdapter);
                            break;
                        case TestFragment.FOLDER:
                            //load last song info
                            SharedPreferences appSharedPrefs = PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext());

                            String lastPath = appSharedPrefs.getString("LASTSONG_PATH", null);

                            if(lastPath == null) break;
//                            ArrayList<IDTag> songList = ListGenerator.getAllSongsInPathWithoutChild(getApplicationContext(), lastPath);

//                            TestFragment lastFragment = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + TestFragment.FOLDER);
                            tf.folderAdapter = new FolderRecyclerAdapter(baseActivity, ListGenerator.getDirectoryList(baseActivity.getApplicationContext()));
                            tf.folderAdapter.moveIntoSpecificFolder(lastPath.substring(lastPath.lastIndexOf("/") + 1), lastPath.substring(1, lastPath.length()));

//                            tf.folderAdapter = new FolderRecyclerAdapter(getApplicationContext(), ListGenerator.getDirectoryList(getApplicationContext()));
                            tf.folderAdapter.notifyDataSetChanged();
                            tf.recyclerView.setAdapter(tf.folderAdapter);
                            break;
                    }
                }
            });

        }
    }

    public void timelineChangeEvent(final MainActivity baseActivity) {
        //reload list
        final TestFragment tf = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + TestFragment.TIMELINE);

        if (tf == null || !tf.isFinish()) return; //Fragment does not finish generating list

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tf.timelineAdapter = new TimeLineRecyclerAdapter(baseActivity, // getApplication 쓰면 에러남.. (android.app.Application 반환 됨).
                        ListGenerator.getTimelineList(getApplicationContext()),
                        (LinearLayoutManager) tf.recyclerView.getLayoutManager());

                tf.timelineAdapter.notifyDataSetChanged();

                tf.recyclerView.setAdapter(tf.timelineAdapter);

            }
        });
    }

    public void setPlayActivity(IDTag songIDTag) {
        if (songIDTag == null) return;
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri, songIDTag.albumId);

        Transformation transformation = new BlurTransformation(getApplicationContext(), 25, 4);

        Picasso.with(getApplicationContext()).load(sAlbumArtUri).
                placeholder(mPlayImgViewblur.getDrawable()).
                error(R.drawable.background_blur).
                transform(transformation).
                into(mPlayImgViewblur);

        Picasso.with(getApplicationContext()).load(sAlbumArtUri).
                placeholder(mPlayImgViewMain.getDrawable()).
                error(R.drawable.ic_no_album_hd).
                into(mPlayImgViewMain);
    }

    public boolean isActivityVisible() {
        return isActivityVisible;
    }

    public void loadLastSong() {
        //load last song list and index
        //update preview, play view
        //if no last song
        //if there is last song
        //if playing

        //load last song info
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        //TODO change tabIndex to tab ID (genre, artist)
        //TODO song index have a weakness. if user change some song(add, delete, move,..)
        int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
        int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

        if (tabIndex != -1 && songIndex != -1) {
            //get song list from DB

            ArrayList<IDTag> songList = null;

            switch (tabIndex) {
                case TestFragment.TITLE:
                    songList = ListGenerator.getAllSongList(getApplicationContext());
                    break;
                case TestFragment.SKIP:
                    songList = ListGenerator.getSkipSongList(getApplicationContext());
                    break;
                case TestFragment.FOLDER:
                    String lastPath = appSharedPrefs.getString("LASTSONG_PATH", null);
                    songList = ListGenerator.getAllSongsInPathWithoutChild(getApplicationContext(), lastPath);

                    TestFragment lastFragment = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
                    lastFragment.folderAdapter.moveIntoSpecificFolder(lastPath.substring(lastPath.lastIndexOf("/") + 1), lastPath.substring(1, lastPath.length()));
                    break;
                case TestFragment.RECENT:
                    songList = ListGenerator.getRecentlyAddedList(getApplicationContext());
                    break;
                default:
                    songList = null;

            }

                        /*  Exception handling
                1.songList.size() = 0 : no song, no operation
                2.songList.size() <= songIndex : set first song
             */
            if (songList != null && songList.size() > 0) {

                if (songIndex >= songList.size())
                    songIndex = 0;

                updateMiniPlayView(songList.get(songIndex));
                setPlayActivity(songList.get(songIndex));

//                //move scroll to position of currently playing song
//                TestFragment lastTF = (TestFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
//                LinearLayoutManager llm = (LinearLayoutManager) lastTF.recyclerView.getLayoutManager();
//
//                //TODO support folder(does not have llm), support folder + song index
//                if (llm != null)
//                    llm.scrollToPositionWithOffset(songIndex, 400); // TODO offset hardcoded

                slidingPanelLayout.openPane();

            } else {
                //no operation
            }

            mIndicator.onPageSelected(tabIndex);


//            //init music player
//            AwesomePlayer.instance.setSongIndex(songIndex);
//            AwesomePlayer.instance.setSongs(songList);

//            //init UI(mini preview, tab focus, selected song focus)
//            if ( songList != null && songList.size()>songIndex && songList.size() > 0) {
//                updateMiniPlayView(songList.get(songIndex));
//            }
//            mIndicator.onPageSelected(tabIndex);
//
//            //move scroll to position of currently playing song
//            TestFragment tf = (TestFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
//            RecyclerView recyclerView = tf.recyclerView;
//            recyclerView.getLayoutManager();
//            LinearLayoutManager llm = (LinearLayoutManager) tf.recyclerView.getLayoutManager();
//
//            //TODO support folder(does not have llm), support folder + song index
//            if(llm != null && songIndex >= 0)
//                llm.scrollToPositionWithOffset(songIndex, 400); // TODO offset hardcoded
//
//            if (songList != null && songIndex >= 0) {
//                setPlayActivity(songList.get(songIndex));
//            }
//            slidingPanelLayout.openPane();
//        }
        }

    }


    public void setPlayViewButton() {

        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingPanelLayout.closePane();
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AwesomePlayer.instance.isPlaying()) {
                    AwesomePlayer.instance.playPrev();
                } else {
                    AwesomePlayer.instance.prevSong();
                }
                AwesomePlayer.instance.updateUIActivity();
            }
        });

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AwesomePlayer.instance.isPlaying()) { // pause
                    AwesomePlayer.instance.pausePlayer();

                } else if (AwesomePlayer.instance.doesHasSongList()) { //currently not playing, but player has been initialized (can be played)
                    if (AwesomePlayer.instance.isPaused && !AwesomePlayer.instance.isSongChanged()) // paused state, resume play
                        AwesomePlayer.instance.start();
                    else // new song is selected, play from first
                        AwesomePlayer.instance.playSong();
                }
                AwesomePlayer.instance.updateUIActivity();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AwesomePlayer.instance.isPlaying()) {
                    AwesomePlayer.instance.playNext();
                } else {
                    AwesomePlayer.instance.nextSong();
                }
                AwesomePlayer.instance.updateUIActivity();
            }
        });

        mReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO CHANGE IMAGE !
                switch (AwesomePlayer.instance.replay) {
                    case AwesomePlayer.NOREPLAY:
                        AwesomePlayer.instance.replay = AwesomePlayer.instance.ALLREPLAY;
                        mReplay.setImageResource(R.drawable.ic_action_replay_seleted);
                        break;
                    case AwesomePlayer.ALLREPLAY:
                        AwesomePlayer.instance.replay = AwesomePlayer.instance.SINGLEREPLAY;
                        mReplay.setImageResource(R.drawable.ic_action_replay_one);
                        break;
                    case AwesomePlayer.SINGLEREPLAY:
                        AwesomePlayer.instance.replay = AwesomePlayer.instance.NOREPLAY;
                        mReplay.setImageResource(R.drawable.ic_action_replay);
                        break;
                }
                saveOption();
            }
        });

        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AwesomePlayer.instance.isShuffling()) {
                    AwesomePlayer.instance.shuffle = false;
                    AwesomePlayer.instance.setShuffle();
                    mShuffle.setSelected(false);
                    //mShuffle.setImageResource(R.drawable.);   //Image change turn off
                } else {
                    AwesomePlayer.instance.shuffle = true;
                    AwesomePlayer.instance.setShuffle();
                    mShuffle.setSelected(true);
                    //mShuffle.setImageResource(R.drawable.);   //Image change turn on
                }
                //AwesomePlayer.instance.setShuffle();
                saveOption();
            }
        });
        mSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumUP();
            }
        });
        mNoSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumnDOWN();
            }
        });
    }

    public void changePlayButton(boolean bool) {
        if (bool)
            mPlayPauseButton.setSelected(false);
        else {
            mPlayPauseButton.setSelected(true);
        }
    }

    public void changeSeekBarProcess() {

        //자리수 맞추기
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        numberFormat.setMinimumIntegerDigits(2);

        //progress init()
        seekBar.setProgress(0);
        //Seekbar change
        mSeekMaxTime = seekBar.getMax();
        mSeekMaxTimeMin = mSeekMaxTime / 60000;
        mSeekMaxTimeSec = (mSeekMaxTime % 60000) / 1000;
        SeekbarMaxTime.setText(numberFormat.format(mSeekMaxTimeMin) + ":" + numberFormat.format(mSeekMaxTimeSec));
    }

    public void stateButton() {

        switch (AwesomePlayer.instance.replay) {
            case AwesomePlayer.NOREPLAY:
                mReplay.setImageResource(R.drawable.ic_action_replay);
                break;
            case AwesomePlayer.ALLREPLAY:
                mReplay.setImageResource(R.drawable.ic_action_replay_seleted);
                break;
            case AwesomePlayer.SINGLEREPLAY:
                mReplay.setImageResource(R.drawable.ic_action_replay_one);
                break;
        }

        if (AwesomePlayer.instance.isPlaying()) {
            mPlayPauseButton.setSelected(true);
        } else {
            mPlayPauseButton.setSelected(false);
        }

        if (AwesomePlayer.instance.isShuffling()) {
            mShuffle.setSelected(true);
        } else {
            mShuffle.setSelected(false);
        }
    }

    public void VolumUP() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currVol = am.getStreamVolume(StreamType);
        int maxVol = am.getStreamMaxVolume(StreamType);
        //Log.d("VOL",  "CURRENT VOL = " + currVol + ", MAX VOL = " + maxVol);
        if (currVol < maxVol) {
            am.setStreamVolume(StreamType, currVol + 1, AudioManager.FLAG_PLAY_SOUND);
            mSoundSeekBar.setProgress(am.getStreamVolume(StreamType));
        }
    }

    public void VolumnDOWN() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currVol = am.getStreamVolume(StreamType);
        //Log.d("VOL",  "CURRENT VOL = " + currVol);
        if (currVol > 0) {
            am.setStreamVolume(StreamType, currVol - 1, AudioManager.FLAG_PLAY_SOUND);
            mSoundSeekBar.setProgress(am.getStreamVolume(StreamType));
        }
    }

    public void init() {

        setVolumeControlStream ( AudioManager.STREAM_MUSIC );
        //Sound Seekbar init
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //set Max volum
        mSoundSeekBar.setMax(am.getStreamMaxVolume(StreamType));
        //set Progress
        mSoundSeekBar.setProgress(am.getStreamVolume(StreamType));
    }

    public void setSeekbarChangeListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    songSeek = progress;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //AwesomePlayer.instance.seek(songSeek);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                AwesomePlayer.instance.seek(songSeek);
            }
        });
        mMainMusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    songSeek = progress;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //AwesomePlayer.instance.seek(songSeek);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                AwesomePlayer.instance.seek(songSeek);
            }
        });
        mSoundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                am.setStreamVolume(StreamType, progress, 0);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    public boolean onKeyDown(int keycode, KeyEvent event){

        switch(keycode)
        {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(slidingPanelLayout.isOpen()) {
                    VolumnDOWN();
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
                if(slidingPanelLayout.isOpen()){
                    VolumUP();
                    return true;
                }
                break;
        }
        return super.onKeyDown(keycode, event);
    }

    public void saveOption(){
        SharedPreferences pref = getSharedPreferences("option", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("suffle", AwesomePlayer.instance.isShuffling());
        editor.putInt("replay", AwesomePlayer.instance.replay); //키값, 저장값
        editor.commit();
    }

    public void loadOption(){
        SharedPreferences prefs =getSharedPreferences("option", MODE_PRIVATE);
        AwesomePlayer.instance.replay = (short) prefs.getInt("replay", AwesomePlayer.instance.replay);
        AwesomePlayer.instance.shuffle = prefs.getBoolean("suffle", AwesomePlayer.instance.shuffle);
        stateButton();
    }

    public PageIndicator getIndicator(){
        return mIndicator;
    }
}
