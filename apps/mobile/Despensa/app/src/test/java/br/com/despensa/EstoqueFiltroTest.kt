package br.com.despensa

import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.domain.StatusValidade
import br.com.despensa.presentation.estoque.aplicarFiltroStatus
import br.com.despensa.presentation.estoque.contarValidade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Issue #14 (JVM): destrava a lista — filtro em memória nunca congela.
 * AC: com itens → renderiza (filtro devolve subconjunto sem exceção);
 * sem itens → vazio amigável (nunca trava).
 */
class EstoqueFiltroTest {
    private val zona = ZoneId.systemDefault()
    private fun epoch(d: LocalDate): Long = d.atStartOfDay(zona).toInstant().toEpochMilli()
    private fun hojeData(): LocalDate = LocalDate.now(zona)
    private fun agora(): Long = System.currentTimeMillis()

    private fun item(nome: String, validade: Long?): EstoqueEntity = EstoqueEntity(
        nome = nome,
        quantidade = 1.0,
        unidade = "un",
        validade = validade,
        local = LocalEstoque.DESPENSA
    )

    private fun massa(): List<EstoqueEntity> {
        val hoje = hojeData()
        return listOf(
            item("Fresco", epoch(hoje.plusDays(30))),
            item("Atencao", epoch(hoje.plusDays(1))),
            item("Vencido", epoch(hoje.minusDays(2))),
            item("SemVal", null)
        )
    }

    @Test fun `sem filtro devolve todos (nunca trava)`() {
        val lista = massa()
        assertEquals(4, aplicarFiltroStatus(lista, null, agora()).size)
    }

    @Test fun `filtro atencao devolve so atencao`() {
        val r = aplicarFiltroStatus(massa(), StatusValidade.ATENCAO, agora())
        assertEquals(listOf("Atencao"), r.map { it.nome })
    }

    @Test fun `filtro vencido devolve so vencidos`() {
        val r = aplicarFiltroStatus(massa(), StatusValidade.VENCIDO, agora())
        assertEquals(listOf("Vencido"), r.map { it.nome })
    }

    @Test fun `filtro fresco devolve so frescos`() {
        val r = aplicarFiltroStatus(massa(), StatusValidade.FRESCO, agora())
        assertEquals(listOf("Fresco"), r.map { it.nome })
    }

    @Test fun `lista vazia nunca congela — vazio em todo filtro`() {
        val vazia = emptyList<EstoqueEntity>()
        assertTrue(aplicarFiltroStatus(vazia, null, agora()).isEmpty())
        assertTrue(aplicarFiltroStatus(vazia, StatusValidade.ATENCAO, agora()).isEmpty())
        assertTrue(aplicarFiltroStatus(vazia, StatusValidade.VENCIDO, agora()).isEmpty())
        assertTrue(aplicarFiltroStatus(vazia, StatusValidade.FRESCO, agora()).isEmpty())
    }

    @Test fun `contadores batem com o semaforo (sem-validade fora)`() {
        val c = contarValidade(massa(), agora())
        assertEquals(1, c.frescos)
        assertEquals(1, c.atencao)
        assertEquals(1, c.vencidos)
    }

    @Test fun `contadores zerados sem itens`() {
        val c = contarValidade(emptyList(), agora())
        assertEquals(0, c.frescos)
        assertEquals(0, c.atencao)
        assertEquals(0, c.vencidos)
    }
}
