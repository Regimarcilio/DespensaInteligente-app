package br.com.despensa

import br.com.despensa.presentation.estoque.dataDaqui
import br.com.despensa.presentation.estoque.epochParaBr
import br.com.despensa.presentation.estoque.formatarValidadeAmigavel
import br.com.despensa.presentation.estoque.parseDataBr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Paridade web v2.0 (formatarDiasRestantes/atalhos): textos amigáveis e
 * ida-e-volta da data. Puras — sem Android/Room.
 */
class ValidadeTextoTest {

    @Test
    fun textosAmigaveis() {
        assertEquals("Sem validade cadastrada", formatarValidadeAmigavel(null))
        assertEquals("Venceu há 5 dias", formatarValidadeAmigavel(-5))
        assertEquals("Venceu ontem", formatarValidadeAmigavel(-1))
        assertEquals("Vence hoje!", formatarValidadeAmigavel(0))
        assertEquals("Vence amanhã", formatarValidadeAmigavel(1))
        assertEquals("Vence em 2 dias", formatarValidadeAmigavel(2))
        assertEquals("Vence em 30 dias", formatarValidadeAmigavel(30))
    }

    @Test
    fun atalhoHojeConfereComDataAtual() {
        val hoje = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        assertEquals(hoje, dataDaqui(0))
    }

    @Test
    fun epochIdaEVolta() {
        val epoch = parseDataBr("25/12/2026")
        assertEquals("25/12/2026", epochParaBr(epoch!!))
    }
}
