package com.example.faceappdemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.DetectResponse;
import com.megvii.facepp.api.bean.Face;
import com.megvii.facepp.api.bean.HumanBodyDetectResponse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetectActivity extends AppCompatActivity {
    private static  final  String API_KEY = "9nikybcsoQMgF3VxU7AxW12YUpUE8SVG";
    private static final  String API_SECRET = "6Xe9igUcUFvq4sGZHERDLsvY7_TR7ekE";
    private static final int PHOTO_REQUEST_CODE = 100;
    private ImageView ivpic;
    private Button btnCamera, btnDetect, btnGallery, btnSave;
    private  String picPath;
    private  Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        initViews();
        initEvents();
    }

    private void initEvents() {
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetectActivity.this, "btnCamera", Toast.LENGTH_SHORT).show();
            }
        });
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> params = new HashMap<>();
                params.put("return_attributes", "gender,age");
                params.put("return_landmark", "0");

                FacePPApi faceppApi = new FacePPApi(API_KEY, API_SECRET);
                faceppApi.detect(params, toByteArray(bitmap), new IFacePPCallBack<DetectResponse>() {
                    @Override
                    public void onSuccess(DetectResponse detectResponse) {
                        Canvas canvas = new Canvas(bitmap);
                        for (Face face:detectResponse.getFaces()){
                            int height = face.getFace_rectangle().getHeight();
                            int width = face.getFace_rectangle().getWidth();
                            int left = face.getFace_rectangle().getLeft();
                            int top = face.getFace_rectangle().getTop();

                            int right = left + width;
                            int bottom = top + height;

                            Paint paint = new Paint();
                            paint.setColor(Color.YELLOW);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(8);
                            canvas.drawRect(left, top, right, bottom, paint);

                            int distance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                            int rectWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                            int rectHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                            distance = distance + bitmap.getHeight() / getResources().getDisplayMetrics().heightPixels;
                            rectWidth = rectWidth * bitmap.getWidth() / getResources().getDisplayMetrics().widthPixels;
                            rectHeight = rectHeight * bitmap.getHeight() / getResources().getDisplayMetrics().heightPixels;

                            int faceCenterX = left + width /2;
                            int faceCenterY = top + height / 2;
                            int rectCenterX = faceCenterX;
                            int rectCenterY = faceCenterY - distance - rectHeight /2 - height / 2;
                            int rectLeft = rectCenterX - rectWidth / 2;
                            int rectTop = rectCenterY - rectHeight / 2;
                            int rectRight = rectCenterX + rectWidth / 2;
                            int rectBottom = rectCenterY + rectHeight / 2;

                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

                            paint.setColor(Color.WHITE);
                            String content = face.getAttributes().getGender().getValue() + "," + face.getAttributes().getAge().getValue();
                            int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,getResources().getDisplayMetrics());
                            textSize = textSize * bitmap.getWidth() / getResources().getDisplayMetrics().widthPixels;
                            Rect textRect = new Rect();
                            paint.setTextSize(textSize);
                            paint.getTextBounds(content, 0, content.length(), textRect);
                            paint.setTextAlign(Paint.Align.CENTER);

                            canvas.drawText(content, rectCenterX, rectCenterY + textRect.height() / 2, paint);
                        }
                        ivpic.post(new Runnable() {
                            @Override
                            public void run() {
                                ivpic.invalidate();
                            }
                        });

                        Log.i("faceapp", "on success");
                    }

                    @Override
                    public void onFailed(String s) {
                        Log.i("faceapp", "on success");
                    }
                });

                Toast.makeText(DetectActivity.this, "btnDetect", Toast.LENGTH_SHORT).show();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHOTO_REQUEST_CODE);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetectActivity.this, "btnSave", Toast.LENGTH_SHORT).show();

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Toast.makeText(DetectActivity.this, "访问SD卡失败， 不能保存图片", Toast.LENGTH_SHORT).show();
                    
                }

                File bitmapDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "faceAppPic");
                if (!bitmapDir.exists()){
                    bitmapDir.mkdirs();
                }
                
                File picFile = new File(bitmapDir, newPicName());
                try {
                    picFile.createNewFile();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(picFile));
                    sendBroadcast(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    private String newPicName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmsssss");
        String str = format.format(new Date()) + ".jpeg";
        return str;
    }


    private void initViews() {
        ivpic = findViewById(R.id.iv_pic);
        btnCamera = findViewById(R.id.btn_camera);
        btnDetect = findViewById(R.id.btn_detect);
        btnGallery = findViewById(R.id.btn_gallery);
        btnSave = findViewById(R.id.btn_save);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                picPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picPath, options);
                int outHeight = options.outHeight;
                int outWidth = options.outWidth;

                int ivpicHeight = ivpic.getHeight();
                int ivpicWidth = ivpic.getWidth();

                int ratioHeight = outHeight / ivpicHeight;
                int ratioWidth = outWidth / ivpicWidth;
                int ratio = Math.max(ratioHeight, ratioWidth);

                options.inJustDecodeBounds = false;
                options.inSampleSize = ratio;
                options.inMutable = true;
                bitmap = BitmapFactory.decodeFile(picPath, options);
                ivpic.setImageBitmap(bitmap);
            }
        }

    }

    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

}