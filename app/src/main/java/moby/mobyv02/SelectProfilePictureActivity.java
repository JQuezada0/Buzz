package moby.mobyv02;

import android.os.Bundle;

import moby.mobyv02.BuildConfig;
import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class SelectProfilePictureActivity extends LeanplumFragmentActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_profile_picture);

    }
}
