package kr.baggum.awesomemusic.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import kr.baggum.awesomemusic.Data.IDTag;
import kr.baggum.awesomemusic.Data.UserDB;
import kr.baggum.awesomemusic.Service.AwesomePlayer;

import java.util.Calendar;

/**
 * Created by user on 15. 8. 17.
 */

//songpick 100
//just listend 50
//skip -20
public class AwesomeDBHelper {
    //TODO Implement CountSkip and more DB WRITE METHOD

    private Context cxt;
    private ContentValues timeline;

    public AwesomeDBHelper(Context context){
        cxt = context;
        timeline = new ContentValues();
    }

    public void countSkip(int pos){
        SQLiteDatabase db = UserDB.getInstance(cxt).getDatabase();
        IDTag tag = AwesomePlayer.instance.getCurrentSong();

        String whereClause = "title='"+tag.title.replaceAll("'", "''")+"' AND " +
                "artist='"+tag.artist.replaceAll("'", "''")+"' AND " +
                "album='"+tag.album.replaceAll("'", "''")+"'";

        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_TABLE_NAME + " " +
                "WHERE " + whereClause , null);
        cursor.moveToFirst();
        int skip = cursor.getInt(cursor.getColumnIndex("skipcount"));

        Log.i("ccc", "what : " + pos);

        if( pos/1000 <= 30 ){
            //increase skip count
            Log.i("ccc", "what : ");
            ContentValues listRow = new ContentValues();
            listRow.put("skipcount", skip+1);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);

            //apply change to list if the skip count exceed threshold
            if( skip+1 >= 3 ){
                Log.d("aaa","hi you are delted!");
                listRow = new ContentValues();
                listRow.put("skipflag",1);
                db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

                AwesomePlayer.instance.removeSongPos( AwesomePlayer.instance.getSongPos() );
                AwesomePlayer.instance.sendListChangeEvent();
            }
        }else{
            Log.i("ccc", "no");
            ContentValues listRow = new ContentValues();
            listRow.put("skipcount", 0);
            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);

            listRow = new ContentValues();
            AwesomePlayer.instance.increasePlayCount();
            listRow.put("playcount", AwesomePlayer.instance.getCurrentSong().playCount);

            db.update(UserDB.SONG_TABLE_NAME, listRow, whereClause, null);
            db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

            if(AwesomePlayer.instance.getCurrentSong().skipFlag) {
                listRow = new ContentValues();
                listRow.put("skipflag", 0);
                db.update(UserDB.SONG_LIST_NAME, listRow, whereClause, null);

                AwesomePlayer.instance.removeSongPos(AwesomePlayer.instance.getSongPos());
                AwesomePlayer.instance.sendListChangeEvent();
            }
        }
        //
        boolean isSkipped = (AwesomePlayer.instance.getDur() - pos)>30000;
        setSkipPoint(isSkipped, pos);
    }

    //player playsong call
    public void inputSongData(){
        //isSongPicked score 100

        IDTag tag = AwesomePlayer.instance.getCurrentSong();

        timeline.put("title", tag.title);
        timeline.put("artist", tag.artist);
        timeline.put("album", tag.album);
        timeline.put("albumid", tag.albumId);
        timeline.put("genre", tag.genre);
        timeline.put("issongpicked", AwesomePlayer.instance.isPicked);
        getPlayTime();
    }

    //player seek called
    public void setStartPoint(int pos){
        if( AwesomePlayer.instance.isPlaying() ) {
            int seek = pos / 1000;
            String mm = String.valueOf(seek / 60);
            String ss = String.valueOf(seek % 60);
            ss = ss.length() > 1 ? ss : "0"+ss;
            timeline.put("startpoint", mm + ":" + ss);
        }
    }

    //player countSkip Called
    public void setSkipPoint(boolean isSkipped, int pos){
        ContentValues row = new ContentValues();
        int score=0;
        if( AwesomePlayer.instance.isPicked ) score += 100;

        int seek = pos / 1000;
        String mm = String.valueOf(seek / 60);
        String ss = String.valueOf(seek % 60);
        ss = ss.length() > 1 ? ss : "0"+ss;
        if( isSkipped ) {
            timeline.put("skipflag", 1);
            int tmpScore = ((AwesomePlayer.instance.getDur() - pos)/1000);
            if( tmpScore >= 120 )    score -= 150;
            else if( tmpScore >= 60) score -= 100;
            else                     score -= 50;
        }else{
            int tmpScore = pos/1000;
            if( tmpScore >= 120 )    score += 150;
            else if( tmpScore >= 60) score += 100;
            else                     score += 50;
        }

        timeline.put("skippoint", mm+":"+ss);

        SQLiteDatabase db = UserDB.getInstance(cxt).getDatabase();
        db.insert(UserDB.TIMELINE_NAME, null, timeline);


        IDTag tag = AwesomePlayer.instance.getCurrentSong();

        Log.d("aaa", "testtag " + tag);

        String whereClause = "title='"+tag.title.replaceAll("'", "''")+"' AND " +
                "artist='"+tag.artist.replaceAll("'", "''")+"' AND " +
                "album='"+tag.album.replaceAll("'", "''")+"'";

        tag.score += score;

        row.put("score", tag.score);
        Log.i("ccc", "title : " + tag.title + ", score : " + score);
        db.update(UserDB.SONG_TABLE_NAME,row, whereClause, null);
        db.update(UserDB.SONG_LIST_NAME, row, whereClause, null);

        AwesomePlayer.instance.isPicked=false;
    }

    private String getPlayTime(){
        Calendar t = Calendar.getInstance();
        String year = Integer.toString(t.get(Calendar.YEAR));

        String month = Integer.toString((t.get(Calendar.MONTH) + 1));
        month = month.length() > 1 ? month : "0" + month;   //한자리 숫자, 예를 들면 1월인 경우 "01"등으로 문자열 변환

        //오늘의 날짜 구함
        String day = Integer.toString(t.get(Calendar.DAY_OF_MONTH));
        day = day.length() > 1 ? day : "0" + day;

        //현재 시각의 시를 구함.
        String hh = Integer.toString(t.get(Calendar.HOUR_OF_DAY));
        hh = hh.length() > 1 ? hh : "0"+hh;

        //현재 시각의 분을 구함.
        String mm = Integer.toString(t.get(Calendar.MINUTE));
        mm = mm.length() > 1 ? mm : "0"+mm;

        timeline.put("year", year);
        timeline.put("month", month);
        timeline.put("day", day);

        timeline.put("listentime", hh+":"+mm);

        return year+"-"+month+"-"+day+"/"+hh+":"+mm;
    }

}
