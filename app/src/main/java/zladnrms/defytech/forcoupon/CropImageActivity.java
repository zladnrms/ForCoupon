package zladnrms.defytech.forcoupon;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

import zladnrms.defytech.forcoupon.customview.CropImageView;

public class CropImageActivity extends AppCompatActivity {

    CropImageView myCropView;
    BitmapDrawable b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);


        myCropView = (CropImageView)findViewById(R.id.image_preview);

        Uri imageUri = getIntent().getExtras().getParcelable("path");
        //b = (BitmapDrawable) BitmapDrawable.createFromPath(imageUri.getPath());
        myCropView.initBitmap(getPathFromUri(imageUri));
    }

    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }
}
