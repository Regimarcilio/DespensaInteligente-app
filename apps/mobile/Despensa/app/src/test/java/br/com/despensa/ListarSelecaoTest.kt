package br.com.despensa

import br.com.despensa.domain.podeAdicionarNaLista
import br.com.despensa.presentation.estoque.SUBTITULO_LISTAR
import br.com.despensa.presentation.estoque.descricaoCardListar
import br.com.despensa.presentation.estoque.mensagemRemocaoListar
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #10 (JVM): adicionar o item na Lista de Compras.
 * AC: tocar "+" abre a tela; adicionar la coloca na Lista (sem duplicar,
 * sem vazio, sem mudar RN-003).
 */
class ListarSelecaoTest {
    @Test fun `nome novo pode entrar`() {
        assertTrue(podeAdicionarNaLista(listOf("Arroz", "Leite"), "Café"))
    }

    @Test fun `duplicado case-insensitive nao entra`() {
        assertFalse(podeAdicionarNaLista(listOf("Arroz"), "arroz"))
        assertFalse(podeAdicionarNaLista(listOf("  Leite  "), "leite"))
    }

    @Test fun `vazio ou branco nao entra`() {
        assertFalse(podeAdicionarNaLista(emptyList(), ""))
        assertFalse(podeAdicionarNaLista(emptyList(), "   "))
    }

    @Test fun `lista vazia aceita primeiro item`() {
        assertTrue(podeAdicionarNaLista(emptyList(), "Sal"))
    }

    // Issue #16 (JVM): card Listar tem identificação de utilidade.
    @Test fun `subtitulo explica a utilidade sem tocar`() {
        assertTrue(SUBTITULO_LISTAR.contains("Lista de Compras"))
    }

    @Test fun `descricao do card singular cita utilidade e total`() {
        val d = descricaoCardListar(1)
        assertTrue(d.contains(SUBTITULO_LISTAR))
        assertTrue(d.contains("1 item no estoque"))
    }

    // Issue #18 (JVM): remover com confirmação sai do Estoque e do Listar.
    @Test fun `mensagem de remocao cita estoque e listar`() {
        val m = mensagemRemocaoListar("Arroz")
        assertTrue(m.contains("Estoque"))
        assertTrue(m.contains("Listar"))
        assertTrue(m.contains("Arroz"))
    }

    @Test fun `mensagem de remocao apara espacos`() {
        val m = mensagemRemocaoListar("  Leite  ")
        assertTrue(m.contains("\"Leite\""))
    }

    @Test fun `descricao do card plural cita utilidade e total`() {
        val d = descricaoCardListar(3)
        assertTrue(d.contains(SUBTITULO_LISTAR))
        assertTrue(d.contains("3 itens no estoque"))
    }
}
