package ru.otus.cryptosample.coins.feature.animation

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class SlideItemAnimator : DefaultItemAnimator() {

    init {
        addDuration = 500
        removeDuration = 500
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val position = holder.adapterPosition

        if (position == -1) return false

        val view = holder.itemView
        val animation = TranslateAnimation(
            view.width.toFloat(), 0f, 0f, 0f
        )
        animation.duration = addDuration
        animation.interpolator = AccelerateDecelerateInterpolator()

        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {
                dispatchAddStarting(holder)
            }

            override fun onAnimationEnd(animation: android.view.animation.Animation) {
                dispatchAddFinished(holder)
                view.clearAnimation()
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
        })

        view.startAnimation(animation)
        return false
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val position = holder.adapterPosition

        if (position == -1) {
            dispatchRemoveFinished(holder)
            return false
        }

        val view = holder.itemView
        val animation = TranslateAnimation(
            0f, -view.width.toFloat(), 0f, 0f
        )
        animation.duration = removeDuration
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.fillAfter = true

        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {
                dispatchRemoveStarting(holder)
            }

            override fun onAnimationEnd(animation: android.view.animation.Animation) {
                dispatchRemoveFinished(holder)
                view.clearAnimation()
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
        })

        view.startAnimation(animation)
        return false
    }

}