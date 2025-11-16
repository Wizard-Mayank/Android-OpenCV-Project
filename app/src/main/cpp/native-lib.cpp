#include <jni.h>
#include <string>
#include <vector>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>

using namespace cv;

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_androidopencvproject_MainActivity_processImage(
        JNIEnv *env,
        jobject /* this */,
        jint width,
        jint height,
        jbyteArray yPlane,
        jint yStride) {

    jbyte *yBytes = env->GetByteArrayElements(yPlane, NULL);

    Mat yuvMat(height + height / 2, yStride, CV_8UC1, (unsigned char *) yBytes);
    Mat grayMat(height, width, CV_8UC1);

    yuvMat.rowRange(0, height).colRange(0, width).copyTo(grayMat);

    Mat cannyEdges;
    Canny(grayMat, cannyEdges, 50, 150);

    std::vector<unsigned char> buffer;
    imencode(".jpg", cannyEdges, buffer);

    jbyteArray processedImageArray = env->NewByteArray(buffer.size());
    env->SetByteArrayRegion(processedImageArray, 0, buffer.size(), (jbyte *) buffer.data());


    env->ReleaseByteArrayElements(yPlane, yBytes, 0);

    return processedImageArray;
}