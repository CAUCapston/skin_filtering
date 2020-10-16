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

        // And we also need a shape_predictor.  This is the tool that will predict face
        // landmark positions given an image and face bounding box.  Here we are just
        // loading the model from the shape_predictor_68_face_landmarks.dat file you gave
        // as a command line argument.
        shape_predictor sp;
        deserialize("/storage/emulated/0/shape_predictor_68_face_landmarks.dat") >> sp;

        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "load shape_predictor_68_face_landmarks");

        array2d<rgb_pixel> img;
        assign_image(img, cv_image<bgr_pixel>(img_input));
        //dlib::cv_image<dlib::rgb_pixel> img(img_input);

        std::vector<dlib::rectangle> dets = detector(img);
        for (unsigned long j = 0; j < dets.size(); ++j) {
            dlib::full_object_detection shape = sp(img, dets[j]);
            auto right_cheek_x1 = shape.part(5).x();
            auto right_cheek_x2 = shape.part(49).x();
            unsigned long right_cheek_width = right_cheek_x1 - right_cheek_x2;
            auto right_cheek_y1 = shape.part(30).y();
            auto right_cheek_y2 = shape.part(34).y();
            unsigned long right_cheek_height = right_cheek_y1 - right_cheek_y2;
            Rect roi1(right_cheek_x1, right_cheek_y1, right_cheek_width, right_cheek_height);
            cheek_right = img_input(roi1);

            auto left_cheek_x1 = shape.part(55).x();
            auto left_cheek_x2 = shape.part(13).x();
            unsigned long left_cheek_width = left_cheek_x1 - left_cheek_x2;
            auto left_cheek_y1 = shape.part(30).y();
            auto left_cheek_y2 = shape.part(34).y();
            unsigned long left_cheek_height = left_cheek_y1 - left_cheek_y2;
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
JNIEXPORT void JNICALL
Java_com_example_real_skindetection_avglab(JNIEnv *env, jobject thiz, jlong cheek,
                                           jdouble avgLAB[]) {
    // TODO: implement avglab()
    Mat &cheek_right = *(Mat *) cheek;

    cvtColor(cheek_right,cheek_right,COLOR_BGR2Lab);
    int step = cheek_right.step;
    int channels = cheek_right.channels();
    for(int y = 0; y <cheek_right.rows; y++){
        for(int x = 0; x<cheek_right.cols; x++){
            Vec3b intensity = cheek_right.at<Vec3b>(y,x);
            // L : 0~255
            double L = intensity.val[0];
            // A : 0~255
            double A = intensity.val[1];
            // B : 0~255
            double B = intensity.val[2];
            avgLAB[0] += L;
            avgLAB[1] += A;
            avgLAB[2] += B;
        }
    }
    avgLAB[0] = avgLAB[0] / (cheek_right.rows *cheek_right.cols);
    avgLAB[1] = avgLAB[1] / (cheek_right.rows *cheek_right.cols);
    avgLAB[2] = avgLAB[2] / (cheek_right.rows *cheek_right.cols);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_real_skindetection_createskin(JNIEnv *env, jobject thiz, jlong output,
                                               jdouble result[]) {
    Mat &image = *(Mat *) output;


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
}