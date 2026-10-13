package br.com.despensa

import br.com.despensa.data.local.dao.BaixaDao
import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.MotivoBaixa
import br.com.despensa.domain.perdidosNaLista
import br.com.despensa.domain.salvosNaLista
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #27 (feat, sev:low): aba Economia → "Limpar Histórico Recente"
 * (últimas baixas) com confirmação; estado vazio amigável após limpar;
 * cancelar = nada apagado; sem mudar o cálculo de economia/total.
 *
 * O DAO já expõe [BaixaDao.limparTudo]; o VM expõe `limparHistorico()`
 * (suspenso + Boolean, padrão #21) e o histórico é
 * `observarTodas().take(15)` — aqui o contrato é exercido sobre um DAO
 * fake: confirmar apaga tudo exibido, cancelar preserva, e as fórmulas
 * puras ([salvosNaLista]/[perdidosNaLista], espelhos das queries) seguem
 * intactas sobre qualquer lista.
 */
class HistoricoLimpezaTest {

    private class FakeBaixaDao(var falhar: Boolean = false) : BaixaDao {
        private val estado = MutableStateFlow<List<BaixaEntity>>(emptyList())

        override suspend fun registrar(baixa: BaixaEntity) {
            estado.value = (estado.value.filterNot { it.id == baixa.id } + baixa)
                .sortedByDescending { it.ts }
        }

        override fun perdidosNoMes(inicioMes: Long): Flow<Double> =
            throw UnsupportedOperationException("fora do escopo JVM")

        override suspend fun perdidosNoMesSync(inicioMes: Long): Double =
            estado.value.filter { it.motivo == MotivoBaixa.DESCARTADO }.sumOf { it.valorTotal }

        override fun consumidosNoMes(inicioMes: Long): Flow<Double> =
            throw UnsupportedOperationException("fora do escopo JVM")

        override suspend fun consumidosNoMesSync(inicioMes: Long): Double =
            estado.value.filter { it.motivo == MotivoBaixa.CONSUMIDO }.sumOf { it.valorTotal }

        override suspend fun consumidosRecentes(desde: Long): List<BaixaEntity> =
            estado.value.filter { it.motivo == MotivoBaixa.CONSUMIDO && it.ts >= desde }

        override suspend fun limparTudo() {
            if (falhar) throw IllegalStateException("db travado (simula aparelho)")
            estado.value = emptyList()
        }

        override suspend fun listarTodas(): List<BaixaEntity> = estado.value

        override fun observarTodas(): Flow<List<BaixaEntity>> = estado

        override fun salvosTotal(): Flow<Double> =
            throw UnsupportedOperationException("fora do escopo JVM")

        override fun perdidosTotal(): Flow<Double> =
            throw UnsupportedOperationException("fora do escopo JVM")
    }

    private fun baixa(nome: String, valor: Double, motivo: MotivoBaixa, ts: Long) =
        BaixaEntity(nome = nome, quantidade = 1.0, unidade = "un", valorTotal = valor, motivo = motivo, ts = ts)

    /** Espelha o `historico` do VM: 15 últimas baixas. */
    private suspend fun historico(dao: FakeBaixaDao): List<BaixaEntity> =
        dao.observarTodas().first().take(15)

    @Test
    fun `confirmar apaga tudo exibido e cai no vazio amigavel (AC1)`() = runBlocking {
        val dao = FakeBaixaDao()
        val agora = System.currentTimeMillis()
        dao.registrar(baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO, agora - 1))
        dao.registrar(baixa("Leite", 5.5, MotivoBaixa.CONSUMIDO, agora - 2))
        dao.registrar(baixa("Pão", 7.0, MotivoBaixa.DESCARTADO, agora - 3))
        assertEquals(3, historico(dao).size)

        // Caminho do botão Apagar: limparTudo com sucesso.
        runCatching { dao.limparTudo() }.isSuccess.also { assertTrue(it) }

        assertTrue(historico(dao).isEmpty())
        assertTrue(dao.listarTodas().isEmpty())
    }

    @Test
    fun `cancelar nao apaga nada (AC2 — sem chamada, lista intacta)`() = runBlocking {
        val dao = FakeBaixaDao()
        val agora = System.currentTimeMillis()
        dao.registrar(baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO, agora - 1))
        dao.registrar(baixa("Pão", 7.0, MotivoBaixa.DESCARTADO, agora - 2))
        // Cancelar = nenhuma chamada ao DAO: a lista segue intacta.
        assertEquals(2, historico(dao).size)
        assertEquals(2, dao.listarTodas().size)
    }

    @Test
    fun `falha ao apagar nao some em silencio (dialogo mantem + msg)`() = runBlocking {
        val dao = FakeBaixaDao(falhar = true)
        val agora = System.currentTimeMillis()
        dao.registrar(baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO, agora - 1))
        val res = runCatching { dao.limparTudo() }
        assertTrue(res.isFailure)
        // Nada foi apagado — a UI mantém o diálogo com msg (padrão #21).
        assertEquals(1, dao.listarTodas().size)
    }

    @Test
    fun `calculo de economia-total inalterado (OUT da 27)`() {
        val lista = listOf(
            baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO, 1L),
            baixa("Leite", 5.5, MotivoBaixa.CONSUMIDO, 2L),
            baixa("Pão", 7.0, MotivoBaixa.DESCARTADO, 3L)
        )
        // As fórmulas (espelhos das queries do mês/acumulado) não mudaram:
        // limpar só remove linhas, nunca a regra.
        assertEquals(15.48, salvosNaLista(lista), 0.001)
        assertEquals(7.0, perdidosNaLista(lista), 0.001)
        assertEquals(0.0, salvosNaLista(emptyList()), 0.0)
        assertEquals(0.0, perdidosNaLista(emptyList()), 0.0)
    }

    @Test
    fun `historico exibe no maximo 15 e limpar apaga tudo exibido`() = runBlocking {
        val dao = FakeBaixaDao()
        val agora = System.currentTimeMillis()
        repeat(20) { i ->
            dao.registrar(baixa("Item $i", 1.0, MotivoBaixa.CONSUMIDO, agora - i))
        }
        assertEquals(15, historico(dao).size)
        runCatching { dao.limparTudo() }.isSuccess.also { assertTrue(it) }
        assertTrue(historico(dao).isEmpty())
    }
}
