package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import zladnrms.defytech.forcoupon.R;

/**
 * Created by kim on 2016-05-31.
 */
public class TouchHelperView extends ImageView implements View.OnTouchListener {

    private Context context;
    private float thv_x, thv_y;
    int Width, Height;

    private boolean onTouch = false;
    private Bitmap TouchHelperBitmap;

    public TouchHelperView(Context context) {
        super(context);
        this.context = context;

        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();


        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        System.out.println("헬퍼 : " + this.getDrawable().getIntrinsicWidth() + " : " + this.getDrawable().getIntrinsicHeight());
        invalidate();
    }

    public void setXY(float X, float Y){
        this.thv_x = X;
        this.thv_y = Y;
    }

    public boolean getOnTouch() {
        return onTouch;
    }

    // 자신이 터치 되었을 때
    public void setOnTouch(boolean onTouch) {
        this.onTouch = onTouch;
        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (onTouch) {
            ImageView view = (ImageView) v;

            final float X = event.getRawX();
            final float Y = event.getRawY();
            final float inX = event.getX();
            final float inY = event.getY();

            return true;
        } else {
            return false;
        }
    }
}