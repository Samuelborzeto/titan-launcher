package com.titanlauncher.titan

import kotlin.math.abs
import kotlin.math.roundToInt

enum class PushSide { FromRight, FromLeft }

data class PushState(
    val visible: Boolean,
    val side: PushSide,
    /** 0f..1f: quanto da troca de página já aconteceu. */
    val progress: Float,
    /** Dedo na tela e praticamente parado. */
    val holding: Boolean,
) {
    companion object {
        val Hidden = PushState(visible = false, side = PushSide.FromRight, progress = 0f, holding = false)
    }
}

private const val MIN_VISIBLE_PROGRESS = 0.001f
private const val HOLD_VELOCITY_PX_PER_SEC = 40f

/**
 * @param offset página atual + fração (0.35 = 35% do caminho da página 0 para a 1)
 * @param settledPage página em repouso antes do gesto
 */
fun computePushState(
    offset: Float,
    settledPage: Int,
    pageCount: Int,
    dragging: Boolean,
    velocityPxPerSec: Float,
): PushState {
    val delta = offset - settledPage
    val progress = abs(delta).coerceIn(0f, 1f)
    if (progress < MIN_VISIBLE_PROGRESS) return PushState.Hidden

    val target = if (delta > 0f) settledPage + 1 else settledPage - 1
    if (target < 0 || target > pageCount - 1) return PushState.Hidden

    return PushState(
        visible = true,
        side = if (delta > 0f) PushSide.FromRight else PushSide.FromLeft,
        progress = progress,
        holding = dragging && abs(velocityPxPerSec) < HOLD_VELOCITY_PX_PER_SEC,
    )
}

fun frameIndex(progress: Float, frameCount: Int): Int {
    if (frameCount <= 1) return 0
    return (progress.coerceIn(0f, 1f) * (frameCount - 1)).roundToInt()
}
