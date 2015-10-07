package moby.mobyv02;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by quezadjo on 9/8/2015.
 */
public class Welcome extends LeanplumFragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {

    private Button signupButton;
    private Button loginButton;
    private TableRow googlePlusSignin;
    private TableRow facebookSignin;
    private GoogleApiClient googleApiClient;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private ConnectionResult connectionResult;
    private String[] permissions = new String[]{"public_profile", "email", "user_birthday", "user_location"};

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        BuzzAnalytics.logScreen(this, BuzzAnalytics.ONBOARDING_CATEGORY, "welcome");
        signupButton = (Button) findViewById(R.id.welcome_signup_button);
        loginButton = (Button) findViewById(R.id.welcome_login_button);
        facebookSignin = (TableRow) findViewById(R.id.facebook_button);
        googlePlusSignin = (TableRow) findViewById(R.id.google_button);
        signupButton.setOnClickListener(signupClickListener);
        loginButton.setOnClickListener(loginClickListener);
        facebookSignin.setOnClickListener(facebookClickListener);
        googlePlusSignin.setOnClickListener(googleClickListener);
    }

    private void facebookLogin(){
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList(permissions), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    if (parseUser.isNew()){
                        BuzzAnalytics.logLogin(Welcome.this, "Facebook", true);
                    } else {
                        BuzzAnalytics.logLogin(Welcome.this, "Facebook", false);
                    }
                    createUser(parseUser);
                } else {
                    if (e!=null){
                        e.printStackTrace();
                    }
                    BuzzAnalytics.logError(Welcome.this, "Error logging in with FB. User pressed back?");
                    System.out.println("Error");
                }

            }
        });

    }

    private void createUser(final ParseUser parseUser){
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = new GraphRequest().newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {
                    try {
                        LocationManager.updateFromSharedPreferences(Welcome.this);
                        System.out.println(object.toString());
                        parseUser.put("gender", object.getString("gender"));
                        parseUser.put("fullName", object.getString("name"));
                        parseUser.put("email", object.getString("email"));
                        parseUser.put("profileImage", "http://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                        parseUser.saveEventually();

                        startActivity(new Intent(Welcome.this, Main.class));
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(response.toString());
                }

            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,location");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private final View.OnClickListener signupClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(Welcome.this, Signup.class));
            finish();
        }
    };

    private final View.OnClickListener loginClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(Welcome.this, Login.class));
            finish();
        }
    };

    private final View.OnClickListener googleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            googleApiClient = new GoogleApiClient.Builder(Welcome.this)
                    .addConnectionCallbacks(Welcome.this)
                    .addOnConnectionFailedListener(Welcome.this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .build();
            googleApiClient.connect();
        }
    };

    private final View.OnClickListener facebookClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
                facebookLogin();
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        final ParseUser user = new ParseUser();
        final Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        ParseUser.logInInBackground(Plus.AccountApi.getAccountName(googleApiClient), Plus.AccountApi.getAccountName(googleApiClient), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    BuzzAnalytics.logLogin(Welcome.this, "Google", false);
                    startActivity(new Intent(Welcome.this, Main.class));
                    finish();
                } else {
                    System.out.println(e.getMessage());
                    user.setUsername(Plus.AccountApi.getAccountName(googleApiClient));
                    user.setPassword(Plus.AccountApi.getAccountName(googleApiClient));
                    user.setEmail(Plus.AccountApi.getAccountName(googleApiClient));
                    user.put("fullName", currentPerson.getName().getGivenName() + " " + currentPerson.getName().getFamilyName());
                    user.put("profileImage", currentPerson.getImage().getUrl());
                    user.put("instagram", false);
                    if (currentPerson.getBirthday() != null) {
                        user.put("birthday", currentPerson.getBirthday());
                    }
                    if (currentPerson.getGender() == 0) {
                        user.put("gender", "male");
                    } else if (currentPerson.getGender() == 1) {
                        user.put("gender", "female");
                    }
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                BuzzAnalytics.logLogin(Welcome.this, "Google", true);
                                LocationManager.updateFromSharedPreferences(Welcome.this);
                                startActivity(new Intent(Welcome.this, Main.class));
                                finish();
                            } else {
                                Toast.makeText(Welcome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void registerWithGoogle(){

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 100);
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        }
        this.connectionResult = connectionResult;
    }

    @Override
    public void onSuccess(LoginResult loginResult) {



    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == 100 && responseCode == RESULT_OK) {
            connectionResult = null;
            googleApiClient.connect();
        }
        ParseFacebookUtils.onActivityResult(Application.FACEBOOK_REQUEST_CODE, responseCode, intent);
    }
}
