package dev.audio.timeruler.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation

fun View.rotate(fromDegrees: Float, toDegrees: Float, duration: Long = 300) {
    val rotateAnimation = RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    rotateAnimation.duration = duration
    rotateAnimation.fillAfter = true
    this.startAnimation(rotateAnimation)
}