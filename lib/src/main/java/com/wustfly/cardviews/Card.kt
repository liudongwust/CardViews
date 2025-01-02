package com.wustfly.cardviews

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class Card(val view: View, attrs: AttributeSet) {

    companion object {
        const val INNER = 0
        const val OUTER = 1

        const val LT_MASK = 0x01
        const val RT_MASK = 0x02
        const val LB_MASK = 0x04
        const val RB_MASK = 0x08

        const val CIRCLE = 0
        const val QUAD = 1
        const val LINE = 2
        const val CUBIC = 3

        const val LEFT_RIGHT = "LEFT_RIGHT"
        const val LT_RB = "LT_RB"
        const val TOP_BOTTOM = "TOP_BOTTOM"
        const val RT_LB = "RT_LB"
        const val RIGHT_LEFT = "RIGHT_LEFT"
        const val RB_LT = "RB_LT"
        const val BOTTOM_TOP = "BOTTOM_TOP"
        const val LB_RT = "LB_RT"
    }

    private val m = this

    var radius = 0f
    var radiusLT = 0f
    var radiusRT = 0f
    var radiusLB = 0f
    var radiusRB = 0f
    var radiusPercent = ""//50%
    var radiusLTPercent = ""//50%
    var radiusRTPercent = ""//50%
    var radiusLBPercent = ""//50%
    var radiusRBPercent = ""//50%
    var strokeColor = Color.TRANSPARENT
    var strokeWidth = 0f
    var strokeStyle = INNER
    var position = 0x0F
    var cornerType = CIRCLE
    var dimensionRatio = ""//1:1
    var linearGradientStr: String? = null
    var shadowStr: String? = null
    var shadowType: Int = 1
    var cubicCoefficient = 0f

    private var linearGradient: LinearGradient? = null

    private var p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    private var bg: Drawable? = null

    private var width = 0
    private var height = 0

    init {
        val ta = view.context.obtainStyledAttributes(attrs, R.styleable.Card)
        radius = ta.getDimension(R.styleable.Card_card_radius, 0f)
        radiusLT = ta.getDimension(R.styleable.Card_card_radius_LT, 0f)
        radiusRT = ta.getDimension(R.styleable.Card_card_radius_RT, 0f)
        radiusLB = ta.getDimension(R.styleable.Card_card_radius_LB, 0f)
        radiusRB = ta.getDimension(R.styleable.Card_card_radius_RB, 0f)
        radiusPercent = ta.getString(R.styleable.Card_card_radius_percent) ?: ""
        radiusLTPercent = ta.getString(R.styleable.Card_card_radius_LT_percent) ?: ""
        radiusRTPercent = ta.getString(R.styleable.Card_card_radius_RT_percent) ?: ""
        radiusLBPercent = ta.getString(R.styleable.Card_card_radius_LB_percent) ?: ""
        radiusRBPercent = ta.getString(R.styleable.Card_card_radius_RB_percent) ?: ""
        strokeColor = ta.getColor(R.styleable.Card_card_stroke_color, Color.TRANSPARENT)
        strokeWidth = ta.getDimension(R.styleable.Card_card_stroke_width, 0f)
        strokeStyle = ta.getInt(R.styleable.Card_card_stroke_style, INNER)
        dimensionRatio = ta.getString(R.styleable.Card_card_dimension_ratio) ?: ""
        position = ta.getInt(R.styleable.Card_card_corner_position, 0x0F)
        cornerType = ta.getInt(R.styleable.Card_card_corner_style, CIRCLE)
        linearGradientStr = ta.getString(R.styleable.Card_card_linear_gradient)
        shadowStr = ta.getString(R.styleable.Card_card_shadow)
        shadowType = ta.getInt(R.styleable.Card_card_shadow_type, 1)
        cubicCoefficient = ta.getFloat(R.styleable.Card_card_cubic_coefficient, 0f)
        ta.recycle()

        if (strokeStyle == OUTER && view is ViewGroup) {
            view.setPadding(
                strokeWidth.toInt(),
                strokeWidth.toInt(),
                strokeWidth.toInt(),
                strokeWidth.toInt()
            )
        }
    }

    //LEFT_RIGHT,colorsNum,#FFFFFF,#000000,#FF0000,positionNum,0,0.6,1
    private fun generateLinearGradient(gradient: String?) {

        if (gradient.isNullOrBlank()) {
            linearGradient = null
            return
        }

        kotlin.runCatching {

            val params = gradient.split(",")

            val orientation = params[0]
            val x1: Float
            val y1: Float
            val x2: Float
            val y2: Float
            when (orientation) {
                LEFT_RIGHT -> {
                    x1 = 0f
                    y1 = 0f
                    x2 = width.toFloat()
                    y2 = 0f
                }

                LT_RB -> {
                    x1 = 0f
                    y1 = 0f
                    x2 = width.toFloat()
                    y2 = height.toFloat()
                }

                TOP_BOTTOM -> {
                    x1 = 0f
                    y1 = 0f
                    x2 = 0f
                    y2 = height.toFloat()
                }

                RT_LB -> {
                    x1 = width.toFloat()
                    y1 = 0f
                    x2 = 0f
                    y2 = height.toFloat()
                }

                RIGHT_LEFT -> {
                    x1 = width.toFloat()
                    y1 = 0f
                    x2 = 0f
                    y2 = 0f
                }

                RB_LT -> {
                    x1 = width.toFloat()
                    y1 = height.toFloat()
                    x2 = 0f
                    y2 = 0f
                }

                BOTTOM_TOP -> {
                    x1 = 0f
                    y1 = height.toFloat()
                    x2 = 0f
                    y2 = 0f
                }

                LB_RT -> {
                    x1 = 0f
                    y1 = height.toFloat()
                    x2 = width.toFloat()
                    y2 = 0f
                }

                else -> {
                    val degree = orientation.toFloat() * Math.PI / 180f

                    val r = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0))

                    var x: Float
                    var y: Float

                    (r * cos(degree) / 2).toFloat().let {

                        if (abs(it) > width / 2f) {
                            x = sign(cos(degree).toFloat()) * (width / 2f)
                            y = (x * tan(degree)).toFloat()
                        } else {
                            y = sign(sin(degree).toFloat()) * (height / 2f)
                            x = (y / tan(degree)).toFloat()
                        }

                    }

            /*        val x = (r * cos(degree) / 2).toFloat()
                        .let { if (abs(it) < width / 2f) it else sign(it) * (width / 2f) }
                    val y = (r * sin(degree) / 2).toFloat()
                        .let { if (abs(it) < height / 2f) it else sign(it) * (height / 2f) }*/

                    x1 = width / 2f - x
                    y1 = height / 2f - y
                    x2 = width / 2f + x
                    y2 = height / 2f + y
                }
            }

            val colorNum = params[1].toInt()
            val colors = mutableListOf<Int>()
            for (i in 2 until colorNum + 2) {
                colors.add(Color.parseColor(params[i]))
            }

            val positionNum = params[colorNum + 2].toInt()
            val positions = mutableListOf<Float>()
            for (i in colorNum + 3 until colorNum + positionNum + 3) {
                positions.add(params[i].toFloat())
            }

            linearGradient = LinearGradient(
                x1,
                y1,
                x2,
                y2,
                colors.toIntArray(),
                positions.toFloatArray(),
                Shader.TileMode.CLAMP
            )

            return
        }

        linearGradient = null

    }

    fun onMeasure(setSize: (w: Int, h: Int) -> Unit) {
        width = view.measuredWidth
        height = view.measuredHeight
        if (dimensionRatio.isNotEmpty()) {
            kotlin.runCatching {
                val h = view.measuredHeight
                val w = view.measuredWidth
                val rw = dimensionRatio.split(":")[0].toFloat()
                val rh = dimensionRatio.split(":")[1].toFloat()

                if (h == 0) {
                    setSize(w, (w * rh / rw).toInt())
                    width = w
                    height = (w * rh / rw).toInt()
                    if (view is ViewGroup) {
                        view.layoutParams.width = w
                        view.layoutParams.height = (w * rh / rw).toInt()
                        view.requestLayout()
                    }
                } else if (w == 0) {
                    setSize((h * rw / rh).toInt(), h)
                    width = (h * rw / rh).toInt()
                    height = h
                    if (view is ViewGroup) {
                        view.layoutParams.width = (h * rw / rh).toInt()
                        view.layoutParams.height = h
                        view.requestLayout()
                    }
                }
            }
        }

        listOf(
            radiusPercent,
            radiusLTPercent,
            radiusRTPercent,
            radiusLBPercent,
            radiusRBPercent,
        ).forEachIndexed { index, percent ->
            if (percent.isNotEmpty() && percent.endsWith("%")) {
                kotlin.runCatching {
                    val value = percent.substring(0, percent.indexOf("%")).toFloat()
                    val ratio = value / 100f
                    (minOf(view.measuredWidth, view.measuredHeight) * ratio).let {
                        when (index) {
                            0 -> radius = it
                            1 -> radiusLT = it
                            2 -> radiusRT = it
                            3 -> radiusLB = it
                            4 -> radiusRB = it
                        }
                    }
                }
            }
        }

        generateLinearGradient(linearGradientStr)
    }

    fun draw() {
        if (view.background != null) {
            bg = view.background
            view.background = null
        }
    }

    fun onDraw(canvas: Canvas, superDraw: () -> Unit) {
        drawBackground(canvas)

        canvas.save()
        if (strokeStyle == OUTER) {
            clipInner(canvas)
        } else {
            clipOuter(canvas)
        }

        superDraw()

        canvas.restore()

        drawStroke(canvas)

        drawShadow(canvas)

    }

    private fun drawBackground(canvas: Canvas) {

        linearGradient?.let {

            canvas.save()
            if (strokeStyle == OUTER) {
                clipInner(canvas)
            } else {
                clipOuter(canvas)
            }
            canvas.drawRect(Rect(0, 0, view.width, view.height), p.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
                style = Paint.Style.FILL
                shader = it
            })
            canvas.restore()

            return
        }

        bg?.let {
            canvas.save()
            if (strokeStyle == OUTER) {
                clipInner(canvas)
            } else {
                clipOuter(canvas)
            }
            it.setBounds(0, 0, view.measuredWidth, view.measuredHeight)
            it.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawStroke(canvas: Canvas) {

        if (strokeWidth == 0f) return

        val ratio = 4f

        canvas.save()
        canvas.scale(1f / ratio, 1f / ratio)
        clipStroke(canvas, ratio)
        canvas.drawColor(m.strokeColor)
        canvas.restore()
    }

    private fun drawShadow(canvas: Canvas) {
        if (shadowStr.isNullOrEmpty()) return

        kotlin.runCatching {
            val s = shadowStr!!.split(",")
            val radius = dpToPx(s[0].toFloat(), view.context)
            val color = Color.parseColor(s[1])

            val path = getStrokePath()
            if (shadowType == 2) {
                path.op(Path().apply {
                    addRect(
                        RectF(0f, 0f, view.width.toFloat(), view.height / 2f),
                        Path.Direction.CW
                    )
                }, Path.Op.DIFFERENCE)
            }

            canvas.save()
            canvas.clipOutPath(getStrokePath())
            canvas.drawPath(path, p.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
                this.color = color
                style = Paint.Style.FILL
                setMaskFilter(BlurMaskFilter(radius.toFloat(), BlurMaskFilter.Blur.OUTER))
            })
            canvas.restore()
        }
    }

    private fun clipInner(canvas: Canvas) {
        canvas.clipPath(getStrokePath(strokeWidth))
    }

    private fun clipOuter(canvas: Canvas) {
        canvas.clipPath(getStrokePath())
    }

    private fun clipStroke(canvas: Canvas, ratio: Float = 1f) {
        val path = getStrokePath(ratio = ratio)
        path.op(getStrokePath(strokeWidth, ratio), Path.Op.DIFFERENCE)
        canvas.clipPath(path)
    }

    private fun getStrokePath(gap: Float = 0f, ratio: Float = 1f): Path {

        return when (cornerType) {
            CIRCLE -> getCircleCornerPath(gap, ratio)
            QUAD -> getQuadCornerPath(gap, ratio)
            LINE -> getLineCornerPath(gap, ratio)
            CUBIC -> getCubicCornerPath(gap, ratio)
            else -> Path()
        }

    }

    private fun getCircleCornerPath(gap: Float = 0f, ratio: Float = 1f): Path {

        val w = width.toFloat()
        val h = height.toFloat()

        //val r = (radius - gap) * ratio
        val r_lt = (radiusLT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rt = (radiusRT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_lb = (radiusLB.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rb = (radiusRB.let { if (it > 0f) it else radius } - gap) * ratio

        val left = gap * ratio
        val right = (w - gap) * ratio
        val top = gap * ratio
        val bottom = (h - gap) * ratio

        val path = Path()

        if (position and LT_MASK != 0 && r_lt > 0f) {
            path.moveTo(left, top + r_lt)
            path.arcTo(RectF(left, top, left + r_lt * 2, top + r_lt * 2), 180f, 90f)
        } else {
            path.moveTo(left, top)
        }

        if (position and RT_MASK != 0 && r_rt > 0f) {
            path.lineTo(right - r_rt, top)
            path.arcTo(RectF(right - r_rt * 2, top, right, top + r_rt * 2), 270f, 90f)
        } else {
            path.lineTo(right, top)
        }

        if (position and RB_MASK != 0 && r_rb > 0f) {
            path.lineTo(right, bottom - r_rb)
            path.arcTo(RectF(right - r_rb * 2, bottom - r_rb * 2, right, bottom), 0f, 90f)
        } else {
            path.lineTo(right, bottom)
        }

        if (position and LB_MASK != 0 && r_lb > 0f) {
            path.lineTo(left + r_lb, bottom)
            path.arcTo(RectF(left, bottom - r_lb * 2, left + r_lb * 2, bottom), 90f, 90f)
        } else {
            path.lineTo(left, bottom)
        }

        path.close()

        return path
    }

    private fun getQuadCornerPath(gap: Float = 0f, ratio: Float = 1f): Path {

        val w = view.width.toFloat()
        val h = view.height.toFloat()

        //val r = (radius - gap) * ratio
        val r_lt = (radiusLT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rt = (radiusRT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_lb = (radiusLB.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rb = (radiusRB.let { if (it > 0f) it else radius } - gap) * ratio
        val left = gap * ratio
        val right = (w - gap) * ratio
        val top = gap * ratio
        val bottom = (h - gap) * ratio

        val path = Path()

        if (position and LT_MASK != 0 && r_lt > 0f) {
            path.moveTo(left, top + r_lt)
            path.rQuadTo(0f, -r_lt, r_lt, -r_lt)
        } else {
            path.moveTo(left, top)
        }

        if (position and RT_MASK != 0 && r_rt > 0f) {
            path.lineTo(right - r_rt, top)
            path.rQuadTo(r_rt, 0f, r_rt, r_rt)
        } else {
            path.lineTo(right, top)
        }

        if (position and RB_MASK != 0 && r_rb > 0f) {
            path.lineTo(right, bottom - r_rb)
            path.rQuadTo(0f, r_rb, -r_rb, r_rb)
        } else {
            path.lineTo(right, bottom)
        }

        if (position and LB_MASK != 0 && r_lb > 0f) {
            path.lineTo(left + r_lb, bottom)
            path.rQuadTo(-r_lb, 0f, -r_lb, -r_lb)
        } else {
            path.lineTo(left, bottom)
        }

        path.close()

        return path
    }

    private fun getCubicCornerPath(gap: Float = 0f, ratio: Float = 1f): Path {

        val w = view.width.toFloat()
        val h = view.height.toFloat()

        //val r = (radius - gap) * ratio
        val r_lt = (radiusLT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rt = (radiusRT.let { if (it > 0f) it else radius } - gap) * ratio
        val r_lb = (radiusLB.let { if (it > 0f) it else radius } - gap) * ratio
        val r_rb = (radiusRB.let { if (it > 0f) it else radius } - gap) * ratio
        val left = gap * ratio
        val right = (w - gap) * ratio
        val top = gap * ratio
        val bottom = (h - gap) * ratio

        val path = Path()

        if (position and LT_MASK != 0 && r_lt > 0f) {
            path.moveTo(left, top + r_lt)
            path.rCubicTo(
                0f,
                -r_lt * cubicCoefficient,
                r_lt * (1f - cubicCoefficient),
                -r_lt,
                r_lt,
                -r_lt
            )
        } else {
            path.moveTo(left, top)
        }

        if (position and RT_MASK != 0 && r_rt > 0f) {
            path.lineTo(right - r_rt, top)
            path.rCubicTo(
                r_rt * cubicCoefficient,
                0f,
                r_rt,
                r_rt * (1 - cubicCoefficient),
                r_rt,
                r_rt
            )
        } else {
            path.lineTo(right, top)
        }

        if (position and RB_MASK != 0 && r_rb > 0f) {
            path.lineTo(right, bottom - r_rb)
            path.rCubicTo(
                0f,
                r_rb * cubicCoefficient,
                -r_rb * (1 - cubicCoefficient),
                r_rb,
                -r_rb,
                r_rb
            )
        } else {
            path.lineTo(right, bottom)
        }

        if (position and LB_MASK != 0 && r_lb > 0f) {
            path.lineTo(left + r_lb, bottom)
            path.rCubicTo(
                -r_lb * cubicCoefficient,
                0f,
                -r_lb,
                -r_lb * (1 - cubicCoefficient),
                -r_lb,
                -r_lb
            )
        } else {
            path.lineTo(left, bottom)
        }

        path.close()

        return path
    }

    private fun getLineCornerPath(gap: Float = 0f, ratio: Float = 1f): Path {

        val w = view.width.toFloat()
        val h = view.height.toFloat()

        //val r = (radius - gap + gap * tan(22.5 * Math.PI / 180)).toFloat() * ratio
        val r_lt =
            (radiusLT.let { if (it > 0f) it else radius } - gap + gap * tan(22.5 * Math.PI / 180)).toFloat() * ratio
        val r_rt =
            (radiusRT.let { if (it > 0f) it else radius } - gap + gap * tan(22.5 * Math.PI / 180)).toFloat() * ratio
        val r_lb =
            (radiusLB.let { if (it > 0f) it else radius } - gap + gap * tan(22.5 * Math.PI / 180)).toFloat() * ratio
        val r_rb =
            (radiusRB.let { if (it > 0f) it else radius } - gap + gap * tan(22.5 * Math.PI / 180)).toFloat() * ratio
        val left = gap * ratio
        val right = (w - gap) * ratio
        val top = gap * ratio
        val bottom = (h - gap) * ratio

        val path = Path()

        if (position and LT_MASK != 0 && r_lt > 0f) {
            path.moveTo(left, top + r_lt)
            path.rLineTo(r_lt, -r_lt)
        } else {
            path.moveTo(left, top)
        }

        if (position and RT_MASK != 0 && r_rt > 0f) {
            path.lineTo(right - r_rt, top)
            path.rLineTo(r_rt, r_rt)
        } else {
            path.lineTo(right, top)
        }

        if (position and RB_MASK != 0 && r_rb > 0f) {
            path.lineTo(right, bottom - r_rb)
            path.rLineTo(-r_rb, r_rb)
        } else {
            path.lineTo(right, bottom)
        }

        if (position and LB_MASK != 0 && r_lb > 0f) {
            path.lineTo(left + r_lb, bottom)
            path.rLineTo(-r_lb, -r_lb)
        } else {
            path.lineTo(left, bottom)
        }

        path.close()

        return path
    }

    fun invalidate() {
        view.requestLayout()
        view.invalidate()
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

}