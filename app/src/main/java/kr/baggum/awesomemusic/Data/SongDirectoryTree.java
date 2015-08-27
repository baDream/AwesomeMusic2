package kr.baggum.awesomemusic.Data;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by user on 15. 7. 19.
 */
public class SongDirectoryTree {
    public String folderName;
    public ArrayList<SongDirectoryTree> nextTree;
    public ArrayList<IDTag> musicData;
    public boolean isFolder;        //폴더인지
    public boolean isPrinting;      //출력해야할거

    public SongDirectoryTree(){         //is Root
        folderName=null;
        musicData=null;
        isFolder=false;
        isPrinting=false;

        nextTree = new ArrayList<SongDirectoryTree>();
    }
    public SongDirectoryTree(String name){    //is Folder
        folderName = name;
        isFolder = true;
        isPrinting = false;
        musicData=null;

        nextTree = new ArrayList<SongDirectoryTree>();
    }
    public SongDirectoryTree(IDTag data){      //is MusicFile
        if( musicData == null )
            musicData = new ArrayList<IDTag>();

        musicData.add(data);
        isFolder=false;
        isPrinting=true;
    }

    public void addMusic(IDTag tag){
        if( musicData == null ){
            isPrinting=true;
            musicData = new ArrayList<IDTag>();
        }
        musicData.add(tag);
    }

    //삽입시 비교를 통해 트리를 구성하는 메소드
    public SongDirectoryTree addNode(String path){
        StringTokenizer st = new StringTokenizer(path, "/");

        String s = st.nextToken();

        if (nextTree.size() == 0) {
            nextTree.add(new SongDirectoryTree( s ));
        }else if( !nextTree.get( nextTree.size()-1 ).folderName.equals( s ) ){
            nextTree.add(new SongDirectoryTree( s ));
            isPrinting=true;
        }
        SongDirectoryTree node = nextTree.get( nextTree.size()-1 );

        while( st.hasMoreTokens() ) {
            s = st.nextToken();
            if( node.nextTree.size() == 0 ){
                node.nextTree.add(new SongDirectoryTree( s ));

            }else if( !node.nextTree.get( node.nextTree.size()-1 ).folderName.equals( s ) ){
                node.nextTree.add(new SongDirectoryTree( s ));
                node.isPrinting=true;
            }
            node = node.nextTree.get( node.nextTree.size()-1 );
        }
        return node;
    }

    //폴더가 두개 이상이거나 음악이 있는 디렉토리를 반환하는 메소
    public SongDirectoryTree getTree(SongDirectoryTree node){
        if( node.isPrinting == false ){
            node = getTree(node.nextTree.get(0));
        }

        return node;
    }

}















