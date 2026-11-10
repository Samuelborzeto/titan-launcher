package com.titanlauncher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.titanlauncher.apps.AppEntry
import com.titanlauncher.apps.AppRepository
import com.titanlauncher.ui.TitanColors

@Composable
fun Dock(apps: List<AppEntry>, repo: AppRepository, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(TitanColors.Graphite.copy(alpha = 0.85f), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        apps.forEach { app ->
            AppIcon(app = app, repo = repo, showLabel = false, onClick = { repo.launch(app) })
        }
    }
}
