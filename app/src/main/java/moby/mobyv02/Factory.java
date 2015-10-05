package moby.mobyv02;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by quezadjo on 9/13/2015.
 */
public class Factory {


    public static TextWatcher getLowerCaseTextFormatter(final EditText et){

        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                et.removeTextChangedListener(this);
                et.setText(et.getText().toString().toLowerCase());
                et.addTextChangedListener(this);
                et.setSelection(editable.length());
            }
        };
    }



}
