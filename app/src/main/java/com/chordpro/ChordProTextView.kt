package com.chordpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat

// Inspiration: https://stackoverflow.com/questions/4342927/how-to-correctly-draw-text-in-an-extended-class-for-textview
class ChordProTextView : androidx.appcompat.widget.AppCompatTextView {
    private val chordPaint = Paint()
    private val textPaint = Paint()
    private val textBounds = Rect()

    constructor(context: Context?) : super(context) {
        setupPaint()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupAttributes(context, attrs)
        setupPaint()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setupAttributes(context, attrs)
        setupPaint()
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?) {
//        mOutlineColor = ContextCompat.getColor(context, com.chordpro.R.color.red)

        // Force this text label to be centered
//        super.setGravity(Gravity.CENTER_HORIZONTAL)
    }

    private fun setupPaint() {
        chordPaint.color = ContextCompat.getColor(context, R.color.blue)
        chordPaint.strokeWidth = textSize
        chordPaint.textSize = textSize
        chordPaint.flags = paintFlags
        chordPaint.typeface = typeface
        chordPaint.isAntiAlias = true

        textPaint.color = ContextCompat.getColor(context, R.color.black)
        textPaint.strokeWidth = textSize
        textPaint.textSize = textSize
        textPaint.flags = paintFlags
        textPaint.typeface = typeface
        textPaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        for (drawModel in buildDrawModel()) {
            canvas.drawText(drawModel.text, drawModel.x, drawModel.y, drawModel.paint)
        }
    }

    // TODO remaining problems:
    // new line with just the chord for the word sitting on the end. (ex. [Am]nights)
    // New line with words containing a chord. (kn[C]ow)
    // TODO fix the 2 above problems with writing the spaces in stead of just added offset to them. Then it is possible and go back and look for spaces(and newlines)
    // Center cords over the next letter
    // Font size scaling for any size
    // TextView contentsize matches its real size
    // support lines without chords
    // enabled/disable chords
    private fun buildDrawModel(): List<DrawModel> {
        val drawModels = mutableListOf<DrawModel>()

        val startsWithChord = text.startsWith("[")
        val textParts = text.split("[", "]")
        var nextIsChord = startsWithChord

        val lineHeight = 28f.dpToPx
        var x = 0f
        var y = lineHeight + 5.dpToPx

        for (textPart in textParts) {
            var hasPassedNewLine = false

            for (textPartWithoutNewLines in textPart.split("\n")) {
                if (hasPassedNewLine) {
                    y += lineHeight * 2
                    x = 0f
                }

                val words = textPartWithoutNewLines.split(" ")
                for (word in words) {

                    val shouldPostfixSpace = true // !nextIsChord && words.count() - 2 == i && words.last().isEmpty()
                    val shouldPrefixSpace = !nextIsChord && drawModels.lastOrNull()?.isChord == false
                    Log.d("ChordPro", "\"${if (shouldPrefixSpace) " " else ""}$word${if (shouldPostfixSpace) " " else ""}\"")

                    val wordWithPrefix = "${if (shouldPrefixSpace) " " else ""}$word${if (shouldPostfixSpace) " " else ""}"
                    super.getPaint().getTextBounds(wordWithPrefix, 0, wordWithPrefix.length, textBounds)

                    if (textBounds.width() + x > super.getWidth()) {
                        y += lineHeight * 2
                        x = 0f
                    }

                    if (nextIsChord) {
                        drawModels.add(DrawModel(true, wordWithPrefix, x, y + lineHeight * 0.5f - lineHeight, chordPaint))
                    } else {
                        drawModels.add(DrawModel(false,
                            if (shouldPrefixSpace && x == 0f) wordWithPrefix.removeRange(0, 1) else wordWithPrefix,
                            x, y + lineHeight * 0.5f, textPaint))
                        x += textBounds.right
                    }
                }

                hasPassedNewLine = true
            }

            nextIsChord = !nextIsChord
        }

        return drawModels
    }

    data class DrawModel(val isChord: Boolean, val text: String, val x: Float, val y: Float, val paint: Paint)
}