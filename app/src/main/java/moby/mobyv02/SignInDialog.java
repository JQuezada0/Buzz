package moby.mobyv02;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Johnil on 10/16/2015.
 */
public class SignInDialog extends DialogFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {


    private Context context;
    private Activity refreshActivity;
    private GoogleApiClient googleApiClient;
    private String[] permissions = new String[]{"public_profile", "email", "user_birthday", "user_location"};
    private static CircleProgressBar progress;

    static SignInDialog newInstance(CircleProgressBar progress, Activity activity) {
        SignInDialog f = new SignInDialog();
        f.setActivity(activity);
        f.progress = progress;
        return f;
    }

    public void setActivity(Activity activity){
        this.refreshActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.context = getActivity();
        MaterialDialog dialog = new MaterialDialog.Builder(this.context)
                .title(R.string.loginOrSignup)
                .content(R.string.loginOrSignupContent)
                .theme(Theme.LIGHT)
                .items(R.array.login_options)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            facebookLogin();
                        } else if (which == 1) {
                            googleLogin();
                        } else {
                            Toast.makeText(context, "Please select a login method", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true; // allow selection
                    }
                })
                .positiveText(R.string.loginContinue)
                .negativeText(R.string.loginCancel)
                .build();
        return dialog;
    }

    private void googleLogin(){
        progress.setVisibility(View.VISIBLE);
        googleApiClient = new GoogleApiClient.Builder(SignInDialog.this.context)
                .addConnectionCallbacks(SignInDialog.this)
                .addOnConnectionFailedListener(SignInDialog.this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
        googleApiClient.connect();

    }

    private void facebookLogin(){
        progress.setVisibility(View.VISIBLE);
        System.out.println("Start facebook login");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this.getActivity(), Arrays.asList(permissions), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    if (parseUser.isNew()) {
                        BuzzAnalytics.logLogin(SignInDialog.this.context, "Facebook", true);
                    } else {
                        BuzzAnalytics.logLogin(SignInDialog.this.context, "Facebook", false);
                    }
                    System.out.println("Start with creating user");
                    createUser(parseUser);
                } else {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    BuzzAnalytics.logError(SignInDialog.this.context, "Error logging in with FB. User pressed back?");
                    System.out.println("Error");
                }

            }
        });

    }

    private void refreshAppAfterLogin(){
        SignInDialog.this.refreshActivity.recreate();
    }

    private void createUser(final ParseUser parseUser){
        System.out.println("Create Facebook User");
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = new GraphRequest().newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {

            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {
                    try {
                        LocationManager.updateFromSharedPreferences(SignInDialog.this.context);
                        System.out.println(object.toString());
                        parseUser.put("gender", object.getString("gender"));
                        parseUser.put("fullName", object.getString("name"));
                        parseUser.put("email", object.getString("email"));
                        parseUser.put("profileImage", "http://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                        parseUser.saveEventually();
                        refreshAppAfterLogin();
                        dismiss();
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

    @Override
    public void onConnected(Bundle bundle) {
        progress.setVisibility(View.VISIBLE);
        final ParseUser user = new ParseUser();
        final Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        ParseUser.logInInBackground(Plus.AccountApi.getAccountName(googleApiClient), Plus.AccountApi.getAccountName(googleApiClient), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    BuzzAnalytics.logLogin(context, "Google", false);
                    refreshAppAfterLogin();
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
                                BuzzAnalytics.logLogin(context, "Google", true);
                                LocationManager.updateFromSharedPreferences(context);
                                refreshAppAfterLogin();
                            } else {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("On Connection failed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(SignInDialog.this.refreshActivity, 100);
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        System.out.println("On Activity result");
        if (requestCode == 100 && responseCode == Activity.RESULT_OK) {
            googleApiClient.connect();
        }
        ParseFacebookUtils.onActivityResult(Application.FACEBOOK_REQUEST_CODE, responseCode, intent);
    }
}
