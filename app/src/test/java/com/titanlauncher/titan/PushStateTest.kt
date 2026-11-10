package com.titanlauncher.titan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushStateTest {
    @Test fun parado_fica_oculto() = assertFalse(computePushState(1f, 1, 3, false, false).visible)

    @Test fun deslize_para_esquerda_vem_da_direita() {
        val s = computePushState(1.35f, 1, 3, true, false)
        assertTrue(s.visible)
        assertEquals(PushSide.FromRight, s.side)
        assertEquals(0.35f, s.progress, 0.001f)
        assertFalse(s.holding)
    }

    @Test fun deslize_para_direita_vem_da_esquerda() {
        val s = computePushState(0.8f, 1, 3, true, false)
        assertEquals(PushSide.FromLeft, s.side)
        assertEquals(0.2f, s.progress, 0.001f)
    }

    @Test fun dedo_parado_no_meio_segura() = assertTrue(computePushState(1.5f, 1, 3, true, true).holding)

    @Test fun soltar_nao_segura() = assertFalse(computePushState(1.5f, 1, 3, false, true).holding)

    @Test fun segunda_pagina_seguida_recomeca_o_progresso() {
        val s = computePushState(1.4f, 0, 3, true, false)
        assertTrue(s.visible)
        assertEquals(PushSide.FromRight, s.side)
        assertEquals(0.4f, s.progress, 0.001f)
    }

    @Test fun segunda_pagina_seguida_voltando() {
        val s = computePushState(0.7f, 2, 3, true, false)
        assertEquals(PushSide.FromLeft, s.side)
        assertEquals(0.3f, s.progress, 0.001f)
    }

    @Test fun chegada_exata_na_pagina_seguinte_e_progresso_1() =
        assertEquals(1f, computePushState(2f, 1, 3, false, false).progress, 0.001f)

    @Test fun dedo_em_movimento_nao_segura() =
        assertFalse(computePushState(1.5f, 1, 3, true, false).holding)

    @Test fun oculto_antes_da_primeira_pagina() =
        assertFalse(computePushState(-0.2f, 0, 3, true, false).visible)

    @Test fun oculto_depois_da_ultima_pagina() =
        assertFalse(computePushState(2.2f, 2, 3, true, false).visible)

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
