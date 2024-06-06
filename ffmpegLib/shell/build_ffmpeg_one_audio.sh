#!/bin/bash
make clean
set -e
archbit=64

if [ $archbit -eq 32 ];then
  echo "build for 32bit"
  ARCH='arm'
  CPU='armv7-a'
  ABI='armeabi-v7a'
  API=21
  PLATFORM='armv7a'
  PLATFORM_ARCH='arm'
  ANDROID='androideabi'
  OPTIMIZE_CFLAGS="-march=$CPU -mfpu=neon -mfloat-abi=softfp -marm"
else
  echo "build for 64bit"
  ARCH='aarch64'
  CPU='armv8-a'
  ABI='arm64-v8a'
  API=21
  PLATFORM='aarch64'
  PLATFORM_ARCH='arm64'
  ANDROID='android'
  OPTIMIZE_CFLAGS="-march=$CPU -mfpu=neon -mfloat-abi=softfp"
fi

uname=`uname`
if [ $uname = "Darwin" ];then
  echo "compile on mac"
  COMPILE_PLAT="darwin"
elif [ $uname = "Linux" ];then
  echo "compile on linux"
  COMPILE_PLAT="linux"
else
  echo "don't support $uname"
fi

export NDK=/Users/chenlu/Library/Android/sdk/ndk/android-ndk-r20b
export TOOL=$NDK/toolchains/llvm/prebuilt/$COMPILE_PLAT-x86_64
export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$COMPILE_PLAT-x86_64/bin
export SYSROOT=$NDK/toolchains/llvm/prebuilt/$COMPILE_PLAT-x86_64/sysroot
export CROSS_PREFIX=$TOOLCHAIN/$ARCH-linux-$ANDROID-
export CC=$TOOLCHAIN/$PLATFORM-linux-$ANDROID$API-clang
export CXX=$TOOLCHAIN/$PLATFORM-linux-$ANDROID$API-clang++
export PLATFORM_API=$NDK/platforms/android-$API/arch-$PLATFORM_ARCH
export PREFIX=../ffmpeg-android/$ABI
export ADDITIONAL_CONFIGURE_FLAG="--cpu=$CPU"

THIRD_LIB=$PREFIX
export EXTRA_CFLAGS="-Os -fPIC $OPTIMIZE_CFLAGS -I$THIRD_LIB/include -I/Users/chenlu/work/lam/lame-3.100//android/aarch64/include"
export EXTRA_LDFLAGS="-lc -lm -ldl -llog -lgcc -lz -landroid -L$THIRD_LIB/lib -L/Users/chenlu/work/lam/lame-3.100//android/aarch64/lib"

function build_one() {
  ./configure \
  --target-os=android \
  --prefix=$PREFIX \
  --cross-prefix=$CROSS_PREFIX \
  --enable-cross-compile \
  --arch=$ARCH \
  --cpu=$CPU \
  --cc=$CC \
  --cxx=$CXX \
  --nm=$TOOLCHAIN/$ARCH-linux-$ANDROID-nm \
  --strip=$TOOLCHAIN/$ARCH-linux-$ANDROID-strip \
  --enable-cross-compile \
  --sysroot=$SYSROOT \
  --enable-hwaccels \
  --enable-static \
  --disable-shared \
  --disable-doc \
  --disable-small \
  --disable-ffmpeg \
  --disable-ffplay \
  --disable-ffprobe \
  --disable-debug \
  --enable-gpl \
  --disable-avdevice \
  --disable-indevs \
  --disable-outdevs \
  --extra-cflags="$EXTRA_CFLAGS" \
  --extra-ldflags="$EXTRA_LDFLAGS" \
  --enable-avcodec \
  --enable-avformat \
  --enable-avutil \
  --enable-swresample \
  --enable-avfilter \
  --enable-network \
  --disable-postproc \
  --enable-filters \
  --disable-encoders \
  --enable-libmp3lame \
  --enable-encoder=libmp3lame \
  --enable-encoder=aac,ac3,g722,g726,adpcm_ima_qt,adpcm_ima_wav,adpcm_ms,alac,eac3,flac,mp2,opus,pcm_alaw,pcm_mulaw,pcm_f32le,pcm_f64le,pcm_s16be,pcm_s16le,pcm_s32be,pcm_s32le,pcm_s64be,pcm_s64le,pcm_s8,pcm_u16le,pcm_u32le,pcm_u8,sonic,truehd,tta,vorbis,wavpack,wmav1,wmav2 \
  --disable-decoders \
  --enable-decoder=aac,aac_latm,ac3,adpcm_ima_qt,adpcm_ima_wav,adpcm_ms,alac,amrnb,amrwb,ape,dolby_e,eac3,flac,g722,g726,g729,m4a,mp3float,mp3,mp3adufloat,mp3adu,mp3on4,opus,pcm_alaw,pcm_mulaw,pcm_dvd,pcm_f16le,pcm_f24le,pcm_f32be,pcm_f32le,pcm_f64be,pcm_f64le,pcm_s16be,pcm_s16le,pcm_s24be,pcm_s24le,pcm_s32be,pcm_s32le,pcm_s64be,pcm_s64le,pcm_u16be,pcm_u16le,pcm_u24be,pcm_u24le,pcm_u32be,pcm_u32le,pcm_vidc,pcm_zork,truehd,truespeech,vorbis,wmav1,wmav2 \
  --enable-jni \
  --enable-mediacodec \
  --enable-muxers \
  --enable-parsers \
  --enable-nonfree \
  --enable-protocols \
  --disable-demuxers \
  --enable-demuxer=aac,ac3,alaw,amr,amrnb,amrwb,ape,codec2,eac3,flac,g722,g726,g729,mp3,mulaw,ogg,pcm_s16be,pcm_s16le,pcm_s32be,pcm_s32le,pcm_f32be,pcm_f32le,pcm_f64be,pcm_f64le,wav \
  $ADDITIONAL_CONFIGURE_FLAG
  make
  make install
}

build_one

function link_one_ffmpeg() {
  $TOOLCHAIN/$ARCH-linux-$ANDROID-ld -rpath-link=$PLATFORM_API/usr/lib -L$PLATFORM_API/usr/lib \
  -L$PREFIX/lib -soname libffmpeg.so \
  -shared -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so \
  $PREFIX/lib/libavcodec.a \
  $PREFIX/lib/libavfilter.a \
  $PREFIX/lib/libswresample.a \
  $PREFIX/lib/libavformat.a \
  $PREFIX/lib/libavutil.a \
  $PREFIX/lib/libswscale.a \
  $PREFIX/lib/libmp3lame.a \
  -lc -lm -lz -ldl -llog -landroid --dynamic-linker=/system/bin/linker $TOOL/lib/gcc/$ARCH-linux-$ANDROID/4.9.x/libgcc_real.a
}

link_one_ffmpeg
