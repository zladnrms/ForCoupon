package zladnrms.defytech.forcoupon.customview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import zladnrms.defytech.forcoupon.R;

/**
 * Created by kim on 2016-06-08.
 */
public class CropImageView extends ImageView {
    private static final String TAG = "Crop_Image";
    float sx, ex, sy, ey;
    static int DEP = 30;  // Crop 경계선의 유효폭(선근처)

    Context context;
    Bitmap bitmap;
    float mWidth;
    float mHeight;
    Paint pnt;


    Bitmap hBmp; // 선 가운데의 세로선택 아이콘
    Bitmap wBmp; // 가로

    private String outFilePath;

    public CropImageView(Context context) {
        super(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        /*
        아래의 onMeasure 메소드에서 초기화를 진행한다
         */

        hBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_crop_height);
        wBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_crop_width);

        // 페인트 설정
        pnt = new Paint();
        pnt.setColor(Color.YELLOW);
        pnt.setStrokeWidth(3);
    }

    public void onDraw(Canvas canvas) {
        if (isInEditMode()) return;
        // 사각형 라인 그리기
        if(bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.drawLine(sx, sy, ex, sy, pnt);
        canvas.drawLine(ex, sy, ex, ey, pnt);
        canvas.drawLine(sx, sy, sx, ey, pnt);
        canvas.drawLine(sx, ey, ex, ey, pnt);
        // 상하좌우 버튼들
        canvas.drawBitmap(hBmp, (ex + sx) / 2 - 19, sy - 19, null); // 폭이 38이므로 그려줄 좌상단 위치 지정
        canvas.drawBitmap(hBmp, (ex + sx) / 2 - 19, ey - 19, null);
        canvas.drawBitmap(wBmp, sx - 19, (ey + sy) / 2 - 19, null);
        canvas.drawBitmap(wBmp, ex - 19, (ey + sy) / 2 - 19, null);
    }

    // 이벤트 처리, 현재의 그리기 모드에 따른 점의 위치를 조정
    float dx = 0, dy = 0;
    float oldx, oldy;
    boolean bsx, bsy, bex, bey;
    boolean bMove = false;

    public boolean onTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            oldx = x;
            oldy = y;

// 눌려진곳이 선 근처인가 확인
            if ((x > sx - DEP) && (x < sx + DEP))
                bsx = true;
            else if ((x > ex - DEP) && (x < ex + DEP))
                bex = true;

            if ((y > sy - DEP) && (y < sy + DEP))
                bsy = true;
            else if ((y > ey - DEP) && (y < ey + DEP))
                bey = true;

            // 어느 하나라도 선택이 되었다면 move에서 값 변경
            if ((bsx || bex || bsy || bey))
                bMove = false;
            else if (((x > sx + DEP) && (x < ex - DEP))
                    && ((y > sy + DEP) && (y < ey - DEP)))
                bMove = true;

            return true;
        }

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (bsx) sx = x;
            if (bex) ex = x;
            if (bsy) sy = y;
            if (bey) ey = y;

// 사각형의 시작 라인보다 끝라인이 크지않게 처리
            if (ex <= sx + DEP) {
                ex = sx + DEP;
                return true;
            }
            if (ey <= sy + DEP) {
                ey = sy + DEP;
                return true;
            }

            // 움직인 거리 구해서 적용
            if (bMove) {
                dx = oldx - x;
                dy = oldy - y;

                sx -= dx;
                ex -= dx;
                sy -= dy;
                ey -= dy;

// 화면밖으로 나가지않게 처리
                if (sx <= 0) sx = 0;
                if (ex >= mWidth) ex = mWidth - 1;

                if (sy <= 0) sy = 0;
                if (ey >= mHeight) ey = mHeight - 1;
            }

            invalidate(); // 움직일때 다시 그려줌
            oldx = x;
            oldy = y;
            return true;
        }

        // ACTION_UP 이면 그리기 종료
        if (e.getAction() == MotionEvent.ACTION_UP) {
            bsx = bex = bsy = bey = bMove = false;
            return true;
        }
        return false;
    }

    // 선택된 사각형의 이미지를 저장
    public void save() {
        Bitmap tmp = Bitmap.createBitmap(bitmap, (int) sx, (int) sy, (int) (ex - sx), (int) (ey - sy));
        byte[] byteArray = bitmapToByteArray(tmp);

        File file = new File(Environment.getExternalStorageDirectory() + "/img_0.png");
        int loop;
        for (loop = 0; loop < 50; loop++) {

            if (file.exists()) {
                System.out.println("sout : 도는중 " + loop);
                file = new File(Environment.getExternalStorageDirectory() + "/img_" + loop + ".png");
            } else {
                file = new File(Environment.getExternalStorageDirectory() + "/img_" + loop + ".png");
                //album_write.uploadFilePath = Environment.getExternalStorageDirectory() + "/img_" + loop + ".png";
                break;
            }
        }

        Log.e("nicehee", file.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.flush();
            fos.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))); // 갤러리에 바로 올라오도록함
        } catch (Exception e) {
            Toast.makeText(this.context, "파일 저장 중 에러 발생 : " +
                    e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // 이미지를 전송하기위한 테스트 코드
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        Drawable d = getDrawable();

        if (d != null) {
            if (getMeasuredWidth() > getMeasuredHeight()) { // 사진 원본의 가로 크기가 세로보다 클 경우, 부모 View의 가로에 맞게 먼저 가로를 채우고 세로를 배정
                width = MeasureSpec.getSize(widthMeasureSpec);
                height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
                setMeasuredDimension(width, height);
            } else {
                height = MeasureSpec.getSize(heightMeasureSpec);
                width = (int) Math.ceil((float) height * (float) d.getIntrinsicWidth() / (float) d.getIntrinsicHeight());
                setMeasuredDimension(width, height);
            }

            mWidth = width;
            mHeight = height;
            sx = mWidth / 5;  // 초기 Crop선의 위치 설정
            ex = mWidth * 4 / 5;
            sy = mHeight / 5;
            ey = mHeight * 4 / 5;

            invalidate();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void initBitmap(String _path){
        outFilePath = _path;
        bitmap = BitmapFactory.decodeFile(_path);
        invalidate();
    }
}
