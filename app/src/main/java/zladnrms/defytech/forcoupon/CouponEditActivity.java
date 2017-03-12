package zladnrms.defytech.forcoupon;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.orhanobut.logger.Logger;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
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
import zladnrms.defytech.forcoupon.customview.StickerView;
import zladnrms.defytech.forcoupon.customview.TexterView;

public class CouponEditActivity extends AppCompatActivity implements ColorPickerDialogListener {

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

    // 추가한 스티커 리스트
    private RecyclerView rv_stickerlist;
    private StickerlistAdapter rv_stickeradapter;
    private LinearLayout llayout_coupon_stickerlist;
    private Button btn_coupon_list;

    // 레이어
    private Button btn_layer;
    private LinearLayout llayout_layer;
    private boolean layerOpen = false;
    private LinearLayout llayout_couponeditor, llayout_texteditor, llayout_stickereditor;

    // 컨텐츠 담는 레이아웃 (중요)
    private FrameLayout llayout_couponview;
    private int layer = 0; // 현재 레이어 (0 : 쿠폰, 1 : 텍스트, 2 : 스티커)

    // 쿠폰 컨텐츠 관련 객체
    private Button btn_add, btn_save, btn_back;
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
    private Button btn_coupon_stickerclose;

    private static final int ADD_IMAGE = 0; // 상수
    private Uri imageUri, croppedimageUri;
    private String imagePath, imageName;

    private Button btn_coupon_couponphase, btn_coupon_textphase, btn_coupon_stickerphase;
    private CouponView couponView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_edit);

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
                = new LinearLayoutManager(CouponEditActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rv_bgrdlist.setLayoutManager(verticalLayoutmanager);
        rv_bgrdlist.setAdapter(rv_bgrdadapter);

        // 추가한 스티커 리스트
        rv_stickerlist = (RecyclerView) findViewById(R.id.rv_coupon_stickerlist);
        rv_stickeradapter = new StickerlistAdapter(svList);
        LinearLayoutManager verticalLayoutmanager2
                = new LinearLayoutManager(CouponEditActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_stickerlist.setLayoutManager(verticalLayoutmanager2);
        rv_stickerlist.setAdapter(rv_stickeradapter);
        llayout_coupon_stickerlist = (LinearLayout) findViewById(R.id.llayout_coupon_stickerlist);


        // 레이어 열기 / 닫기 버튼
        btn_layer = (Button) findViewById(R.id.btn_layer);
        llayout_layer = (LinearLayout) findViewById(R.id.llayout_layer);
        btn_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layerOpen) {
                    llayout_layer.setVisibility(View.GONE);
                    layerOpen = false;
                } else {
                    llayout_layer.setVisibility(View.VISIBLE);
                    layerOpen = true;
                }
            }
        });

        /*
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
                            if( X > svList.get(i).getSv_x() && X < svList.get(i).getSv_x() + svList.get(i).getSv_w() && Y > svList.get(i).getSv_y() && Y < svList.get(i).getSv_y() + svList.get(i).getSv_h()){
                                svList.get(i).setOnTouch(true);
                                break;
                            }
                        }
                        break;
                }

                return false;
            }
        });
        */

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
                if (!et_coupon_textsize.getText().toString().equals("")) {
                    size = Integer.valueOf(et_coupon_textsize.getText().toString().trim());
                }
                String color = textColor;

                if (content.equals("")) {

                } else if (color.equals("")) {

                } else if (size == null) {

                } else {
                    if (couponView != null) {
                        // 텍스트 뷰를 동적 생성하여 레이아웃에 추가한다.
                        TexterView texterView = new TexterView(CouponEditActivity.this); // 객체 생성
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
                        .show(CouponEditActivity.this);
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

        Button btn_ribbon_0 = (Button) findViewById(R.id.sticker_0);
        Button btn_ribbon_1 = (Button) findViewById(R.id.sticker_1);
        Button btn_ribbon_2 = (Button) findViewById(R.id.sticker_2);
        btn_ribbon_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSticker(0);
            }
        });
        btn_ribbon_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSticker(1);
            }
        });
        btn_ribbon_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSticker(2);
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

        btn_coupon_list = (Button) findViewById(R.id.btn_coupon_stickerlist);
        btn_coupon_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlListLayout();
            }
        });

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setAngleBtn(){

        LayoutInflater inflater = LayoutInflater.from(CouponEditActivity.this);
        View view = inflater.inflate(R.layout.view_anglebtn, null );
        setContentView(view, new LinearLayout.LayoutParams(200, 200));
    }

    private void selectSticker(int kind) {

        Bitmap stickerBm = null;
        String stickerType = null;

        switch (layer) {
            case 2: // 스티커 레이어
                switch (kind) {
                    case 0:
                        stickerBm = BitmapFactory.decodeResource(getResources(), R.drawable.smile);
                        stickerType = "스마일";
                        break;
                    case 1:
                        stickerBm = BitmapFactory.decodeResource(getResources(), R.drawable.ribbon_0);
                        stickerType = "긴 리본";
                        break;
                    case 2:
                        stickerBm = BitmapFactory.decodeResource(getResources(), R.drawable.ribbon_1);
                        stickerType = "짧은 리본";
                        break;
                }
                break;
        }

        // 스티커 뷰를 동적 생성하여 레이아웃에 추가한다.
        StickerView stickerView = new StickerView(CouponEditActivity.this); // 객체 생성
        //stickerView.setStickerbitmap(stickerBm); // 비트맵 세팅
        stickerView.setImageBitmap(stickerBm);
        llayout_couponview.addView(stickerView); // 레이아웃에 추가

        svList.add(stickerView); // 스티커뷰 리스트에 추가한다.
        svList.get(svList.size() - 1).setImageBitmap(stickerBm);
        svList.get(svList.size() - 1).setType(stickerType);
        rv_stickeradapter.notifyDataSetChanged();
    }

    private void controlListLayout() {
        FrameLayout.LayoutParams lparam = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        lparam.height = height;
        lparam.gravity = Gravity.BOTTOM;

        FrameLayout.LayoutParams lparam2 = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lparam2.gravity = Gravity.BOTTOM;

        if (rv_stickerlist.getVisibility() == View.GONE) {
            llayout_coupon_stickerlist.setLayoutParams(lparam);
            llayout_coupon_stickerlist.setBackgroundColor(Color.parseColor("#66000000"));
            rv_stickerlist.setVisibility(View.VISIBLE);
        } else {
            llayout_coupon_stickerlist.setLayoutParams(lparam2);
            llayout_coupon_stickerlist.setBackgroundColor(Color.parseColor("#00ff0000"));
            rv_stickerlist.setVisibility(View.GONE);
        }
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

    private void couponSave() {
        llayout_couponview.setDrawingCacheEnabled(true);
        llayout_couponview.buildDrawingCache();

        //이미지 캡쳐
        Bitmap saveBitmap = llayout_couponview.getDrawingCache();

        //sd_card 절대경로를 구함.
        String fileFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ForCoupon";
        File file = new File(fileFolderPath);
        file.mkdir();// 디렉토리 없으면 생성, 있으면 통과

        File saveFile = new File(fileFolderPath + File.separator + "test1.jpg");
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(saveFile);
            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fileFolderPath)))); // 갤러리에 바로 올라오도록함
        } catch (IOException e) {
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 사진 입력 란 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            // 카메라에서 사진 선택 시
            case ADD_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    try {

                        imageUri = data.getData();
                        imagePath = getPath(imageUri);

                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(this);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            // CROP ACTIVITY에서 CROP 확인 시
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    Uri croppedUri = result.getUri();
                    Logger.t("CROPPED-IMAGE-URI").d(croppedUri);

                    // For API >= 23 we need to check specifically that we have permissions to read external storage,
                    // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
                    boolean requirePermissions = false;
                    if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                        // request permissions and handle the result in onRequestPermissionsResult()
                        requirePermissions = true;
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                    } else {
                        couponView.setImageURI(croppedUri);
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Logger.t("CROPPED-IMAGE-ERROR").d(error);
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

    @TargetApi(11)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (croppedimageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                couponView.setImageURI(croppedimageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
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
                    .inflate(R.layout.recyclerview_bgrdlist, parent, false);

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

    // 추가한 스티커 리스트
    public class StickerlistAdapter extends RecyclerView.Adapter<StickerlistAdapter.ViewHolder> {

        private List<StickerView> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_stickerlist_id;
            TextView tv_stickerlist_type;
            TextView tv_stickerlist_transparent;
            Button btn_stickerlist_delete;

            public ViewHolder(View view) {
                super(view);

                tv_stickerlist_id = (TextView) view.findViewById(R.id.tv_stickerlist_id);
                tv_stickerlist_type = (TextView) view.findViewById(R.id.tv_stickerlist_type);
                tv_stickerlist_transparent = (TextView) view.findViewById(R.id.tv_stickerlist_transparent);
                btn_stickerlist_delete = (Button) view.findViewById(R.id.btn_stickerlist_delete);
            }
        }

        public StickerlistAdapter(List<StickerView> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_stickerlist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int id = position;
            final String type = verticalList.get(position).getType();

            holder.tv_stickerlist_id.setText(String.valueOf(id));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verticalList.get(position).setOnTouch(true);
                }
            });

            holder.tv_stickerlist_type.setText(type);

            holder.tv_stickerlist_transparent.setText("투명도 : " + String.valueOf(verticalList.get(position).getTransparentPercent()));

            holder.btn_stickerlist_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llayout_couponview.removeView(verticalList.get(position));
                    verticalList.remove(position);
                    rv_stickeradapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }
}
