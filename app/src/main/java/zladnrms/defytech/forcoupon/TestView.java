package zladnrms.defytech.forcoupon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by kim on 2016-05-31.
 */
public class TestView extends ImageView implements View.OnTouchListener {

    private Context context;

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    private boolean toggle = false;


    private Paint paint;
    private int bitmapWidth, bitmapHeight;
    private float tv_w, tv_h;
    Rect rect;

    public TestView(Context context) {
        super(context);
        this.context = context;

        paint = new Paint();
        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
        init();
        System.out.println("ㅇㅇ생ㅅ1");
    }

    public TestView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;

        paint = new Paint();
        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
        init();
        System.out.println("ㅇㅇ생ㅅ2");
    }

    public TestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        paint = new Paint();
        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
        init();
        System.out.println("ㅇㅇ생ㅅ");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        System.out.println("드로우");
        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        canvas.concat(matrix);
        // canvas.setMatrix(matrix); 했더니 타이틀바부터 0, 0으로 인식함.
        // http://stackoverflow.com/questions/17100355/canvas-is-offset-by-setting-identity-matrix-in-ondraw-of-custom-view 참조함

        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(rect, paint);

        canvas.restore();
    }

    private void init() {
        this.bitmapWidth = this.getDrawable().getIntrinsicWidth();
        this.bitmapHeight = this.getDrawable().getIntrinsicHeight();
        this.tv_w = bitmapWidth;
        this.tv_h = bitmapHeight;
        this.rect = new Rect();
        this.rect.set(0, 0, (int)this.tv_w, (int)this.tv_h);
    }

    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        float[] values = new float[9];
                        matrix.getValues(values);
                        setInitByMatrix(view, values);
                    } else if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                        matrix.getValues(values);
                        setInitByMatrix(view, values);

                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        System.out.println("온터치끝");
        invalidate();
        return true;
    }

    private void setInitByMatrix(ImageView currentView, float[] values) {
        //this.tv_w = (int) (bitmapWidth * values[Matrix.MSCALE_X]);
        //this.tv_h = (int) (bitmapHeight * values[Matrix.MSCALE_Y]);
        this.tv_w = values[Matrix.MSCALE_X]*((ImageView)currentView).getDrawable().getIntrinsicWidth();
        this.tv_h = values[Matrix.MSCALE_Y]*((ImageView)currentView).getDrawable().getIntrinsicHeight();
        System.out.println(tv_w + " : " + tv_h);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        System.out.println(width + " : " + height);
    }


    /**
     * 두 손가락 사이의 거리를 구하는 함수
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 두 손가락의 중점을 구하는 함수
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 회전될 각도를 구하는 함수
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}