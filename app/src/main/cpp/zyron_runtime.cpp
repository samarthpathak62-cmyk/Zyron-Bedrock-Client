#include "zyron_runtime.h"
#include <android/log.h>
#include <atomic>
#include <chrono>
#include <jni.h>
#include <string>

#define ZLOG(...) __android_log_print(ANDROID_LOG_INFO, "ZyronNative", __VA_ARGS__)

namespace zyron {
namespace {
std::atomic<int> gFpsCap{60};
std::atomic<bool> gReady{false};
}

bool init() {
    gReady.store(true);
    ZLOG("runtime init %s %s", kVersion, kAbi);
    return true;
}

const char* version() { return kVersion; }

int64_t monotonicNanos() {
    using namespace std::chrono;
    return duration_cast<nanoseconds>(steady_clock::now().time_since_epoch()).count();
}

void requestFpsCap(int fps) {
    if (fps < 0) fps = 0;
    gFpsCap.store(fps);
}

int currentFpsCap() { return gFpsCap.load(); }
}  // namespace zyron

extern "C" JNIEXPORT jboolean JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeInit(JNIEnv*, jobject) {
    return zyron::init() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeVersion(JNIEnv* env, jobject) {
    return env->NewStringUTF(zyron::version());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeAbi(JNIEnv* env, jobject) {
    return env->NewStringUTF(zyron::kAbi);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeNanos(JNIEnv*, jobject) {
    return static_cast<jlong>(zyron::monotonicNanos());
}

extern "C" JNIEXPORT void JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeSetFpsCap(JNIEnv*, jobject, jint fps) {
    zyron::requestFpsCap(static_cast<int>(fps));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_zyron_client_nativebridge_ZyronNative_nativeGetFpsCap(JNIEnv*, jobject) {
    return zyron::currentFpsCap();
}
