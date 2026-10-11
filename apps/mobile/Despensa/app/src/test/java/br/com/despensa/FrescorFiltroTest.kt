package br.com.despensa

import br.com.despensa.domain.StatusValidade
import br.com.despensa.presentation.estoque.proximoFiltroStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Issue #15 (JVM): toque nos blocos filtra (toggle, §4.2).
 * AC: Atenção → só atenção; Vencido → só vencidos; limpar → todos.
 * A parte "só X" é [EstoqueFiltroTest.aplicarFiltroStatus]; aqui o vaivém.
 */
class FrescorFiltroTest {
    @Test fun `tocar bloco inativo ativa`() {
        assertEquals(StatusValidade.ATENCAO, proximoFiltroStatus(null, StatusValidade.ATENCAO))
        assertEquals(StatusValidade.FRESCO, proximoFiltroStatus(null, StatusValidade.FRESCO))
        assertEquals(StatusValidade.VENCIDO, proximoFiltroStatus(null, StatusValidade.VENCIDO))
    }

    @Test fun `tocar bloco ativo limpa`() {
        assertNull(proximoFiltroStatus(StatusValidade.ATENCAO, StatusValidade.ATENCAO))
        assertNull(proximoFiltroStatus(StatusValidade.VENCIDO, StatusValidade.VENCIDO))
        assertNull(proximoFiltroStatus(StatusValidade.FRESCO, StatusValidade.FRESCO))
    }

    @Test fun `tocar outro bloco troca`() {
        assertEquals(
            StatusValidade.VENCIDO,
            proximoFiltroStatus(StatusValidade.ATENCAO, StatusValidade.VENCIDO)
        )
        assertEquals(
            StatusValidade.FRESCO,
            proximoFiltroStatus(StatusValidade.VENCIDO, StatusValidade.FRESCO)
        )
    }

    @Test fun `limpar volta a todos`() {
        assertNull(proximoFiltroStatus(StatusValidade.ATENCAO, null))
        assertNull(proximoFiltroStatus(null, null))
    }
}
