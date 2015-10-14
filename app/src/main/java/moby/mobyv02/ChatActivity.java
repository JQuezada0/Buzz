package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toolbar;

/**
 * Created by Johnil on
 * 10/5/2015.
 */
public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager viewPager;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

    }

}
