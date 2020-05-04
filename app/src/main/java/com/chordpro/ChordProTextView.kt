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
            Log.d("ChordPro", "Draw: \"${drawModel.text}\"")
            canvas.drawText(drawModel.text, drawModel.x, drawModel.y, drawModel.paint)
        }
    }

    // TODO remaining problems:
    // Center cords over the next letter
    // Font size scaling for any size
    // TextView contentsize matches its real size
    // support lines without chords
    // enabled/disable chords
    private fun buildDrawModel(): List<DrawModel> {
        val drawModels = mutableListOf<DrawModel>()

        val startsWithChord = text.startsWith("[")
        val textParts = text.split("[", "]")

        val lineHeight = 28f.dpToPx
        val drawingModel = BuildingDrawModel(startsWithChord, 0f, lineHeight + 5.dpToPx, lineHeight, calcSpaceWidth())

        for (textPart in textParts) {
            var hasPassedNewLine = false

            for (textPartWithoutNewLines in textPart.split("\n")) {
                if (hasPassedNewLine) {
                    moveToNextLine(drawingModel)
                }

                val words = textPartWithoutNewLines.split(" ")
                for ((i, word) in words.withIndex()) {
                    if (word.isEmpty()) {
                        continue
                    }

                    val shouldPostfixSpace = !drawingModel.nextIsChord && words.count() - 2 == i && words.last().isEmpty()

                    val previousWordIsEmptyJustAfterAChord = i == 1 && words[0].isEmpty()
                    val shouldPrefixSpace = !drawingModel.nextIsChord && drawingModel.x > 0 && (drawModels.lastOrNull()?.isChord == false || previousWordIsEmptyJustAfterAChord)
                    var wordWithSpaces = "${if (shouldPrefixSpace) " " else ""}$word${if (shouldPostfixSpace) " " else ""}"

                    val shouldWrapToNextLine = textWidth(wordWithSpaces.trimEnd(), drawingModel.spaceWidth) + drawingModel.x > super.getWidth()
                    if (shouldWrapToNextLine) {
                        moveToNextLine(drawingModel)
                        ensureWordsOrCordsIsNotBrokenUp(drawingModel, drawModels)
                        wordWithSpaces = wordWithSpaces.trimStart()
                    }

                    Log.d("ChordPro", "Build: \"${wordWithSpaces}\"")

                    drawText(drawModels, wordWithSpaces, drawingModel)
                }

                hasPassedNewLine = true
            }

            drawingModel.nextIsChord = !drawingModel.nextIsChord
        }

        return drawModels
    }

    private fun calcSpaceWidth(): Int {
        return textWidth("x x", 0) - textWidth("x", 0) * 2 // leading/trailing spaces is not take into account https://stackoverflow.com/a/10729081/860488
    }

    private fun ensureWordsOrCordsIsNotBrokenUp(drawingModel: BuildingDrawModel, drawModels: MutableList<DrawModel>) {
        if (!drawingModel.nextIsChord && drawModels.last().isChord) {
            val elementsToMoveToNextLine = drawModels.reversed().takeWhile { it.isChord || !it.text.endsWith(" ") }.reversed()
            val lastIndex = elementsToMoveToNextLine.count() - 1

            for ((i, drawModel) in elementsToMoveToNextLine.withIndex()) {
                drawModels.remove(drawModel)
                drawingModel.nextIsChord = drawModel.isChord
                val text = if (lastIndex == i) drawModel.text.trimStart() else drawModel.text
                drawText(drawModels, text, drawingModel)
            }

            drawingModel.nextIsChord = false

        } else if (drawingModel.nextIsChord && !drawModels.last().isChord && !drawModels.last().text.endsWith(" ")) {

            var lastElement: DrawModel? = null
            val elementsToMoveToNextLine = drawModels.reversed().takeWhile {
                val moveToNextLine = it.isChord || (!it.text.endsWith(" ") && !(lastElement?.text?.startsWith(" ") ?: false))
                lastElement = it
                moveToNextLine
            }.reversed()
            val lastIndex = elementsToMoveToNextLine.count() - 1

            for ((i, drawModel) in elementsToMoveToNextLine.withIndex()) {
                drawModels.remove(drawModel)
                drawingModel.nextIsChord = drawModel.isChord
                val text = if (lastIndex == i) drawModel.text.trimStart() else drawModel.text
                drawText(drawModels, text, drawingModel)
            }

            drawingModel.nextIsChord = true
        }
    }

    private fun drawText(drawModels: MutableList<DrawModel>, word: String, drawingModel: BuildingDrawModel) {
        val x = drawingModel.x
        val y = drawingModel.y
        val lineHeight = drawingModel.lineHeight

        if (drawingModel.nextIsChord) {
            drawModels.add(DrawModel(true, word, x, y + lineHeight * 0.5f - lineHeight, chordPaint))
        } else {
            val textRight = textRight(word, drawingModel.spaceWidth)

            drawModels.add(DrawModel(false, word, x, y + lineHeight * 0.5f, textPaint))
            drawingModel.x += textRight
        }
    }

    private fun moveToNextLine(drawingModel: BuildingDrawModel) {
        drawingModel.y += drawingModel.lineHeight * 2
        drawingModel.x = 0f
    }

    private fun textWidth(text: String, spaceWidth: Int): Int {
        super.getPaint().getTextBounds(text, 0, text.length, textBounds)
        var extraSpaceWidth = if (text.endsWith(" ")) spaceWidth else 0
        extraSpaceWidth += if (text.startsWith(" ")) spaceWidth else 0
        return textBounds.width() + extraSpaceWidth
    }

    private fun textRight(text: String, spaceWidth: Int): Int {
        super.getPaint().getTextBounds(text, 0, text.length, textBounds)
        val trailingSpaceWidth = if (text.endsWith(" ")) spaceWidth else 0
        return textBounds.right + trailingSpaceWidth
    }

    data class DrawModel(val isChord: Boolean, val text: String, val x: Float, val y: Float, val paint: Paint)
    data class BuildingDrawModel(var nextIsChord: Boolean, var x: Float, var y: Float, val lineHeight: Float, val spaceWidth: Int)
}