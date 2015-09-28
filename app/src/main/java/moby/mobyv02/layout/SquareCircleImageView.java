package moby.mobyv02.layout;

import android.content.Context;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by quezadjo on 9/11/2015.
 */
public class SquareCircleImageView extends CircleImageView {
    public SquareCircleImageView(Context context) {
        super(context);
    }

    public SquareCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }
}
