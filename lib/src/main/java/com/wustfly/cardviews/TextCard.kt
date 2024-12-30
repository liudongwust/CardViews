package com.wustfly.cardviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.wustfly.cardviews.ICard

class TextCard(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs), ICard {

    private val card = Card(this, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        card.onMeasure { w, h -> setMeasuredDimension(w, h) }
    }

    override fun draw(canvas: Canvas) {
        card.draw()
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        card.onDraw(canvas) { super.onDraw(canvas) }
    }

    override fun obtainCard(): Card = card

}