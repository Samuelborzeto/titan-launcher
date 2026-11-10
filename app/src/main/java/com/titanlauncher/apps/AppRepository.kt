package com.titanlauncher.apps

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppRepository(context: Context, private val scope: CoroutineScope) {
    private val context = context.applicationContext
    private val pm: PackageManager = this.context.packageManager

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    /** Apps instalados em ordem alfabética, sem o próprio launcher. */
    val apps: StateFlow<List<AppEntry>> = _apps

    private val icons = ConcurrentHashMap<String, ImageBitmap>()
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            icons.clear()
            refresh()
        }
    }

    fun start() {
        refresh()
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        registered = true
    }

    fun stop() {
        if (!registered) return
        runCatching { context.unregisterReceiver(receiver) }
        registered = false
    }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val list = runCatching { pm.queryIntentActivities(intent, 0) }.getOrDefault(emptyList())
                .filter { it.activityInfo.packageName != context.packageName }
                .map {
                    AppEntry(
                        label = it.loadLabel(pm).toString(),
                        packageName = it.activityInfo.packageName,
                        activityName = it.activityInfo.name,
                    )
                }
                .sortedBy { it.label.lowercase() }
            _apps.value = list
        }
    }

    fun launch(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        // Se o app foi removido nesse meio-tempo, só atualiza a lista.
        runCatching { context.startActivity(intent) }.onFailure { refresh() }
    }

    /** Pode ser lento na primeira vez; chamar fora da thread principal. */
    fun icon(app: AppEntry): ImageBitmap {
        val key = "${app.packageName}/${app.activityName}"
        return icons.getOrPut(key) {
            val drawable = runCatching {
                pm.getActivityIcon(ComponentName(app.packageName, app.activityName))
            }.getOrElse { pm.defaultActivityIcon }
            drawable.toBitmap(ICON_PX, ICON_PX).asImageBitmap()
        }
    }

    /** Telefone, mensagens, navegador e câmera, nessa ordem. Os que não existirem ficam de fora. */
    fun dockApps(): List<AppEntry> {
        val current = _apps.value
        val intents = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
        )
        return intents.mapNotNull { intent ->
            val pkg = runCatching {
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            }.getOrNull()
            current.firstOrNull { it.packageName == pkg }
        }.distinct()
    }

    private companion object {
        const val ICON_PX = 144
    }
}
