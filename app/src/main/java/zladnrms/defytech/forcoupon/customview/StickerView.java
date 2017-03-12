package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import zladnrms.defytech.forcoupon.R;

/**
 * Created by kim on 2016-05-31.
 */
public class StickerView extends ImageView implements View.OnTouchListener{

    private Context context;

    // 기본 정보
    private float xDelta, yDelta;
    private float sv_x, sv_y; // 스티커 x, y 좌표
    private int sv_w, sv_h; // 스티커 너비, 높이
    private int bitmapWidth, bitmapHeight; // 스티커 비트맵 원본 너비, 높이
    private float sv_cx, sv_cy; // 스티커 중심좌표 (matrix scale용)
    private String type; // 스티커 타입
    private float transparentPercent = 0;
    private int transparentInt = 0;

    private Bitmap stickerbitmap;
    private Bitmap touchHelperBitmap;

    private Paint paint;
    private Rect rect; // 네모 영역 rect
    private Rect rect_helper;

    private boolean canTouch = false; // 쿠폰 에디터의 레이어 설정으로 인한 터치 가능 여부
    private boolean onTouch = false;
    private boolean onHelperTouch = false;

    private int TaskMode = 0; // 0 : 확장, 회전 둘 다 가능, 1 : 확장만 가능, 2 : 회전만 가능
    private int visible = 1;
    //

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

    public StickerView(Context context) {
        super(context);
        this.context = context;

        setOnTouchListener(this);
        setScaleType(ScaleType.MATRIX);


    }

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            Log.d("TAKE", "Long press!!");
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        if (onTouch) { // 자신이 터치 되었을 시 네모 모서리로 표시됨
            canvas.concat(matrix);
            // canvas.setMatrix(matrix); 했더니 타이틀바부터 0, 0으로 인식함.
            // http://stackoverflow.com/questions/17100355/canvas-is-offset-by-setting-identity-matrix-in-ondraw-of-custom-view 참조함

            canvas.drawRect(rect , paint);
            canvas.drawBitmap(touchHelperBitmap, null, rect_helper, null);
        }

        canvas.restore();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.stickerbitmap = bm;
        Bitmap tempBitmap_1 = BitmapFactory.decodeResource(getResources(), R.drawable.touchhelp_arrow); // 세팅할 비트맵 가져오기
        this.touchHelperBitmap = Bitmap.createScaledBitmap(tempBitmap_1, 100, 100, false);
        invalidate();
        init();
    }

    protected void init() {
        this.sv_x = 0;
        this.sv_y = 0;

        this.bitmapWidth = this.getDrawable().getIntrinsicWidth();
        this.bitmapHeight = this.getDrawable().getIntrinsicHeight();
        this.sv_w = this.bitmapWidth;
        this.sv_h = this.bitmapHeight;

        // 네모 영역 Paint 초기화
        paint = new Paint();
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        this.rect = new Rect();
        this.rect.set(0, 0, (int)this.sv_w, (int)this.sv_h);

        this.rect_helper = new Rect();
        this.rect_helper.set((int)this.sv_w - 50, (int)this.sv_h - 50, (int)this.sv_w + 50, (int)this.sv_h + 50);
    }

    public float getSv_x() {
        return this.sv_x;
    }

    public float getSv_y() {
        return this.sv_y;
    }

    public int getSv_w() {
        return this.sv_w;
    }

    public int getSv_h() {
        return this.sv_h;
    }

    public boolean getCanTouch(){
        return this.canTouch;
    }

    public void setCanTouch(boolean _touch){
        this.canTouch = _touch;
    }

    public boolean getOnTouch() {
        return onTouch;
    }

    public void setType(String _type){
        this.type = _type;
    }

    public String getType(){
        return this.type;
    }

    // 자신이 터치 되었을 때
    public void setOnTouch(boolean onTouch) {
        this.onTouch = onTouch;
        invalidate();
    }

    public float getTransparentPercent(){
        return this.transparentInt;
    }

    // 자신이 터치 되었을 때
    public void setTransparentPercent(int _percent) {
        this.transparentInt = _percent;
        String temp = "0." + String.valueOf(_percent);
        this.transparentPercent = Float.valueOf(temp);
        System.out.println(temp);
        setAlpha(transparentPercent);
    }

    public boolean onTouch(View v, MotionEvent event) {

        final float X = event.getX();
        final float Y = event.getY();

        System.out.println("클릭 X : " + X + ", Y : " + Y );
        System.out.println("좌표 X : " + sv_x + ", Y : " + sv_y);
        System.out.println("좌표 X : " + (X + sv_w) + ", Y : " + (Y + sv_h));

        // 화면 터치 시에 자신을 클릭하는 것인지 파악함
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if(X > this.sv_x && X < this.sv_x + this.sv_w && Y > this.sv_y && Y < this.sv_y + this.sv_h){
                    onTouch = true;
                } else {
                    onTouch = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                onTouch = false;
                break;
        }

        invalidate();

        if (onTouch) {
            ImageView view = (ImageView) v;

            // 터치 헬퍼 클릭 시
            if(X > this.sv_x + this.sv_w - 50 && X < this.sv_x + this.sv_w + 50 && Y > this.sv_y + this.sv_h - 50 && Y < this.sv_y + this.sv_h + 50){
                /*
                switch (TaskMode){
                    case 0:
                        TaskMode = 1;
                        break;
                    case 1:
                        TaskMode = 2;
                        break;
                    case 2:
                        TaskMode = 0;
                        break;
                }
                */
            }

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    handler.postDelayed(mLongPressed, 1000); // 롱 프레스 시도

                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    lastEvent = null;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    handler.removeCallbacks(mLongPressed); // 롱 프레스 해제

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
                    handler.removeCallbacks(mLongPressed); // 롱 프레스 해제

                    onHelperTouch = false;
                    onTouch = false;
                    invalidate();

                case MotionEvent.ACTION_POINTER_UP:
                    handler.removeCallbacks(mLongPressed); // 롱 프레스 해제

                    mode = NONE;
                    lastEvent = null;
                    break;

                case MotionEvent.ACTION_MOVE:
                    handler.removeCallbacks(mLongPressed); // 롱 프레스 해제

                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;
                        matrix.postTranslate(dx, dy);
                        float[] values = new float[9];
                        matrix.getValues(values);
                        setInitByMatrix(values);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f/* && (TaskMode == 0 || TaskMode == 1)*/) {
                            matrix.set(savedMatrix);
                            float scale = (newDist / oldDist);
                            matrix.postScale(scale, scale, mid.x, mid.y);
                            float[] values = new float[9];
                            matrix.getValues(values);
                            setInitByMatrix(values);
                        }
                        if (lastEvent != null && event.getPointerCount() == 2/* && (TaskMode == 0 || TaskMode == 2)*/) {
                            newRot = rotation(event);
                            float r = newRot - d;
                            float[] values = new float[9];
                            matrix.getValues(values);
                            float tx = values[2];
                            float ty = values[5];
                            float sx = values[0];
                            float xc = (this.sv_w / 2) * sx;
                            float yc = (this.sv_h / 2) * sx;
                            matrix.postRotate(r, tx + xc, ty + yc);
                            setInitByMatrix(values);
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            return true;
        } else {
            return false;
        }
    }

    // Matrix 변경 후 Matrix 값 통한 X, Y, 너비, 높이 설정
    private void setInitByMatrix(float[] values){
        this.sv_x = values[2];
        this.sv_y = values[5];
        this.sv_w = (int) (bitmapWidth * values[0]);
        this.sv_h = (int) (bitmapHeight * values[4]);
    }

    /**
     * 두 손가락 사이의 거리를 구하는 함수
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
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