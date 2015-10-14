package moby.mobyv02;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toolbar;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.List;

/**
 * Created by Johnil on 10/14/2015.
 */
public class ChatSearchUsersActivity extends Activity {

    private Toolbar toolbar;
    private ListView list;
    private EditText searchBar;
    private CircleProgressBar progress;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_search_user);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (ListView) findViewById(R.id.list);
        searchBar = (EditText) findViewById(R.id.searchbar);
        progress = (CircleProgressBar) findViewById(R.id.progress);

        timer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void searchForUsers(){



    }

}
