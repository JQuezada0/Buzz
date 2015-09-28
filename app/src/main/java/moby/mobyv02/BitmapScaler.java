package moby.mobyv02;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.io.File;

/**
 * Created by quezadjo on 9/14/2015.
 */
public class BitmapScaler {

    public static Bitmap upscaleBitmap(View v, File file){

        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        int targetHeight = v.getHeight();
        int targetWidth = v.getWidth();

        int currentHeight = bm.getHeight();
        int currentWidth = bm.getWidth();

        if (currentHeight < targetHeight && currentWidth < targetWidth){

            if (targetHeight > targetWidth) {

                while (targetHeight > currentHeight){
                    currentHeight++;
                    currentWidth++;
                }

            } else if (targetWidth > targetHeight) {

                while (targetWidth > currentWidth){
                    currentWidth++;
                    currentHeight++;
                }

            }
        }

        return Bitmap.createScaledBitmap(bm, currentWidth, currentHeight, false);

    }
    public static Bitmap upscaleBitmap(View v, Bitmap bm){

        int targetHeight = v.getHeight();
        int targetWidth = v.getWidth();

        int currentHeight = bm.getHeight();
        int currentWidth = bm.getWidth();

        if (targetHeight > 0 && targetWidth > 0) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) targetWidth / (float) targetHeight;

            int finalWidth = targetWidth;
            int finalHeight = targetHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)targetHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)targetWidth / ratioBitmap);
            }
            bm = Bitmap.createScaledBitmap(bm, finalWidth, finalHeight, true);
            return bm;
        } else {
            return bm;
        }
    }


}
