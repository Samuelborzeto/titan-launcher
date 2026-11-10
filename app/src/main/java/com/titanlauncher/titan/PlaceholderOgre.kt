package com.titanlauncher.titan

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

private val Skin = Color(0xFF7C8F6B)
private val SkinDark = Color(0xFF5E6E52)
private val Outline = Color(0xFF2A3324)
private val Cloth = Color(0xFF3B2F2A)

/**
 * Silhueta provisória de ogro, usada enquanto não há imagens em assets/titan.
 * Desenhada como se viesse da direita: mãos espalmadas na borda esquerda da área.
 * Com progress 0 o corpo está perto das mãos e os braços dobrados; com 1, braços estendidos.
 */
fun DrawScope.drawPlaceholderOgre(progress: Float, alpha: Float = 1f) {
    val p = progress.coerceIn(0f, 1f)
    val w = size.width
    val h = size.height

    val bodyShift = -(1f - p) * 0.20f * w
    val lean = -12f * p
    val hip = Offset(0.66f * w + bodyShift, 0.80f * h)
    val armWidth = 0.11f * w
    val outlineExtra = 0.025f * w

    // Pernas
    for (legX in listOf(0.52f, 0.72f)) {
        drawRoundRect(
            color = Outline,
            topLeft = Offset(legX * w + bodyShift - outlineExtra / 2, 0.76f * h),
            size = Size(0.16f * w + outlineExtra, 0.24f * h),
            cornerRadius = CornerRadius(0.05f * w),
            alpha = alpha,
        )
        drawRoundRect(
            color = Cloth,
            topLeft = Offset(legX * w + bodyShift, 0.765f * h),
            size = Size(0.16f * w, 0.235f * h),
            cornerRadius = CornerRadius(0.05f * w),
            alpha = alpha,
        )
    }

    rotate(degrees = lean, pivot = hip) {
        // Tronco
        val torsoTopLeft = Offset(0.40f * w + bodyShift, 0.34f * h)
        val torsoSize = Size(0.54f * w, 0.47f * h)
        drawRoundRect(
            color = Outline,
            topLeft = torsoTopLeft - Offset(outlineExtra / 2, outlineExtra / 2),
            size = Size(torsoSize.width + outlineExtra, torsoSize.height + outlineExtra),
            cornerRadius = CornerRadius(0.20f * w),
            alpha = alpha,
        )
        drawRoundRect(
            color = Skin,
            topLeft = torsoTopLeft,
            size = torsoSize,
            cornerRadius = CornerRadius(0.20f * w),
            alpha = alpha,
        )
        // Cabeça baixa, entre os ombros
        val head = Offset(0.52f * w + bodyShift, 0.33f * h)
        val headR = 0.14f * w
        drawCircle(Outline, headR + outlineExtra / 2, head, alpha)
        drawCircle(Skin, headR, head, alpha)
        // Sobrancelha e olhos
        drawLine(
            Outline,
            Offset(head.x - headR * 0.75f, head.y - headR * 0.20f),
            Offset(head.x + headR * 0.15f, head.y - headR * 0.02f),
            strokeWidth = 0.022f * w,
            cap = StrokeCap.Round,
            alpha = alpha,
        )
        drawCircle(Outline, 0.016f * w, Offset(head.x - headR * 0.40f, head.y + headR * 0.18f), alpha)
        // Presas
        drawLine(
            Color.White,
            Offset(head.x - headR * 0.45f, head.y + headR * 0.72f),
            Offset(head.x - headR * 0.45f, head.y + headR * 0.50f),
            strokeWidth = 0.02f * w,
            cap = StrokeCap.Round,
            alpha = alpha,
        )
    }

    // Os ombros acompanham a inclinação de forma aproximada (ângulo pequeno).
    val leanShift = -(p * 0.07f * w)
    val shoulderTop = Offset(0.46f * w + bodyShift + leanShift, 0.42f * h)
    val shoulderBottom = Offset(0.50f * w + bodyShift + leanShift, 0.52f * h)

    val handTop = Offset(0.07f * w, 0.40f * h)
    val handBottom = Offset(0.07f * w, 0.54f * h)
    val elbowDrop = (1f - p) * 0.10f * h

    fun arm(shoulder: Offset, hand: Offset, color: Color, width: Float) {
        val elbow = Offset((shoulder.x + hand.x) / 2f, (shoulder.y + hand.y) / 2f + elbowDrop)
        val path = Path().apply {
            moveTo(shoulder.x, shoulder.y)
            lineTo(elbow.x, elbow.y)
            lineTo(hand.x, hand.y)
        }
        drawPath(path, color, alpha, Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    for ((shoulder, hand, tone) in listOf(
        Triple(shoulderBottom, handBottom, SkinDark),
        Triple(shoulderTop, handTop, Skin),
    )) {
        arm(shoulder, hand, Outline, armWidth + outlineExtra)
        arm(shoulder, hand, tone, armWidth)
        // Mão espalmada na borda
        val handSize = Size(0.12f * w, 0.11f * h)
        val handTopLeft = Offset(0f, hand.y - handSize.height / 2f)
        drawRoundRect(
            Outline,
            handTopLeft - Offset(0f, outlineExtra / 2),
            Size(handSize.width + outlineExtra / 2, handSize.height + outlineExtra),
            CornerRadius(0.04f * w),
            alpha = alpha,
        )
        drawRoundRect(tone, handTopLeft, handSize, CornerRadius(0.04f * w), alpha = alpha)
    }
}
