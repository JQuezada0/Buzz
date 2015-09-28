package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by quezadjo on 9/9/2015.
 */
public class Signup extends LeanplumFragmentActivity {

    SignUpFragment signUpFragment;
    SelectProfilePictureFragment profilePictureFragment;
    SignupViewPagerAdapter adapter;
    private MainViewPager signUpViewPager;
    private File file;
    private CircleProgressBar progressBar;
    private InputStream profileImageInputStream;
    public byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
        signUpFragment = new SignUpFragment();
        profilePictureFragment = new SelectProfilePictureFragment();
        file = Application.getImageCacheFile(this);
        System.out.println(file.getAbsolutePath());
        adapter = new SignupViewPagerAdapter(getSupportFragmentManager(), new Fragment[]{signUpFragment, profilePictureFragment});
        signUpViewPager = (MainViewPager) findViewById(R.id.signupViewPager);
        signUpViewPager.setAdapter(adapter);
        progressBar = (CircleProgressBar) findViewById(R.id.signup_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
    }

    @Override
    protected void onResume(){
        super.onResume();
        file = Application.getImageCacheFile(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case 200:
                    try {
                        profileImageInputStream = getContentResolver().openInputStream(data.getData());
                        FileOutputStream fos = new FileOutputStream(file);
                        int read = 0;
                        byte[] bytes = new byte[1024];

                        while ((read = profileImageInputStream.read(bytes)) != -1) {
                            fos.write(bytes, 0, read);
                            profileImageInputStream.close();
                            fos.close();
                        }
                        Intent intent = new Intent(this, CropperActivity.class);
                        startActivityForResult(intent, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 300:
                    profilePictureFragment.hideDialog();
                    profilePictureFragment.setProfilePicture();
                    break;
                case 400:
                    Intent intent = new Intent(this, CropperActivity.class);
                    startActivityForResult(intent, 300);
            }

        }

    }

    public ViewPager getSignUpViewPager(){
        return signUpViewPager;
    }

    public SignUpFragment getSignUpFragment(){
        return signUpFragment;
    }

    public SelectProfilePictureFragment getProfilePictureFragment(){
        return profilePictureFragment;
    }

    public void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress(){
        progressBar.setVisibility(View.GONE);
    }


    private class SignupViewPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments;


        public SignupViewPagerAdapter(FragmentManager fm, Fragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
