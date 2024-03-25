package dev.android.player.framework.utils

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment


fun Context.dimen(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)

fun Context.dimenF(@DimenRes resId: Int): Float = resources.getDimension(resId)


// Fragment
fun Fragment.dimen(@DimenRes resId: Int): Int = context?.dimen(resId) ?: 0

fun Fragment.dimenF(@DimenRes resId: Int): Float = context?.dimenF(resId) ?: 0f

// View
fun View.dimen(@DimenRes resId: Int): Int = context.dimen(resId)

fun View.dimenF(@DimenRes resId: Int): Float = context.dimenF(resId)
