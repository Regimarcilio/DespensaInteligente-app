package br.com.despensa

import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.presentation.estoque.montarItemCadastro
import br.com.despensa.presentation.estoque.persistirItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #21 (bug crítico: Salvar não persistia).
 * Classe do defeito: falha de DAO engolida em silêncio + diálogo fechando
 * como se tivesse salvo. Contratos novos: [montarItemCadastro] valida em
 * puro e [persistirItem] devolve Result (falha vira sinal, nunca silêncio).
 * DAO fake em memória — sem Android/Room.
 */
class CadastroPersistenciaTest {

    private class FakeEstoqueDao(var falhar: Boolean = false) : EstoqueDao {
        val itens = mutableListOf<EstoqueEntity>()

        override suspend fun upsert(item: EstoqueEntity) {
            if (falhar) throw IllegalStateException("db travado (simula aparelho)")
            itens.removeAll { it.id == item.id }
            itens.add(item)
        }

        override suspend fun remover(id: String) {
            itens.removeAll { it.id == id }
        }

        override fun observarTodas(busca: String): Flow<List<EstoqueEntity>> =
            flowOf(itens.toList())

        override fun observarPorLocal(
            busca: String,
            local: LocalEstoque
        ): Flow<List<EstoqueEntity>> = flowOf(itens.toList())

        override suspend fun buscarPorId(id: String): EstoqueEntity? =
            itens.firstOrNull { it.id == id }

        override suspend fun listarTudo(): List<EstoqueEntity> = itens.toList()
    }

    @Test
    fun `item valido monta entidade com nome aparado`() {
        val res = montarItemCadastro(
            "  Leite Integral  ", 2.0, "L", null, LocalEstoque.GELADEIRA, 4.99
        )
        assertTrue(res.isSuccess)
        val item = res.getOrThrow()
        assertEquals("Leite Integral", item.nome)
        assertEquals(2.0, item.quantidade)
        assertEquals(LocalEstoque.GELADEIRA, item.local)
    }

    @Test
    fun `nome em branco nao monta item`() {
        val res = montarItemCadastro("   ", 1.0, "un", null, LocalEstoque.DESPENSA, null)
        assertTrue(res.isFailure)
        assertEquals("Nome obrigatório", res.exceptionOrNull()?.message)
    }

    @Test
    fun `qtd zerada nao monta item`() {
        val res = montarItemCadastro("Iogurte", 0.0, "un", null, LocalEstoque.DESPENSA, null)
        assertTrue(res.isFailure)
    }

    @Test
    fun `persistir guarda no DAO e e visivel no listar`() = runBlocking {
        val dao = FakeEstoqueDao()
        val item = montarItemCadastro("Arroz", 1.0, "pct", null, LocalEstoque.DESPENSA, null)
            .getOrThrow()
        val res = persistirItem(dao, item)
        assertTrue(res.isSuccess)
        assertEquals(listOf(item), dao.listarTudo())
    }

    @Test
    fun `falha de DAO vira failure nunca silencio (regressao 21)`() = runBlocking {
        val dao = FakeEstoqueDao(falhar = true)
        val item = montarItemCadastro("Feijão", 1.0, "kg", null, LocalEstoque.DESPENSA, null)
            .getOrThrow()
        val res = persistirItem(dao, item)
        // Antes do fix: exceção engolida e diálogo fechava como sucesso.
        // Agora: failure explícito → diálogo fica aberto + msg (AC1).
        assertTrue(res.isFailure)
        assertTrue(dao.listarTudo().isEmpty())
    }
}
