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
import java.util.List;

import moby.mobyv02.parse.Friend;

/**
 * Created by Johnil on 10/11/2015.
 */
public class PendingFriendsFragment extends Fragment {

    private FriendsActivity friendsActivity;
    private ProfileRowAdapter profileRowAdapter;
    private ListView list;
    private CircleProgressBar progress;
    private List<Friend> friends;
    private List<ParseUser> users;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.pending_friends_fragment, null);
        list = (ListView) v.findViewById(R.id.user_list);
        friendsActivity = (FriendsActivity) getActivity();
        profileRowAdapter = new ProfileRowAdapter(friendsActivity);
        list.setAdapter(profileRowAdapter);
        progress = (CircleProgressBar) v.findViewById(R.id.progress);
        progress.setColorSchemeResources(R.color.moby_blue);
        list.setOnItemClickListener(profileClickListener);
        getFriends();
        return v;
    }

    public void getFriends(){

        ParseOperation.getPendingFriends(new ParseOperation.GetFriendsCallback() {
            @Override
            public void finished(boolean success, List<Friend> friends, ParseException e) {

                if (success) {

                    PendingFriendsFragment.this.friends = friends;
                    ArrayList<ParseUser> users = new ArrayList<ParseUser>();
                    for (Friend friend : friends) {
                        users.add(friend.getTo());
                    }
                    PendingFriendsFragment.this.profileRowAdapter.setUsers(users);
                    PendingFriendsFragment.this.progress.setVisibility(View.GONE);
                } else {
                    if (e != null) {
                        Toast.makeText(PendingFriendsFragment.this.friendsActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }
        }, PendingFriendsFragment.this.friendsActivity);

    }

    public AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ParseUser user = (ParseUser) parent.getItemAtPosition(position);
            Intent i = new Intent(friendsActivity, ProfileActivity.class);
            i.putExtra("user", user.getObjectId());
            friendsActivity.startActivity(i);
        }
    };
}
