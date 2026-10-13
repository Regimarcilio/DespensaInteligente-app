package br.com.despensa

import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.presentation.estoque.LOCAIS_FILTRO
import br.com.despensa.presentation.estoque.rotuloLocalSelecionado
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Issue #32 (JVM): a suspensa de local tem as mesmas 4 opções dos chips
 * (LOCAIS_FILTRO) e o rótulo reflete o filtro ativo — mesmo comportamento,
 * só mudou o contêiner.
 */
class ListarLocalFiltroTest {

    @Test fun `suspensa tem as 4 opcoes cf LOCAIS_FILTRO`() {
        assertEquals(4, LOCAIS_FILTRO.size)
        assertEquals(
            listOf(null, LocalEstoque.GELADEIRA, LocalEstoque.FREEZER, LocalEstoque.DESPENSA),
            LOCAIS_FILTRO.map { it.second }
        )
    }

    @Test fun `sem filtro rotulo e Todos`() {
        assertEquals("Todos", rotuloLocalSelecionado(null))
    }

    @Test fun `cada local resolve seu rotulo`() {
        assertEquals(
            LOCAIS_FILTRO.first { it.second == LocalEstoque.GELADEIRA }.first,
            rotuloLocalSelecionado(LocalEstoque.GELADEIRA)
        )
        assertEquals(
            LOCAIS_FILTRO.first { it.second == LocalEstoque.FREEZER }.first,
            rotuloLocalSelecionado(LocalEstoque.FREEZER)
        )
        assertEquals(
            LOCAIS_FILTRO.first { it.second == LocalEstoque.DESPENSA }.first,
            rotuloLocalSelecionado(LocalEstoque.DESPENSA)
        )
    }
}
