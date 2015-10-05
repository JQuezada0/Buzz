package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.leanplum.activities.LeanplumFragmentActivity;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.apache.commons.io.IOUtils;

import java.io.File;
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
        signUpFragment = new SignUpFragment();
        profilePictureFragment = new SelectProfilePictureFragment();
        file = Application.getImageCacheFile(this);
        System.out.println(file.getAbsolutePath());
        adapter = new SignupViewPagerAdapter(getSupportFragmentManager(), new Fragment[]{signUpFragment, profilePictureFragment});
        signUpViewPager = (MainViewPager) findViewById(R.id.signupViewPager);
        signUpViewPager.setAdapter(adapter);
        signUpViewPager.addOnPageChangeListener(pageChangeListener);
        progressBar = (CircleProgressBar) findViewById(R.id.signup_progressbar);
        progressBar.setColorSchemeResources(R.color.moby_blue);
        signUpViewPager.setCurrentItem(0);
        BuzzAnalytics.logScreen(Signup.this, BuzzAnalytics.ONBOARDING_CATEGORY, "signupForm");
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
                        IOUtils.write(IOUtils.toByteArray(profileImageInputStream), fos);
                        Intent intent = new Intent(this, CropperActivity.class);
                        startActivityForResult(intent, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 300:
                    System.out.println("Result obtained from taking pic");
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

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position){

                case 0:
                    BuzzAnalytics.logScreen(Signup.this, BuzzAnalytics.ONBOARDING_CATEGORY, "signupForm");
                    break;
                case 1:
                    BuzzAnalytics.logScreen(Signup.this, BuzzAnalytics.ONBOARDING_CATEGORY, "signupProfilePicture");
                    break;

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
