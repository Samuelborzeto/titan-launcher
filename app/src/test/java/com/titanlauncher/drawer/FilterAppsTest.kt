package com.titanlauncher.drawer

import com.titanlauncher.apps.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAppsTest {
    private val lista = listOf(
        AppEntry("Câmera", "c", "c"),
        AppEntry("Calculadora", "k", "k"),
        AppEntry("Mapas", "m", "m"),
    )

    @Test fun vazio_devolve_tudo() = assertEquals(3, filterApps(lista, "  ").size)

    @Test fun ignora_maiusculas_e_acentos() =
        assertEquals(listOf("Câmera"), filterApps(lista, "CAMERA").map { it.label })

    @Test fun busca_no_meio_do_nome() =
        assertEquals(listOf("Calculadora"), filterApps(lista, "lcul").map { it.label })

    @Test fun sem_resultado() = assertEquals(0, filterApps(lista, "xyz").size)
}
