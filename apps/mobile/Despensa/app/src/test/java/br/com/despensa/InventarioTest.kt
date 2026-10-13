package br.com.despensa

import br.com.despensa.domain.alcancaBuscaInventario
import br.com.despensa.domain.ItemLancado
import br.com.despensa.domain.compararInventario
import br.com.despensa.domain.contarDivergencias
import br.com.despensa.domain.dataResumidaInventario
import br.com.despensa.domain.gerarIdInventario
import br.com.despensa.domain.gerarTextoInventario
import br.com.despensa.domain.mensagemRemocaoInventario
import br.com.despensa.domain.numeroInventario
import br.com.despensa.domain.resumoContagem
import br.com.despensa.domain.resumoInventario
import br.com.despensa.domain.textoComparado
import br.com.despensa.presentation.inventario.SUBTITULO_INVENTARIO
import br.com.despensa.presentation.inventario.descricaoCardInventario
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #26 (JVM): fechar = comparar contado x lançado, salvar com ID/data,
 * compartilhar como texto. Regras de filtro/validade/RN-003/exportação
 * intactas (nenhum teste existente muda).
 */
class InventarioTest {
    private val itens = listOf(
        ItemLancado("m", "Manteiga com Sal", 1.0, "pote"),
        ItemLancado("q", "Queijo Muçarela", 1.0, "pct"),
        ItemLancado("l", "Leite Integral", 2.0, "L")
    )

    @Test fun `intocado conta como lancado sem divergencia`() {
        val divs = compararInventario(itens, emptyMap())
        assertEquals(3, divs.size)
        assertEquals(0, contarDivergencias(divs))
        assertTrue(divs.all { !it.diverge && it.contado == it.lancado })
    }

    @Test fun `contado diferente diverge`() {
        val divs = compararInventario(itens, mapOf("q" to 0.0))
        assertEquals(1, contarDivergencias(divs))
        val queijo = divs.first { it.id == "q" }
        assertTrue(queijo.diverge)
        assertEquals(1.0, queijo.lancado)
        assertEquals(0.0, queijo.contado)
    }

    @Test fun `contado negativo cai para o lancado`() {
        val divs = compararInventario(itens, mapOf("m" to -2.0))
        assertFalse(divs.first { it.id == "m" }.diverge)
    }

    @Test fun `igual mostra igual sem prefixos`() {
        val divs = compararInventario(itens, mapOf("m" to 1.0))
        assertEquals("1 = 1", textoComparado(divs.first { it.id == "m" }))
    }

    @Test fun `divergente mostra lancado seta contado`() {
        val divs = compararInventario(itens, mapOf("q" to 0.0))
        assertEquals("1 → 0", textoComparado(divs.first { it.id == "q" }))
    }

    @Test fun `id tem ano e seq com 3 digitos`() {
        assertEquals("INV-2026-007", gerarIdInventario(2026, 7))
        assertEquals("INV-2026-123", gerarIdInventario(2026, 123))
    }

    @Test fun `numero do titulo tem hashtag e 3 digitos`() {
        assertEquals("#007", numeroInventario(7))
    }

    @Test fun `resumo abrevia a data`() {
        assertEquals("12/10", dataResumidaInventario("12/10/2026"))
    }

    @Test fun `resumo final tem id data total e divergencias`() {
        assertEquals(
            "ID INV-2026-007 • 12/10 • 3 itens • 1 divergência",
            resumoInventario("INV-2026-007", "12/10", 3, 1)
        )
    }

    @Test fun `resumo pluraliza item e divergencia`() {
        assertEquals("1 item • 0 divergências", resumoContagem(1, 0))
        assertEquals("3 itens • 2 divergências", resumoContagem(3, 2))
    }

    @Test fun `texto de share tem cabecalho linhas e resumo`() {
        val divs = compararInventario(itens, mapOf("q" to 0.0))
        val texto = gerarTextoInventario("INV-2026-007", "12/10/2026", divs)
        assertTrue(texto.contains("INV-2026-007"))
        assertTrue(texto.contains("12/10/2026"))
        assertTrue(texto.contains("Queijo Muçarela"))
        assertTrue(texto.contains("1 → 0"))
        assertTrue(texto.contains("1 divergência"))
    }

    @Test fun `busca vazia alcanca tudo e com texto filtra sem case`() {
        assertTrue(alcancaBuscaInventario("Manteiga com Sal", ""))
        assertTrue(alcancaBuscaInventario("Manteiga com Sal", "manteiga"))
        assertTrue(alcancaBuscaInventario("Queijo Muçarela", "  QUEIJO "))
        assertFalse(alcancaBuscaInventario("Leite Integral", "manteiga"))
    }

    @Test fun `mensagem de remocao cita estoque e inventario`() {
        val m = mensagemRemocaoInventario("Arroz")
        assertTrue(m.contains("Estoque"))
        assertTrue(m.contains("inventário"))
        assertTrue(m.contains("Arroz"))
    }

    @Test fun `card inventario tem utilidade e total no talkback`() {
        assertTrue(SUBTITULO_INVENTARIO.isNotBlank())
        val d = descricaoCardInventario(3)
        assertTrue(d.contains("Inventário"))
        assertTrue(d.contains(SUBTITULO_INVENTARIO))
        assertTrue(d.contains("3 itens no estoque"))
    }

    @Test fun `mensagem de remocao apara espacos`() {
        assertTrue(mensagemRemocaoInventario("  Leite  ").contains("\"Leite\""))
    }
}
