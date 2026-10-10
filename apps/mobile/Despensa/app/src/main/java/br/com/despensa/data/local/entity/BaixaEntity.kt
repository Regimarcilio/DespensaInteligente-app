package br.com.despensa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** Motivo da baixa (RF-003). CONSUMIDO = salvo · DESCARTADO = perdido (soma no "perdido em R$"). */
enum class MotivoBaixa {
    CONSUMIDO,
    DESCARTADO
}

@Entity(tableName = "baixa")
data class BaixaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val quantidade: Double,
    val unidade: String,
    /** qtd × precoRef (0.0 quando sem preço). Base da tela R$ da Sprint 3 (RF-005). */
    val valorTotal: Double = 0.0,
    val motivo: MotivoBaixa,
    val ts: Long = System.currentTimeMillis()
)
