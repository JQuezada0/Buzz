package moby.mobyv02;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class MapAdapter extends FragmentPagerAdapter {

    public static View googleMap;

    public MapAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return new MapFragment();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
