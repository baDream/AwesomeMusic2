package kr.baggum.awesomemusic.Data;

/**
 * Created by user on 15. 7. 12.
 */
public class IDTag{
    public String id;
    public String path;
    public String title;
    public String artist;
    public String album;
    public long albumId;
    public String genre;
    public String duration;

    public String date;
    public int year;
    public int month;
    public int day;
    public String listenTime;

    public int playCount;
    public int skipCount;
    public String startPoint;
    public String endPoint;
    public boolean startFlag;
    public boolean endFlag;
    public boolean isDeleted;
    public boolean isModified;

    public boolean skipFlag;
    public boolean isSongPicked;

    public int score;


    public IDTag(){
        id="null";
        path="null";
        title="unknown";
        artist="unknown";
        album="unknown";
        albumId=0;
        genre="normal";
        duration="0";

        date="0";
        playCount=0;
        skipCount=0;
        startPoint="0:00";
        endPoint="0:00";
        startFlag=false;
        endFlag=false;
        isDeleted=false;
        isModified=false;
    }

    public IDTag(String _title, String _artist, String _album)
    {
        title = _title;
        artist = _artist;
        album = _album;
    }


    public IDTag(String _title, String _artist, String _album,
          int _playCount, int _skipCount,
          String _startPoint, String _endPoint, boolean _startFlag,
          boolean _endFlag, boolean _isDeleted, boolean _isModified){
        title = _title;
        artist = _artist;
        album = _album;
        playCount = _playCount;
        skipCount = _skipCount;
        startPoint = _startPoint;
        endPoint = _endPoint;
        startFlag = _startFlag;
        endFlag = _endFlag;
        isDeleted = _isDeleted;
        isModified= _isModified;
    }

    public IDTag(String _path, String _id, String _title, String _artist, String _album) {
        path = _path;
        id = _id;
        title = _title;
        artist = _artist;
        album = _album;
    }
    public IDTag(String _path, String _title, String _artist, String _album){
        path = _path;
        title = _title;
        artist = _artist;
        album = _album;
    }

    public IDTag(String _path, String _id, String _title, String _artist, String _album,
          long _albumId, String _genre, String _duration, int _playCount,
          boolean _skipFlag,int _score)
    {
        path = _path;
        id = _id;
        title = _title;
        artist = _artist;
        album = _album;
        albumId = _albumId;
        genre = _genre;
        duration = _duration;
        playCount = _playCount;
        skipFlag = _skipFlag;
        score = _score;
    }

    public IDTag(String _title, String _artist, String _album,
                 long _albumId, String _genre, int _year, int _month, int _day, String _listenTime, String _startPoint, String _skipPoint,
                 boolean _skipFlag, boolean _isSongPicked)
    {
        title = _title;
        artist = _artist;
        album = _album;
        albumId = _albumId;
        genre = _genre;
        year = _year;
        month = _month;
        day = _day;
        listenTime = _listenTime;
        startPoint = _startPoint;
        endPoint = _skipPoint;
        skipFlag = _skipFlag;
        isSongPicked = _isSongPicked;
    }
}