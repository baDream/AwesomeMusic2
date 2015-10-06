package kr.baggum.awesomemusic.Service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.UserDB;
import kr.baggum.awesomemusic.UI.Activity.MainActivity;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * MediaScan 클래스
 * MediaStore를 이용해 Media File을 스캔해
 * File Directory를 받아온 후 메타데이터를 추출해
 * Database에 저장해준다.
 */
public class MediaScan extends IntentService {

    private Context cxt;
    private SQLiteDatabase userdb;

    public static MediaScan instance;

    private int oldCount;
    private int newCount;
    private static MainActivity mainActivity;

    public MediaScan(){
        super("MediaScan");
    }

    private void updateUserDB(){
        Log.d("aaa", "updateUserDB");

        //userdb.rawQuery("TRUNCATE TABLE " + UserDB.SONG_LIST_NAME, null);
        userdb.delete(UserDB.SONG_LIST_NAME,null,null);
        Cursor cursor = userdb.rawQuery("SELECT * FROM " + UserDB.SONG_TABLE_NAME, null);

        Log.d("aaa", "# of metadatas from old version userDB : " + cursor.getCount());
        oldCount = cursor.getCount();
        //MediaStore를 읽어와 msSet에 넣는다.
        Cursor msCursor = this.cxt.getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC");

        userdb.beginTransaction();

        try {
            while (msCursor.moveToNext()) {
                String id= msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String path = msCursor.getString(msCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                String title = msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long albumId = msCursor.getLong(msCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM_ID));
                String duration = msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String date = msCursor.getString(msCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                String genre = "";

                String[] genresProjection = {
                        MediaStore.Audio.Genres.NAME,
                        MediaStore.Audio.Genres._ID
                };

                Log.i("aaa", path);

                Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", Integer.parseInt(id));
                Cursor genresCursor = this.getContentResolver().query(uri,
                        genresProjection, null, null, null);

                int genre_column_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);

                if (genresCursor.moveToFirst()) {
                    do {
                        if(genre.equals(""))
                            genre = genresCursor.getString(genre_column_index);
                        else
                            genre = genre + "/" +genresCursor.getString(genre_column_index);
                    } while (genresCursor.moveToNext());
                }
                genresCursor.close();


                IDTag tag = fillMetadataFromPath(path, title, artist, album);

                ContentValues listRow = new ContentValues();
                listRow.put("path", path);
                listRow.put("id", id);
                listRow.put("title", tag.title);
                listRow.put("artist", tag.artist);
                listRow.put("album", tag.album);
                listRow.put("albumid", albumId);
                listRow.put("duration", duration);
                listRow.put("date", date);
                listRow.put("genre", genre);

                String whereClause = "title='"+tag.title.replaceAll("'", "''")+"' AND " +
                        "artist='"+tag.artist.replaceAll("'", "''")+"' AND " +
                        "album='"+tag.album.replaceAll("'", "''")+"'";

                //미디어 스토어의 음악이 userDB에 있는지 검색한다.
                cursor = userdb.rawQuery("SELECT playcount, isdeleted, skipcount, score FROM " + UserDB.SONG_TABLE_NAME + " " +
                        "WHERE " + whereClause, null);

                //없을 경우 userDB에 삽입하고 msSet에도 삽입한다.
                //수정된 파일이므로 ismodified를 1로한다.
                if (cursor.getCount() <= 0) {
                    Log.i("aaa", "insert new song : " + tag.title);
                    ContentValues row = new ContentValues();

                    // NOTE Do not put a string including double quote.
                    // 'put' method can handle single quotes in a string!
                    row.put("title", tag.title);
                    row.put("artist", tag.artist);
                    row.put("album", tag.album);
                    row.put("genre", genre);
                    row.put("ismodified", 1);

                    userdb.insert(UserDB.SONG_TABLE_NAME, null, row);
                    userdb.insert(UserDB.SONG_LIST_NAME, null, listRow);
                } else {
                    //있을 경우 해당 파일이 지워졌던 것인지 조사한다.
                    cursor.moveToFirst();
                    boolean isDeleted = cursor.getInt(cursor.getColumnIndex("isdeleted"))>0;
                    int playCount = cursor.getInt(cursor.getColumnIndex("playcount"));
                    int score = cursor.getInt(cursor.getColumnIndex("score"));

                    listRow.put("playcount", playCount);
                    listRow.put("score", score);

                    //지워졌던 파일이면 지웠다가 다시 추가한 음악이므로 isdeleted 와 skipcount를 초기화하고
                    //한번 수정을 거친 자료이므로 ismodified를 1로한다.
                    if( isDeleted ){
                        Log.i("aaa", "update deleted file : " + tag.title);
                        ContentValues values = new ContentValues();
                        values.put("skipcount", 0);
                        values.put("isdeleted", 0);
                        values.put("ismodified",1);

                        userdb.update(UserDB.SONG_TABLE_NAME, values, whereClause, null );
                        userdb.insert(UserDB.SONG_LIST_NAME, null, listRow);
                    }else{
//                      Log.i("aaa", "file modified");
                        //지워진 파일이 아니면 skipcount를 조사해 유저가 안 듣는 노래인지 판단한다.
                        boolean skipFlag = cursor.getInt(cursor.getColumnIndex("skipcount"))>2;

                        //현재 있는 노래이므로 ismodified를 1로한다.
                        ContentValues values = new ContentValues();
                        values.put("ismodified",1);
                        userdb.update(UserDB.SONG_TABLE_NAME, values, whereClause, null );

                        //안 듣는 노래일 경우 msSet에 추가하지 않는다.
                        if( !skipFlag ){
//                          Log.i("aaa", "is not skip file");
                            userdb.insert(UserDB.SONG_LIST_NAME, null, listRow);
                        }else{
                            // for debugging
                            Log.i("aaa", "user doesn't like this song : " + tag.title);
                            listRow.put("skipflag", 1);
                            userdb.insert(UserDB.SONG_LIST_NAME, null, listRow);
                        }
                    }
                }
                cursor.close();
            }

//          Log.i("aaa", "is deleting");
            //ismodified가 0인 노래들을 찾아 현재 가지고 있지 않은 노래라고 판단해
            //전부 isdeleted를 1로해준다.
            ContentValues values1 = new ContentValues();
            values1.put("isdeleted", 1);
            userdb.update(UserDB.SONG_TABLE_NAME,values1,"ismodified=0",null);

/*            // for debugging
            String whereClause = "isdeleted = 1";

            //미디어 스토어의 음악이 userDB에 있는지 검색한다.
            cursor = userdb.rawQuery("SELECT * FROM " + UserDB.SONG_TABLE_NAME + " " +
                    "WHERE " + whereClause, null);

            while(cursor.moveToNext()){
                Log.d("aaa", "deleted file : " + cursor.getString(cursor.getColumnIndex("title")));
            }
            //// debugging end
*/
//                Log.i("aaa", "set modified");
            //다음번 실행시 정상 작동을 위해 모든 노래의 ismodified를 0으로 해준다.
            ContentValues values2 = new ContentValues();
            values2.put("ismodified", 0);
            userdb.update(UserDB.SONG_TABLE_NAME, values2, null, null);

            userdb.setTransactionSuccessful();
        }finally {
            userdb.endTransaction();
            Log.d("aaa", "Update user DB complete");

            MediaScannerReceiver.mediaSync =false;

            cursor = userdb.rawQuery("SELECT count(id) FROM " + UserDB.SONG_TABLE_NAME, null);
            cursor.moveToFirst();
            Log.d("aaa", "# of metadatas from new version userDB : " + cursor.getInt(0));
            newCount = cursor.getInt(0);

            Log.i("aaa", "finish Scan");
            //send complete event of DB scan to MainActivity
            if(mainActivity != null && mainActivity.isActivityVisible()) {
                Log.i("aaa", "refresh event of UI list triggered from MediaScan");
                mainActivity.listChangeEvent(mainActivity);
            }
        }
    }


    /********************************************************************/
    /*FileName에서 정해진 형식을 뽑아 메타데이터가 없는 파일에 메타데이터를 넣어준다.*/
    /*             매개변수 path는 음원 파일의 경로를 나타낸다.                */
    /********************************************************************/
    private IDTag setMetadataFromFileName(String path){
        File file = new File(path);         //path를 읽어 파일을 받아온다.
        String fileName = path.substring(path.lastIndexOf('/') + 1);   //path경로에서 파일 제목만 가져온다.(ex. storage/Music/aaa.mp3 -> aaa.mp3 )
        fileName = fileName.substring(0,fileName.lastIndexOf('.'));    //음원 확장자에서 음원 제목만 가져온다(ex. aaa.mp3 -> aaa )
        MusicMetadataSet srcSet = null;

        try {
            srcSet = new MyID3().read(file);  //file에서 ID3 Tag(Metadata)를 읽어온다.
        }catch(IOException io) {
            Log.i("aaa", "IOException");
            return new IDTag();
        }

        //ID3 Tag가 없을 경우 종료한다.
        if(srcSet == null ) return new IDTag();

        MusicMetadata meta = srcSet.merged;  //메타데이터 정보를 받아온다.
        meta = setMetadata(meta,fileName);   //메타데이터 정보를 수정한다.

        try{
            new MyID3().update(file,srcSet,meta);   //파일에 수정한 정보를 적용한다.
        }catch(UnsupportedEncodingException e){
            Log.i("ERROR", "UnsupportedEncodingException");
            e.printStackTrace();
        }catch(ID3WriteException e){
            Log.i("ERROR", "ID3WriteException");
            e.printStackTrace();
        }catch(IOException e){
            Log.i("ERROR", "IOException");
            e.printStackTrace();
        }

        return new IDTag(meta.getSongTitle(),meta.getArtist(),meta.getAlbum(),meta.getGenre());
    }

    /********************************************************************/
    /*  setMetadataFromFileName에서 호출되는 메소드로 타이틀, 가수, 앨범명이    */
    /*          비어있을 경우 Default인 unknown으로 초기화 시켜주거나          */
    /*    (가수 - 제목) 형식의 파일명일 경우 그걸 뽑아와 메타데이터를 저장시켜준다. */
    /*******************************************************************/
    private MusicMetadata setMetadata(MusicMetadata iMusicMetadata, String fileName){
        MusicMetadata meta = new MusicMetadata(fileName);
        int idx = fileName.lastIndexOf("-");  //파일명에서 마지막으로 등장하는 -의 index를 찾는다.

        String title = iMusicMetadata.getSongTitle();
        String artist = iMusicMetadata.getArtist();
        String album = iMusicMetadata.getAlbum();

        //파일명에 -가 존재할 경우 (가수 - 제목) 형식으로 값을 뽑아와서 각 Metadata가 null일때 초기화해준다.
        if( idx >= 0 ) {
            if ( title == null) {
                title = fileName.substring(idx+1);
            }
            if( artist == null){
                artist = fileName.substring(0,idx);
            }
            if( album == null){
                album = "unknown";
            }
        }else{ // (가수 - 제목) 형식이 아닐 경우 Metadata가 null이면 unknown으로 초기화해준다.
            if ( title == null) {
                title = fileName;
            }
            if( artist == null){
                artist = "unknown";
            }
            if( album == null){
                album = "unknown";
            }
        }
        meta.setSongTitle(title);
        meta.setArtist(artist);
        meta.setAlbum(album);

        return meta;
    }

    //FilePath에서 메타데이터를 추출하고 수정한다.
    private IDTag fillMetadataFromPath(String path, String title, String artist, String album){
        IDTag tag = new IDTag(path, title, artist, album);

        //메타데이터가 비었을 경우 초기화 해준다.
        if( title == null || album == null || artist == null)
            tag = setMetadataFromFileName(path);

        return tag;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MediaScan", "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mainActivity = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("aaa", "MediaScan Start : " + this);
        instance = this;
        cxt = getApplicationContext();
        userdb = UserDB.getInstance(cxt.getApplicationContext()).getDatabase();
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        Log.d("aaa", "MediaScan - onStartCommand()");

        updateUserDB();
    }

    @Override
    public void onDestroy() {
        Log.d("aaa", "Media scan - onDestroy()");

        //super.onDestroy();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public class mediaScanBinder extends Binder {
        public MediaScan getService() {
            return MediaScan.this;
        }
    }
}
