package com.san.audioeditor.config.glide

import android.content.Context
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.san.audioeditor.R
import com.san.audioeditor.config.glide.loader.SongArtLoader
import dev.android.player.framework.data.model.Song
import java.io.InputStream

@GlideModule
class MusicImageModuleConfig : AppGlideModule() {


    companion object {
        const val KB = 1024
        const val MB = 1024 * KB
        const val IMAGE_CACHE_FOLDER_NAME = "image_cache"
        const val MAX_DISK_CACHE_SIZE = 1024 * MB
    }

    private val default = RequestOptions().error(R.drawable.default_holder) //暂时先设置自定义的Error占位
        .placeholder(R.drawable.default_holder).disallowHardwareConfig()
        .format(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) DecodeFormat.PREFER_ARGB_8888
                else DecodeFormat.PREFER_RGB_565)

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setDefaultRequestOptions(default)
        val calculator = MemorySizeCalculator.Builder(context).setMemoryCacheScreens(4f)
            .setBitmapPoolScreens(4f).build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
        builder.setBitmapPool(LruBitmapPool(calculator.bitmapPoolSize.toLong())) //设置磁盘缓存
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context, IMAGE_CACHE_FOLDER_NAME, MAX_DISK_CACHE_SIZE.toLong()))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(Song::class.java, InputStream::class.java, SongArtLoader.Factory())
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

}