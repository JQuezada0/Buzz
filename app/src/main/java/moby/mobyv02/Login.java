package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import moby.mobyv02.parse.Heart;
import moby.mobyv02.parse.Follow;
import moby.mobyv02.parse.Upvote;

/**
 * Created by quezadjo on 9/9/2015.
 */
public class Login extends LeanplumFragmentActivity {

    Button loginButton;
    EditText username;
    EditText password;
    Button registerButton;
    CircleProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        progressBar = (CircleProgressBar) findViewById(R.id.login_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
 //       username.addTextChangedListener(Factory.getLowerCaseTextFormatter(username));
        loginButton.setOnClickListener(loginClickListener);
        registerButton.setOnClickListener(registerClickListener);
    }

    final View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            System.out.println("Button clicked");

            if (loginButton.getText().toString().isEmpty() || password.getText().toString().isEmpty()){

                Toast.makeText(Login.this, "please fill out both fields", Toast.LENGTH_SHORT).show();

            } else {
                progressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);
                ParseOperation.logIn(username.getText().toString(), password.getText().toString(), new ParseOperation.ParseOperationCallback() {
                    @Override
                    public void finished(boolean success, final ParseException e) {
                        if (success) {
                            Application.logger.logEvent("Successful Login");
                            getFollowed();
                        } else {
                            Login.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loginButton.setEnabled(true);
                                    Application.logger.logEvent("Login failed");
                                }
                            });
                        }
                    }
                }, Login.this);
            }
        }
    };

    final View.OnClickListener registerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(Login.this, Signup.class));
        }

    };

    private void getFollowed(){

        final List<Follow> follows = new ArrayList<Follow>();
        ParseQuery<Follow> followQuery = Follow.getQuery();
        followQuery.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        followQuery.findInBackground(new FindCallback<Follow>() {
            @Override
            public void done(List<Follow> list, ParseException e) {
                follows.addAll(list);
                for (Follow follow : follows){
                    follow.pinInBackground();
                    follow.saveEventually();
                }
                getFavorites();
            }
        });

    }

    private void getFavorites(){

        final List<Heart> hearts = new ArrayList<Heart>();
        ParseQuery<Heart> favoriteQuery = Heart.getQuery();
        favoriteQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        favoriteQuery.findInBackground(new FindCallback<Heart>() {
            @Override
            public void done(List<Heart> list, ParseException e) {

                hearts.addAll(list);
                for (Heart heart : hearts){
                    heart.pinInBackground();
                    heart.saveEventually();
                }
                getUpvotes();
            }
        });

    }

    private void getUpvotes(){
        final List<Upvote> upvotes = new ArrayList<Upvote>();
        ParseQuery<Upvote> upvoteQuery = Upvote.getQuery();
        upvoteQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        upvoteQuery.findInBackground(new FindCallback<Upvote>() {
            @Override
            public void done(List<Upvote> list, ParseException e) {

                upvotes.addAll(list);
                for (Upvote upvote : upvotes){
                    upvote.pinInBackground();
                }
                LocationManager.updateFromSharedPreferences(Login.this);
                startActivity(new Intent(Login.this, Main.class));
                finish();
            }
        });

    }

}
