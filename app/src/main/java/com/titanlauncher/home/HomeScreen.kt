package com.titanlauncher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.titanlauncher.apps.AppEntry
import com.titanlauncher.apps.AppRepository
import com.titanlauncher.apps.paginate
import com.titanlauncher.titan.AssetFrameSource
import com.titanlauncher.titan.PushState
import com.titanlauncher.titan.TitanLayer
import com.titanlauncher.titan.computePushState
import com.titanlauncher.ui.TitanColors
import kotlin.math.abs

private const val PAGES = 3
private const val COLUMNS = 4
private const val ROWS = 5

@Composable
fun HomeScreen(
    repo: AppRepository,
    titanEnabled: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val apps by repo.apps.collectAsState()
    val pages = remember(apps) { paginate(apps, PAGES, COLUMNS * ROWS) }
    val dock = remember(apps) { repo.dockApps() }
    val pagerState = rememberPagerState(pageCount = { PAGES })

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val frames = remember { AssetFrameSource(context.applicationContext, scope) }

    val openDrawer by rememberUpdatedState(onOpenDrawer)
    val openSettings by rememberUpdatedState(onOpenSettings)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(TitanColors.Black, TitanColors.DeepPurple, TitanColors.Black),
                ),
            ),
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .pointerInput(Unit) { detectTapGestures(onLongPress = { openSettings() }) }
                .pointerInput(Unit) {
                    val threshold = 72.dp.toPx()
                    var total = 0f
                    var fired = false
                    detectVerticalDragGestures(
                        onDragStart = { total = 0f; fired = false },
                        onVerticalDrag = { _, dy ->
                            total += dy
                            if (!fired && total < -threshold) {
                                fired = true
                                openDrawer()
                            }
                        },
                    )
                },
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                beyondViewportPageCount = 1,
            ) { page ->
                PageGrid(apps = pages[page], repo = repo)
            }
            PageIndicator(current = pagerState.currentPage)
            Dock(apps = dock, repo = repo)
        }

        if (titanEnabled) {
            val push = rememberPushState(pagerState, widthPx)
            if (push.visible) {
                TitanLayer(state = push, frames = frames, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

private const val STILL_AFTER_NANOS = 150_000_000L

/**
 * Converte o estado do pager no estado do ogro. O dedo conta como parado quando
 * a tela andou menos de 4 dp nos últimos 150 ms.
 */
@Composable
private fun rememberPushState(pagerState: PagerState, widthPx: Float): PushState {
    val dragging by pagerState.interactionSource.collectIsDraggedAsState()
    var still by remember { mutableStateOf(false) }
    val stillDistancePx = with(LocalDensity.current) { 4.dp.toPx() }

    LaunchedEffect(pagerState.isScrollInProgress, widthPx) {
        still = false
        if (!pagerState.isScrollInProgress) return@LaunchedEffect
        var anchorPx = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * widthPx
        var anchorNanos = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val px = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * widthPx
            if (abs(px - anchorPx) > stillDistancePx) {
                anchorPx = px
                anchorNanos = now
                still = false
            } else if (now - anchorNanos > STILL_AFTER_NANOS) {
                still = true
            }
        }
    }

    return computePushState(
        offset = pagerState.currentPage + pagerState.currentPageOffsetFraction,
        settledPage = pagerState.settledPage,
        pageCount = pagerState.pageCount,
        dragging = dragging,
        still = still,
    )
}

@Composable
private fun PageGrid(apps: List<AppEntry>, repo: AppRepository) {
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        for (row in 0 until ROWS) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                for (column in 0 until COLUMNS) {
                    val app = apps.getOrNull(row * COLUMNS + column)
                    if (app != null) {
                        AppIcon(
                            app = app,
                            repo = repo,
                            modifier = Modifier.weight(1f),
                            onClick = { repo.launch(app) },
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(current: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(PAGES) { index ->
            Box(
                Modifier
                    .padding(horizontal = 5.dp)
                    .size(8.dp)
                    .background(
                        if (index == current) Color.White else TitanColors.Purple.copy(alpha = 0.6f),
                        CircleShape,
                    ),
            )
        }
    }
}
