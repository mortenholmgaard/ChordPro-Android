package com.chordpro

import android.content.res.Resources

val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density
val Float.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
