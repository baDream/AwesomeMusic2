package kr.baggum.awesomemusic.UI.View;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import kr.baggum.awesomemusic.Data.ListGenerator;
import kr.baggum.awesomemusic.R;
import kr.baggum.awesomemusic.Service.AwesomePlayer;
import kr.baggum.awesomemusic.UI.Activity.MainActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public final class TestFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";

    public static final int TITLE=0;
    public static final int ARTIST=10;
    public static final int ALBUM=11;
    public static final int FOLDER=1;
    public static final int RECENT=2;
    public static final int MOSTPLAYED=12;
    public static final int SKIP=3;
    public static final int TIMELINE=4;
    public static final int GRAPH=5;

    private View view;

    public RecyclerView recyclerView;
    public TitleRecyclerAdapter titleAdapter;
    public FolderRecyclerAdapter folderAdapter;
    public TimeLineRecyclerAdapter timelineAdapter;

    private Activity baseActivity;

    private boolean isFinish = false;

    public static TestFragment newInstance(String content) {
        TestFragment fragment = new TestFragment();
        fragment.mContent = content;

        return fragment;
    }

    private String mContent = "???";

    private void setAdapterView(LayoutInflater inflater, ViewGroup container, int cases){
        view = inflater.inflate(R.layout.list, container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        if( cases == TIMELINE ){
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            timelineAdapter = new TimeLineRecyclerAdapter(getActivity(),
                    ListGenerator.getTimelineList(getActivity().getApplicationContext()),
                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(timelineAdapter);

            return;
        }

        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).
                color(Color.LTGRAY).sizeResId(R.dimen.divider).marginResId(R.dimen.leftmargin, R.dimen.rightmargin).build());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if( cases == TITLE ) {
            titleAdapter = new TitleRecyclerAdapter(getActivity(),
                                                    ListGenerator.getAllSongList(getActivity().getApplicationContext()),
                                                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(titleAdapter);
        }else if( cases == FOLDER ) {
            folderAdapter = new FolderRecyclerAdapter(getActivity(), ListGenerator.getDirectoryList(getActivity().getApplicationContext()));
            recyclerView.setAdapter(folderAdapter);
        }else if( cases == RECENT ) {
            titleAdapter = new TitleRecyclerAdapter(getActivity(),
                                                    ListGenerator.getRecentlyAddedList(getActivity().getApplicationContext()),
                                                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(titleAdapter);
        }else if( cases == MOSTPLAYED ) {
            titleAdapter = new TitleRecyclerAdapter(getActivity(),
                    ListGenerator.getMostPlayedList(getActivity().getApplicationContext()),
                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(titleAdapter);
        }else if( cases == SKIP ){
            titleAdapter = new TitleRecyclerAdapter(getActivity(),
                    ListGenerator.getSkipSongList(getActivity().getApplicationContext()),
                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(titleAdapter);
        }else if( cases == GRAPH ){
            titleAdapter = new TitleRecyclerAdapter(getActivity(),
                    ListGenerator.getRankingList(getActivity().getApplicationContext()),
                    (LinearLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(titleAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isFinish = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mContent.equalsIgnoreCase("title")){ //Ignore Lower Upper case
            setAdapterView(inflater, container, TITLE);
        }else if(mContent.equalsIgnoreCase("artist")){
            TextView text = new TextView(getActivity());
            text.setGravity(Gravity.CENTER);
            text.setText(mContent);
            text.setTextSize(20 * getResources().getDisplayMetrics().density);
            text.setPadding(20, 20, 20, 20);

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            layout.setGravity(Gravity.CENTER);
            layout.addView(text);

            return layout;
        }else if(mContent.equalsIgnoreCase("album")){
            TextView text = new TextView(getActivity());
            text.setGravity(Gravity.CENTER);
            text.setText(mContent);
            text.setTextSize(20 * getResources().getDisplayMetrics().density);
            text.setPadding(20, 20, 20, 20);

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            layout.setGravity(Gravity.CENTER);
            layout.addView(text);

            return layout;
        }else if(mContent.equalsIgnoreCase("folder")){;
            setAdapterView(inflater, container, FOLDER);

        }else if(mContent.equalsIgnoreCase("recent")) {
            setAdapterView(inflater, container, RECENT);
        }else if(mContent.equalsIgnoreCase("mostplayed")) {
            setAdapterView(inflater, container, MOSTPLAYED);
        }else if(mContent.equalsIgnoreCase("skip")){
            setAdapterView(inflater, container, SKIP);
        }else if(mContent.equalsIgnoreCase("timeline")) {
            setAdapterView(inflater, container, TIMELINE);
        }else if(mContent.equalsIgnoreCase("graph")) {
            setAdapterView(inflater, container, GRAPH);
        }
        else{
            TextView text = new TextView(getActivity());
            text.setGravity(Gravity.CENTER);
            text.setText(mContent);
            text.setTextSize(20 * getResources().getDisplayMetrics().density);
            text.setPadding(20, 20, 20, 20);

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            layout.setGravity(Gravity.CENTER);
            layout.addView(text);

            return layout;
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        baseActivity = activity;

        Log.d("aaa", mContent + " testFragment onAttach : " + activity.getLocalClassName());
    }

    @Override
    public void onStart() {
        super.onStart();

        //update UI if last selected tab is created.
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(baseActivity);

        int tabIndex = appSharedPrefs.getInt("LASTSONG_TAB_INDEX", -1); // -1 means error
        int songIndex = appSharedPrefs.getInt("LASTSONG_SONG_INDEX", -1);

        if( AwesomePlayer.instance != null && AwesomePlayer.instance.isPlaying() ){ // song is being played
            ((MainActivity) baseActivity).updateMiniPlayView(AwesomePlayer.instance.getCurrentSong());
            ((MainActivity) baseActivity).setPlayActivity(AwesomePlayer.instance.getCurrentSong());

            if (tabIndex != -1 && songIndex != -1 && tabIndex == content2index(mContent)) {
                //get song list from DB

                switch (tabIndex) {
                    case TestFragment.TITLE:
                        break;
                    case TestFragment.SKIP:
                        break;
                    case TestFragment.FOLDER:
                        String lastPath = appSharedPrefs.getString("LASTSONG_PATH", null);

                        TestFragment lastFragment = (TestFragment) ((MainActivity) baseActivity).getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + tabIndex);
                        lastFragment.folderAdapter.moveIntoSpecificFolder(lastPath.substring(lastPath.lastIndexOf("/") + 1), lastPath.substring(1, lastPath.length()));
                        break;
                    case TestFragment.RECENT:
                        break;
                }

                ((MainActivity) baseActivity).getIndicator().onPageSelected(tabIndex);
            }


        }
        else if(tabIndex == content2index(mContent))
            ((MainActivity) baseActivity).loadLastSong();
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    public boolean isFinish() {
        return isFinish;
    }

    private int content2index(String content){
        for(int i = 0 ; i < TestFragmentAdapter.CONTENT.length; i++){
            if(TestFragmentAdapter.CONTENT[i].equalsIgnoreCase(content)) {
                return i;
            }
        }
        return -1; // error
    }
}
