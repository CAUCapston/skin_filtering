#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <map>
#include <vector>
#include "opencv2/imgproc/imgproc.hpp"
#include "iostream"
#include <opencv2/core/core_c.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/render_face_detections.h>
#include <dlib/image_processing/scan_fhog_pyramid.h>
#include <dlib/image_processing.h>
#include <dlib/image_transforms.h>
#include <dlib/image_io.h>
#include <dlib/opencv.h>
#include <android/log.h>

using namespace std;
using namespace cv;
using namespace dlib;
void LAB2RGB(int L, int a, int b, unsigned char & R, unsigned char & G, unsigned char & B)
{
    float X, Y, Z, fX, fY, fZ;
    int RR, GG, BB;

    fY = pow((L + 16.0) / 116.0, 3.0);
    if (fY < 0.008856)
        fY = L / 903.3;
    Y = fY;

    if (fY > 0.008856)
        fY = powf(fY, 1.0/3.0);
    else
        fY = 7.787 * fY + 16.0/116.0;

    fX = a / 500.0 + fY;
    if (fX > 0.206893)
        X = powf(fX, 3.0);
    else
        X = (fX - 16.0/116.0) / 7.787;

    fZ = fY - b /200.0;
    if (fZ > 0.206893)
        Z = powf(fZ, 3.0);
    else
        Z = (fZ - 16.0/116.0) / 7.787;

    X *= (0.950456 * 255);
    Y *=             255;
    Z *= (1.088754 * 255);

    RR =  (int)(3.240479*X - 1.537150*Y - 0.498535*Z + 0.5);
    GG = (int)(-0.969256*X + 1.875992*Y + 0.041556*Z + 0.5);
    BB =  (int)(0.055648*X - 0.204043*Y + 1.057311*Z + 0.5);

    R = (unsigned char)(RR < 0 ? 0 : RR > 255 ? 255 : RR);
    G = (unsigned char)(GG < 0 ? 0 : GG > 255 ? 255 : GG);
    B = (unsigned char)(BB < 0 ? 0 : BB > 255 ? 255 : BB);

    //printf("Lab=(%f,%f,%f) ==> RGB(%f,%f,%f)\n",L,a,b,*R,*G,*B);
}
void balance_white(cv::Mat mat) {
    double discard_ratio = 0.05;
    int hists[3][256];
    memset(hists, 0, 3*256*sizeof(int));

    for (int y = 0; y < mat.rows; ++y) {
        uchar* ptr = mat.ptr<uchar>(y);
        for (int x = 0; x < mat.cols; ++x) {
            for (int j = 0; j < 3; ++j) {
                hists[j][ptr[x * 3 + j]] += 1;
            }
        }
    }

    // cumulative hist
    int total = mat.cols*mat.rows;
    int vmin[3], vmax[3];
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 255; ++j) {
            hists[i][j + 1] += hists[i][j];
        }
        vmin[i] = 0;
        vmax[i] = 255;
        while (hists[i][vmin[i]] < discard_ratio * total)
            vmin[i] += 1;
        while (hists[i][vmax[i]] > (1 - discard_ratio) * total)
            vmax[i] -= 1;
        if (vmax[i] < 255 - 1)
            vmax[i] += 1;
    }


    for (int y = 0; y < mat.rows; ++y) {
        uchar* ptr = mat.ptr<uchar>(y);
        for (int x = 0; x < mat.cols; ++x) {
            for (int j = 0; j < 3; ++j) {
                int val = ptr[x * 3 + j];
                if (val < vmin[j])
                    val = vmin[j];
                if (val > vmax[j])
                    val = vmax[j];
                ptr[x * 3 + j] = static_cast<uchar>((val - vmin[j]) * 255.0 / (vmax[j] - vmin[j]));
            }
        }
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_real_autoimageprocessing_autowhitebalancingprocessing(JNIEnv *env, jobject thiz,
                                                                       jlong input_image,
                                                                       jlong output_image) {
    // TODO: implement autowhitebalancingprocessing()


    Mat &img_input = *(Mat *) input_image;

    Mat &img_output = *(Mat *) output_image;

    cvtColor(img_input,img_output,COLOR_BGRA2BGR);
    Mat result;
    balance_white(img_output);
   // img_output = result.clone();
}




extern "C"
JNIEXPORT void JNICALL
Java_com_example_real_skindetection_Detect(JNIEnv *env, jobject thiz,jlong input_image, jlong right_cheek,jlong left_cheek) {
    // TODO: implement Detect()
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "start");

    try {
        // We need a face detector.  We will use this to get bounding boxes for
        // each face in an image.
        frontal_face_detector detector = get_frontal_face_detector();
        Mat &img_input = *(Mat *) input_image;
        Mat &cheek_right = *(Mat *) right_cheek;
        Mat &cheek_left = *(Mat *) left_cheek;
        cvtColor(img_input,img_input,COLOR_BGR2RGB);
        // And we also need a shape_predictor.  This is the tool that will predict face
        // landmark positions given an image and face bounding box.  Here we are just
        // loading the model from the shape_predictor_68_face_landmarks.dat file you gave
        // as a command line argument.
        shape_predictor sp;
        deserialize("/storage/emulated/0/shape_predictor_68_face_landmarks.dat") >> sp;

        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "load shape_predictor_68_face_landmarks");

        dlib::array2d<dlib::rgb_pixel> img;
        dlib::assign_image(img, dlib::cv_image<rgb_pixel>(img_input));
        //dlib::cv_image<dlib::rgb_pixel> img(img_input);
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "img : mat to array2d");
        std::vector<dlib::rectangle> dets = detector(img);

        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "dets size : %d",(int)dets.size());
        std::vector<dlib::full_object_detection> shapes;
        for (unsigned long j = 0; j < dets.size(); ++j) {
            dlib::full_object_detection shape = sp(img, dets[j]);
           auto right_cheek_x1 = shape.part(3).x();
           auto right_cheek_x2 = shape.part(6).x();
           unsigned long right_cheek_width = right_cheek_x2 - right_cheek_x1;
           auto right_cheek_y1 = shape.part(29).y();
           auto right_cheek_y2 = shape.part(32).y();
           unsigned long right_cheek_height = right_cheek_y2 - right_cheek_y1;
           Rect roi1 = Rect(right_cheek_x1, right_cheek_y1, right_cheek_width,right_cheek_height);
           cheek_right = img_input(roi1);

           auto left_cheek_x1 = shape.part(47).x();
           auto left_cheek_x2 = shape.part(13).x();
           unsigned long left_cheek_width = left_cheek_x2 - left_cheek_x1;
           auto left_cheek_y1 = shape.part(30).y();
           auto left_cheek_y2 = shape.part(34).y();
           unsigned long left_cheek_height = left_cheek_y2 - left_cheek_y1;
           Rect roi2(left_cheek_x1, left_cheek_y1, left_cheek_width, left_cheek_height);
           cheek_left = img_input(roi2);

        }
    }
    catch(exception& e){
        cout << "\nexception thrown!" << endl;
        cout << e.what() << endl;
    }

}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_real_skindetection_avgBGR(JNIEnv *env, jobject thiz, jlong cheek) {
    // TODO: implement avglab()
    Mat &cheek_right = *(Mat *) cheek;
    jdouble avgBGR[3];
    jdoubleArray avg_BGR = env->NewDoubleArray(3);
    cvtColor(cheek_right,cheek_right,COLOR_BGR2RGB);

    for(int y = 0; y <cheek_right.rows; y++){
        for(int x = 0; x<cheek_right.cols; x++){
            Vec3b intensity = cheek_right.at<Vec3b>(y,x);
            // B : 0~255
            double B = intensity.val[0];
            // G : 0~255
            double G = intensity.val[1];
            // R : 0~255
            double R = intensity.val[2];
            avgBGR[0] += B;
            avgBGR[1] += G;
            avgBGR[2] += R;
        }
    }
    avgBGR[0] = avgBGR[0] / (cheek_right.rows *cheek_right.cols); //B
    avgBGR[1] = avgBGR[1] / (cheek_right.rows *cheek_right.cols); //G
    avgBGR[2] = avgBGR[2] / (cheek_right.rows *cheek_right.cols); //R
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "B : %2f, G : %2f, R : %2f",avgBGR[0],avgBGR[1],avgBGR[2]);
    env->SetDoubleArrayRegion(avg_BGR,0,3,avgBGR);
    return avg_BGR;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_real_skindetection_createskin(JNIEnv *env, jobject thiz, jlong output,
                                               jdoubleArray result) {
    Mat &image = *(Mat *) output;

    jdouble* ptr;
    ptr = env->GetDoubleArrayElements(result,NULL);
    __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                        "R : %2f, G : %2f, B : %2f",ptr[2],ptr[1],ptr[0]);
    image = Scalar(ptr[2],ptr[1],ptr[0]);
    /*
    Mat skinimage(image.rows,image.cols,CV_8UC3,Scalar(0,0,0));
    cvtColor(skinimage,skinimage,COLOR_BGR2Lab);

    for (int y = 0; y < skinimage.rows; y++)
    {
        for (int x = 0; x < skinimage.cols; x++)
        {
            Vec3b intensity = skinimage.at<Vec3b>(y, x);
            double L = result[0];
            double a = result[1];
            double b = result[2];
        }
    }

    cvtColor(skinimage,image,COLOR_Lab2BGR);
     */
}