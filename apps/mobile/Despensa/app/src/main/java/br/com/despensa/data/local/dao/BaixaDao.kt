package br.com.despensa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.despensa.data.local.entity.BaixaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BaixaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(baixa: BaixaEntity)

    /** Total R$ perdido no mês (soma DESCARTADO desde o 1º dia do mês, 00h local). */
    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'DESCARTADO' AND ts >= :inicioMes")
    fun perdidosNoMes(inicioMes: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'DESCARTADO' AND ts >= :inicioMes")
    suspend fun perdidosNoMesSync(inicioMes: Long): Double

    /** Total R$ salvo no mês (soma CONSUMIDO desde o 1º dia do mês). Espelho de [perdidosNoMes]. */
    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'CONSUMIDO' AND ts >= :inicioMes")
    fun consumidosNoMes(inicioMes: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'CONSUMIDO' AND ts >= :inicioMes")
    suspend fun consumidosNoMesSync(inicioMes: Long): Double

    /**
     * Última baixa CONSUMIDO por nome desde [desde] (sugestões US06: nome + última
     * qtd/un/preço). Subquery com MAX(ts) em vez de window function p/ rodar no
     * SQLite antigo (minSdk 26). Empates de ts duplicam — o ViewModel aplica
     * distinctBy(nome) defensivo.
     */
    @Query(
        "SELECT * FROM baixa b WHERE motivo = 'CONSUMIDO' AND ts >= :desde " +
            "AND ts = (SELECT MAX(ts) FROM baixa WHERE nome = b.nome " +
            "AND motivo = 'CONSUMIDO' AND ts >= :desde) ORDER BY ts DESC"
    )
    suspend fun consumidosRecentes(desde: Long): List<BaixaEntity>

    @Query("DELETE FROM baixa")
    suspend fun limparTudo()

    /** Tudo p/ backup JSON (issue #2): export espelha `EstoqueDao.listarTudo`. */
    @Query("SELECT * FROM baixa ORDER BY ts DESC")
    suspend fun listarTodas(): List<BaixaEntity>

    /**
     * Paridade web v2.0 (EconomiaView Histórico Recente + total acumulado):
     * queries SOMENTE-leitura, sem migração — mesmo padrão dos fluxos do mês.
     */
    @Query("SELECT * FROM baixa ORDER BY ts DESC")
    fun observarTodas(): Flow<List<BaixaEntity>>

    /** Total R$ salvo acumulado (CONSUMIDO sem filtro de mês). */
    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'CONSUMIDO'")
    fun salvosTotal(): Flow<Double>

    /** Total R$ perdido acumulado (DESCARTADO sem filtro de mês). */
    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM baixa WHERE motivo = 'DESCARTADO'")
    fun perdidosTotal(): Flow<Double>
}
