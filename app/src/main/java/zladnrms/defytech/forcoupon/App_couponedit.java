package zladnrms.defytech.forcoupon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import zladnrms.defytech.forcoupon.customview.CouponView;
import zladnrms.defytech.forcoupon.contentinfo.BgrdInfo;
import zladnrms.defytech.forcoupon.contentinfo.ContentInfo;
import zladnrms.defytech.forcoupon.customview.StickerView;
import zladnrms.defytech.forcoupon.customview.TexterView;

public class App_couponedit extends AppCompatActivity implements ColorPickerDialogListener {

    static final String URLlink = "http://115.71.238.61"; // 호스팅 URL
    private JSONArray jarray = null; // PHP에서 받아온 JSON Array에 대한 처리

    private OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler();

    private RotateLoading rotateLoading;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // 쿠폰 배경 리스트
    private RecyclerView rv_bgrdlist;
    private ArrayList<BgrdInfo> bgrdlist;
    private BgrdlistAdapter rv_bgrdadapter;

    // 쿠폰 추가 컨텐츠 리스트
    private RecyclerView rv_contentlist;
    private ArrayList<ContentInfo> contentlist;
    private ContentlistAdapter rv_contentadapter;

    // 레이어
    private Button btn_layer;
    private LinearLayout llayout_layer;
    private boolean layerOpen = false;
    private LinearLayout llayout_couponeditor, llayout_texteditor, llayout_stickereditor;

    // 컨텐츠 담는 레이아웃 (중요)
    private FrameLayout llayout_couponview;
    private int layer = 0; // 현재 레이어 (0 : 쿠폰, 1 : 텍스트, 2 : 스티커)

    // 쿠폰 컨텐츠 관련 객체
    private Button btn_add, btn_save;
    private Button btn_coupon_couponclose;

    // 텍스트 컨텐츠 관련 객체
    private ArrayList<TexterView> tvList = new ArrayList<TexterView>();
    private LinearLayout llayout_coupon_text;
    private EditText et_coupon_textcontent, et_coupon_textsize;
    private String textColor;
    private Button btn_coupon_textcolor, btn_coupon_textadd;
    private Button btn_coupon_textclose;

    // 스티커 컨텐츠 관련 객체
    private ArrayList<StickerView> svList = new ArrayList<StickerView>();
    private LinearLayout llayout_coupon_sticker;
    private Button btn_coupon_stickeradd;
    private Button btn_coupon_stickerclose;

    private static final int ADD_IMAGE = 0; // 상수
    private Uri imageUri;
    private String imagePath, imageName;

    private Button btn_coupon_couponphase, btn_coupon_textphase, btn_coupon_stickerphase;
    private CouponView couponView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_couponedit);

        // 모든 것 담는 레이아웃
        llayout_couponview = (FrameLayout) findViewById(R.id.llayout_couponview);

        // 쿠폰 뷰
        couponView = (CouponView) findViewById(R.id.v_couponview);

        // 레이어
        llayout_couponeditor = (LinearLayout) findViewById(R.id.llayout_couponeditor);
        llayout_texteditor = (LinearLayout) findViewById(R.id.llayout_texteditor);
        llayout_stickereditor = (LinearLayout) findViewById(R.id.llayout_stickereditor);

        // 쿠폰 배경 리스트
        rv_bgrdlist = (RecyclerView) findViewById(R.id.rv_coupon_bgrdlist);
        bgrdlist = new ArrayList<>();
        rv_bgrdadapter = new BgrdlistAdapter(bgrdlist);
        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(App_couponedit.this, LinearLayoutManager.HORIZONTAL, false);
        rv_bgrdlist.setLayoutManager(verticalLayoutmanager);
        rv_bgrdlist.setAdapter(rv_bgrdadapter);

        // 추가한 콘텐츠 리스트
        rv_contentlist = (RecyclerView) findViewById(R.id.rv_coupon_contentlist);
        contentlist = new ArrayList<>();
        rv_contentadapter = new ContentlistAdapter(contentlist);
        LinearLayoutManager verticalLayoutmanager2
                = new LinearLayoutManager(App_couponedit.this, LinearLayoutManager.VERTICAL, false);
        rv_contentlist.setLayoutManager(verticalLayoutmanager2);
        rv_contentlist.setAdapter(rv_contentadapter);

        // 레이어 열기 / 닫기 버튼
        btn_layer = (Button) findViewById(R.id.btn_layer);
        llayout_layer = (LinearLayout) findViewById(R.id.llayout_layer);
        btn_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(layerOpen){
                    llayout_layer.setVisibility(View.GONE);
                    layerOpen = false;
                }else{
                    llayout_layer.setVisibility(View.VISIBLE);
                    layerOpen = true;
                }
            }
        });

        llayout_couponview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                final float X = event.getX();
                final float Y = event.getY();

                Log.d("쿠폰 뷰 터치", "좌표 [ X : " + X + ", Y : " + Y + " ]");

                switch (layer){
                    case 0: // 쿠폰 레이어
                        break;
                    case 1: // 텍스트 레이어
                        for(int i = 0; i < tvList.size(); i ++) {
                            if( X > tvList.get(i).getX() && X < tvList.get(i).getX() + tvList.get(i).getSv_w() && Y > tvList.get(i).getY() && Y < tvList.get(i).getY() + tvList.get(i).getSv_h()){
                                tvList.get(i).setOnTouch(true);
                                break;
                            }
                        }
                        break;
                    case 2: // 스티커 레이어
                        for(int i = 0; i < svList.size(); i ++) {
                            if( X > svList.get(i).getX() && X < svList.get(i).getX() + svList.get(i).getSv_w() && Y > svList.get(i).getY() && Y < svList.get(i).getY() + svList.get(i).getSv_h()){
                                svList.get(i).setOnTouch(true);
                                break;
                            }
                        }
                        break;
                }

                return false;
            }
        });

        // 쿠폰 레이어 버튼
        btn_coupon_couponphase = (Button) findViewById(R.id.btn_coupon_couponphase);
        btn_coupon_couponphase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (couponView != null) {
                    layer = 0;
                    llayout_couponeditor.setVisibility(View.VISIBLE);
                    llayout_texteditor.setVisibility(View.GONE);
                    llayout_stickereditor.setVisibility(View.GONE);
                } else {

                }
            }
        });

        // 쿠폰 추가 버튼
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, ADD_IMAGE);
            }
        });

        // 쿠폰 저장
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                couponSave();
            }
        });

        // 쿠폰 에디터 닫기
        btn_coupon_couponclose = (Button) findViewById(R.id.btn_coupon_couponclose);
        btn_coupon_couponclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llayout_couponeditor.setVisibility(View.GONE);
            }
        });

        // 텍스트 레이어 버튼
        btn_coupon_textphase = (Button) findViewById(R.id.btn_coupon_textphase);
        btn_coupon_textphase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (couponView != null) {
                    layer = 1;
                    llayout_couponeditor.setVisibility(View.GONE);
                    llayout_texteditor.setVisibility(View.VISIBLE);
                    llayout_stickereditor.setVisibility(View.GONE);
                } else {

                }
            }
        });

        // 텍스트 추가
        llayout_coupon_text = (LinearLayout) findViewById(R.id.llayout_coupon_text);
        et_coupon_textcontent = (EditText) findViewById(R.id.et_coupon_textcontent);
        et_coupon_textsize = (EditText) findViewById(R.id.et_coupon_textsize);
        btn_coupon_textadd = (Button) findViewById(R.id.btn_coupon_textadd);
        btn_coupon_textadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = et_coupon_textcontent.getText().toString();
                Integer size = null;
                if(!et_coupon_textsize.getText().toString().equals("")){
                    size = Integer.valueOf(et_coupon_textsize.getText().toString().trim());
                }
                String color = textColor;

                if (content.equals("")) {

                } else if (color.equals("")) {

                } else if (size == null) {

                } else {
                    if (couponView != null) {
                        // 텍스트 뷰를 동적 생성하여 레이아웃에 추가한다.
                        TexterView texterView = new TexterView(App_couponedit.this); // 객체 생성
                        texterView.setText(content, size, color); // 비트맵 세팅
                        llayout_couponview.addView(texterView); // 레이아웃에 추가

                        tvList.add(texterView); // 텍스트뷰 리스트에 추가한다.
                        System.out.println("텍스트 추가");
                    }
                }
            }
        });

        // 텍스트 색상 변경
        btn_coupon_textcolor = (Button) findViewById(R.id.btn_coupon_textcolor);
        btn_coupon_textcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(1)
                        .setColor(Color.BLACK)
                        .setShowAlphaSlider(true)
                        .show(App_couponedit.this);
            }
        });

        // 텍스트 에디터 닫기
        btn_coupon_textclose = (Button) findViewById(R.id.btn_coupon_textclose);
        btn_coupon_textclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llayout_texteditor.setVisibility(View.GONE);
            }
        });

        // 스티커 레이어 버튼
        btn_coupon_stickerphase = (Button) findViewById(R.id.btn_coupon_stickerphase);
        btn_coupon_stickerphase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (couponView != null) {
                    layer = 2;
                    llayout_couponeditor.setVisibility(View.GONE);
                    llayout_texteditor.setVisibility(View.GONE);
                    llayout_stickereditor.setVisibility(View.VISIBLE);
                } else {

                }
            }
        });

        // 스티커 추가
        llayout_coupon_sticker = (LinearLayout) findViewById(R.id.llayout_coupon_sticker);
        btn_coupon_stickeradd = (Button) findViewById(R.id.btn_coupon_stickeradd);
        btn_coupon_stickeradd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 스티커 뷰를 동적 생성하여 레이아웃에 추가한다.
                Bitmap stickerBm = BitmapFactory.decodeResource(getResources(), R.drawable.smile); // 세팅할 비트맵 가져오기
                StickerView stickerView = new StickerView(App_couponedit.this); // 객체 생성
                stickerView.setStickerbitmap(stickerBm); // 비트맵 세팅
                llayout_couponview.addView(stickerView); // 레이아웃에 추가

                svList.add(stickerView); // 스티커뷰 리스트에 추가한다.
            }
        });
        // 스티커 에디터 닫기
        btn_coupon_stickerclose = (Button) findViewById(R.id.btn_coupon_stickerclose);
        btn_coupon_stickerclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llayout_stickereditor.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        switch (dialogId) {
            case 1:
                // We got result from the dialog that is shown when clicking on the icon in the action bar.
                textColor = "#" + Integer.toHexString(color);
                btn_coupon_textcolor.setText(textColor);
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void couponSave(){
        llayout_couponview.setDrawingCacheEnabled(true);
        llayout_couponview.buildDrawingCache();

        //이미지 캡쳐
        Bitmap saveBitmap = llayout_couponview.getDrawingCache();

        //sd_card 절대경로를 구함.
        String fileFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"ForCoupon";
        File file = new File(fileFolderPath);
        file.mkdir();// 디렉토리 없으면 생성, 있으면 통과

        File saveFile = new File(fileFolderPath + File.separator + "test1.jpg");
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(saveFile);
            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fileFolderPath)))); // 갤러리에 바로 올라오도록함
        } catch(IOException e) {
        } finally {
            if(output!=null) { try{output.close();}catch(Exception e){e.printStackTrace();}}
        }
    }

    // 사진 입력 란 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ADD_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        imageUri = data.getData();
                        imagePath = getPath(imageUri);
                        imageName = getName(imageUri);

                        Log.d("사진 추가 LOG", "Path : " + imagePath);
                        Log.d("사진 추가 LOG", "Name : " + imageName);

                        int n = 1; // 2048 * n
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        if (getBitmapOfWidth(imagePath) >= 8184) {
                            options.inSampleSize = 4;
                            n = 4;
                        } else if (getBitmapOfWidth(imagePath) >= 4096) {
                            options.inSampleSize = 2;
                            n = 2;
                        } else if (getBitmapOfHeight(imagePath) >= 8184) {
                            options.inSampleSize = 4;
                            n = 4;
                        } else if (getBitmapOfHeight(imagePath) >= 4096) {
                            options.inSampleSize = 2;
                            n = 2;
                        }

                        Bitmap src = BitmapFactory.decodeFile(imagePath, options);
                        Bitmap imageBm = Bitmap.createScaledBitmap(src, getBitmapOfWidth(imagePath) / n, getBitmapOfHeight(imagePath) / n, true);
                        couponView.setBitmap(imageBm);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    // 실제 경로 찾기
    private String getPath(Uri uri) {

        Cursor c = getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
        c.moveToNext();
        String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
        return absolutePath;
    }

    // 파일명 찾기
    private String getName(Uri uri) {
        Cursor c = getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
        int column_index = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        c.moveToNext();
        return c.getString(column_index);
    }

    // 사진의 길이
    public static int getBitmapOfWidth(String fileName) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outWidth;
        } catch (Exception e) {
            return 0;
        }
    }

    // 사진의 높이
    public static int getBitmapOfHeight(String fileName) {

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outHeight;
        } catch (Exception e) {
            return 0;
        }
    }

    public class BgrdlistAdapter extends RecyclerView.Adapter<BgrdlistAdapter.ViewHolder> {

        private List<BgrdInfo> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView rv_roomlist_img;
            TextView tv_roomlist_room;
            TextView tv_roomlist_step;
            TextView tv_roomlist_stage;
            TextView tv_roomlist_people;

            public ViewHolder(View view) {
                super(view);

                //rv_bgrdlist_img = (ImageView) view.findViewById(R.id.iv_roomlist);
                //tv_bgrdlist_room = (TextView) view.findViewById(R.id.tv_roomlist_name);
            }
        }

        public BgrdlistAdapter(List<BgrdInfo> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_bgrdlist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int bgrdId = bgrdlist.get(position).getId();
            final String bgrdName = bgrdlist.get(position).getName();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }

    // 추가한 콘텐츠 리스트
    public class ContentlistAdapter extends RecyclerView.Adapter<ContentlistAdapter.ViewHolder> {

        private List<ContentInfo> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_contentlist_text;
            TextView tv_contentlist_size;
            TextView tv_contentlist_color;
            Button btn_contentlist_adjust;
            Button btn_contentlist_delete;

            public ViewHolder(View view) {
                super(view);

                tv_contentlist_text = (TextView) view.findViewById(R.id.tv_contentlist_text);
                tv_contentlist_size = (TextView) view.findViewById(R.id.tv_contentlist_size);
                tv_contentlist_color = (TextView) view.findViewById(R.id.tv_contentlist_color);
                btn_contentlist_adjust = (Button) view.findViewById(R.id.btn_contentlist_adjust);
                btn_contentlist_delete = (Button) view.findViewById(R.id.btn_contentlist_delete);
            }
        }

        public ContentlistAdapter(List<ContentInfo> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_contentlist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int contentId = contentlist.get(position).getId();
            final String contentName = contentlist.get(position).getName();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }
}
