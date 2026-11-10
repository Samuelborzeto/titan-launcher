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
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    // Um quadro por vez, para um deslize rápido não disparar dezenas de decodificações.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val decoder = Dispatchers.IO.limitedParallelism(1)

    @Volatile
    private var wanted = 0

    override val frameCount: Int get() = names.size

    /** O quadro pedido ou, enquanto ele não carrega (ou se o arquivo é inválido), o carregado mais próximo. */
    override fun frame(index: Int): ImageBitmap? {
        loadedTick
        cache.get(index)?.let { return it }
        val loaded = cache.snapshot()
        val nearest = loaded.keys.minByOrNull { abs(it - index) } ?: return null
        return loaded[nearest]
    }

    override fun prefetch(center: Int) {
        wanted = center
        for (i in (center - NEIGHBORS)..(center + NEIGHBORS)) {
            if (i !in names.indices) continue
            if (cache.get(i) != null || i in failed || !loading.add(i)) continue
            scope.launch(decoder) {
                // O dedo já foi para longe deste quadro: não vale decodificar.
                if (abs(i - wanted) > NEIGHBORS) {
                    loading.remove(i)
                    return@launch
                }
                var outOfMemory = false
                val bitmap = try {
                    assets.open("$FOLDER/${names[i]}").use { BitmapFactory.decodeStream(it) }
                } catch (e: OutOfMemoryError) {
                    outOfMemory = true
                    null
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    when {
                        bitmap != null -> cache.put(i, bitmap.asImageBitmap())
                        // Falta de memória é passageira; só arquivo inválido fica marcado.
                        !outOfMemory -> failed.add(i)
                    }
                    loading.remove(i)
                    loadedTick++
                }
            }
        }
    }

    init {
        if (names.isNotEmpty()) prefetch(0)
    }

    private companion object {
        const val FOLDER = "titan"
        const val NEIGHBORS = 2
        const val CACHE_SIZE = NEIGHBORS * 2 + 1
    }
}
