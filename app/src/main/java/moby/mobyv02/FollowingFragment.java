package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Johnil on 10/7/2015.
 */
public class FollowingFragment extends Fragment {

    private PeopleActivity peopleActivity;
    private ProfileRowAdapter profileRowAdapter;
    private ListView list;
    private CircleProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.following_fragment, null);
        peopleActivity = (PeopleActivity) getActivity();
        list = (ListView) v.findViewById(R.id.user_list);
        profileRowAdapter = new ProfileRowAdapter(peopleActivity);
        list.setAdapter(profileRowAdapter);
        progress = (CircleProgressBar) v.findViewById(R.id.progress);
        progress.setColorSchemeResources(R.color.moby_blue);
        list.setOnItemClickListener(profileClickListener);
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        loadUsers(true);
    }

    private void loadUsers(final boolean reset){
        progress.setVisibility(View.VISIBLE);
        new ParseOperation("Network").getFollowing(new ParseOperation.GetUsersCallback() {
            @Override
            public void finished(boolean success, ArrayList<ParseUser> users, ParseException e) {

                if (success){
                    progress.setVisibility(View.GONE);
                    if (reset){
                        profileRowAdapter.setUsers(users);
                    } else {
                        profileRowAdapter.addUsers(users);
                    }
                    System.out.println("Amount of following users is " + users.size());
                } else {
                    if (e != null){
                        Toast.makeText(peopleActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }
        }, peopleActivity);
    }

    public AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ParseUser user = (ParseUser) parent.getItemAtPosition(position);
            Intent i = new Intent(peopleActivity, ProfileActivity.class);
            i.putExtra("user", user.getObjectId());
            peopleActivity.startActivity(i);
        }
    };

}
