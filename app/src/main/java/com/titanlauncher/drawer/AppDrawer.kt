package com.titanlauncher.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.titanlauncher.apps.AppRepository
import com.titanlauncher.ui.TitanColors

@Composable
fun AppDrawer(repo: AppRepository, onClose: () -> Unit, onOpenSettings: () -> Unit) {
    val apps by repo.apps.collectAsState()
    var query by remember { mutableStateOf("") }
    val shown = remember(apps, query) { filterApps(apps, query) }
    val close by rememberUpdatedState(onClose)

    BackHandler(onBack = onClose)

    // Puxar para baixo com a lista já no topo fecha a gaveta.
    val closeThreshold = with(LocalDensity.current) { 96.dp.toPx() }
    val pullToClose = remember(closeThreshold) {
        object : NestedScrollConnection {
            var pulled = 0f
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f) {
                    pulled += available.y
                    if (pulled > closeThreshold) {
                        pulled = 0f
                        close()
                    }
                } else if (consumed.y != 0f) {
                    pulled = 0f
                }
                return Offset.Zero
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TitanColors.Graphite)
            // Impede que o toque passe para a tela inicial que está por baixo.
            .pointerInput(Unit) { detectTapGestures { } }
            .systemBarsPadding()
            .imePadding()
            .nestedScroll(pullToClose),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Buscar aplicativo") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        ) {
            items(shown, key = { "${it.packageName}/${it.activityName}" }) { app ->
                com.titanlauncher.home.AppIcon(
                    app = app,
                    repo = repo,
                    onClick = {
                        repo.launch(app)
                        onClose()
                    },
                )
            }
        }
    }
}
