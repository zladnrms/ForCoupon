package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by kim on 2016-05-31.
 */
public class TexterView extends ImageView {

    private Context context;

    private float xDelta, yDelta;
    private float sv_x, sv_y; // 스티커 x, y 좌표
    private int sv_w, sv_h; // 스티커 너비, 높이

    private String content; // 텍스트 내용
    private int size; // 텍스트 크기
    private String color; // 텍스트 색상

    private boolean onTouch = false;

    // 소스상에서 생성할 때
    public TexterView(Context context) {
        super(context);
        this.context = context;
    }

    // xml 을 통해 생성할 때 attribute 들이 attrs 로 넘어온다.
    public TexterView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(onTouch){ // 자신이 터치 되었을 시 네모 모서리로 표시됨
            Paint paint = new Paint();
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, sv_w, sv_h, paint);
        }

        if(sv_w > 0 && sv_h > 0) { // 텍스트 출력
            //Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            paint.setStyle(Paint.Style.FILL);
            System.out.println("미졀텍스트" + paint.measureText(content));
            System.out.println(size + " : " + color);
            paint.setColor(Color.parseColor(color));
            paint.setTextSize(size);
            paint.setTextAlign(Paint.Align.LEFT);
            Paint.FontMetrics metric = paint.getFontMetrics();
            int textHeight = (int) Math.ceil(metric.descent - metric.ascent);
            int y = (int)(textHeight - metric.descent);
            canvas.drawText(content, 0, y, paint);
        }

        canvas.save();
    }

    // 스티커 Bitmap 세팅
    public void setText(String content, int size, String color){
        this.content = content;
        this.size = size;
        this.color = color;
        invalidate();
        init();
    }

    // text width 측정
    private float measureWidthText(String content, int size) {
        Paint p = new Paint();
        Rect bounds = new Rect();
        p.setTextSize(size);

        p.getTextBounds(content, 0, content.length(), bounds);
        float mt = p.measureText(content);

        return mt;
    }

    // text height 측정
    private float measureHeightText(String content, int size) {
        Paint p = new Paint();
        Rect bounds = new Rect();
        p.setTextSize(size);

        p.getTextBounds(content, 0, content.length(), bounds);

        return bounds.height();
    }

    // x, y좌표, 너비 높이 초기화
    private void init(){
        this.sv_x = 0;
        this.sv_y = 0;
        this.sv_w = (int) measureWidthText(content, size);
        this.sv_h = (int) measureHeightText(content, size);
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

        Log.w("ㅇㅇ","onMeasure("+widthMeasureSpec+","+heightMeasureSpec+")");

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
                case MotionEvent.ACTION_POINTER_DOWN:
                    System.out.println("두번째손다운");
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    System.out.println("두번째손업");
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