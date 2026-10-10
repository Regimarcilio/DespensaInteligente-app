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

    @Query(
        "SELECT * FROM estoque " +
            "WHERE (:local IS NULL OR local = :local) " +
            "AND nome LIKE '%' || :busca || '%' " +
            "ORDER BY CASE WHEN validade IS NULL THEN 1 ELSE 0 END, validade ASC, nome ASC"
    )
    fun observar(busca: String, local: LocalEstoque?): Flow<List<EstoqueEntity>>

    @Query("SELECT * FROM estoque WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): EstoqueEntity?

    @Query("SELECT * FROM estoque ORDER BY CASE WHEN validade IS NULL THEN 1 ELSE 0 END, validade ASC")
    suspend fun listarTudo(): List<EstoqueEntity>
}
