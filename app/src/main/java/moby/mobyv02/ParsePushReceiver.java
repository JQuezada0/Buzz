package moby.mobyv02;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by quezadjo on 9/30/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(final Context mContext, Intent intent) {
        System.out.println("Received notification");
        final JSONObject data;
        try {
            data = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            ImageLoader imageLoader = new ImageLoader(Volley.newRequestQueue(mContext), new ImageLoader.ImageCache() {

                private final android.support.v4.util.LruCache<String, Bitmap> cache = new android.support.v4.util.LruCache<String, Bitmap>(20);

                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            });
            System.out.println("reading info");
            String profileImage = data.optString("image");
            String uriString = data.optString("activity");
            String objectId = data.optString("user");
            Intent uriIntent = new Intent(mContext, Main.class);
            if (uriString.equals("")) {
                uriIntent = new Intent(mContext, Main.class);
            }
            final Intent finalUriIntent = uriIntent;
            System.out.println("Getting profile Image");
            if (profileImage != null && !profileImage.equals("")){
                imageLoader.get(profileImage, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null){
                            try {
                                System.out.println("Show notification");
                                showNotification(response.getBitmap(), mContext, data.getString("title"), data.getString("alert"), finalUriIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            showNotification(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_icon_graybg), mContext, data.getString("title"), data.getString("alert"), finalUriIntent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                showNotification(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_icon_graybg), mContext, data.getString("title"), data.getString("alert"), finalUriIntent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showNotification(Bitmap bm, Context context, String title, String message, Intent intent){

        PendingIntent resultIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.buzz_parse_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.buzz_parse_icon))
                .setContentIntent(resultIntent)
                .setVibrate(new long[] { 250, 250})
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(1250, notification);
    }

}
