package br.com.despensa.notif

/**
 * Texto da notificação-resumo de validade (RF-002/US04). Puro — sem dependência Android.
 * Regras: 0 itens → null (não notifica); 1 → singular; N → plural; lista até 5 + linha "+ N outros".
 */
data class ItemVencendo(val nome: String, val dias: Int)

data class ResumoValidade(val titulo: String, val linhas: List<String>)

fun fraseDias(dias: Int): String = when {
    dias < 0 -> "venceu há ${-dias}d"
    dias == 0 -> "vence hoje"
    dias == 1 -> "vence amanhã"
    else -> "vence em ${dias}d"
}

fun resumoValidade(itens: List<ItemVencendo>): ResumoValidade? {
    if (itens.isEmpty()) return null
    val titulo = if (itens.size == 1) "🧊 1 item vencendo" else "🧊 ${itens.size} itens vencendo"
    val linhas = itens.take(5).map { "• ${it.nome} — ${fraseDias(it.dias)}" }.toMutableList()
    if (itens.size > 5) linhas += "+ ${itens.size - 5} outros"
    return ResumoValidade(titulo, linhas)
}
