package br.com.despensa.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Locale pt-BR fixo: texto gerado sempre com vírgula decimal (parser do ListaCompras aceita). */
private val PT_BR = Locale("pt", "BR")

/** Item da lista de reposição (US06). precoUnit null/≤0 = sem preço (sai R$ 0,00, parser aceita). */
data class ItemReposicao(
    val nome: String,
    val quantidade: Double,
    val unidade: String = "un",
    val precoUnit: Double? = null
)

/** Inteiro sem casa decimal; fracionado com toString (ponto — parser aceita ponto e vírgula). */
fun formatarQtdReposicao(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

/** "4,99" — sempre vírgula, independente do locale do aparelho (build determinístico). */
fun formatarReais(valor: Double): String =
    String.format(PT_BR, "%.2f", valor)

fun dataAtualReposicao(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", PT_BR).format(Date())

/**
 * Monta o texto de reposição no formato EXATO que o ListaCompras v2.0 importa
 * (ver ListaCompras `share/ImportarTexto.kt` + `ListaAtiva.montarTextoWhatsApp`):
 * `🛒 <nome> (<data>)`, itens `• <nome> — <qtd><un> x R$ <unit> = R$ <sub>`,
 * final `TOTAL: R$ <x>`. Pura e testável — [data] tem default = agora p/ a UI,
 * e é injetável nos testes p/ asserção exata.
 */
fun gerarTexto(
    nomeLista: String,
    itens: List<ItemReposicao>,
    data: String = dataAtualReposicao()
): String {
    val nome = nomeLista.trim().ifBlank { "Reposição Despensa" }
    val sb = StringBuilder("🛒 $nome ($data)\n")
    var total = 0.0
    itens
        .filter { it.nome.isNotBlank() && it.quantidade > 0 }
        .forEach { item ->
            val unidade = item.unidade.trim().ifBlank { "un" }
            val unit = item.precoUnit?.takeIf { it > 0 } ?: 0.0
            val sub = item.quantidade * unit
            total += sub
            sb.append(
                "• ${item.nome.trim()} — ${formatarQtdReposicao(item.quantidade)}$unidade" +
                    " x R$ ${formatarReais(unit)} = R$ ${formatarReais(sub)}\n"
            )
        }
    sb.append("TOTAL: R$ ${formatarReais(total)}")
    return sb.toString()
}
