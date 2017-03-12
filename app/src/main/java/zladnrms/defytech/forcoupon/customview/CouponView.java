package zladnrms.defytech.forcoupon.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by kim on 2016-05-31.
 */
public class CouponView extends ImageView {

    private Context context;

    private Bitmap bitmap; // 쿠폰 비트맵

    private int cv_x, cv_y;
    private int cv_w, cv_h;

    public CouponView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
    }

    // FOR LOLLIPOP, http://stackoverflow.com/questions/27674701/why-do-we-need-a-4th-constructor-for-lollipop 참고
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CouponView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 쿠폰 사진이 있으면
        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, 0, 0, null);
        }

        canvas.save();
    }

    // 스티커 Bitmap 세팅
    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
        invalidate();
        init();
    }

    // x, y좌표, 너비 높이 초기화
    private void init(){
        this.cv_x = 0;
        this.cv_y = 0;
        this.cv_w = this.bitmap.getWidth();
        this.cv_h = this.bitmap.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // height 진짜 크기 구하기
        int heightMode = MeasureSpec.EXACTLY;
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
        int widthMode =  MeasureSpec.EXACTLY;
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
        System.out.println("CouponView-onMeasure  : " + widthSize + " : " + heightSize );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.v("CouponView-onLayout", "rect : (x, y, w, h) : " + this.getLeft() + " " + this.getTop() + " " + (this.getRight()-this.getLeft()) + " " + (this.getBottom()-this.getRight()));
    }
}