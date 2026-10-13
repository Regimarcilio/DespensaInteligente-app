package br.com.despensa.presentation.lista

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.despensa.domain.ItemCarrinho
import br.com.despensa.domain.descricaoItemCarrinho
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.formatarQtdCarrinho
import br.com.despensa.domain.formatarReais
import br.com.despensa.presentation.estoque.epochParaBr
import br.com.despensa.presentation.estoque.formatarValidadeAmigavel
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.NoiteEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.sombraFeira

/**
 * Issue #36 (modo supermercado): TELA PRÓPRIA do carrinho — via ÍCONE no
 * header da Lista (não nova aba, não seção; mesmo padrão Listar #10/#18:
 * substitui o conteúdo, Voltar fecha sem perder estado). Lista o carrinho;
 * "Finalizar compra" com seleção dos comprados → entram no Estoque como
 * disponíveis (upsert existente, sem regra nova de validade).
 * RN-003/exportação/pagamento intactos (não toca na Lista).
 */
@Composable
fun CarrinhoScreen(
    itens: List<ItemCarrinho>,
    escuro: Boolean = false,
    onVoltar: () -> Unit,
    onAlternar: (String) -> Unit = {},
    onRemover: (String) -> Unit = {},
    onFinalizar: () -> Unit = {}
) {
    // Mesmo padrão Listar (#18): voltar físico/gesto volta à Lista.
    BackHandler(onBack = onVoltar)
    val comprados = itens.count { it.comprado }
    val verde = if (escuro) MentaEcom else TurquesaProfunda
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.semantics {
                    contentDescription = "Voltar à Lista Compras"
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar à Lista Compras")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Carrinho • $comprados ${if (comprados == 1) "comprado" else "comprados"}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Marque os comprados e finalize no Estoque",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (itens.isEmpty()) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    "Carrinho vazio",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Toque no carrinho em cada item da Lista e salve na edição padrão.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itens, key = { it.id }) { item ->
                    BlocoCarrinho(
                        item = item,
                        escuro = escuro,
                        onAlternar = { onAlternar(item.id) },
                        onRemover = { onRemover(item.id) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            // CTA único da tela, sempre LaranjaEcom (maré, regra 3 — mesmo
            // padrão do "Gerar texto" da Lista). Disabled com texto-guia.
            Button(
                onClick = onFinalizar,
                enabled = comprados > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjaEcom,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = LaranjaEcom.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    if (comprados > 0) "Finalizar compra ($comprados)" else "Finalizar compra",
                    fontWeight = FontWeight.Bold
                )
            }
            if (comprados == 0) {
                Text(
                    "Marque os comprados ↑",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BlocoCarrinho(
    item: ItemCarrinho,
    escuro: Boolean,
    onAlternar: () -> Unit,
    onRemover: () -> Unit
) {
    val verde = if (escuro) MentaEcom else TurquesaProfunda
    val agora = System.currentTimeMillis()
    val textoValidade = formatarValidadeAmigavel(diasParaVencer(item.validade, agora))
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sombraFeira(escuro, RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = false) {
                contentDescription = descricaoItemCarrinho(item)
            },
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { onAlternar() },
                modifier = Modifier.semantics {
                    onClick(label = "Alternar comprado de ${item.nome}", action = null)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = verde,
                    checkmarkColor = if (escuro) NoiteEcom else androidx.compose.ui.graphics.Color.White
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.nome,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "📦 ${formatarQtdCarrinho(item.quantidade)} ${item.unidade}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.precoRef?.takeIf { it > 0 }?.let {
                    Text(
                        "R$ ${formatarReais(it)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = verde,
                        maxLines = 1
                    )
                }
                Text(
                    if (item.validade != null) "$textoValidade • ${epochParaBr(item.validade)}"
                    else textoValidade,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onRemover,
                modifier = Modifier
                    .size(36.dp)
                    .semantics {
                        onClick(label = "Remover ${item.nome} do carrinho", action = null)
                    }
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remover ${item.nome} do carrinho",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}
