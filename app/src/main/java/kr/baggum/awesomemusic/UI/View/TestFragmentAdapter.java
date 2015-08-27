package kr.baggum.awesomemusic.UI.View;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import kr.baggum.awesomemusic.R;

public class TestFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "title", "folder", "recent", "skip", "timeline","graph", };
    protected static final int[] ICONS = new int[] {
            R.drawable.perm_group_music,
            R.drawable.perm_group_folder,
            R.drawable.perm_group_recent,
            R.drawable.perm_group_trash,
            R.drawable.perm_group_timeline,
            R.drawable.perm_group_heart
    };

    private int mCount = CONTENT.length;

    public TestFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return TestFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    @Override
    public int getIconResId(int index) {
      return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}