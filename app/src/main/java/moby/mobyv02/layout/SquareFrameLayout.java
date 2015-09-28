package moby.mobyv02.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by quezadjo on 9/9/2015.
 */
public class SquareFrameLayout extends FrameLayout {


    public SquareFrameLayout(Context context) {
        super(context);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

}
