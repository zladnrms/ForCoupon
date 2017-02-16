package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import zladnrms.defytech.forcoupon.App_couponedit;
import zladnrms.defytech.forcoupon.R;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 쿠폰 사진이 있으면
        if (this.bitmap != null) {

            canvas.drawBitmap(scaleCenterCrop(this.bitmap, cv_h, cv_w), 0, 0, null);
        }

        canvas.save();
    }

    // ImageView의 scaletype="CenterCrop" 과 같은 기능하는 소스, (사진 Bitmap, 원판의 height, width)
    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
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

    /*
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction(); // 현재의 터치 액션의 종류를 받아온다.
        float x, y; // 터치 된 x, y좌표

        x = event.getX(); // 해당 뷰 내에서의 좌표를 받아옴
        y = event.getY();

        Log.d("터치 좌표", "x좌표 : " + x + " || y좌표 : " + y);

        boolean moveOk = false; // 움직일 수 있는 상황일 때 true
        boolean sizeOk = false; // 확장 / 축소 시킬 수 있는 상황일 때 true

        switch (phase) {
            case 0: // 쿠폰 phase
                break;
            case 1: // 텍스트 phase
                int textNum = 0; // 움직일 대상 텍스트

                for (int i = 0; i < textArr.size(); i++) { // 터치한 곳에 있는 텍스트를 찾는다
                    if (x > textArr.get(i).getX() && x < textArr.get(i).getX() + textArr.get(i).getTextWidth() && y > textArr.get(i).getY() && y < textArr.get(i).getY() + textArr.get(i).getTextHeight()) {
                        textNum = textArr.get(i).getNum(); // 해당 텍스트가 무엇인지 받음
                        moveOk = true;
                        break;
                    } else {
                        moveOk = false;
                    }
                }

                switch (action) { // 액션의 종류에 따른 역할 수행
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX(); // 해당 뷰 내에서의 좌표를 받아옴
                        y = event.getRawY();
                        System.out.println("DOWN : x : " + x + ", y : " + y);
                        break;
                    case MotionEvent.ACTION_MOVE: // 드래그 되었을 때의 이벤트 처리

                        // 움직이기
                        if (moveOk) { // 움직일 수 있는 상황이라면

                            if (textArr.get(textNum).getPrevX() > 0 && textArr.get(textNum).getPrevY() > 0) {
                                textArr.get(textNum).setX(x, textArr.get(textNum).getPrevX()); // 그만큼 이동하기 위해
                                textArr.get(textNum).setY(y, textArr.get(textNum).getPrevY());
                            }

                            // 현재의 좌표들이 지난 좌표가 된다.
                            textArr.get(textNum).setPrevX(x);
                            textArr.get(textNum).setPrevY(y);

                            // 좌표 이동이 끝났으면 화면을 갱신한다.
                            invalidate();
                        }
                        break;
                }
                break;
            case 2: // 스티커 phase
                int stickerNum = 0; // 움직일 대상 스티커

                for (int i = 0; i < stickerArr.size(); i++) { // 터치한 곳에 있는 스티커를 찾는다
                    if (x > stickerArr.get(i).getX() && x < stickerArr.get(i).getX() + stickerArr.get(i).getStickerWidth() && y > stickerArr.get(i).getY() && y < stickerArr.get(i).getY() + stickerArr.get(i).getStickerHeight()) {
                        stickerNum = stickerArr.get(i).getNum(); // 해당 스티커가 무엇인지 받음
                        moveOk = true;
                        break;
                    } else {
                        moveOk = false;
                    }
                }


                for (int i = 0; i < stickerArr.size(); i++) { // 터치한 곳에 있는 스티커의 화살표를 찾는다
                    if (x > stickerArr.get(i).getArrowX() && x < stickerArr.get(i).getArrowX() + stickerArr.get(i).getArrowWidth() && y > stickerArr.get(i).getArrowY() && y < stickerArr.get(i).getArrowY() + stickerArr.get(i).getArrowHeight()) {
                        stickerNum = stickerArr.get(i).getNum(); // 해당 스티커가 무엇인지 받음
                        sizeOk = true;
                        break;
                    } else {
                        sizeOk = false;
                    }
                }


                switch (action) { // 액션의 종류에 따른 역할 수행
                    case MotionEvent.ACTION_DOWN:
                        x = event.getRawX(); // 해당 뷰 내에서의 좌표를 받아옴
                        y = event.getRawY();
                        System.out.println("DOWN : x : " + x + ", y : " + y + "DOWN@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        break;
                    case MotionEvent.ACTION_MOVE: // 드래그 되었을 때의 이벤트 처리

                        // 움직이기
                        if (moveOk) { // 움직일 수 있는 상황이라면

                            if (stickerArr.get(stickerNum).getPrevX() > 0 && stickerArr.get(stickerNum).getPrevY() > 0) {
                                stickerArr.get(stickerNum).setX(x, stickerArr.get(stickerNum).getPrevX()); // 그만큼 이동하기 위해
                                stickerArr.get(stickerNum).setY(y, stickerArr.get(stickerNum).getPrevY());
                            }

                            // 현재의 좌표들이 지난 좌표가 된다.
                            stickerArr.get(stickerNum).setPrevX(x);
                            stickerArr.get(stickerNum).setPrevY(y);


                            System.out.println("넓이, 높이 :" + stickerArr.get(stickerNum).getStickerWidth() + "." + stickerArr.get(stickerNum).getStickerHeight());

                            float arrowx = stickerArr.get(stickerNum).getX() + stickerArr.get(stickerNum).getStickerWidth();
                            float arrowy = stickerArr.get(stickerNum).getY() + stickerArr.get(stickerNum).getStickerHeight();

                            if (stickerArr.get(stickerNum).getArrowPrevX() > 0 && stickerArr.get(stickerNum).getArrowPrevY() > 0) {
                                stickerArr.get(stickerNum).setArrowX(arrowx, stickerArr.get(stickerNum).getArrowPrevX()); // 그만큼 이동하기 위해
                                stickerArr.get(stickerNum).setArrowY(arrowy, stickerArr.get(stickerNum).getArrowPrevY());
                            }

                            // 현재의 좌표들이 지난 좌표가 된다.
                            stickerArr.get(stickerNum).setArrowPrevX(arrowx);
                            stickerArr.get(stickerNum).setArrowPrevY(arrowy);

                            // 좌표 이동이 끝났으면 화면을 갱신한다.
                            invalidate();
                        }


                        // 사이즈 확장 / 축소
                        if (sizeOk) {

                            stickerArr.get(stickerNum).setStickerWidth(x);
                            stickerArr.get(stickerNum).setStickerHeight(y);

                            float arrowx = stickerArr.get(stickerNum).getX() + stickerArr.get(stickerNum).getStickerWidth();
                            float arrowy = stickerArr.get(stickerNum).getY() + stickerArr.get(stickerNum).getStickerHeight();

                            if (stickerArr.get(stickerNum).getArrowPrevX() > 0 && stickerArr.get(stickerNum).getArrowPrevY() > 0) {
                                stickerArr.get(stickerNum).setArrowX(arrowx, stickerArr.get(stickerNum).getArrowPrevX()); // 그만큼 이동하기 위해
                                stickerArr.get(stickerNum).setArrowY(arrowy, stickerArr.get(stickerNum).getArrowPrevY());
                            }

                            System.out.println("넓이, 높이 :" + stickerArr.get(stickerNum).getStickerWidth() + "." + stickerArr.get(stickerNum).getStickerHeight());

                            // 현재의 좌표들이 지난 좌표가 된다.
                            stickerArr.get(stickerNum).setArrowPrevX(arrowx);
                            stickerArr.get(stickerNum).setArrowPrevY(arrowy);

                            // 좌표 이동이 끝났으면 화면을 갱신한다.
                            invalidate();
                        }
                        break;

                }
                break;
        }
        return true;
    }
    */
}