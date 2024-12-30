package com.wustfly.cardviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.wustfly.cardviews.ICard
import kotlin.math.pow

open class Tab(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), ICard {

    private val card = Card(this, attrs)

    private val indicator by lazy { Indicator() }

    private val anim by lazy { Anim() }

    private val transition by lazy { Transition() }

    private val titles = mutableListOf<String>()
    var current: Int
    private val selectColor: Int
    private val unSelectColor: Int
    private val selectTxSize: Float
    private val unSelectTxSize: Float
    private val selectTxBold: Boolean
    private val unSelectTxBold: Boolean
    private val selectTxFont: Typeface?
    private val unSelectTxFont: Typeface?
    private val indicatorColor: Int
    private val indicatorRadius: Float

    private var pager: ViewPager2? = null

    private var bindTab: Tab? = null

    private var isViewPager2ScrollDragging = false

    var onTap = {}

    var onSelect: (Int) -> Unit = {}

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.Tab)
        ta.getString(R.styleable.Tab_tab_titles)
            .let { if (!it.isNullOrEmpty()) titles.addAll(it.split(",")) }
        current = ta.getInt(R.styleable.Tab_tab_current, 0)
        selectColor = ta.getColor(R.styleable.Tab_tab_select_color, Color.BLACK)
        unSelectColor = ta.getColor(R.styleable.Tab_tab_unselect_color, Color.GRAY)
        selectTxSize = ta.getDimension(R.styleable.Tab_tab_select_tx_size, sp2Px(14f))
        unSelectTxSize = ta.getDimension(R.styleable.Tab_tab_unselect_tx_size, sp2Px(14f))
        selectTxBold = ta.getBoolean(R.styleable.Tab_tab_select_tx_bold, false)
        unSelectTxBold = ta.getBoolean(R.styleable.Tab_tab_select_tx_bold, false)
        selectTxFont = ta.getFont(R.styleable.Tab_tab_select_tx_font)
        unSelectTxFont = ta.getFont(R.styleable.Tab_tab_unselect_tx_font)
        indicatorColor = ta.getColor(R.styleable.Tab_tab_indicator_color, Color.WHITE)
        indicatorRadius = ta.getDimension(R.styleable.Tab_tab_indicator_radius, 0f)
        ta.recycle()

        orientation = HORIZONTAL

        titles.forEachIndexed { index, s ->
            addView(generateItemView(index, s), LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        card.onMeasure { w, h -> setMeasuredDimension(w, h) }
    }

    override fun draw(canvas: Canvas) {
        card.draw()
        super.draw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        card.onDraw(canvas) {
            indicator.draw(canvas)
            super.dispatchDraw(canvas)
        }
    }

    override fun obtainCard(): Card = card

    fun generateItemView(index: Int, title: String): View {
        return TextView(context).apply {
            text = title
            if (index == current) selectStyle(this) else unSelectStyle(this)
            gravity = Gravity.CENTER
            setOnClickListener {
                onTap()
                select(index)
            }
        }
    }

    fun selectStyle(tv: View) {
        (tv as TextView).apply {
            setTextColor(selectColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, selectTxSize)
            typeface = selectTxFont ?: if (selectTxBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    fun unSelectStyle(tv: View) {
        (tv as TextView).apply {
            setTextColor(unSelectColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, unSelectTxSize)
            typeface =
                unSelectTxFont ?: if (unSelectTxBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    private fun updateChild() {
        children.forEachIndexed { i, view ->
            if (i == current) selectStyle(view) else unSelectStyle(
                view
            )
        }
    }

    private fun select(index: Int) {
        if (isViewPager2ScrollDragging) return
        if (index == current) return
        val view = getChildAt(index)
        val point1 = PointF(view.left.toFloat(), view.top.toFloat())
        val point2 = PointF(view.right.toFloat(), view.bottom.toFloat())
        anim.start(point1, point2, before = {
            pager?.setCurrentItem(index, true)
            pager?.tag = pager?.isUserInputEnabled
            pager?.isUserInputEnabled = false

            bindTab?.current = current
            bindTab?.transition?.current = current
        }, running = { position ->
            bindTab?.transition?.run(position)
        }, after = {
            current = index
            updateChild()
            if (pager?.tag == true) pager?.isUserInputEnabled = true

            bindTab?.current = index
            bindTab?.updateChild()
            bindTab?.requestLayout()

            onSelect(index)
        })
    }

    fun setSuffix(index: Int, suffix: String) {
        kotlin.runCatching {
            (getChildAt(index) as TextView).text = titles[index] + suffix
        }
    }

    fun setTitles(titles: String) {
        kotlin.runCatching {
            this.titles.clear()
            this.titles.addAll(titles.split(","))
            removeAllViews()
            this.titles.forEachIndexed { index, s ->
                addView(generateItemView(index, s), LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
            }
            requestLayout()
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        indicator.use()
    }

    inner class Indicator {
        lateinit var point1: PointF
        lateinit var point2: PointF

        fun use() {
            val view = getChildAt(current)
            point1 = PointF(view.left.toFloat(), view.top.toFloat())
            point2 = PointF(view.right.toFloat(), view.bottom.toFloat())
        }

        fun draw(canvas: Canvas) {
            canvas.drawRoundRect(
                RectF(point1.x, point1.y, point2.x, point2.y),
                indicatorRadius,
                indicatorRadius,
                Paint().apply {
                    style = Paint.Style.FILL
                    color = indicatorColor
                })
        }
    }

    inner class Anim {

        private lateinit var startPoint1: PointF
        private lateinit var startPoint2: PointF

        private lateinit var endPoint1: PointF
        private lateinit var endPoint2: PointF

        private var action = {}

        private var running = { _: Float -> }

        private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener {
                val value = it.animatedValue as Float

                val deltaX1 = endPoint1.x - startPoint1.x
                val deltaX2 = endPoint2.x - startPoint2.x

                if (deltaX1 > 0) {
                    indicator.point1.x = startPoint1.x + deltaX1 * value.pow(3)
                    indicator.point2.x = startPoint2.x + deltaX2 * value.pow(1f / 3)
                    running(value)
                } else {
                    indicator.point1.x = startPoint1.x + deltaX1 * value.pow(1f / 3)
                    indicator.point2.x = startPoint2.x + deltaX2 * value.pow(3)
                    running(-value)
                }

                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    action()
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    action()
                }
            })
        }

        fun start(
            endPoint1: PointF,
            endPoint2: PointF,
            before: () -> Unit = {},
            running: (Float) -> Unit = {},
            after: () -> Unit = {}
        ) {
            if (anim.isRunning) return
            this.action = after
            this.running = running

            before()

            startPoint1 = PointF(indicator.point1.x, indicator.point1.y)
            startPoint2 = PointF(indicator.point2.x, indicator.point2.y)
            this.endPoint1 = endPoint1
            this.endPoint2 = endPoint2
            anim.start()
        }

        fun cancel() {
            anim.cancel()
        }
    }

    inner class Transition {

        var current = 0

        fun run(position: Float) {
            kotlin.runCatching {
                if (position < 0 && position >= -1) {
                    next(-position)
                } else if (position > 0 && position <= 1) {
                    previous(position)
                }
            }
        }

        private fun next(position: Float) {
            var view = getChildAt(current)
            val startPoint1 = PointF(view.left.toFloat(), view.top.toFloat())
            val startPoint2 = PointF(view.right.toFloat(), view.bottom.toFloat())

            view = getChildAt(current + 1)
            val endPoint1 = PointF(view.left.toFloat(), view.top.toFloat())
            val endPoint2 = PointF(view.right.toFloat(), view.bottom.toFloat())

            val deltaX1 = endPoint1.x - startPoint1.x
            val deltaX2 = endPoint2.x - startPoint2.x

            indicator.point1.x = startPoint1.x + deltaX1 * position.pow(3)
            indicator.point2.x = startPoint2.x + deltaX2 * position.pow(1f / 3)

            invalidate()

        }

        private fun previous(position: Float) {
            var view = getChildAt(current)
            val startPoint1 = PointF(view.left.toFloat(), view.top.toFloat())
            val startPoint2 = PointF(view.right.toFloat(), view.bottom.toFloat())

            view = getChildAt(current - 1)
            val endPoint1 = PointF(view.left.toFloat(), view.top.toFloat())
            val endPoint2 = PointF(view.right.toFloat(), view.bottom.toFloat())

            val deltaX1 = endPoint1.x - startPoint1.x
            val deltaX2 = endPoint2.x - startPoint2.x

            indicator.point1.x = startPoint1.x + deltaX1 * position.pow(1f / 3)
            indicator.point2.x = startPoint2.x + deltaX2 * position.pow(3)

            invalidate()
        }
    }

    fun bindPager(pager: ViewPager2) {

        this.pager = pager

        val rv = pager.getChildAt(0) as RecyclerView

        pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {

                if (!isViewPager2ScrollDragging) return

                current = position
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                if (!isViewPager2ScrollDragging) return

                val offset = -positionOffset
                for (i in 0 until rv.childCount) {
                    val view = rv.getChildAt(i)
                    val viewPosition = rv.getChildAdapterPosition(view)
                    val viewOffset = offset + (viewPosition - position)

                    if (viewPosition == transition.current) {
                        transition.run(viewOffset)
                    }
                }

            }

            override fun onPageScrollStateChanged(state: Int) {

                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    anim.cancel()
                    transition.current = current
                    isViewPager2ScrollDragging = true
                }

                if (!isViewPager2ScrollDragging) return

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    updateChild()
                    isViewPager2ScrollDragging = false
                }
            }

        })
    }

    fun bindTab(tab: Tab) {
        this.bindTab = tab
        tab.bindTab = this
    }

    private fun sp2Px(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }


    companion object {
        @BindingAdapter("tab_titles", requireAll = false)
        @JvmStatic
        fun setCardParams(view: Tab, tabTitles: String?) {
            tabTitles?.let { view.setTitles(it) }
        }
    }


}