package moby.mobyv02;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by quezadjo on 9/14/2015.
 */
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;
    LayoutInflater inflater;

    public InfoWindowAdapter(Context c){
        context = c;
        inflater = LayoutInflater.from(c);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = inflater.inflate(R.layout.post_photo_layout, null);

        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
