package br.com.despensa

import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.domain.ItemCarrinho
import br.com.despensa.domain.descricaoItemCarrinho
import br.com.despensa.domain.formatarQtdCarrinho
import br.com.despensa.domain.mapearParaEstoque
import br.com.despensa.domain.mensagemFinalizarCarrinho
import br.com.despensa.domain.podeAdicionarNaLista
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #36 (JVM, modo supermercado): Lista → carrinho → Estoque.
 * AC1: add ao carrinho + salvar na edição padrão = aparece no carrinho
 * (ícone no header). AC2: marcar comprados + finalizar = entram no Estoque
 * como disponíveis. Sem regra nova de validade, sem tocar RN-003.
 */
class CarrinhoTest {

    @Test fun `novo item nasce comprado (finalizar direto)`() {
        assertTrue(ItemCarrinho(nome = "Arroz").comprado)
    }

    @Test fun `vazio ou branco nao entra (mesma regra da Lista)`() {
        assertFalse(podeAdicionarNaLista(listOf("Arroz"), ""))
        assertFalse(podeAdicionarNaLista(listOf("Arroz"), "   "))
    }

    @Test fun `duplicado case-insensitive nao entra (mesma regra da Lista)`() {
        assertFalse(podeAdicionarNaLista(listOf("Arroz"), "arroz"))
        assertFalse(podeAdicionarNaLista(listOf("  Leite  "), "leite"))
    }

    @Test fun `nome novo pode entrar`() {
        assertTrue(podeAdicionarNaLista(listOf("Arroz"), "Café"))
        assertTrue(podeAdicionarNaLista(emptyList(), "Sal"))
    }

    @Test fun `mapear repassa validade direto sem regra nova (null segue null)`() {
        val item = ItemCarrinho(nome = "Leite", validade = null)
        assertNull(mapearParaEstoque(item).validade)
    }

    @Test fun `mapear repassa validade direto sem regra nova (data intacta)`() {
        val validade = 1_800_000_000_000L
        val item = ItemCarrinho(nome = "Iogurte", validade = validade)
        assertEquals(validade, mapearParaEstoque(item).validade)
    }

    @Test fun `mapear preserva campos e apara o nome`() {
        val item = ItemCarrinho(
            nome = "  Café  ",
            quantidade = 2.0,
            unidade = "pct",
            local = LocalEstoque.GELADEIRA,
            precoRef = 4.99,
            fotoPath = "/tmp/foto.webp"
        )
        val estoque = mapearParaEstoque(item)
        assertEquals("Café", estoque.nome)
        assertEquals(2.0, estoque.quantidade)
        assertEquals("pct", estoque.unidade)
        assertEquals(LocalEstoque.GELADEIRA, estoque.local)
        assertEquals(4.99, estoque.precoRef!!, 0.001)
        assertEquals("/tmp/foto.webp", estoque.fotoPath)
    }

    @Test fun `finalizar leva so os comprados (selecao respeitada)`() {
        val itens = listOf(
            ItemCarrinho(nome = "Arroz", comprado = true),
            ItemCarrinho(nome = "Feijão", comprado = false)
        )
        val alvos = itens.filter { it.comprado }
        assertEquals(listOf("Arroz"), alvos.map { it.nome })
        assertEquals(listOf("Arroz"), alvos.map { mapearParaEstoque(it).nome })
    }

    @Test fun `descricao cita nome qtd preco e estado`() {
        val d = descricaoItemCarrinho(
            ItemCarrinho(nome = "Arroz", quantidade = 2.0, unidade = "kg", precoRef = 5.0)
        )
        assertTrue(d.contains("Arroz"))
        assertTrue(d.contains("2 kg"))
        assertTrue(d.contains("comprado"))
    }

    @Test fun `descricao sem preco omite cifra e narra nao comprado`() {
        val d = descricaoItemCarrinho(
            ItemCarrinho(nome = "Sal", comprado = false)
        )
        assertFalse(d.contains("R$"))
        assertTrue(d.contains("não comprado"))
    }

    @Test fun `qtd inteira sem decimal fracionada com ponto`() {
        assertEquals("2", formatarQtdCarrinho(2.0))
        assertEquals("1.5", formatarQtdCarrinho(1.5))
    }

    @Test fun `mensagem singular e plural`() {
        assertTrue(mensagemFinalizarCarrinho(1).contains("1 item no Estoque"))
        assertTrue(mensagemFinalizarCarrinho(3).contains("3 itens no Estoque"))
    }
}
