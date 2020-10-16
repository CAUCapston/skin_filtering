package com.example.real;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.w3c.dom.Text;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public static Context context_main;


    ImageView imageVIewInput;
    ImageView imageVIewOuput;
    private Mat img_input;
    private Mat img_output;
    private float threshold1=262;
    private float threshold2=1;
    private int threshold3=1;
    private int threshold4=50;

    private static final String TAG = "opencv";
    private final int GET_GALLERY_IMAGE = 200;

    boolean isReady = false;

    Canvas canvas;
    ColorMatrix matrix = new ColorMatrix();
    Bitmap bitmapOutput;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageVIewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageVIewOuput = (ImageView)findViewById(R.id.imageViewOutput);


        imageVIewInput.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK); // 선택된 이미지 전달
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });


        final TextView textView1 = (TextView)findViewById(R.id.textView_threshold1);
        textView1.setText(256+"");
        SeekBar seekBar1=(SeekBar)findViewById(R.id.seekBar_threshold1);
        seekBar1.setMax(512);
        seekBar1.setProgress(256);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold1 = progress ;
                textView1.setText((int)threshold1 +"");
                ColorMatrix matrix = calculateColorMatrix(threshold1,threshold2,threshold3,threshold4);
                bitmapOutput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_input, bitmapOutput);
                drawBitmap(matrix,bitmapOutput);
                /*
                textView1.setText(progress / 100f +"");

                Bitmap bitmapOutput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_input, bitmapOutput);
                Bitmap result = changeBitmapContrastBrightness(bitmapOutput, progress / 100f, 1,matrix);
                drawBitmap(matrix,canvas,result);

                 */
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final TextView textView2 = (TextView)findViewById(R.id.textView_threshold2);
        textView2.setText(10+"");
        SeekBar seekBar2=(SeekBar)findViewById(R.id.seekBar_threshold2);
        seekBar2.setProgress(10);
        seekBar2.setMax(50);
        seekBar2.setMin(10);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold2 = progress / 10f ;
                textView2.setText(threshold2+"");

                ColorMatrix matrix = calculateColorMatrix(threshold1,threshold2,threshold3,threshold4);
                bitmapOutput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_input, bitmapOutput);
                drawBitmap(matrix,bitmapOutput);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView textView3 = (TextView)findViewById(R.id.textView_threshold3);
        textView3.setText(0+"");
        SeekBar seekBar3 =(SeekBar)findViewById(R.id.seekBar_threshold3);
        seekBar3.setProgress(0);
        seekBar3.setMax(255);
        seekBar3.setMin(0);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold3 = progress;
                textView3.setText(threshold3+"");
                ColorMatrix matrix = calculateColorMatrix(threshold1,threshold2,threshold3,threshold4);
                bitmapOutput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_input, bitmapOutput);
                drawBitmap(matrix,bitmapOutput);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView textView4 = (TextView)findViewById(R.id.textView_threshold4);
        textView4.setText(50+"");
        SeekBar seekBar4 =(SeekBar)findViewById(R.id.seekBar_threshold4);
        seekBar4.setProgress((int)threshold4);
        seekBar4.setMax(99);
        seekBar4.setMin(0);
        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold4 = progress;
                textView4.setText(((int)threshold4+1)+"");
                ColorMatrix matrix = calculateColorMatrix(threshold1,threshold2,threshold3,threshold4);
                bitmapOutput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img_input, bitmapOutput);
                drawBitmap(matrix,bitmapOutput);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        }


        Button Button1 = (Button)findViewById(R.id.main);
        Button1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Startbutton.class);
                startActivity(intent);
            }
        });
        Button Button2 = (Button)findViewById(R.id.skinextraction);
        Button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),skindetection.class);
                startActivity(intent);
            }
        });


        Intent intent = new Intent();
        intent.setAction("imagefilter");
        intent.putExtra("img",bitmapOutput);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isReady = true;
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE) {


            if (data.getData() != null) {
                Uri uri = data.getData();

                try {
                    String path = getRealPathFromURI(uri);
                    int orientation = getOrientationOfImage(path); // 런타임 퍼미션 필요
                    Bitmap temp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap bitmap = getRotatedBitmap(temp, orientation);
                    imageVIewInput.setImageBitmap(bitmap);

                    img_input = new Mat();
                    Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32, img_input);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    public int getOrientationOfImage(String filepath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.d("@@@", e.toString());
            return -1;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        }

        return 0;
    }

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }



    // 퍼미션 코드
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }

    private void drawBitmap(ColorMatrix colorMatrix,Bitmap image) {
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Bitmap result = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap( result, 0, 0, paint);
        imageVIewInput.setImageBitmap(result);
    }
    // 필터가 설정이 된 colormatrix를 canvas에 paint를 이용해 bitmap형식인 이미지에 그 필터를 씌워 bitmap형식인 result를 imageview에 나타나게 하였다..
    private ColorMatrix calculateColorMatrix(float saturation, float contrast,int brightness, int whitebalance) {

        ColorMatrix colorMatrix = new ColorMatrix();

        // saturation
        colorMatrix.setSaturation(saturation / 256);


        //contrast & brightness
        float cb = contrast == 0 ? 0 : -(contrast / 1.8f) * 5;
        colorMatrix.postConcat(new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, contrast, 0
                }));

        //white balancing
        int[][] kelvin_table = {
                {255, 56, 0}, {255, 71, 0},{255, 83, 0},{255, 93, 0},{255, 101, 0},{255, 109, 0},{255, 115, 0},{255, 121, 0},{255, 126, 0},{255, 131, 0},{255, 138, 18},{255, 142, 33},{255, 147, 44},{255, 152, 54},{255, 157, 63},{255, 161, 72},{255, 165, 79},{255, 169, 87},{255, 173, 94},{255, 177, 101},{255, 180, 107},{255, 184, 114},{255, 187, 120}, {255, 190, 126}, {255, 193, 132}, {255, 196, 137}, {255, 199, 143}, {255, 201, 148}, {255, 204, 153}, {255, 206, 159}, {255, 209, 163}, {255, 211, 168}, {255, 213, 173}, {255, 215, 177}, {255, 217, 182}, {255, 219, 186}, {255, 221, 190}, {255, 223, 194}, {255, 225, 198}, {255, 227, 202}, {255, 228, 206}, {255, 230, 210}, {255, 232, 213}, {255, 233, 217}, {255, 235, 220}, {255, 236, 224}, {255, 238, 227}, {255, 239, 230}, {255, 240, 233}, {255, 242, 236}, {255, 243, 239}, {255, 244, 242}, {255, 245, 245}, {255, 246, 247}, {255, 248, 251}, {255, 249, 253}, {254, 249, 255}, {252, 247, 255}, {249, 246, 255}, {247, 245, 255}, {245, 243, 255}, {243, 242, 255}, {240, 241, 255}, {239, 240, 255}, {237, 239, 255}, {235, 238, 255}, {233, 237, 255}, {231, 236, 255}, {230, 235, 255}, {228, 234, 255}, {227, 233, 255}, {225, 232, 255}, {224, 231, 255}, {222, 230, 255}, {221, 230, 255}, {220, 229, 255}, {218, 229, 255}, {217, 227, 255}, {216, 227, 255}, {215, 226, 255}, {214, 225, 255}, {212, 225, 255}, {211, 224, 255}, {210, 223, 255}, {209, 223, 255}, {208, 222, 255}, {207, 221, 255}, {207, 221, 255}, {206, 220, 255}, {205, 220, 255},{207, 218, 255},{207, 218, 255},{206, 217, 255},{205, 217, 255},{204, 216, 255},{204, 216, 255},{202, 215, 255},{202, 214, 255},{200, 213, 255}, {200, 213, 255}, {199, 212, 255},{198, 212, 255}, {198, 212, 255},{197, 211, 255},{197, 211, 255},{197, 210, 255},{196, 210, 255},{195, 210, 255},{195, 209, 255}
        };
        colorMatrix.postConcat(new ColorMatrix(new float[] {
                kelvin_table[whitebalance][0]/255.0f, 0, 0, 0, 0,
                0, kelvin_table[whitebalance][1]/255.0f, 0, 0, 0,
                0, 0, kelvin_table[whitebalance][2]/255.0f, 0, 0,
                0, 0, 0, 1, 0}));


        return colorMatrix;
    }
}