package com.example.real;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.airbnb.lottie.LottieAnimationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class skindetection extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    static {
        System.loadLibrary("dlib");
        System.loadLibrary("native-lib");
    }
    private static final String TAG = "skin-detection";



    ConstraintLayout skinlayout;
    LinearLayout animationlayout;
    ImageView filteringimage;
    ImageView skinimage;
    TextView Hex;
    TextView loading;
    Bitmap filtering_bitmap;
    Mat filter_image;
    Mat right_cheek = new Mat();
    Mat left_cheek = new Mat();


    double[] avg_right = new double[3];
    double[] avg_left = new double[3];
    double[] result = new double[3];
    boolean end = false;
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        copyFile("shape_predictor_68_face_landmarks.dat");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skindetection);

        filteringimage = (ImageView)findViewById(R.id.filteringimage);
        skinimage = (ImageView)findViewById(R.id.skinimage);
        skinlayout = (ConstraintLayout)findViewById(R.id.Constraint);
        animationlayout = (LinearLayout)findViewById(R.id.animation);
        loading = (TextView)findViewById(R.id.loading);

        // 피부색 보정 결과 이미지 가져오기
        Bitmap image;
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        filtering_bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        filter_image = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_UNCHANGED);

        // filtering된 이미지 가져와서 imageview에 넣기
        filteringimage.setImageBitmap(filtering_bitmap);


        final LottieAnimationView lottie = (LottieAnimationView) findViewById(R.id.animationView);



        Hex= (TextView)findViewById(R.id.hexacode);
        final ProgressDialog mDialog = new ProgressDialog(this);




        Button extract = (Button)findViewById(R.id.extraction);
        extract.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loading.setVisibility(View.INVISIBLE);
                skincolor_extraction();
                Hex.setText(String.format("#%02X%02X%02X",(int)result[2],(int)result[1],(int)result[0]));
                Bitmap bitmapOutput;
                bitmapOutput = Bitmap.createBitmap(filter_image.cols(), filter_image.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(filter_image, bitmapOutput);
                skinimage.setImageBitmap(bitmapOutput);
                skinlayout.setVisibility(View.VISIBLE);
            }
        });


        Button main = (Button)findViewById(R.id.mainbutton);
        main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Startbutton.class);
                startActivity(intent);
            }
        });

        Button complete = (Button)findViewById(R.id.complete);
        main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(),Startbutton.class);
                //startActivity(intent);
            }
        });
    }


    public native void Detect(long faceimage,long right,long left);
    public native double[] avgBGR(long cheek);
    public native void createskin(long output, double result[]);

    public void skincolor_extraction(){


        Detect(filter_image.getNativeObjAddr() ,right_cheek.getNativeObjAddr(),left_cheek.getNativeObjAddr());


        //각 볼의 평균 lab값 구하기
        avg_right = avgBGR(right_cheek.getNativeObjAddr());
        avg_left = avgBGR(left_cheek.getNativeObjAddr());
        //두 볼의 평균 lab값 구하기
        result[0] = (avg_left[0] + avg_right[0]) / 2; //B
        result[1] = (avg_left[1] + avg_right[1]) / 2; //G
        result[2] = (avg_left[2] + avg_right[2]) / 2; //R
        Log.d("native-lib ::: result ","" + result[2]+ " "+result[1]+ " " +result[0]);

        //평균 lab값을 이용해 이미지 채우기
        createskin(filter_image.getNativeObjAddr(),result);
    }


}
