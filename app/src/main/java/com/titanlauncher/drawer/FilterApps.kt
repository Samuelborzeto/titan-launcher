package com.titanlauncher.drawer

import com.titanlauncher.apps.AppEntry
import java.text.Normalizer

private val ACCENTS = Regex("""\p{Mn}+""")

private fun normalize(text: String): String =
    ACCENTS.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "").lowercase().trim()

/** Busca por nome, sem diferenciar maiúsculas nem acentos. Busca vazia devolve tudo. */
fun filterApps(apps: List<AppEntry>, query: String): List<AppEntry> {
    val q = normalize(query)
    if (q.isEmpty()) return apps
    return apps.filter { normalize(it.label).contains(q) }
}
