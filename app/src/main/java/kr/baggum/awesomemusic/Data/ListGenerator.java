package kr.baggum.awesomemusic.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import kr.baggum.awesomemusic.Service.MediaScannerReceiver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dongmin on 2015-07-16.
 */
public class ListGenerator {

    private static IDTag createIDTag(Cursor listDBcursor){
        while( MediaScannerReceiver.mediaSync != false ) {
        }
        String path     =  listDBcursor.getString( listDBcursor.getColumnIndex("path"));
        String id       =  listDBcursor.getString( listDBcursor.getColumnIndex("id"));
        String title    =  listDBcursor.getString( listDBcursor.getColumnIndex("title"));
        String artist   =  listDBcursor.getString( listDBcursor.getColumnIndex("artist"));
        String album    =  listDBcursor.getString( listDBcursor.getColumnIndex("album"));
        long albumId    =  listDBcursor.getLong  ( listDBcursor.getColumnIndex("albumid"));
        String genre    =  listDBcursor.getString( listDBcursor.getColumnIndex("genre"));
        String duration =  listDBcursor.getString( listDBcursor.getColumnIndex("duration"));
        int playCount   =  listDBcursor.getInt   ( listDBcursor.getColumnIndex("playcount"));
        boolean skipFlag=  listDBcursor.getInt   ( listDBcursor.getColumnIndex("skipflag"))>0;
        int score       =  listDBcursor.getInt   ( listDBcursor.getColumnIndex("score"));

        return new IDTag(path, id, title, artist, album, albumId, genre, duration, playCount, skipFlag, score);
    }

    public static ArrayList<IDTag> getTimelineList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();
        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.TIMELINE_NAME + " ORDER BY id DESC", null);

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            String title    =  listDBcursor.getString( listDBcursor.getColumnIndex("title"));
            String artist   =  listDBcursor.getString( listDBcursor.getColumnIndex("artist"));
            String album    =  listDBcursor.getString( listDBcursor.getColumnIndex("album"));
            long albumId    =  listDBcursor.getLong  ( listDBcursor.getColumnIndex("albumid"));
            String genre    =  listDBcursor.getString( listDBcursor.getColumnIndex("genre"));

            int year =  listDBcursor.getInt(listDBcursor.getColumnIndex("year"));
            int month =  listDBcursor.getInt( listDBcursor.getColumnIndex("month"));
            int day =  listDBcursor.getInt( listDBcursor.getColumnIndex("day"));
            String listenTime =  listDBcursor.getString( listDBcursor.getColumnIndex("listentime"));
            String startPoint =  listDBcursor.getString( listDBcursor.getColumnIndex("startpoint"));
            String skipPoint =  listDBcursor.getString( listDBcursor.getColumnIndex("skippoint"));
            boolean skipFlag=  listDBcursor.getInt   ( listDBcursor.getColumnIndex("skipflag"))>0;
            boolean isSongPicked=  listDBcursor.getInt   ( listDBcursor.getColumnIndex("issongpicked"))>0;

            songList.add(new IDTag(title,artist,album,albumId,genre,year,month,day,listenTime,startPoint,skipPoint,skipFlag,isSongPicked));
        }

        return songList;
    }
/*
    public static SongDirectoryTree getSkipSongList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();
        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=1 ORDER BY path ASC", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        SongDirectoryTree root = new SongDirectoryTree();
        ArrayList<String> pathList = new ArrayList<String>();       //path를 기록하는 List
        ArrayList<SongDirectoryTree> node = new ArrayList<SongDirectoryTree>(); //각 경로를 다이렉트로 접근할 List
        int idx = 0;  //path의 index

        listDBcursor.moveToFirst();
        IDTag tag = createIDTag(listDBcursor);

        pathList.add(tag.path.substring(0,tag.path.lastIndexOf('/')));
        node.add(root.addNode(pathList.get(idx)));
        idx++;
        node.get(node.size()-1).addMusic(tag);

        while(listDBcursor.moveToNext()){
            tag = createIDTag(listDBcursor);

            if( !pathList.get(idx-1).equals(tag.path.substring(0,tag.path.lastIndexOf('/'))) ){
                pathList.add(tag.path.substring(0,tag.path.lastIndexOf('/')));
                node.add(root.addNode(pathList.get(idx)));
                idx++;
            }

            node.get(node.size()-1).addMusic(tag);
        }
//        return root.getTree(root);
        return root;
    }
*/
    public static ArrayList<IDTag> getSkipSongList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " WHERE skipflag=1", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            songList.add( createIDTag(listDBcursor) );
        }

        return songList;
    }

    public static ArrayList<IDTag> getAllSongList(Context context){

        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=0", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            songList.add( createIDTag(listDBcursor) );
        }

        return songList;
    }

    public static HashMap<String, ArrayList<IDTag>> getAlbumList(Context context){
        // get identical album names by using SELECT - GROUP BY
        // get song lists for each album name by using SELECT
        // make map (key - album name, value - array list of songs of the album)
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT album FROM " + UserDB.SONG_LIST_NAME + " GROUP BY album", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        HashMap<String, ArrayList<IDTag>> albumSongMap = new HashMap<>();

        while(listDBcursor.moveToNext()){
            String uniqueAlbumName    =  listDBcursor.getString( listDBcursor.getColumnIndex("album"));
            Cursor songOfAlbumCursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " WHERE skipflag=0 AND album='" + uniqueAlbumName.replaceAll("'", "''")+"'", null);

            ArrayList<IDTag> songList = new ArrayList<>();

            while(songOfAlbumCursor.moveToNext()){
                songList.add( createIDTag(songOfAlbumCursor) );
            }

            albumSongMap.put(uniqueAlbumName, songList);
        }

        int num = 0;
        for(String key : albumSongMap.keySet()){
            num += albumSongMap.get(key).size();
            for(IDTag idTag : albumSongMap.get(key)){
                Log.d("aaa", key + " : " + idTag.title);
            }
        }
        Log.d("aaa", "songs of album : " + num);


        return albumSongMap;
    }

    public static HashMap<String, ArrayList<IDTag>> getArtistList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT artist FROM " + UserDB.SONG_LIST_NAME+ " GROUP BY artist", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        HashMap<String, ArrayList<IDTag>> artistSongMap = new HashMap<>();

        while(listDBcursor.moveToNext()){
            String uniqueArtistName    =  listDBcursor.getString( listDBcursor.getColumnIndex("artist"));
            Cursor songOfArtistCursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " WHERE skipflag=0 AND artist='" + uniqueArtistName.replaceAll("'", "''") + "'", null);

            ArrayList<IDTag> songList = new ArrayList<>();

            while(songOfArtistCursor.moveToNext()){
                songList.add( createIDTag(songOfArtistCursor) );
            }

            artistSongMap.put(uniqueArtistName, songList);
        }

        int num = 0 ;
        for(String key : artistSongMap.keySet()){
            num += artistSongMap.get(key).size();
            for(IDTag idTag : artistSongMap.get(key)){
                Log.d("aaa", key + " : " + idTag.title);
            }
        }

        Log.d("aaa", "songs of artist : " + num);

        return artistSongMap;
    }

    public static HashMap<String, ArrayList<IDTag>> getGenreList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT genre FROM " + UserDB.SONG_LIST_NAME+ " GROUP BY genre", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        HashMap<String, ArrayList<IDTag>> genreSongMap = new HashMap<>();

        while(listDBcursor.moveToNext()){
            String uniqueGenreName    =  listDBcursor.getString( listDBcursor.getColumnIndex("genre"));
            Cursor songOfGenreCursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " WHERE skipflag=0 AND genre='" + uniqueGenreName.replaceAll("'", "''") + "'", null);

            ArrayList<IDTag> songList = new ArrayList<>();

            while(songOfGenreCursor.moveToNext()){
                songList.add( createIDTag(songOfGenreCursor) );
            }

            genreSongMap.put(uniqueGenreName, songList);
        }

        int num = 0 ;
        for(String key : genreSongMap.keySet()){
            num += genreSongMap.get(key).size();
            for(IDTag idTag : genreSongMap.get(key)){
                Log.d("aaa", key + " : " + idTag.title);
            }
        }

        Log.d("aaa", "songs of genre : " + num);

        return genreSongMap;
    }

    public static HashMap<String, ArrayList<IDTag>> getPlaylistList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        //get unique playlist name by grouping
        Cursor playlistcursor = db.rawQuery("SELECT playlist_name FROM " + UserDB.PLAYLIST_NAME + " GROUP BY playlist_name", null);

        if(playlistcursor == null)
            return null;

        HashMap<String, ArrayList<IDTag>> playlistSongMap = new HashMap<>();

        // TODO SELECT statement can be improved by using JOIN (search the usage of CursorJoiner class, but raw query is more simple and short)

        //find songs correspond to each playlist
        while(playlistcursor.moveToNext()){
            String uniquePlaylistName    =  playlistcursor.getString( playlistcursor.getColumnIndex("playlist_name"));
            Cursor songOfPlaylistCursor = db.rawQuery("SELECT song_id FROM " + UserDB.PLAYLIST_NAME + " WHERE playlist_name='" + uniquePlaylistName.replaceAll("'", "''") + "'", null);

            ArrayList<IDTag> songList = new ArrayList<>();

            while(songOfPlaylistCursor.moveToNext()){
                //get song_id for each playlist
                String songIdInPlaylist = songOfPlaylistCursor.getString( songOfPlaylistCursor.getColumnIndex("song_id"));

                //find song data for listing on UI. this data are fetched from UserDB.listTable.
                Cursor songlistCursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " WHERE id='" + songIdInPlaylist + "'", null);

                while(songlistCursor.moveToNext()){
                    songList.add( createIDTag(songlistCursor) );
                }

            }

            playlistSongMap.put(uniquePlaylistName, songList);
        }

        // TODO TEST ME!
        // delete songs in playlist that were deleted in device
        String selectStatement = "SELECT id FROM " + UserDB.SONG_LIST_NAME;
        String deleteStatement = "DELETE FROM " + UserDB.PLAYLIST_NAME + " WHERE song_id NOT IN ( " + selectStatement + " )";

        db.rawQuery(deleteStatement, null);

        // delete me - code for test
        int num = 0 ;
        for(String key : playlistSongMap.keySet()){
            num += playlistSongMap.get(key).size();
            for(IDTag idTag : playlistSongMap.get(key)){
                Log.d("aaa", key + " : " + idTag.title);
            }
        }

        Log.d("aaa", "songs of playlist : " + num);

        return playlistSongMap;
    }

    public static ArrayList<IDTag> getMostPlayedList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=0 ORDER BY playcount DESC", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            songList.add( createIDTag(listDBcursor) );
        }

        return songList;
    }

    public static ArrayList<IDTag> getRecentlyAddedList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();
        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=0 ORDER BY date DESC", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            songList.add( createIDTag(listDBcursor) );
        }
        return songList;
    }

    public static ArrayList<IDTag> getRankingList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();
        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + " ORDER BY score DESC", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            if( listDBcursor.getInt( listDBcursor.getColumnIndex("score")) >0 ) songList.add( createIDTag(listDBcursor) );
            else break;
            if( songList.size()>=50) break;
        }
        return songList;
    }

    public static SongDirectoryTree getDirectoryList(Context context){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();
        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=0 ORDER BY path ASC", null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        SongDirectoryTree root = new SongDirectoryTree();
        ArrayList<String> pathList = new ArrayList<String>();       //path를 기록하는 List
        ArrayList<SongDirectoryTree> node = new ArrayList<SongDirectoryTree>(); //각 경로를 다이렉트로 접근할 List
        int idx = 0;  //path의 index

        listDBcursor.moveToFirst();
        IDTag tag = createIDTag(listDBcursor);

        pathList.add(tag.path.substring(0,tag.path.lastIndexOf('/')));
        node.add(root.addNode(pathList.get(idx)));
        idx++;
        node.get(node.size()-1).addMusic(tag);

        while(listDBcursor.moveToNext()){
            tag = createIDTag(listDBcursor);

            if( !pathList.get(idx-1).equals(tag.path.substring(0,tag.path.lastIndexOf('/'))) ){
                pathList.add(tag.path.substring(0,tag.path.lastIndexOf('/')));
                node.add(root.addNode(pathList.get(idx)));
                idx++;
            }

            node.get(node.size()-1).addMusic(tag);
        }
//        return root.getTree(root);
        return root;
    }

    /**
     * @param context
     * @param path ex) /storage/emulated/0/Download
     * @return Songs in the specific path without child path search
     */
    public static ArrayList<IDTag> getAllSongsInPathWithoutChild(Context context,String path){
        SQLiteDatabase db = UserDB.getInstance(context).getDatabase();

        String whereClause =" WHERE skipflag=0 AND " +
                            "path LIKE '"+ path+"/%' AND " +
                            "path NOT LIKE '" + path+"/%/%'" + " ORDER BY path ASC";

        Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME + whereClause , null);

        if(listDBcursor == null || listDBcursor.getCount()<=0)
            return null;

        ArrayList<IDTag> songList = new ArrayList<>();

        while(listDBcursor.moveToNext()){
            songList.add( createIDTag(listDBcursor) );
        }

        return songList;
    }

    //public static ArrayList<IDTag> getAllSongsInPathWithChild(Context context){
    //Cursor listDBcursor = db.rawQuery("SELECT * FROM " + UserDB.SONG_LIST_NAME+ " WHERE skipflag=0 AND path LIKE '"+ path+"/%'
}






















