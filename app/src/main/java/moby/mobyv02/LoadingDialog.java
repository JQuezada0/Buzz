package moby.mobyv02;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class LoadingDialog {

    private static Dialog d;

    public static void showDialog(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("One moment please");
        ProgressBar progressBar = new ProgressBar(c);
        builder.setView(progressBar);
        builder.setCancelable(false);
        d = builder.create();
        d.show();
    }

    public static void hideDialog(){
        d.hide();
    }

}
