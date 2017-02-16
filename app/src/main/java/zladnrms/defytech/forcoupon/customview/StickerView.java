package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import zladnrms.defytech.forcoupon.R;

/**
 * Created by kim on 2016-05-31.
 */
public class StickerView extends ImageView {

    private Context context;

    private float xDelta, yDelta;
    private float sv_x, sv_y; // 스티커 x, y 좌표
    private int sv_w, sv_h; // 스티커 너비, 높이
    private float sv_cx, sv_cy; // 스티커 중심좌표 (matrix scale용)

    private Bitmap stickerbitmap;
    private Bitmap touchHelperBitmap;
    private Matrix matrix;
    private Paint paint;

    private boolean onTouch = false;

    // 소스상에서 생성할 때
    public StickerView(Context context) {
        super(context);
        this.context = context;
    }

    // xml 을 통해 생성할 때 attribute 들이 attrs 로 넘어온다.
    public StickerView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(onTouch){ // 자신이 터치 되었을 시 네모 모서리로 표시됨
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, stickerbitmap.getWidth(), stickerbitmap.getHeight(), paint);

            canvas.drawBitmap(touchHelperBitmap, stickerbitmap.getWidth() - 50, stickerbitmap.getHeight() - 50, null);
        }

        if(stickerbitmap != null) { // 스티커 출력
            canvas.drawBitmap(stickerbitmap, 0, 0, null);
        }

        canvas.save();
    }

    // 스티커 Bitmap 세팅
    public void setStickerbitmap(Bitmap stickerbitmap){
        this.stickerbitmap = stickerbitmap;
        Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.touchhelp_arrow); // 세팅할 비트맵 가져오기
        this.touchHelperBitmap = Bitmap.createScaledBitmap(tempBitmap, 100, 100, false);
        invalidate();
        init();
    }

    // x, y좌표, 너비 높이 초기화
    private void init(){
        this.sv_x = 0;
        this.sv_y = 0;
        this.sv_w = stickerbitmap.getWidth();
        this.sv_h = stickerbitmap.getHeight();

        paint = new Paint();
        matrix = new Matrix();
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

    public boolean onTouchEvent(MotionEvent event) {

        if(onTouch) {
            final float X = event.getRawX();
            final float Y = event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    xDelta = (X - getTranslationX());
                    yDelta = (Y - getTranslationY());
                    break;
                case MotionEvent.ACTION_UP:
                    onTouch = false;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    setTranslationX(X - xDelta);
                    setTranslationY(Y - yDelta);
                    break;
            }

            // 현재 좌표를 업데이트
            sv_x = getTranslationX();
            sv_y = getTranslationY();

            return true;
        } else {
            return false;
        }

    }
}