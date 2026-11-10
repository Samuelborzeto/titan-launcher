package com.titanlauncher.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class PaginateTest {
    private fun apps(n: Int) = (1..n).map { AppEntry("App $it", "p$it", "a$it") }

    @Test fun sempre_3_paginas() = assertEquals(3, paginate(apps(5)).size)

    @Test fun enche_20_por_pagina() =
        assertEquals(listOf(20, 20, 5), paginate(apps(45)).map { it.size })

    @Test fun corta_o_excedente() = assertEquals(60, paginate(apps(100)).sumOf { it.size })

    @Test fun lista_menor_apos_desinstalar() =
        assertEquals(listOf(4, 0, 0), paginate(apps(4)).map { it.size })

    @Test fun lista_vazia() = assertEquals(listOf(0, 0, 0), paginate(emptyList()).map { it.size })
}
