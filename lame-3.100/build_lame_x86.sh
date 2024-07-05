#!/bin/bash

export NDK=/Users/chenlu/Library/Android/sdk/ndk/android-ndk-r20b
export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/darwin-x86_64
export SYSROOT=$TOOLCHAIN/sysroot

set -e

ARCH=x86
PLATFORM=i686-linux-android
API=21
PREFIX=$(pwd)/android/$ARCH

# 创建输出目录
mkdir -p $PREFIX

# 配置编译参数
./configure \
    --host=$PLATFORM \
    --disable-shared \
    --enable-static \
    --prefix=$PREFIX \
    CC=$TOOLCHAIN/bin/$PLATFORM$API-clang \
    CXX=$TOOLCHAIN/bin/$PLATFORM$API-clang++ \
    LD=$TOOLCHAIN/bin/i686-linux-android-ld \
    AR=$TOOLCHAIN/bin/i686-linux-android-ar \
    RANLIB=$TOOLCHAIN/bin/i686-linux-android-ranlib \
    STRIP=$TOOLCHAIN/bin/i686-linux-android-strip \
    --with-sysroot=$SYSROOT

# 编译并安装
make clean
make -j$(nproc)
make install
