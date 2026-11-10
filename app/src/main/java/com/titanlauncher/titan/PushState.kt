package com.titanlauncher.titan

import kotlin.math.abs
import kotlin.math.floor
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

/**
 * @param offset página atual + fração (0.35 = 35% do caminho da página 0 para a 1)
 * @param settledPage página em repouso antes do gesto
 * @param still o dedo está praticamente parado
 */
fun computePushState(
    offset: Float,
    settledPage: Int,
    pageCount: Int,
    dragging: Boolean,
    still: Boolean,
): PushState {
    val delta = offset - settledPage
    val distance = abs(delta)
    if (distance < MIN_VISIBLE_PROGRESS) return PushState.Hidden

    // Se o dedo agarrou a tela de novo e seguiu para mais uma página, o progresso
    // conta a partir da página que acabou de ser atravessada.
    val whole = floor(distance)
    val crossed = if (distance == whole) (whole - 1f).toInt() else whole.toInt()
    val progress = (distance - crossed).coerceIn(0f, 1f)
    if (progress < MIN_VISIBLE_PROGRESS) return PushState.Hidden

    val direction = if (delta > 0f) 1 else -1
    val target = settledPage + direction * (crossed + 1)
    if (target < 0 || target > pageCount - 1) return PushState.Hidden

    return PushState(
        visible = true,
        side = if (direction > 0) PushSide.FromRight else PushSide.FromLeft,
        progress = progress,
        holding = dragging && still,
    )
}

fun frameIndex(progress: Float, frameCount: Int): Int {
    if (frameCount <= 1) return 0
    return (progress.coerceIn(0f, 1f) * (frameCount - 1)).roundToInt()
}
