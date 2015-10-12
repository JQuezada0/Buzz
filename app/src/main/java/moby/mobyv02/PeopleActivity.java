package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johnil on 10/7/2015.
 */
public class PeopleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProfileRowAdapter profileRowAdapter;
    private ListView list;
    private CircleProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);
        list = (ListView) findViewById(R.id.user_list);
        profileRowAdapter = new ProfileRowAdapter(this);
        list.setAdapter(profileRowAdapter);
        progress = (CircleProgressBar) findViewById(R.id.progress);
        progress.setColorSchemeResources(R.color.moby_blue);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list.setOnItemClickListener(profileClickListener);
        getUsers(true);
    }

    private void getUsers(final boolean reset) {
        progress.setVisibility(View.VISIBLE);
        new ParseOperation("Network").getDiscovery(new ParseOperation.GetUsersCallback() {
            @Override
            public void finished(boolean success, ArrayList<ParseUser> users, ParseException e) {

                if (success) {
                    progress.setVisibility(View.GONE);
                    if (reset) {
                        profileRowAdapter.setUsers(users);
                    } else {
                        profileRowAdapter.addUsers(users);
                    }
                } else {
                    if (e != null) {
                        Toast.makeText(PeopleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }
        }, this);
    }

    public AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ParseUser user = (ParseUser) parent.getItemAtPosition(position);
            Intent i = new Intent(PeopleActivity.this, ProfileActivity.class);
            i.putExtra("user", user.getObjectId());
            PeopleActivity.this.startActivity(i);
        }
    };

}
