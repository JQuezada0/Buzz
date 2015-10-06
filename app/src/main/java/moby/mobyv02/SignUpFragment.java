package moby.mobyv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by quezadjo on 9/14/2015.
 */
public class SignUpFragment extends Fragment {

    Button signupButton;
    Button loginButton;
    EditText username;
    EditText password;
    EditText confirmPassword;
    EditText email;
    TextView birthday;
    Spinner gender;
    EditText fullName;
    Signup signup;
    DatePicker datePicker;
    Date date;
    private ArrayAdapter<String> genderAdapter;
    //change

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            View v = View.inflate(getActivity(), R.layout.signup_fragment, null);
            signupButton = (Button) v.findViewById(R.id.signup_button);
            username = (EditText) v.findViewById(R.id.signup_username);
            password = (EditText) v.findViewById(R.id.signup_password);
            confirmPassword = (EditText) v.findViewById(R.id.signup_confirm_password);
            email = (EditText) v.findViewById(R.id.signup_email);
            gender = (Spinner) v.findViewById(R.id.signup_gender);
            birthday = (TextView) v.findViewById(R.id.signup_birthday);
            fullName = (EditText) v.findViewById(R.id.signup_fullname);
            loginButton = (Button) v.findViewById(R.id.login_button);
            signup = (Signup) getActivity();
            datePicker = new DatePicker();
            datePicker.setSignUpFragment(this);
            genderAdapter = new ArrayAdapter<String>
                    (signup, android.R.layout.simple_spinner_item, Arrays.asList(new String[]{"Male", "Female"}));
//        username.addTextChangedListener(Factory.getLowerCaseTextFormatter(username));
//        email.addTextChangedListener(Factory.getLowerCaseTextFormatter(email));

            signupButton.setOnClickListener(signupClickListener);
            loginButton.setOnClickListener(loginClickListener);
            birthday.setOnClickListener(birthdayClickListener);
            gender.setAdapter(genderAdapter);

            return v;
        }

    final View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            startActivity(new Intent(signup, Login.class));

        }
    };

    final View.OnClickListener signupClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty() || confirmPassword.getText().toString().isEmpty()
                    || email.getText().toString().isEmpty() || fullName.getText().toString().isEmpty()){

                Toast.makeText(signup, "Please fill all fields", Toast.LENGTH_SHORT).show();

            } else if (!password.getText().toString().equals(confirmPassword.getText().toString())){

                Toast.makeText(signup, "Please make sure passwords match", Toast.LENGTH_SHORT).show();

            } else if (date == null){
                Toast.makeText(signup, "Please select your birth date", Toast.LENGTH_SHORT).show();
            } else {
                signup.getProfilePictureFragment().nameText.setText(fullName.getText().toString());
                signup.getSignUpViewPager().setCurrentItem(1,true);
            }

        }
    };

    final View.OnClickListener birthdayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            datePicker.show(getFragmentManager(), "datePicker");
        }
    };

    public void setDate(int year, int month, int day, Date d){
        date = d;
        birthday.setText(String.valueOf(year) + "/" + (month + 1) + "/" + day);

    }
}
