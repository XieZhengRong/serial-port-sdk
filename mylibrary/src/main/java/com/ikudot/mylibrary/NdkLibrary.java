package com.ikudot.mylibrary;

public class NdkLibrary {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String stringFromJNI();
    
}
