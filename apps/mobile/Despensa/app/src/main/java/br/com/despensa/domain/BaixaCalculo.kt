package br.com.despensa.domain

/** Cálculo do valor da baixa (RF-003/RF-005). Puro. Sem preço → 0.0 (nunca null, nunca crash). */
fun valorTotalBaixa(quantidade: Double, precoRef: Double?): Double {
    if (quantidade <= 0) return 0.0
    val preco = precoRef?.takeIf { it > 0 } ?: return 0.0
    return quantidade * preco
}
