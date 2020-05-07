package com.chordpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import kotlin.math.max

// Inspiration: https://stackoverflow.com/questions/4342927/how-to-correctly-draw-text-in-an-extended-class-for-textview
class ChordProTextView : androidx.appcompat.widget.AppCompatTextView {
    var hideChords = false
    var centerChordsOverNextCharacter = false

    private val chordPaint = Paint()
    private val textPaint = Paint()
    private val textBounds = Rect()

    @Suppress("RegExpRedundantEscape")
    private val hideChordsRegex = Regex("\\[(?:[^\\]])*\\]*\\s*")

    constructor(context: Context?) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupAttributes(context, attrs)
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setupAttributes(context, attrs)
        setup()
    }

    private fun setupAttributes(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ChordProTextView)
        chordPaint.color = array.getColor(R.styleable.ChordProTextView_chordColor, currentTextColor)
        hideChords = array.getBoolean(R.styleable.ChordProTextView_hideChords, false)
        centerChordsOverNextCharacter = array.getBoolean(R.styleable.ChordProTextView_centerChordsOverNextCharacter, false)
        array.recycle()
    }

    private fun setup() {
        chordPaint.isAntiAlias = true
        textPaint.isAntiAlias = true
    }

    // Call every time it is needed to ensure using the current textSize
    private fun setupPaint() {
        chordPaint.textSize = textSize
        chordPaint.flags = paintFlags
        chordPaint.typeface = typeface

        textPaint.color = currentTextColor
        textPaint.textSize = textSize
        textPaint.flags = paintFlags
        textPaint.typeface = typeface
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        if (!hideChords) {
            height = (buildDrawModel(widthSize).last().y + calcLineHeight() - textSize + 3.dpToPx).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (hideChords) {
            val text = text
            setText(text.replace(hideChordsRegex, ""))
            super.onDraw(canvas)
            setText(text)
        } else {
            setupPaint()

            for (drawModel in buildDrawModel()) {
//                Log.d("ChordPro", "Draw: \"${drawModel.text}\"")
                canvas.drawText(drawModel.text, drawModel.x, drawModel.y, if (drawModel.isChord) chordPaint else textPaint)
            }
        }
    }

    private fun calcLineHeight(): Float {
        setupPaint()
        return textPaint.descent() - textPaint.ascent()
    }

    private fun buildDrawModel(viewWidth: Int = super.getWidth()): List<DrawModel> {
        val drawModelsByLine = mutableListOf<MutableList<DrawModel>>()
        drawModelsByLine.add(mutableListOf())

        val startsWithChord = text.startsWith("[")
        val textParts = text.split("[", "]")

        val lineHeight = calcLineHeight()
        val drawingModel = BuildingDrawModel(startsWithChord, 0f, lineHeight + 5.dpToPx, lineHeight, calcSpaceWidth())

        for (textPart in textParts) {
            var hasPassedNewLine = false

            for (textPartWithoutNewLines in textPart.split("\n")) {
                if (hasPassedNewLine) {
                    moveToNextLine(drawingModel, drawModelsByLine)
                }

                val words = textPartWithoutNewLines.split(" ")
                for ((i, word) in words.withIndex()) {
                    if (word.isEmpty()) {
                        continue
                    }

                    val shouldPostfixSpace = !drawingModel.nextIsChord && words.count() - 2 == i && words.last().isEmpty()
                    val previousWordIsEmptyJustAfterAChord = i == 1 && words[0].isEmpty()
                    val shouldPrefixSpace =
                        !drawingModel.nextIsChord && drawingModel.x > 0 && (drawModelsByLine.lastOrNull()?.lastOrNull()?.isChord == false || previousWordIsEmptyJustAfterAChord)
                    var wordWithSpaces = "${if (shouldPrefixSpace) " " else ""}$word${if (shouldPostfixSpace) " " else ""}"

                    val shouldWrapToNextLine = textWidth(wordWithSpaces.trimEnd(), drawingModel.spaceWidth) + drawingModel.x > viewWidth
                    if (shouldWrapToNextLine) {
                        moveToNextLine(drawingModel, drawModelsByLine)
                        ensureWordsOrCordsIsNotBrokenUp(drawingModel, drawModelsByLine)
                        wordWithSpaces = wordWithSpaces.trimStart()
                    }

//                    Log.d("ChordPro", "Build: \"${wordWithSpaces}\"")

                    drawText(drawModelsByLine.last(), wordWithSpaces, drawingModel)
                }

                hasPassedNewLine = true
            }

            drawingModel.nextIsChord = !drawingModel.nextIsChord
        }

        if (centerChordsOverNextCharacter) {
            adjustChordPositions(drawModelsByLine)
        }

        return adjustLineHeights(drawModelsByLine, drawingModel.lineHeight)
    }

    private fun adjustChordPositions(drawModelsByLine: List<List<DrawModel>>) {
        var previousDrawModel: DrawModel? = null

        for (drawModelLine in drawModelsByLine.reversed()) {
            for (drawModel in drawModelLine.reversed()) {
                if (previousDrawModel != null && drawModel.isChord && !previousDrawModel.text.startsWith(" ")) {
                    val letterWidth = textWidth(previousDrawModel.text.first().toString(), 0)
                    drawModel.x = max(0f, drawModel.x - (drawModel.width - letterWidth) / 2f)
                }
                previousDrawModel = drawModel
            }
        }
    }

    private fun calcSpaceWidth(): Int {
        return textWidth("x x", 0) - textWidth("x", 0) * 2 // leading/trailing spaces is not take into account https://stackoverflow.com/a/10729081/860488
    }

    private fun ensureWordsOrCordsIsNotBrokenUp(drawingModel: BuildingDrawModel, drawModelsByLine: MutableList<MutableList<DrawModel>>) {
        val lastLineDrawModel = drawModelsByLine.last()
        val previousLineDrawModel = drawModelsByLine.dropLast(1).last()

        if (!drawingModel.nextIsChord && previousLineDrawModel.last().isChord) {
            val elementsToMoveToNextLine = previousLineDrawModel.reversed().takeWhile { it.isChord || !it.text.endsWith(" ") }.reversed()
            val lastIndex = elementsToMoveToNextLine.count() - 1

            for ((i, drawModel) in elementsToMoveToNextLine.withIndex()) {
                previousLineDrawModel.remove(drawModel)
                drawingModel.nextIsChord = drawModel.isChord
                val text = if (lastIndex == i) drawModel.text.trimStart() else drawModel.text
                drawText(lastLineDrawModel, text, drawingModel)
            }

            drawingModel.nextIsChord = false

        } else if (drawingModel.nextIsChord && !previousLineDrawModel.last().isChord && !previousLineDrawModel.last().text.endsWith(" ")) {

            var lastElement: DrawModel? = null
            val elementsToMoveToNextLine = previousLineDrawModel.reversed().takeWhile {
                val moveToNextLine = it.isChord || (!it.text.endsWith(" ") && !(lastElement?.text?.startsWith(" ") ?: false))
                lastElement = it
                moveToNextLine
            }.reversed()
            val lastIndex = elementsToMoveToNextLine.count() - 1

            for ((i, drawModel) in elementsToMoveToNextLine.withIndex()) {
                previousLineDrawModel.remove(drawModel)
                drawingModel.nextIsChord = drawModel.isChord
                val text = if (lastIndex == i) drawModel.text.trimStart() else drawModel.text
                drawText(lastLineDrawModel, text, drawingModel)
            }

            drawingModel.nextIsChord = true
        }
    }

    private fun drawText(drawModels: MutableList<DrawModel>, word: String, drawingModel: BuildingDrawModel) {
        val x = drawingModel.x
        val y = drawingModel.y
        val lineHeight = drawingModel.lineHeight

        val textWidth = textWidth(word, drawingModel.spaceWidth)
        if (drawingModel.nextIsChord) {
            drawModels.add(DrawModel(true, word, x, y + lineHeight * 0.5f - lineHeight, textWidth))
        } else {
            drawModels.add(DrawModel(false, word, x, y + lineHeight * 0.5f, textWidth))
            drawingModel.x += textWidth
        }
    }

    private fun moveToNextLine(drawingModel: BuildingDrawModel, drawModels: MutableList<MutableList<DrawModel>>) {
        drawingModel.y += drawingModel.lineHeight * 2
        drawingModel.x = 0f
        drawModels.add(mutableListOf())
    }

    private fun textWidth(text: String, spaceWidth: Int): Int {
        super.getPaint().getTextBounds(text, 0, text.length, textBounds)
        val extraSpaceWidth = if (text.endsWith(" ")) spaceWidth else 0
        return textBounds.width() + extraSpaceWidth + textBounds.left
    }

    private fun adjustLineHeights(drawModelsByLine: List<List<DrawModel>>, lineHeight: Float): List<DrawModel> {
        var lineHeightToSubtract = 0f
        for (drawModelsLine in drawModelsByLine) {
            if (drawModelsLine.isEmpty() || drawModelsLine.all { !it.isChord }) {
                lineHeightToSubtract += lineHeight
            }

            if (lineHeightToSubtract > 0) {
                for (drawModel in drawModelsLine) {
                    drawModel.y -= lineHeightToSubtract
                }
            }
        }

        return drawModelsByLine.flatten()
    }

    data class DrawModel(val isChord: Boolean, val text: String, var x: Float, var y: Float, val width: Int)
    data class BuildingDrawModel(var nextIsChord: Boolean, var x: Float, var y: Float, val lineHeight: Float, val spaceWidth: Int)
}