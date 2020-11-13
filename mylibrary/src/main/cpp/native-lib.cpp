#include <jni.h>
#include <string>
#include "native-lib.h"
#include <iostream>
extern "C"
using namespace std;
extern "C" JNIEXPORT jstring
JNICALL
Java_com_ikudot_mylibrary_NdkLibrary_stringFromJNI(
        JNIEnv *env,
        jclass clazz) {
    string hello = "Hello from C++";
    cout << "哈哈哈哈" << endl;
    return env->NewStringUTF(hello.c_str());
}