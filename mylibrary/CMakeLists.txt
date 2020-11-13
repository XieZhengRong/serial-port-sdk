# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
#添加源码库
add_library( # Specifies the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.cpp)

add_library(SerialPort SHARED
        src/main/cpp/SerialPort.c)
# Specifies a path to native header files.
# 声明头文件位置
include_directories(src/main/cpp/include/)
#添加Android日志类库
find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log-lib
        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log)

#链接所有添加的类库
target_link_libraries(
        native-lib
        ${log-lib}
        SerialPort
)
