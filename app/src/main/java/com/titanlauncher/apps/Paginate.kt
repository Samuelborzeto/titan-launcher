package com.titanlauncher.apps

data class AppEntry(val label: String, val packageName: String, val activityName: String)

/** Distribui os apps em [pages] páginas de até [perPage] ícones. Sempre devolve [pages] listas. */
fun paginate(apps: List<AppEntry>, pages: Int = 3, perPage: Int = 20): List<List<AppEntry>> {
    val chunks = apps.take(pages * perPage).chunked(perPage)
    return List(pages) { chunks.getOrElse(it) { emptyList() } }
}
