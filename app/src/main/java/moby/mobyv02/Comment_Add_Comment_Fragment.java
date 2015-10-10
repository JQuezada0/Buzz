package moby.mobyv02;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by quezadjo on 9/10/2015.
 */
public class Comment_Add_Comment_Fragment extends Fragment {

    Button cancelButton;
    Button postButton;
    TextView fullName;
    EditText commentText;
    CircleImageView image;
    private CommentActivity commentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.comment_add_comment_fragment, null);
        commentActivity = (CommentActivity) getActivity();
        cancelButton = (Button) v.findViewById(R.id.cancel_button);
        postButton = (Button) v.findViewById(R.id.create_comment_button);
        commentText = (EditText) v.findViewById(R.id.create_comment_text);
        fullName = (TextView) v.findViewById(R.id.create_comment_fullname);
        image = (CircleImageView) v.findViewById(R.id.create_comment_image);
        postButton.setOnClickListener(postButtonClickListener);
        cancelButton.setOnClickListener(cancelClickListener);
        fullName.setText(ParseUser.getCurrentUser().getUsername());
        Application.loadImage(image, ParseUser.getCurrentUser().getString("profileImage"));
        return v;
    }

    private final View.OnClickListener postButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            commentActivity.showProgressBar();
            new ParseOperation("Network").createComment(commentText.getText().toString(), new ParseOperation.ParseOperationCallback() {
                @Override
                public void finished(boolean success, ParseException e) {
                    commentActivity.hideProgressBar();
                    if (success) {
                        BuzzAnalytics.logScreen(commentActivity, BuzzAnalytics.COMMENT_CATEGORY, "viewComments");
                        BuzzAnalytics.logComment(commentActivity);
                        Comment_Add_Comment_Fragment.this.getFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(Comment_Add_Comment_Fragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, commentActivity);
        }

    };

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            BuzzAnalytics.logScreen(commentActivity, BuzzAnalytics.COMMENT_CATEGORY, "viewComments");
            Comment_Add_Comment_Fragment.this.getFragmentManager().popBackStack();

        }
    };

}
