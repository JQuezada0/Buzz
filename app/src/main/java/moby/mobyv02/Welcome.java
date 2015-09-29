package moby.mobyv02;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        signupButton = (Button) findViewById(R.id.welcome_signup_button);
        loginButton = (Button) findViewById(R.id.welcome_login_button);
        facebookSignin = (TableRow) findViewById(R.id.facebook_button);
        googlePlusSignin = (TableRow) findViewById(R.id.google_button);
        signupButton.setOnClickListener(signupClickListener);
        loginButton.setOnClickListener(loginClickListener);
        facebookSignin.setOnClickListener(facebookClickListener);
        googlePlusSignin.setOnClickListener(googleClickListener);

        try{
            PackageInfo info = getPackageManager().getPackageInfo(
                    "moby.mobyv02", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    private void facebookLogin(){
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList(permissions), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    createUser(parseUser);
                } else {
                    e.printStackTrace();
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
                        parseUser.put("gender", object.getString("gender"));
                        parseUser.saveEventually();
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("user", parseUser.getObjectId());
                        params.put("method", "facebook");
                        Leanplum.track("login", params);
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
        final Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        final ParseUser user = new ParseUser();
        ParseUser.logInInBackground(currentPerson.getId(), currentPerson.getId(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null){
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("user", user.getObjectId());
                    params.put("method", "google");
                    Leanplum.track("login", params);
                    startActivity(new Intent(Welcome.this, Main.class));
                    finish();
                } else {
                    System.out.println(e.getMessage());
                    user.setUsername(currentPerson.getId());
                    user.setPassword(currentPerson.getId());
                    user.setEmail(Plus.AccountApi.getAccountName(googleApiClient));
                    user.put("fullName", currentPerson.getName().getGivenName() + " " + currentPerson.getName().getFamilyName());
                    user.put("profileImage", currentPerson.getImage().getUrl());
                    user.put("instagram", false);
                    if (currentPerson.getBirthday() != null){
                        user.put("birthday", currentPerson.getBirthday());
                    }
                    if (currentPerson.getGender() == 0){
                        user.put("gender", "male");
                    } else if (currentPerson.getGender() == 1) {
                        user.put("gender", "female");
                    }
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("user", user.getObjectId());
                                params.put("method", "google");
                                Leanplum.track("registration", params);
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
