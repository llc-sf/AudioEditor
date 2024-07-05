#!/bin/bash

export NDK=/Users/chenlu/Library/Android/sdk/ndk/android-ndk-r20b
export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/darwin-x86_64
export SYSROOT=$TOOLCHAIN/sysroot

set -e

ARCH=arm
PLATFORM=armv7a-linux-androideabi
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
    LD=$TOOLCHAIN/bin/arm-linux-androideabi-ld \
    AR=$TOOLCHAIN/bin/arm-linux-androideabi-ar \
    RANLIB=$TOOLCHAIN/bin/arm-linux-androideabi-ranlib \
    STRIP=$TOOLCHAIN/bin/arm-linux-androideabi-strip \
    --with-sysroot=$SYSROOT

# 编译并安装
make clean
make -j$(nproc)
make install
