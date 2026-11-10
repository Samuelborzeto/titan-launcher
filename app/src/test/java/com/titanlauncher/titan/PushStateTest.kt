package com.titanlauncher.titan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushStateTest {
    @Test fun parado_fica_oculto() = assertFalse(computePushState(1f, 1, 3, false, 0f).visible)

    @Test fun deslize_para_esquerda_vem_da_direita() {
        val s = computePushState(1.35f, 1, 3, true, 900f)
        assertTrue(s.visible)
        assertEquals(PushSide.FromRight, s.side)
        assertEquals(0.35f, s.progress, 0.001f)
        assertFalse(s.holding)
    }

    @Test fun deslize_para_direita_vem_da_esquerda() {
        val s = computePushState(0.8f, 1, 3, true, -900f)
        assertEquals(PushSide.FromLeft, s.side)
        assertEquals(0.2f, s.progress, 0.001f)
    }

    @Test fun dedo_parado_no_meio_segura() = assertTrue(computePushState(1.5f, 1, 3, true, 10f).holding)

    @Test fun soltar_nao_segura() = assertFalse(computePushState(1.5f, 1, 3, false, 0f).holding)

    @Test fun progresso_limitado_a_1_ao_atravessar_2_paginas() =
        assertEquals(1f, computePushState(2.4f, 0, 3, true, 2000f).progress, 0.001f)

    @Test fun oculto_antes_da_primeira_pagina() =
        assertFalse(computePushState(-0.2f, 0, 3, true, -500f).visible)

    @Test fun oculto_depois_da_ultima_pagina() =
        assertFalse(computePushState(2.2f, 2, 3, true, 500f).visible)

    @Test fun quadro_segue_progresso() {
        assertEquals(0, frameIndex(0f, 24))
        assertEquals(12, frameIndex(0.5f, 24))
        assertEquals(23, frameIndex(1f, 24))
    }

    @Test fun quadro_com_0_ou_1_imagem() {
        assertEquals(0, frameIndex(0.7f, 0))
        assertEquals(0, frameIndex(0.7f, 1))
    }
}
