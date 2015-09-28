package moby.mobyv02;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by quezadjo on 9/25/2015.
 */
public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private SignUpFragment signUpFragment;


    public void setSignUpFragment(SignUpFragment signUpFragment){
        this.signUpFragment = signUpFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

        signUpFragment.setDate(year, monthOfYear, dayOfMonth);

    }
}
