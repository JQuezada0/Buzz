package moby.mobyv02.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by quezadjo on 9/19/2015.
 */
public class GestureFrameLayout extends FrameLayout{

    private GestureDetector gestureDetector;
    private Context context;


    public GestureFrameLayout(Context context) {
        super(context);
        this.context = context;
    }

    public GestureFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GestureFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setGestureDetector(GestureDetector.SimpleOnGestureListener detector){
        gestureDetector = new GestureDetector(context, detector);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        gestureDetector.onTouchEvent(ev);
        return false;
    }
}
