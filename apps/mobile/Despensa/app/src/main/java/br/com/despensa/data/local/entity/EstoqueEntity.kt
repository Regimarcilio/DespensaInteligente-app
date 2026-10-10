package br.com.despensa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class LocalEstoque {
    GELADEIRA,
    FREEZER,
    DESPENSA
}

@Entity(tableName = "estoque", indices = [Index("local"), Index("nome"), Index("validade")])
data class EstoqueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val quantidade: Double = 1.0,
    val unidade: String = "un",
    /** epoch millis nullable — null = sem validade (RN-002). */
    val validade: Long? = null,
    val local: LocalEstoque = LocalEstoque.DESPENSA,
    val fotoPath: String? = null,
    /** Preço de referência unitário em R$ (opcional, Sprint 2 prepara tela R$ da Sprint 3). */
    val precoRef: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
