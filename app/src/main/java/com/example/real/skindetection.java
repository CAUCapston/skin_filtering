package com.example.real;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

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

    // 피부색 보정 결과 이미지 가져오기
    Mat image = (Mat) getIntent().getExtras().get("img");

    ConstraintLayout skinlayout;
    ImageView filteringimage;
    ImageView skinimage;
    Mat filter_image;
    Mat skin_image;



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
        setContentView(R.layout.activity_main);

        filteringimage = (ImageView)findViewById(R.id.filteringimage);
        skinimage = (ImageView)findViewById(R.id.skinimage);
        skinlayout = (ConstraintLayout)findViewById(R.id.skinlayout);


        // filtering된 이미지 가져와서 imageview에 넣기
        Bitmap bitmapOutput;
        bitmapOutput = Bitmap.createBitmap(filter_image.cols(), filter_image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(filter_image, bitmapOutput);
        filteringimage.setImageBitmap(bitmapOutput);



        Button Button1 = (Button)findViewById(R.id.extraction);
        Button1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                LottieAnimationView lottie = (LottieAnimationView) findViewById(R.id.lottie);
                lottie.playAnimation();
                lottie.loop(true);

                skin_image = skincolor_extraction();
                // 가져온 사진의 두 볼을 인식해 색 평균값을 가진 skinimage에 넣기
                Bitmap bitmapOutput;
                bitmapOutput = Bitmap.createBitmap(skin_image.cols(), skin_image.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(skin_image, bitmapOutput);
                skinimage.setImageBitmap(bitmapOutput);

                // 결과 이미지 가져와주기
                skinlayout.setVisibility(View.VISIBLE);
            }
        });


    }
    public native void Detect(long faceimage,long right,long left);
    public native void avglab(long cheek,double avg[]);
    public native void createskin(long output, double result[]);

    public Mat skincolor_extraction(){
        Mat right_cheek = null;
        Mat left_cheek = null;
        Detect(image.getNativeObjAddr() ,right_cheek.getNativeObjAddr(),left_cheek.getNativeObjAddr());

        double[] avg_right = new double[3];
        double[] avg_left = new double[3];
        double[] result = new double[3];
        avglab(right_cheek.getNativeObjAddr(),avg_right);
        avglab(right_cheek.getNativeObjAddr(),avg_left);
        result[0] = (avg_left[0] + avg_right[0]) / 2;
        result[1] = (avg_left[1] + avg_right[1]) / 2;
        result[2] = (avg_left[2] + avg_right[2]) / 2;
        int column = right_cheek.cols();
        int row = right_cheek.rows();

        Mat skinimage = null;
        createskin(skinimage.getNativeObjAddr(),result);
        return skinimage;
    }
}