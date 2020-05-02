package com.chordpro

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.content.ContextCompat

class OutlinedTextView : androidx.appcompat.widget.AppCompatTextView {
    /* ===========================================================
     * Members
     * =========================================================== */
    private val mStrokePaint: Paint = Paint()
    private val mTextPaint: Paint = Paint()
    private val mTextBounds: Rect = Rect()
    private var mOutlineColor: Int = Color.TRANSPARENT

    /* ===========================================================
     * Constructors
     * =========================================================== */
    constructor(context: Context?) : super(context) {
        setupPaint()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupPaint()
        setupAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setupPaint()
        setupAttributes(context, attrs)
    }

    /* ===========================================================
     * Overrides
     * =========================================================== */
    override fun onDraw(canvas: Canvas) {
        // Get the text to print
        val textSize = super.getTextSize()
        val text = super.getText().toString()

        // setup stroke
        mStrokePaint.color = ContextCompat.getColor(context, R.color.red)
        mStrokePaint.strokeWidth = textSize
        mStrokePaint.textSize = textSize
        mStrokePaint.flags = super.getPaintFlags()
        mStrokePaint.typeface = super.getTypeface()

        mTextPaint.color = ContextCompat.getColor(context, R.color.black)
        mTextPaint.strokeWidth = textSize
        mTextPaint.textSize = textSize
        mTextPaint.flags = super.getPaintFlags()
        mTextPaint.typeface = super.getTypeface()

        val startsWithChord = text.startsWith("[")
        val textParts = text.split("[", "]")

        val lineHeight = 28f.dpToPx
        var x = 0f
        var y = lineHeight + 5.dpToPx
        super.getPaint().getTextBounds(" ", 0, 1, mTextBounds)
        val spaceWidth = textSize / 4

        var nextIsChord = startsWithChord
        for (textPart in textParts) {
            var hasPassedNewline = false

            for (textPartWithoutNewLines in textPart.split("\n")) { // TODO remove empty entries?
                if (hasPassedNewline) {
                    y += lineHeight * 2
                    x = 0f
                }

//                for (word in textPartWithoutNewLines.split(" ")) {
                    super.getPaint().getTextBounds(textPartWithoutNewLines, 0, textPartWithoutNewLines.length, mTextBounds)

                    if (mTextBounds.width() + x > super.getWidth()) {
                        y += lineHeight * 2
                        x = 0f
                    }

                    if (nextIsChord) {
                        canvas.drawText(textPartWithoutNewLines,
                            x, y + lineHeight * 0.5f - lineHeight,
                            mStrokePaint)
                    } else {
//                        if (x > 0) {
//                            x += spaceWidth // TODO build model before draw. Undgå ekstra spaces ved chords
//                        }
                        canvas.drawText(textPartWithoutNewLines,
                            x, y + lineHeight * 0.5f,
                            mTextPaint)
                        x += mTextBounds.right
                    }
//                }
                hasPassedNewline = true
            }

            nextIsChord = !nextIsChord
        }
    }

    /* ===========================================================
     * Private/Protected Methods
     * =========================================================== */
    private fun setupPaint() {
//        mStrokePaint.isAntiAlias = true
//        mStrokePaint.style = Paint.Style.STROKE
//        mStrokePaint.textAlign = Paint.Align.CENTER
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?) {
        mOutlineColor = ContextCompat.getColor(context, com.chordpro.R.color.red)

        // Force this text label to be centered
//        super.setGravity(Gravity.CENTER_HORIZONTAL)
    }

    companion object {
        /* ===========================================================
     * Constants
     * =========================================================== */
        private const val OUTLINE_PROPORTION = 0.1f
    }
}