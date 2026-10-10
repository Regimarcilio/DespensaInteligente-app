package br.com.despensa

import br.com.despensa.domain.ItemReposicao
import br.com.despensa.domain.gerarTexto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListaReposicaoTest {

    private val data = "09/10/2026 10:00"

    @Test fun `formato exato com 2 itens mais TOTAL`() {
        val itens = listOf(
            ItemReposicao("Arroz", 2.0, "un", 4.99),
            ItemReposicao("Leite", 1.0, "L", 5.5)
        )
        assertEquals(
            "🛒 Compra semana (09/10/2026 10:00)\n" +
                "• Arroz — 2un x R$ 4,99 = R$ 9,98\n" +
                "• Leite — 1L x R$ 5,50 = R$ 5,50\n" +
                "TOTAL: R$ 15,48",
            gerarTexto("Compra semana", itens, data)
        )
    }

    @Test fun `lista vazia gera so cabecalho e TOTAL zerado`() {
        assertEquals(
            "🛒 Reposição Despensa (09/10/2026 10:00)\nTOTAL: R$ 0,00",
            gerarTexto("Reposição Despensa", emptyList(), data)
        )
    }

    @Test fun `nome em branco ganha default`() {
        assertEquals(
            "🛒 Reposição Despensa (09/10/2026 10:00)\nTOTAL: R$ 0,00",
            gerarTexto("   ", emptyList(), data)
        )
    }

    @Test fun `sem preco sai zerado sem crash`() {
        val itens = listOf(ItemReposicao("Sal", 1.0, "un", null))
        assertEquals(
            "🛒 Reposição Despensa (09/10/2026 10:00)\n" +
                "• Sal — 1un x R$ 0,00 = R$ 0,00\n" +
                "TOTAL: R$ 0,00",
            gerarTexto("Reposição Despensa", itens, data)
        )
    }

    @Test fun `qtd fracionada e unidade em branco`() {
        val itens = listOf(ItemReposicao("Café", 0.5, "  ", 9.9))
        assertEquals(
            "🛒 Reposição Despensa (09/10/2026 10:00)\n" +
                "• Café — 0.5un x R$ 9,90 = R$ 4,95\n" +
                "TOTAL: R$ 4,95",
            gerarTexto("Reposição Despensa", itens, data)
        )
    }

    @Test fun `itens invalidos sao ignorados`() {
        val itens = listOf(
            ItemReposicao("  ", 2.0, "un", 1.0),
            ItemReposicao("Açúcar", 0.0, "kg", 3.0)
        )
        assertEquals(
            "🛒 Reposição Despensa (09/10/2026 10:00)\nTOTAL: R$ 0,00",
            gerarTexto("Reposição Despensa", itens, data)
        )
    }
}
