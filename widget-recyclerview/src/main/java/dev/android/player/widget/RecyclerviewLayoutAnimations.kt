package dev.android.player.widget

import android.view.animation.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 创建Recyclerview 布局动画
 */
class RecyclerviewLayoutAnimations {
    companion object {
        @JvmStatic
        fun build(manager: RecyclerView.LayoutManager): LayoutAnimationController {

            val animation = AnimationSet(true)
            animation.duration = 300
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.addAnimation(AlphaAnimation(0f, 1f))
            animation.addAnimation(TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, -0.2f, Animation.RELATIVE_TO_SELF, 0f))

            return when (manager) {
                is LinearLayoutManager -> {
                    LayoutAnimationController(animation, 0.15f)
                }
                is GridLayoutManager -> {
                    GridLayoutAnimationController(animation, 0.15f, 0.15f)
                }
                else -> {
                    LayoutAnimationController(animation, 0.15f)
                }
            }

        }
    }
}

fun RecyclerView.onAttachLayoutAnim(isRemoveItemAnim: Boolean = false) {
    if (isRemoveItemAnim) {
        this.itemAnimator = null
    }
    this.layoutAnimation = RecyclerviewLayoutAnimations.build(layoutManager!!)
}

