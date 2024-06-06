package com.san.audioeditor.view

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.R
import dev.android.player.framework.utils.doOnGlobalLayout
import musicplayer.playmusic.audioplayer.base.adapter.IndexFastScrollAdapter
import musicplayer.playmusic.audioplayer.base.view.CommonLoadingView
import musicplayer.playmusic.audioplayer.base.view.EmptyViewBuilder

/**
 * 主页面几个空视图业务逻辑
 */
object EmptyViewBusiness {


    private const val TAG_LOADING_VIEW = "TAG_LOADING_VIEW"
    private const val TAG_EMPTY_VIEW = "TAG_EMPTY_VIEW"


    /**
     * 绑定主页面上的几个空视图
     */
    fun bindCommonEmptyView(view: RecyclerView, isFolder: Boolean = false): EmptyLoadingController {

        val builder = EmptyViewBuilder(view.context)
        builder.setStateImage(R.drawable.ic_null_photos)
        builder.setEmptyText(R.string.no_songs_found)
        if (isFolder) {
            builder.addBusinessButton(R.drawable.rectangle_8white_radius_all_8_bg, R.string.directories) {

            }.addBusinessButton(R.drawable.rectangle_8white_radius_all_8_bg, R.string.hidden_directory) {

            }
        }
        builder.addBusinessButton(R.drawable.rectangle_8white_radius_all_8_bg, R.string.scan_library) {

        }

        val loading = CommonLoadingView(view.context)
        val empty = builder.build()
        bindLoadingView(view, empty, loading)
        return EmptyLoadingController(view, empty, loading)
    }


    /**
     *
     * @param view recyclerView
     * @param emptyView 空视图，可以为空，不同页面中的空视图指定不一样的就行，如果指定为null的话，加载结束还会显示原来的Recyclerview
     * @param loadingView 加载视图 默认为通用的。
     */
    fun bindLoadingView(view: RecyclerView,
                        empty: View? = null,
                        loading: View? = CommonLoadingView(view.context)): EmptyLoadingController {
        view.doOnGlobalLayout {
            replaceView(view, empty, loading)
        }
        addEmptyViewShowBusiness(view, empty, loading)
        return EmptyLoadingController(view, empty, loading)
    }

    /**
     * 替换RecyclerView 显示位置，添加空视图
     */
    fun replaceView(view: View, empty: View?, loading: View?) {
        val params = view.layoutParams
        val container = FrameLayout(view.context)
        container.layoutParams = params

        if (view.parent is ViewGroup) {
            val parent = view.parent as ViewGroup
            val index = parent.indexOfChild(view)

            val containerParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) //替换RecyclerView
            parent.removeView(view)
            container.addView(view, containerParams) //添加空视图
            if (empty != null && empty.parent != null) {
                (empty.parent as ViewGroup).removeView(empty)
            }
            if (empty != null) {
                empty.setTag(TAG_EMPTY_VIEW)
                container.addView(empty, containerParams)
            } //添加加载视图
            if (loading != null && loading.parent != null) {
                (loading.parent as ViewGroup).removeView(loading)
            }
            if (loading != null) {
                loading.setTag(TAG_LOADING_VIEW)
                container.addView(loading, containerParams)
            } //添加到父容器
            parent.addView(container, index, params)
        }
    }

    /**
     * 添加空视图显示业务
     */
    private fun addEmptyViewShowBusiness(view: RecyclerView, empty: View?, loading: View? = null) {
        val adapter = view.adapter
        view.visibility = View.INVISIBLE
        empty?.visibility = View.GONE
        loading?.visibility = View.VISIBLE
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                loading?.visibility = View.GONE //判断是否有数据
                val isEmpty = isContentEmpty(adapter)
                if (empty == null) {
                    view.visibility = View.VISIBLE
                } else {
                    if (isEmpty) {
                        empty.visibility = View.VISIBLE
                        view.visibility = View.GONE
                    } else {
                        empty.visibility = View.GONE
                        view.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    /**
     * 判断视图是否有数据
     */
    fun isContentEmpty(adapter: RecyclerView.Adapter<*>): Boolean {
        val result = when (adapter) {
            is IndexFastScrollAdapter<*, *> -> adapter.dataSize == 0
            is ConcatAdapter -> {
                var size = 0
                for (i in 0 until adapter.adapters.size) {
                    val item = adapter.adapters[i]
                    size += if (item is IndexFastScrollAdapter<*, *>) {
                        item.dataSize
                    } else {
                        item.itemCount
                    }
                }
                size == 0
            }

            else -> adapter.itemCount == 0
        }
        return result
    }


    class EmptyLoadingController internal constructor(target: View,
                                                      private var empty: View?,
                                                      private var loading: View?) {
        private var target: View? = target


        /**
         * 显示加载视图
         */
        fun showLoading() {
            loading?.visibility = View.VISIBLE
            empty?.visibility = View.GONE
            target?.visibility = View.GONE
        }

        /**
         * 显示空视图
         */
        fun showEmpty() {
            loading?.visibility = View.GONE
            empty?.visibility = View.VISIBLE
            target?.visibility = View.GONE
        }

        /**
         * 显示目标视图
         */
        fun showTarget() {
            loading?.visibility = View.GONE
            empty?.visibility = View.GONE
            target?.visibility = View.VISIBLE
        }
    }
}