package com.wustfly.cardviews

import android.graphics.Color
import android.view.View
import androidx.databinding.BindingAdapter
import com.wustfly.cardviews.Card

interface ICard {

    fun obtainCard(): Card

    companion object {
        @BindingAdapter(
            "card_radius",
            "card_radius_LT",
            "card_radius_RT",
            "card_radius_LB",
            "card_radius_RB",
            "card_radius_percent",
            "card_linear_gradient",
            "card_stroke_color",
            "card_shadow",
            requireAll = false
        )
        @JvmStatic
        fun setCardParams(
            view: View,
            cardRadius: Float?,
            cardRadiusLT: Float?,
            cardRadiusRT: Float?,
            cardRadiusLB: Float?,
            cardRadiusRB: Float?,
            cardRadiusPercent: String?,
            cardLinearGradient: String?,
            cardStrokeColor: String?,
            cardShadow: String?,
        ) {
            if (view is ICard) {
                val card = view.obtainCard()
                card.radius = cardRadius ?: card.radius
                card.radiusLT = cardRadiusLT ?: card.radiusLT
                card.radiusRT = cardRadiusRT ?: card.radiusRT
                card.radiusLB = cardRadiusLB ?: card.radiusLB
                card.radiusRB = cardRadiusRB ?: card.radiusRB
                card.radiusPercent = cardRadiusPercent ?: card.radiusPercent
                card.linearGradientStr = cardLinearGradient ?: card.linearGradientStr
                card.strokeColor = cardStrokeColor?.let { Color.parseColor(it) } ?: card.strokeColor
                card.shadowStr = cardShadow ?: card.shadowStr
                card.invalidate()
            }

        }
    }

}