package moby.mobyv02;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by quezadjo on 9/14/2015.
 */
public class SelectProfilePictureFragment extends Fragment {

    private CircleImageView profilePicture;
    private Button continueButton;
    private Button selectImageButton;
    byte[] image;
    private Bundle userInfo;
    private Signup signup;
    private Button backButton;
    public TextView nameText;
    AlertDialog dialog;
    private InputStream profileImageInputStream;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaneState){
        signup = (Signup) getActivity();
        View v = View.inflate(signup, R.layout.select_profile_picture, null);
        userInfo = getArguments();
        nameText = (TextView) v.findViewById(R.id.select_profile_picture_name);
        backButton = (Button) v.findViewById(R.id.select_profile_picture_back_button);
        selectImageButton = (Button) v.findViewById(R.id.select_profile_picture_take_image);
        profilePicture = (CircleImageView) v.findViewById(R.id.select_profile_picture_image);
        selectImageButton.setOnClickListener(profilePictureClickListener);
        continueButton = (Button) v.findViewById(R.id.select_profile_picture_continue);
        continueButton.setOnClickListener(continueButtonClickListener);
        continueButton.setVisibility(View.GONE);
        backButton.setOnClickListener(backClickListener);
        return v;
    }

    private final View.OnClickListener continueButtonClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            continueButton.setEnabled(false);
            signup.showProgress();
            new ParseOperation("Network").uploadImage(Application.getImageCacheFile(signup), new ParseOperation.ImageUploadCallback() {
                @Override
                public void finished(boolean success, ParseFile file, ParseException e) {

                    if (success) {

                        ParseUser user = new ParseUser();
                        final SignUpFragment signup = SelectProfilePictureFragment.this.signup.getSignUpFragment();
                        user.setUsername(signup.username.getText().toString());
                        user.setPassword(signup.password.getText().toString());
                        user.setEmail(signup.email.getText().toString());
                        user.put("fullName", signup.fullName.getText().toString());
                        user.put("profileImage", file.getUrl());
                        user.put("birthday", signup.date);
                        user.put("gender", signup.gender.getSelectedItem().toString())
;                        new ParseOperation("Network").signUp(user, new ParseOperation.ParseOperationCallback() {
                            @Override
                            public void finished(boolean success, final ParseException e) {

                                if (e == null) {
                                    Intent i = new Intent(SelectProfilePictureFragment.this.signup, Main.class);
                                    BuzzAnalytics.logLogin(SelectProfilePictureFragment.this.signup, "Buzz", true);
                                    LocationManager.updateFromSharedPreferences(SelectProfilePictureFragment.this.signup);
                                    SelectProfilePictureFragment.this.signup.startActivity(i);
                                } else {
                                    SelectProfilePictureFragment.this.signup.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SelectProfilePictureFragment.this.signup.hideProgress();
                                            Toast.makeText(SelectProfilePictureFragment.this.signup, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            SelectProfilePictureFragment.this.signup.getSignUpViewPager().setCurrentItem(0, true);
                                            continueButton.setEnabled(true);
                                        }
                                    });

                                }

                            }
                        }, SelectProfilePictureFragment.this.signup);

                    } else {
                        signup.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                signup.hideProgress();
                            }
                        });
                    }

                }
            }, signup);

        }

    };

    private final View.OnClickListener profilePictureClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(signup, R.style.DialogStyle));
            ListView v = new ListView(signup);
            v.setAdapter(new DialogListAdapter());
            v.setOnItemClickListener(profileImageDialogItemClickListener);
            builder.setView(v);
            dialog = builder.create();
            dialog.show();
        }


    };
    private final View.OnClickListener backClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            signup.getSignUpViewPager().setCurrentItem(0, true);
        }

    };

    public void setProfilePicture(){
        File file = Application.getImageCacheFile(signup);
        profilePicture.setImageBitmap(null);
        profilePicture.setImageURI(Uri.fromFile(file));
        continueButton.setVisibility(View.VISIBLE);
        selectImageButton.setText("Retake");
        hideDialog();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    private class DialogListAdapter extends BaseAdapter {

        String[] options = new String[]{"Select from gallery", "Take new photo"};

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int i) {
            return options[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(signup).inflate(R.layout.drawer_item, null);
            TextView tv = (TextView) view.findViewById(R.id.drawer_item_text);
            ImageView iv = (ImageView) view.findViewById(R.id.drawer_item_image);
            tv.setText(options[position]);
            switch (position){
                case 0:
                    iv.setImageDrawable(ContextCompat.getDrawable(signup, R.drawable.select_photo_icon));
                    break;
                case 1:
                    iv.setImageDrawable(ContextCompat.getDrawable(signup, R.drawable.take_photo_icon));
                    break;
            }
            return view;
        }
    }

    private AdapterView.OnItemClickListener profileImageDialogItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            switch (position){

                case 0:
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    signup.startActivityForResult(photoPickerIntent, 200);
                    break;
                case 1:
                    Intent takePictureIntent = new Intent(signup, CameraActivity.class);
                    signup.startActivityForResult(takePictureIntent, 400);

            }
        }
    };

}
