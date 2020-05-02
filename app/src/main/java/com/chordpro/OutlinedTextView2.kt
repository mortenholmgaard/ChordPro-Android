package com.chordpro

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.green
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat

class OutlinedTextView2 : androidx.appcompat.widget.AppCompatTextView {
    /* ===========================================================
     * Members
     * =========================================================== */
    private val mStrokePaint: Paint = Paint()
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
        mStrokePaint.setColor(mOutlineColor)
        mStrokePaint.setStrokeWidth(textSize * OUTLINE_PROPORTION)
        mStrokePaint.setTextSize(textSize)
        mStrokePaint.setFlags(super.getPaintFlags())
        mStrokePaint.setTypeface(super.getTypeface())

        // Figure out the drawing coordinates
        //mStrokePaint.getTextBounds(text, 0, text.length(), mTextBounds);

        // draw everything
        canvas.drawText(text,
            super.getWidth() * 0.5f, super.getBottom() * 0.5f,
            mStrokePaint)
        super.onDraw(canvas)
    }

    /* ===========================================================
     * Private/Protected Methods
     * =========================================================== */
    private fun setupPaint() {
        mStrokePaint.isAntiAlias = true
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.textAlign = Paint.Align.CENTER
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?) {
        mOutlineColor = ContextCompat.getColor(context, R.color.green)

        // Force this text label to be centered
        super.setGravity(Gravity.CENTER_HORIZONTAL)
    }

    companion object {
        /* ===========================================================
     * Constants
     * =========================================================== */
        private const val OUTLINE_PROPORTION = 0.1f
    }
}