package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Johnil on 10/7/2015.
 */
public class DiscoverFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.discover_fragment, group, false);

        return v;
    }

}
