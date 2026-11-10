package com.titanlauncher.titan

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface FrameSource {
    /** 0 = não há imagens, usar a silhueta. */
    val frameCount: Int

    /** null = ainda carregando ou arquivo inválido. */
    fun frame(index: Int): ImageBitmap?

    /** Carrega os quadros vizinhos a [center] fora da thread principal. */
    fun prefetch(center: Int)
}

private val FRAME_NUMBER = Regex("""\d+""")

/** Só arquivos push_*.webp, em ordem numérica. */
fun sortedFrameNames(all: List<String>): List<String> =
    all.filter { it.startsWith("push_") && it.endsWith(".webp") }
        .sortedWith(compareBy({ FRAME_NUMBER.find(it)?.value?.toIntOrNull() ?: Int.MAX_VALUE }, { it }))

class AssetFrameSource(
    context: Context,
    private val scope: CoroutineScope,
) : FrameSource {
    private val assets = context.assets
    private val names: List<String> =
        runCatching { sortedFrameNames(assets.list(FOLDER)?.toList().orEmpty()) }.getOrDefault(emptyList())

    private val cache = LruCache<Int, ImageBitmap>(CACHE_SIZE)
    private val loading = ConcurrentHashMap.newKeySet<Int>()
    private val failed = ConcurrentHashMap.newKeySet<Int>()

    // Lido em frame() para a tela redesenhar quando um quadro termina de carregar.
    private var loadedTick by mutableIntStateOf(0)

    override val frameCount: Int get() = names.size

    override fun frame(index: Int): ImageBitmap? {
        loadedTick
        return cache.get(index)
    }

    override fun prefetch(center: Int) {
        for (i in (center - NEIGHBORS)..(center + NEIGHBORS)) {
            if (i !in names.indices) continue
            if (cache.get(i) != null || i in failed || !loading.add(i)) continue
            scope.launch(Dispatchers.IO) {
                val bitmap = runCatching {
                    assets.open("$FOLDER/${names[i]}").use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
                withContext(Dispatchers.Main) {
                    if (bitmap != null) cache.put(i, bitmap.asImageBitmap()) else failed.add(i)
                    loading.remove(i)
                    loadedTick++
                }
            }
        }
    }

    private companion object {
        const val FOLDER = "titan"
        const val NEIGHBORS = 2
        const val CACHE_SIZE = NEIGHBORS * 2 + 1
    }
}
