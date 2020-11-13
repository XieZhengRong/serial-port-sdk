#include <jni.h>
#include <string>
extern "C"
JNIEXPORT jstring JNICALL
Java_com_ikudot_mylibrary_NdkLibrary_stringFromJNI(JNIEnv *env, jclass clazz);