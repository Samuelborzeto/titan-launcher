package com.titanlauncher.titan

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val OGRE_WIDTH_FRACTION = 0.45f
private const val FRAME_ASPECT = 1600f / 720f
private const val MAX_HEIGHT_FRACTION = 0.85f
private const val FADE_OUT_START = 0.90f
private const val TREMOR_INTERVAL_MS = 60L

/**
 * Desenha o ogro por cima das páginas. As mãos ficam coladas na divisa entre a
 * página que sai e a que entra; quando a divisa anda, ele anda junto.
 */
@Composable
fun TitanLayer(state: PushState, frames: FrameSource, modifier: Modifier = Modifier) {
    if (!state.visible) return

    val index = frameIndex(state.progress, frames.frameCount)
    LaunchedEffect(index, frames) { frames.prefetch(index) }

    var tremor by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state.holding) {
        if (!state.holding) {
            tremor = 0f
            return@LaunchedEffect
        }
        var sign = 1f
        while (true) {
            tremor = sign
            sign = -sign
            delay(TREMOR_INTERVAL_MS)
        }
    }

    val image = if (frames.frameCount > 0) frames.frame(index) else null

    Canvas(modifier) {
        val screenW = size.width
        val screenH = size.height

        var ogreW = screenW * OGRE_WIDTH_FRACTION
        var ogreH = ogreW * FRAME_ASPECT
        val maxH = screenH * MAX_HEIGHT_FRACTION
        if (ogreH > maxH) {
            ogreH = maxH
            ogreW = ogreH / FRAME_ASPECT
        }

        val alpha = if (state.progress <= FADE_OUT_START) 1f
        else ((1f - state.progress) / (1f - FADE_OUT_START)).coerceIn(0f, 1f)

        // Calculado como se viesse da direita; o outro lado é o mesmo desenho espelhado.
        val left = screenW * (1f - state.progress) + tremor * 2.dp.toPx()
        val top = screenH - ogreH

        scale(scaleX = if (state.side == PushSide.FromLeft) -1f else 1f, scaleY = 1f, pivot = center) {
            if (image != null) {
                drawImage(
                    image = image,
                    dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                    dstSize = IntSize(ogreW.roundToInt(), ogreH.roundToInt()),
                    alpha = alpha,
                )
            } else {
                inset(left = left, top = top, right = screenW - left - ogreW, bottom = 0f) {
                    drawPlaceholderOgre(state.progress, alpha)
                }
            }
        }
    }
}
