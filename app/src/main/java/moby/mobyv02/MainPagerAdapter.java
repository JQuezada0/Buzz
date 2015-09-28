package moby.mobyv02;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

/**
 * Created by quezadjo on 9/12/2015.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = new Fragment[]{new FeedFragment(), new MapFragment()};

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }
}
