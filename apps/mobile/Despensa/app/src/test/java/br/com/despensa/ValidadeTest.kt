package br.com.despensa

import br.com.despensa.domain.StatusValidade
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.statusValidade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class ValidadeTest {
    private val zona = ZoneId.systemDefault()
    private fun epoch(d: LocalDate): Long = d.atStartOfDay(zona).toInstant().toEpochMilli()
    private fun hoje(): Long = System.currentTimeMillis()
    private fun hojeData(): LocalDate = LocalDate.now(zona)

    @Test fun `null vira sem validade`() {
        assertEquals(StatusValidade.SEM_VALIDADE, statusValidade(null, hoje()))
        assertNull(diasParaVencer(null, hoje()))
    }

    @Test fun `ontem esta vencido`() {
        val ontem = epoch(hojeData().minusDays(1))
        assertEquals(StatusValidade.VENCIDO, statusValidade(ontem, hoje()))
        assertEquals(-1, diasParaVencer(ontem, hoje()))
    }

    @Test fun `hoje nao esta vencido - atencao`() {
        val hoje00h = epoch(hojeData())
        assertEquals(StatusValidade.ATENCAO, statusValidade(hoje00h, hoje()))
        assertEquals(0, diasParaVencer(hoje00h, hoje()))
    }

    @Test fun `3 dias e atencao (borda)`() {
        val d3 = epoch(hojeData().plusDays(3))
        assertEquals(StatusValidade.ATENCAO, statusValidade(d3, hoje()))
        assertEquals(3, diasParaVencer(d3, hoje()))
    }

    @Test fun `4 dias e fresco (borda)`() {
        val d4 = epoch(hojeData().plusDays(4))
        assertEquals(StatusValidade.FRESCO, statusValidade(d4, hoje()))
        assertEquals(4, diasParaVencer(d4, hoje()))
    }

    @Test fun `30 dias e fresco`() {
        val d30 = epoch(hojeData().plusDays(30))
        assertEquals(StatusValidade.FRESCO, statusValidade(d30, hoje()))
    }

    @Test fun `hoje meio-dia ainda conta como hoje`() {
        val meioDia = epoch(hojeData()) + 12 * 60 * 60 * 1000L
        assertEquals(StatusValidade.ATENCAO, statusValidade(meioDia, hoje()))
        assertEquals(0, diasParaVencer(meioDia, hoje()))
    }
}
