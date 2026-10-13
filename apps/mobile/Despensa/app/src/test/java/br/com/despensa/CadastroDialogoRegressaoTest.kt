package br.com.despensa

import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.domain.StatusValidade
import br.com.despensa.presentation.estoque.CadastroResolvido
import br.com.despensa.presentation.estoque.aplicarFiltroStatus
import br.com.despensa.presentation.estoque.montarItemCadastro
import br.com.despensa.presentation.estoque.persistirItem
import br.com.despensa.presentation.estoque.resolverCadastroParaSalvar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #21 (reaberta na v3.0 — regressão crítica do diálogo compartilhado
 * "Novo Item na Despensa", usado pelo + do hero, Novo item da Listar e Novo
 * item do Inventário).
 *
 * Contrato sob teste (o MESMO nos 3 pontos, via [resolverCadastroParaSalvar]
 * + [montarItemCadastro] + [persistirItem]):
 * 1. preencher válido + Salvar = persistido e listável (AC1 ≤2s, AC2 reabrir);
 * 2. inválido = erro sem tocar no DAO (diálogo fica aberto, sem perder o digitado);
 * 3. falha de DAO = failure explícito (diálogo fica aberto + msg, nunca silêncio);
 * 4. item salvo sob filtro ativo (Listar filtrada da #25) fica oculto até
 *    limpar busca/filtros — o que o fix faz automaticamente no create.
 */
class CadastroDialogoRegressaoTest {

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
    fun `campos validos resolvem pronto (contrato dos 3 pontos)`() {
        val r = resolverCadastroParaSalvar("Leite Integral", "2", "", "")
        assertTrue(r is CadastroResolvido.Pronto)
        val pronto = r as CadastroResolvido.Pronto
        assertEquals(2.0, pronto.quantidade)
        assertEquals(null, pronto.validade)
        assertEquals(null, pronto.precoRef)
    }

    @Test
    fun `virgula br e data-preco opcionais resolvem`() {
        val r = resolverCadastroParaSalvar("Arroz", "1,5", "25/12/2026", "4,99")
            as CadastroResolvido.Pronto
        assertEquals(1.5, r.quantidade)
        assertTrue(r.validade != null)
        assertEquals(4.99, r.precoRef!!, 0.001)
    }

    @Test
    fun `nome em branco nao toca no DAO (erro antes de salvar)`() {
        val r = resolverCadastroParaSalvar("   ", "1", "", "")
        assertTrue(r is CadastroResolvido.Erro)
        assertEquals("Nome obrigatório", (r as CadastroResolvido.Erro).mensagem)
    }

    @Test
    fun `qtd zerada ou ilegivel nao toca no DAO`() {
        listOf("0", "abc", "").forEach { qtd ->
            val r = resolverCadastroParaSalvar("Iogurte", qtd, "", "")
            assertTrue(r is CadastroResolvido.Erro, "qtd=$qtd")
            assertEquals("Quantidade deve ser > 0", (r as CadastroResolvido.Erro).mensagem)
        }
    }

    @Test
    fun `data invalida nao toca no DAO`() {
        val r = resolverCadastroParaSalvar("Feijão", "1", "99/99/9999", "")
        assertTrue(r is CadastroResolvido.Erro)
        assertEquals("Data inválida (use dd/MM/yyyy)", (r as CadastroResolvido.Erro).mensagem)
    }

    @Test
    fun `preco invalido nao toca no DAO`() {
        listOf("abc", "-2").forEach { preco ->
            val r = resolverCadastroParaSalvar("Café", "1", "", preco)
            assertTrue(r is CadastroResolvido.Erro, "preco=$preco")
            assertEquals("Preço inválido (ex 4,99)", (r as CadastroResolvido.Erro).mensagem)
        }
    }

    @Test
    fun `AC preencher valido + Salvar = persistido e listavel (reproduz 21)`() = runBlocking {
        val dao = FakeEstoqueDao()
        // Caminho idêntico ao botão Salvar do diálogo nos 3 pontos.
        val resolvido = resolverCadastroParaSalvar("  Arroz  ", "1", "", "")
            as CadastroResolvido.Pronto
        val item = montarItemCadastro(
            resolvido.nome, resolvido.quantidade, "pct",
            resolvido.validade, LocalEstoque.DESPENSA, resolvido.precoRef
        ).getOrThrow()
        assertEquals("Arroz", item.nome)
        val res = persistirItem(dao, item)
        assertTrue(res.isSuccess)
        // Visível em seguida e persistido (reabrir = reler do DAO).
        assertEquals(listOf(item), dao.listarTudo())
    }

    @Test
    fun `falha de DAO vira failure — dialogo fica aberto com msg (nunca silencio)`() = runBlocking {
        val dao = FakeEstoqueDao(falhar = true)
        val resolvido = resolverCadastroParaSalvar("Feijão", "1", "", "")
            as CadastroResolvido.Pronto
        val item = montarItemCadastro(
            resolvido.nome, resolvido.quantidade, "kg",
            resolvido.validade, LocalEstoque.DESPENSA, resolvido.precoRef
        ).getOrThrow()
        val res = persistirItem(dao, item)
        assertTrue(res.isFailure)
        assertTrue(dao.listarTudo().isEmpty())
    }

    @Test
    fun `item salvo sob Listar filtrada aparece apos limpar busca e filtros (#25)`() = runBlocking {
        val dao = FakeEstoqueDao()
        val item = montarItemCadastro("Arroz", 1.0, "pct", null, LocalEstoque.DESPENSA, null)
            .getOrThrow()
        assertTrue(persistirItem(dao, item).isSuccess)

        // Espelha o LIKE da query (observarTodas(busca)): com busca ativa que
        // não casa, o item existe no Room mas não aparece — sintoma "não salvou".
        fun visivelPorBusca(lista: List<EstoqueEntity>, busca: String) =
            if (busca.isBlank()) lista
            else lista.filter { it.nome.contains(busca.trim(), ignoreCase = true) }

        val salvos = dao.listarTudo()
        assertTrue(visivelPorBusca(salvos, "leite").isEmpty())
        // Filtro de status ativo também esconde (sem validade ≠ FRESCO)...
        assertTrue(aplicarFiltroStatus(salvos, StatusValidade.FRESCO).isEmpty())
        // ...e limpar busca + filtros (o que o fix faz no create) revela ≤2s.
        assertEquals(listOf(item), visivelPorBusca(salvos, ""))
        assertEquals(listOf(item), aplicarFiltroStatus(salvos, null))
    }
}
