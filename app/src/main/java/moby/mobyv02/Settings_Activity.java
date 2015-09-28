package moby.mobyv02;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.leanplum.Leanplum;
import com.leanplum.activities.LeanplumFragmentActivity;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class Settings_Activity extends LeanplumFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "dev_DZYELDJSN3ASeJHFHQUuuCuSf2t4uxOJvw5wUAimw6c");
        } else {
            Leanplum.setAppIdForProductionMode("app_fHaR2B7Xb1mamIGfU4z9FXb50eVY5QeHvPURmpXAFio", "prod_Y0Uw1nzvxdrA8sY4ruuMOt2OI84pdudG3GbpCAqbhwY");
        }
        Leanplum.start(this);
    }

}
