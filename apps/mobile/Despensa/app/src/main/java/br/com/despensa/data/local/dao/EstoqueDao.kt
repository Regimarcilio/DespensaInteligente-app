package br.com.despensa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import kotlinx.coroutines.flow.Flow

@Dao
interface EstoqueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: EstoqueEntity)

    @Query("DELETE FROM estoque WHERE id = :id")
    suspend fun remover(id: String)

    /**
     * Issue #14: queries null-safe separadas. A query antiga
     * `(:local IS NULL OR local = :local)` amarrava um param nullable ao
     * TypeConverter nao-nulo de LocalEstoque — em alguns aparelhos o bind
     * de null derrubava o Flow (lista congelada/vazia). Sem migracao:
     * mesma tabela, mesma ordem; o ViewModel escolhe qual observar.
     */
    @Query(
        "SELECT * FROM estoque " +
            "WHERE nome LIKE '%' || :busca || '%' " +
            "ORDER BY CASE WHEN validade IS NULL THEN 1 ELSE 0 END, validade ASC, nome ASC"
    )
    fun observarTodas(busca: String): Flow<List<EstoqueEntity>>

    @Query(
        "SELECT * FROM estoque " +
            "WHERE local = :local " +
            "AND nome LIKE '%' || :busca || '%' " +
            "ORDER BY CASE WHEN validade IS NULL THEN 1 ELSE 0 END, validade ASC, nome ASC"
    )
    fun observarPorLocal(busca: String, local: LocalEstoque): Flow<List<EstoqueEntity>>

    @Query("SELECT * FROM estoque WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): EstoqueEntity?

    @Query("SELECT * FROM estoque ORDER BY CASE WHEN validade IS NULL THEN 1 ELSE 0 END, validade ASC")
    suspend fun listarTudo(): List<EstoqueEntity>
}
