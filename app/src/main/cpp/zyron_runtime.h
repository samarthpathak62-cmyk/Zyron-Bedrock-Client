#pragma once
#include <jni.h>
#include <cstdint>
#include <string>

namespace zyron {
constexpr const char* kVersion = "1.0.0";
constexpr const char* kAbi = "arm64-v8a";
bool init();
const char* version();
int64_t monotonicNanos();
void requestFpsCap(int fps);
int currentFpsCap();
}
