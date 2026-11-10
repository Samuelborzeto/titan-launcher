package com.titanlauncher.titan

import org.junit.Assert.assertEquals
import org.junit.Test

class FrameNamesTest {
    @Test fun ordena_e_filtra() = assertEquals(
        listOf("push_01.webp", "push_03.webp", "push_10.webp"),
        sortedFrameNames(listOf("push_10.webp", "leia.txt", "push_03.webp", ".gitkeep", "push_01.webp")),
    )

    @Test fun ordem_numerica_sem_zero_a_esquerda() = assertEquals(
        listOf("push_2.webp", "push_10.webp"),
        sortedFrameNames(listOf("push_10.webp", "push_2.webp")),
    )

    @Test fun pasta_vazia() = assertEquals(emptyList<String>(), sortedFrameNames(listOf(".gitkeep")))
}
