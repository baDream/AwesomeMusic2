package kr.baggum.awesomemusic.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * UserDB 클래스
 * Singleton pattern
 */

public class UserDB extends SQLiteOpenHelper {
    //한 개의 DB만 존재하기 위해 static으로 UserDB를 선언했다.
    private static UserDB userDB = null;

    public static final String DB_NAME = "userDB.db";
    public static final String SONG_TABLE_NAME = "userData";   //유저가 들었던 모든 노래
    public static final String SONG_LIST_NAME = "List";        //현재 가지고 있는 노래
    public static final String TIMELINE_NAME = "TimeLine";      //유저가 노래를 들은 히스토리
    public static final String PLAYLIST_NAME = "playlist";      //유저 플레이리스트

    private static final int DB_VERSION = 1;

    private static final String KEY_COLUMN = "_id";

    private static SQLiteDatabase db;

    private Context mCxt;
    //DB 생성부분 최초 한번만 실행된다.
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + SONG_TABLE_NAME + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT DEFAULT NULL," +
                "artist TEXT DEFAULT NULL," +
                "album TEXT DEFAULT NULL," +
                "genre TEXT DEFAULT NULL," +
                "playcount INTEGER DEFAULT 0," +
                "skipcount INTEGER DEFAULT 0," +
                "startpoint INTEGER DEFAULT 0, " +
                "endpoint INTEGER DEFAULT 0," +
                "startflag INTEGER DEFAULT 0," +
                "endflag INTEGER DEFAULT 0," +
                "isdeleted INTEGER DEFAULT 0," +
                "ismodified INTEGER DEFAULT 0," +
                "score INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + SONG_LIST_NAME + "(" +
                "path TEXT DEFAULT NULL," +
                "id TEXT DEFAULT NULL," +
                "title TEXT DEFAULT NULL," +
                "artist TEXT DEFAULT NULL," +
                "album TEXT DEFAULT NULL," +
                "albumid INTEGER DEFAULT 0," +
                "genre TEXT DEFAULT NULL," +
                "duration TEXT DEFAULT NULL," +
                "playcount INTEGER DEFAULT 0," +
                "date TEXT DEFAULT NULL," +
                "skipflag INTEGER DEFAULT 0," +
                "score INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + PLAYLIST_NAME + "(" +
                "playlist_name TEXT NOT NULL," +
                "song_id TEXT NOT NULL" + ")");

        db.execSQL("CREATE TABLE " + TIMELINE_NAME + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +       //타임라인 아이디      time line id
                "title TEXT DEFAULT NULL," +    //들은 노래 제목      what song name
                "artist TEXT DEFAULT NULL," +   //들은 노래의 가수     who song singer
                "album TEXT DEFAULT NULL," +    //들은 노래 앨범 제목   what song album title
                "albumid INTEGER DEFAULT 0," +
                "genre TEXT DEFAULT NULL," +    //들은 노래 장르      what song genre
                "year INTEGER DEFAULT 1992," +  //노래를 언제 들었는지 when you listen
                "month INTEGER DEFAULT 1," +  //노래를 언제 들었는지 when you listen
                "day INTEGER DEFAULT 1," +  //노래를 언제 들었는지 when you listen
                "listentime TEXT DEFAULT NULL," +  //노래를 들은 시작 시간 when start song
                "startpoint TEXT DEFAULT NULL," +  //노래를 들은 시작 시간 when start song
                "skippoint TEXT DEFAULT NULL," +   //노래를 몇분 듣고 스킵했는지 또는 노래의 끝시간  when you skip song duration or end song
                "skipflag INTEGER DEFAULT 0," +   //노래를 스킵했는지 Did Skipped?
                "issongpicked INTEGER DEFAULT 0)");   //노래를 유저가 선택한건지 Did You Select this song?
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

    //DB instance를 받아오는 메소드
    //이미 DB가 초기화 됐다면 userDB를 반환한다.
    public static UserDB getInstance(Context ctx){
        if( userDB == null ) {
            userDB = new UserDB(ctx.getApplicationContext());
            try{
                db = userDB.getWritableDatabase();
            }catch(SQLiteException se){

            }
        }
        return userDB;
    }

    public SQLiteDatabase getDatabase(){
        return db;
    }


    private UserDB( Context cxt){
        super(cxt, DB_NAME, null, DB_VERSION);
        this.mCxt = cxt;
    }

    public void close(){
        if( userDB != null ) {
            db.close();
            userDB = null;
        }
    }
    public Cursor getCursor(String table, String[] columns){
        return db.query(table, columns, null, null, null, null, null);
    }

    public Cursor getCursor(String table, String[] columns, long id){
        Cursor cursor = db.query(true, table, columns, KEY_COLUMN + "=" + id, null
        , null, null, null, null);
        if( cursor != null ){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getCursor(String sql){
        return db.rawQuery(sql, null);
    }
    public long insert(String table, ContentValues values){
        return db.insert(table, null, values);
    }
    public int update(String table, ContentValues values, long id){
        return db.update(table, values, KEY_COLUMN + "=" + id, null);
    }
    public int delete(String table, String whereClause){
        return db.delete(table, whereClause, null);
    }
    public int delete(String table, long id){
        return db.delete(table, KEY_COLUMN + "=" + id, null);
    }
    public void exec(String sql){
        db.execSQL(sql);
    }
}
