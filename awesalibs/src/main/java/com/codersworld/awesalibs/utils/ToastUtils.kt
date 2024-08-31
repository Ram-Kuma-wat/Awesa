package com.codersworld.awesalibs.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast


/**
 * Provides a simplified way to show toast messages without having to create the toast, set the
 * desired gravity, etc.
 */
class ToastUtils private constructor() {
    enum class Duration {
        SHORT, LONG
    }

    init {
        throw AssertionError()
    }

    companion object {
        @JvmOverloads
        fun showToast(context: Context, stringResId: Int, duration: Duration = Duration.SHORT): Toast {
            return showToast(context, context.getString(stringResId), duration)
        }

        @JvmOverloads
        fun showToast(
            context: Context?,
            text: String?,
            duration: Duration = Duration.SHORT,
            gravity: Int = Gravity.CENTER,
            xOffset: Int = 0,
            yOffset: Int = 0
        ): Toast {
            val toast = Toast.makeText(
                context, text,
                (if (duration == Duration.SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
            )
            toast.setGravity(gravity, xOffset, yOffset)
            toast.show()
            return toast
        }
    }
}
