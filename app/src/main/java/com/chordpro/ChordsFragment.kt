package com.chordpro

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_chords.*

class ChordsFragment : Fragment() {
    private var fontScale = 1f
    private var previousScale = 1f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chords, container, false)
    }

    override fun onStart() {
        super.onStart()

        val scaleDetector = ScaleGestureDetector(activity, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                changeFontSize(detector.scaleFactor)
                return false
            }
        })
        scrollView.setScaleDetector(scaleDetector)
    }

    private fun changeFontSize(scale: Float) {
        if (scale == 1f) {
            previousScale = scale
            return
        }

        val scaleChange = scale - previousScale
        val tempScale = fontScale * (1 + scaleChange)
        previousScale = scale
        if (tempScale > 3 || tempScale < 0.5) {
            return
        }
        fontScale = tempScale

        chordProTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24 * fontScale)

        contentView.invalidate()
        contentView.requestLayout()
    }
}
