#include <jni.h>
#include <string.h>
#include <time.h>

JNIEXPORT void JNICALL Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_YUVtoRBGA(JNIEnv * env, jobject obj, jbyteArray yuv420sp, jint width, jint height, jintArray rgbOut)
{
    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jint *rgbData = (jint*) ((*env)->GetPrimitiveArrayCritical(env, rgbOut, 0));
    jbyte* yuv = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for(j = 0; j < h; j++) {
             pixPtr = j * w;
             jDiv2 = j >> 1;
             for(i = 0; i < w; i++) {
                     Y = yuv[pixPtr];
                     if(Y < 0) Y += 255;
                     if((i & 0x1) != 1) {
                             cOff = sz + jDiv2 * w + (i >> 1) * 2;
                             Cb = yuv[cOff];
                             if(Cb < 0) Cb += 127; else Cb -= 128;
                             Cr = yuv[cOff + 1];
                             if(Cr < 0) Cr += 127; else Cr -= 128;
                     }
                     
                     //ITU-R BT.601 conversion
                     //
                     //R = 1.164*(Y-16) + 2.018*(Cr-128);
                     //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
                     //B = 1.164*(Y-16) + 1.596*(Cb-128);
                     //
                     Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
                     R = Y + (Cr << 1) + (Cr >> 6);
                     if(R < 0) R = 0; else if(R > 255) R = 255;
                     G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
                     if(G < 0) G = 0; else if(G > 255) G = 255;
                     B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
                     if(B < 0) B = 0; else if(B > 255) B = 255;
                     rgbData[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
             }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

JNIEXPORT void JNICALL Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_YUVtoARBG(JNIEnv * env, jobject obj, jbyteArray yuv420sp, jint width, jint height, jintArray rgbOut)
{
    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;
    int             jDiv2 = 0;
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jint *rgbData = (jint*) ((*env)->GetPrimitiveArrayCritical(env, rgbOut, 0));
    jbyte* yuv = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for(j = 0; j < h; j++) {
             pixPtr = j * w;
             jDiv2 = j >> 1;
             for(i = 0; i < w; i++) {
                     Y = yuv[pixPtr];
                     if(Y < 0) Y += 255;
                     if((i & 0x1) != 1) {
                             cOff = sz + jDiv2 * w + (i >> 1) * 2;
                             Cb = yuv[cOff];
                             if(Cb < 0) Cb += 127; else Cb -= 128;
                             Cr = yuv[cOff + 1];
                             if(Cr < 0) Cr += 127; else Cr -= 128;
                     }
                     
                     //ITU-R BT.601 conversion
                     //
                     //R = 1.164*(Y-16) + 2.018*(Cr-128);
                     //G = 1.164*(Y-16) - 0.813*(Cb-128) - 0.391*(Cr-128);
                     //B = 1.164*(Y-16) + 1.596*(Cb-128);
                     //
                     Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
                     R = Y + (Cr << 1) + (Cr >> 6);
                     if(R < 0) R = 0; else if(R > 255) R = 255;
                     G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
                     if(G < 0) G = 0; else if(G > 255) G = 255;
                     B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
                     if(B < 0) B = 0; else if(B > 255) B = 255;
                     rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
             }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

int64_t getTimeNsec() {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t) now.tv_sec*1000000000LL + now.tv_nsec;
}

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_rgb2Yuv420p(JNIEnv *env, jclass type,
                                                                         jbyteArray rgb_,
                                                                         jint width, jint height,
                                                                         jbyteArray out_) {
    jbyte *rgb = (*env)->GetByteArrayElements(env, rgb_, NULL);
    jbyte *out = (*env)->GetByteArrayElements(env, out_, NULL);

    size_t image_size = width * height;
    size_t upos = image_size;
    size_t i = 0;

    size_t line;
    for( line = 0; line < height; ++line ) {
        if( !(line % 2) ) {
            size_t x;
            for( x = 0; x < width; x += 2 ) {
                uint8_t r = rgb[4 * i];
                uint8_t g = rgb[4 * i + 1];
                uint8_t b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;

                out[upos++] = ((112*r + -94*g + -18*b) >> 8) + 128;
                out[upos++] = ((-38*r + -74*g + 112*b) >> 8) + 128;

                r = rgb[4 * i];
                g = rgb[4 * i + 1];
                b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;
            }
        }
        else {
            size_t x;
            for( x = 0; x < width; x += 1 ) {
                uint8_t r = rgb[4 * i];
                uint8_t g = rgb[4 * i + 1];
                uint8_t b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;
            }
        }
    }

    (*env)->ReleaseByteArrayElements(env, rgb_, rgb, 0);
    (*env)->ReleaseByteArrayElements(env, out_, out, 0);
}

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_compressYuv(JNIEnv *env, jclass type,
                                                                         jbyteArray yuv_,
                                                                         jint width, jint height,
                                                                         jbyteArray out_) {
    jbyte *yuv = (*env)->GetByteArrayElements(env, yuv_, NULL);
    jbyte *out = (*env)->GetByteArrayElements(env, out_, NULL);

    // TODO

    (*env)->ReleaseByteArrayElements(env, yuv_, yuv, 0);
    (*env)->ReleaseByteArrayElements(env, out_, out, 0);
}

JNIEXPORT void JNICALL
Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_rgb2Yuv420pCompress(JNIEnv *env,
                                                                                 jclass type,
                                                                                 jbyteArray rgb_,
                                                                                 jint originLen,
                                                                                 jint width,
                                                                                 jint height,
                                                                                 jbyteArray out_) {
    jbyte *rgb = (*env)->GetByteArrayElements(env, rgb_, NULL);
    jbyte *out = (*env)->GetByteArrayElements(env, out_, NULL);

    // TODO

    size_t image_size = width * height;
    size_t upos = image_size;
    size_t i = 0;

    size_t line;
    for( line = 0; line < height; ++line ) {
        if( !(line % 2) ) {
            size_t x;
            for( x = 0; x < width; x += 2 ) {
                uint8_t r = rgb[4 * i];
                uint8_t g = rgb[4 * i + 1];
                uint8_t b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;

                out[upos++] = ((112*r + -94*g + -18*b) >> 8) + 128;
                out[upos++] = ((-38*r + -74*g + 112*b) >> 8) + 128;

                r = rgb[4 * i];
                g = rgb[4 * i + 1];
                b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;
            }
        }
        else {
            size_t x;
            for( x = 0; x < width; x += 1 ) {
                uint8_t r = rgb[4 * i];
                uint8_t g = rgb[4 * i + 1];
                uint8_t b = rgb[4 * i + 2];

                out[i++] = ((66*r + 129*g + 25*b) >> 8) + 16;
            }
        }
    }

    (*env)->ReleaseByteArrayElements(env, rgb_, rgb, 0);
    (*env)->ReleaseByteArrayElements(env, out_, out, 0);
}