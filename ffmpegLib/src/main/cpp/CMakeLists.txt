# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("audioeditor")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 支持gnu++11
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")


# 1. 定义so库和头文件所在目录，方面后面使用
set(ffmpeg_lib_dir ${CMAKE_SOURCE_DIR}/../../../libs/${ANDROID_ABI})
set(ffmpeg_head_dir ${CMAKE_SOURCE_DIR})

# 2. 添加头文件目录
#include_directories(${ffmpeg_head_dir}/../include/)
#include_directories(${ffmpeg_head_dir}/../ffmpeg/)
#include_directories(${ffmpeg_head_dir}/)
#include_directories(.)
#include_directories(${ffmpeg_lib_dir}/)

include_directories(.)
include_directories(include)

# 3. 添加ffmpeg相关的so库
add_library(libffmpeg
        SHARED
        IMPORTED)
set_target_properties(libffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_lib_dir}/libffmpeg.so)

# 查找代码中使用到的系统库
find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

set(SRC_FFMPEG
        amplituda-native-lib.cpp
        ffmpeg_cmd.c
        ffmpeg/cmdutils.c
        ffmpeg/ffmpeg.c
        ffmpeg/ffmpeg_demux.c
        ffmpeg/ffmpeg_filter.c
        ffmpeg/ffmpeg_hw.c
        ffmpeg/ffmpeg_mux.c
        ffmpeg/ffmpeg_mux_init.c
        ffmpeg/ffmpeg_opt.c
        ffmpeg/ffprobe.c
        ffmpeg/objpool.c
        ffmpeg/opt_common.c
        ffmpeg/sync_queue.c
        ffmpeg/thread_queue.c
        ffprobe_cmd.cpp
        native-lib.cpp
        )

add_library( # Sets the name of the library.
        media-handle
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${SRC_FFMPEG}
        )


# 指定编译目标库时，cmake要链接的库
target_link_libraries(
        # 4. 连接 FFmpeg 相关的库
        media-handle

        libffmpeg

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

