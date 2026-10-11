package br.com.despensa

import br.com.despensa.domain.podeAdicionarNaLista
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
}
