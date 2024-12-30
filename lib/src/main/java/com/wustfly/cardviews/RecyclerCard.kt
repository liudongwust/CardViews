package com.wustfly.cardviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.wustfly.cardviews.ICard

open class RecyclerCard(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs), ICard {

    val card = Card(this, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        card.onMeasure { w, h -> setMeasuredDimension(w, h) }
    }

    override fun draw(canvas: Canvas) {
        card.draw()
        super.draw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        card.onDraw(canvas) { super.dispatchDraw(canvas) }
    }

    override fun obtainCard(): Card = card
}