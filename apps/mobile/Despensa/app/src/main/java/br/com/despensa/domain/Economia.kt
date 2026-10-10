package br.com.despensa.domain

import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.MotivoBaixa
import java.util.Calendar

/**
 * Lógica pura da tela R$ (US07). Os Flows do Room ([BaixaDao.consumidosNoMes] /
 * [BaixaDao.perdidosNoMes]) alimentam a UI; estas funções espelham a mesma soma
 * sobre listas em memória p/ testes unitários sem Android/Room.
 */

/** Soma CONSUMIDO (R$ salvos no mês). */
fun salvosNaLista(baixas: List<BaixaEntity>): Double =
    baixas.filter { it.motivo == MotivoBaixa.CONSUMIDO }.sumOf { it.valorTotal }

/** Soma DESCARTADO (R$ perdidos no mês). */
fun perdidosNaLista(baixas: List<BaixaEntity>): Double =
    baixas.filter { it.motivo == MotivoBaixa.DESCARTADO }.sumOf { it.valorTotal }

/** 1º dia do mês 00h local — mesmo marco usado nas queries do [BaixaDao]. */
fun inicioDoMes(agora: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = agora
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

/** Fração consumida p/ a barra consumido×descartado (0..1; 0 quando nada lançado). */
fun proporcaoConsumida(consumido: Double, descartado: Double): Float {
    val total = consumido + descartado
    if (total <= 0) return 0f
    return (consumido / total).toFloat().coerceIn(0f, 1f)
}
