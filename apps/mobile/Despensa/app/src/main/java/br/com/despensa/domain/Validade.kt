package br.com.despensa.domain

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Semáforo de validade (RF-002 / RN-001 / RN-002). Puro — sem dependência Android. */
enum class StatusValidade {
    VENCIDO,
    ATENCAO,
    FRESCO,
    SEM_VALIDADE
}

/**
 * VENCIDO se validade < hoje 00h; ATENÇÃO se vence em ≤3 dias;
 * FRESCO no resto; SEM_VALIDADE se null.
 */
fun statusValidade(validadeEpochMillis: Long?, agora: Long): StatusValidade {
    if (validadeEpochMillis == null) return StatusValidade.SEM_VALIDADE
    val zona = ZoneId.systemDefault()
    val hoje = Instant.ofEpochMilli(agora).atZone(zona).toLocalDate()
    val hoje00h = hoje.atStartOfDay(zona).toInstant().toEpochMilli()
    if (validadeEpochMillis < hoje00h) return StatusValidade.VENCIDO
    val dataValidade = Instant.ofEpochMilli(validadeEpochMillis).atZone(zona).toLocalDate()
    val dias = ChronoUnit.DAYS.between(hoje, dataValidade)
    return if (dias <= 3) StatusValidade.ATENCAO else StatusValidade.FRESCO
}

/** Dias (data − hoje) até vencer; null quando sem validade. Negativo = vencido há N dias. */
fun diasParaVencer(validadeEpochMillis: Long?, agora: Long): Int? {
    if (validadeEpochMillis == null) return null
    val zona = ZoneId.systemDefault()
    val hoje = Instant.ofEpochMilli(agora).atZone(zona).toLocalDate()
    val dataValidade = Instant.ofEpochMilli(validadeEpochMillis).atZone(zona).toLocalDate()
    return ChronoUnit.DAYS.between(hoje, dataValidade).toInt()
}
