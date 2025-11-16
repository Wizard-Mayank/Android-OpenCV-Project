cmake_minimum_required(VERSION 3.22.1)

project("androidopencvproject")

set(OPENCV_NATIVE_ROOT "${CMAKE_SOURCE_DIR}/../../../../opencv")

include_directories(${OPENCV_NATIVE_ROOT}/jni/include)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)

add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES
        IMPORTED_LOCATION ${OPENCV_NATIVE_ROOT}/libs/${ANDROID_ABI}/libopencv_java4.so
)

target_link_libraries(${CMAKE_PROJECT_NAME}
        lib_opencv
        android
        log)