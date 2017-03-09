package zladnrms.defytech.forcoupon.test_portrait;

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
public class StickerView_finalbackup extends ImageView implements View.OnTouchListener {

    private Context context;

    // 기본 정보
    private float xDelta, yDelta;
    private float sv_x, sv_y; // 스티커 x, y 좌표
    private int sv_w, sv_h; // 스티커 너비, 높이
    private float sv_cx, sv_cy; // 스티커 중심좌표 (matrix scale용)

    private Bitmap stickerbitmap;
    private Bitmap touchHelperBitmap;
    private Paint paint;

    private boolean onTouch = false;
    private boolean onHelperTouch = false;

    // 핀치 투 줌 (확대 축소)
    private static final String TAG = "ViewTouchImage";
    private static final boolean D = false;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Matrix savedMatrix2 = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;

    private boolean isInit = false;

    int scaleWidth, scaleHeight;

    public StickerView_finalbackup(Context context) {
        super(context);
        this.context = context;

        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        if(onTouch){ // 자신이 터치 되었을 시 네모 모서리로 표시됨
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawRect(0, 0, scaleWidth, scaleHeight, paint);
            canvas.drawBitmap(touchHelperBitmap, scaleWidth - 50, scaleHeight - 50, null);
        }

        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (D) Log.i(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);
        if (isInit == false){
            init();
            isInit = true;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (D) Log.i(TAG, "setImageBitmap");
        super.setImageBitmap(bm);
        //
        this.stickerbitmap = bm;
        Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.touchhelp_arrow); // 세팅할 비트맵 가져오기
        this.touchHelperBitmap = Bitmap.createScaledBitmap(tempBitmap, 100, 100, false);
        invalidate();
        //
        isInit = false;
        init();
    }

    protected void init() {

        this.sv_x = 0;
        this.sv_y = 0;

        this.sv_w = super.getWidth();
        this.sv_h = super.getHeight();

        paint = new Paint();

        matrixTurning(matrix, this);
        setImageMatrix(matrix);
        setImagePit();
    }

    public float getSv_y() {
        return sv_y;
    }

    public float getSv_x() {
        return sv_x;
    }

    public int getSv_w() {
        return sv_w;
    }

    public int getSv_h() {
        return sv_h;
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // height 진짜 크기 구하기
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = 0;
        switch(heightMode) {
            case MeasureSpec.UNSPECIFIED:    // mode 가 셋팅되지 않은 크기가 넘어올때
                heightSize = heightMeasureSpec;
                break;
            case MeasureSpec.AT_MOST:        // wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
                heightSize = 0;
                break;
            case MeasureSpec.EXACTLY:        // fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
                heightSize = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }

        // width 진짜 크기 구하기
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = 0;
        switch(widthMode) {
            case MeasureSpec.UNSPECIFIED:    // mode 가 셋팅되지 않은 크기가 넘어올때
                widthSize = widthMeasureSpec;
                break;
            case MeasureSpec.AT_MOST:        // wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
                widthSize = 0;
                break;
            case MeasureSpec.EXACTLY:        // fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
                widthSize = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }

        setMeasuredDimension(widthSize, heightSize);
        System.out.println(widthSize + " : " + heightSize);
    }


    /**
     * 이미지 핏
     */
    public void setImagePit(){

        // 매트릭스 값
        float[] value = new float[9];
        this.matrix.getValues(value);

        // 뷰 크기
        int width = this.getWidth();
        int height = this.getHeight();

        // 이미지 크기
        Drawable d = this.getDrawable();
        if (d == null)  return;
        int imageWidth = d.getIntrinsicWidth();
        int imageHeight = d.getIntrinsicHeight();
        //int scaleWidth = (int) (imageWidth * value[0]);
        //int scaleHeight = (int) (imageHeight * value[4]);
        scaleWidth = (int) (imageWidth * value[0]);
        scaleHeight = (int) (imageHeight * value[4]);

        // 이미지가 바깥으로 나가지 않도록.

        value[2] = 0;
        value[5] = 0;

        if (imageWidth > width || imageHeight > height){
            int target = WIDTH;
            if (imageWidth < imageHeight) target = HEIGHT;

            if (target == WIDTH) value[0] = value[4] = (float)width / imageWidth;
            if (target == HEIGHT) value[0] = value[4] = (float)height / imageHeight;

            scaleWidth = (int) (imageWidth * value[0]);
            scaleHeight = (int) (imageHeight * value[4]);

            if (scaleWidth > width) value[0] = value[4] = (float)width / imageWidth;
            if (scaleHeight > height) value[0] = value[4] = (float)height / imageHeight;
        }

        matrix.setValues(value);

        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(onTouch) {
            ImageView view = (ImageView) v;

            final float X = event.getRawX();
            final float Y = event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    xDelta = (X - getTranslationX());
                    yDelta = (Y - getTranslationY());
                    System.out.println(X - getTranslationX());

                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    onHelperTouch = false;
                    onTouch = false;
                    invalidate();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);

                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }

                    setTranslationX(X - xDelta);
                    setTranslationY(Y - yDelta);
                    System.out.println(X - xDelta);
                    break;
            }

            sv_x = getTranslationX();
            sv_y = getTranslationY();

            // 매트릭스 값 튜닝.
            matrixTurning(matrix, view);

            view.setImageMatrix(matrix);

            return true;
        } else {
            return false;
        }
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void matrixTurning(Matrix matrix, ImageView view){
        // 매트릭스 값
        float[] value = new float[9];
        matrix.getValues(value);
        float[] savedValue = new float[9];
        savedMatrix2.getValues(savedValue);

        // 뷰 크기
        int width = view.getWidth();
        int height = view.getHeight();

        // 이미지 크기
        Drawable d = view.getDrawable();
        if (d == null)  return;
        int imageWidth = d.getIntrinsicWidth();
        int imageHeight = d.getIntrinsicHeight();
        //int scaleWidth = (int) (imageWidth * value[0]);
        //int scaleHeight = (int) (imageHeight * value[4]);
        scaleWidth = (int) (imageWidth * value[0]);
        scaleHeight = (int) (imageHeight * value[4]);

        // 이미지가 바깥으로 나가지 않도록.
        if (value[2] < width - scaleWidth)   value[2] = width - scaleWidth;
        if (value[5] < height - scaleHeight)   value[5] = height - scaleHeight;
        if (value[2] > 0)   value[2] = 0;
        if (value[5] > 0)   value[5] = 0;

        // 10배 이상 확대 하지 않도록
        if (value[0] > 10 || value[4] > 10){
            value[0] = savedValue[0];
            value[4] = savedValue[4];
            value[2] = savedValue[2];
            value[5] = savedValue[5];
        }

        // 화면보다 작게 축소 하지 않도록
        if (imageWidth > width || imageHeight > height){
            if (scaleWidth < width && scaleHeight < height){
                int target = WIDTH;
                if (imageWidth < imageHeight) target = HEIGHT;

                if (target == WIDTH) value[0] = value[4] = (float)width / imageWidth;
                if (target == HEIGHT) value[0] = value[4] = (float)height / imageHeight;

                scaleWidth = (int) (imageWidth * value[0]);
                scaleHeight = (int) (imageHeight * value[4]);

                if (scaleWidth > width) value[0] = value[4] = (float)width / imageWidth;
                if (scaleHeight > height) value[0] = value[4] = (float)height / imageHeight;
            }
        }

        // 원래부터 작은 얘들은 본래 크기보다 작게 하지 않도록
        else{
            if (value[0] < 1)   value[0] = 1;
            if (value[4] < 1)   value[4] = 1;
        }

        matrix.setValues(value);
        savedMatrix2.set(matrix);
    }
}