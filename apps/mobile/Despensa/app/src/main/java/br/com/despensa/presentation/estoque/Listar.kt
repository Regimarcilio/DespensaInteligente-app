package br.com.despensa.presentation.estoque

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.domain.StatusValidade
import br.com.despensa.domain.chaveLista
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.statusValidade
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.LaranjaQueimada
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.corFundoSelo
import br.com.despensa.presentation.theme.corTextoSelo
import br.com.despensa.presentation.theme.sombraFeira

/**
 * Issue #16 (puro, testável em JVM): subtítulo fixo de utilidade do card
 * "Listar" + descrição TalkBack completa. Texto único p/ visível e
 * acessibilidade — sem mudar comportamento/navegação do card.
 */
const val SUBTITULO_LISTAR = "Escolha itens do estoque para a Lista de Compras"

fun descricaoCardListar(totalItens: Int): String =
    "Listar. $SUBTITULO_LISTAR. " +
        "$totalItens ${if (totalItens == 1) "item no estoque" else "itens no estoque"}. " +
        "Toque no mais para montar a Lista de Compras."

/**
 * Issue #10 (spec aceita 11/10): card verde "Listar" abaixo do Controle de
 * Frescor, com botão "+" no mesmo padrão do card acima. Tocar no "+" abre a
 * nova tela com os itens em blocos; adicionar lá coloca na Lista de Compras.
 * Sem telas sobrepostas (tela dedicada, não diálogo) e sem mudar RN-003.
 */
@Composable
fun CardListar(
    totalItens: Int,
    escuro: Boolean,
    onAbrir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(TurquesaProfunda, Color(0xFF0E7C6B))
                ),
                RoundedCornerShape(28.dp)
            )
            .then(
                if (escuro) Modifier.border(
                    1.dp,
                    MentaEcom.copy(alpha = 0.25f),
                    RoundedCornerShape(28.dp)
                ) else Modifier
            )
            .clickable(onClickLabel = "Abrir tela Listar com itens em blocos") { onAbrir() }
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = descricaoCardListar(totalItens)
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Listar",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1
                )
                // Issue #16: subtítulo fixo de utilidade (visível + TalkBack) —
                // sem mudar comportamento/navegação do card.
                Text(
                    SUBTITULO_LISTAR,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "$totalItens ${if (totalItens == 1) "item no estoque" else "itens no estoque"} • toque no +",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.80f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            // "+" no padrão verde do card acima: círculo branco sobre o teal
            // (espelho do FAB teal-sobre-claro), 48dp, TalkBack nomeado.
            Surface(
                shape = CircleShape,
                color = Color.White,
                contentColor = TurquesaProfunda,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        onClick(label = "Abrir Listar", action = null)
                    }
                    .clickable(onClickLabel = "Abrir Listar") { onAbrir() }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Abrir Listar",
                    tint = TurquesaProfunda,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * Nova tela dedicada (não diálogo, não overlay): itens do Estoque em blocos
 * com dados expostos (nome, qtd, local • preço, pílula de validade + data).
 * Cada bloco tem "Adicionar" → entra na Lista de Compras (via MANUAL).
 */
@Composable
fun ListarScreen(
    itens: List<EstoqueEntity>,
    nomesNaLista: Set<String>,
    escuro: Boolean = false,
    onAdicionar: (EstoqueEntity) -> Unit,
    onVoltar: () -> Unit
) {
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
                    contentDescription = "Voltar ao Estoque"
                }
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar ao Estoque")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Listar • ${itens.size} ${if (itens.size == 1) "item" else "itens"}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Toque em Adicionar para pôr na Lista de Compras",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (itens.isEmpty()) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    "Nada para listar",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Cadastre itens no Estoque para montar sua Lista de Compras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itens, key = { it.id }) { item ->
                    BlocoListar(
                        item = item,
                        jaNaLista = nomesNaLista.contains(chaveLista(item.nome)),
                        escuro = escuro,
                        onAdicionar = { onAdicionar(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlocoListar(
    item: EstoqueEntity,
    jaNaLista: Boolean,
    escuro: Boolean,
    onAdicionar: () -> Unit
) {
    val agora = System.currentTimeMillis()
    val status = statusValidade(item.validade, agora)
    val dias = diasParaVencer(item.validade, agora)
    val textoAmigavel = formatarValidadeAmigavel(dias)
    val iconeSelo = when (status) {
        StatusValidade.VENCIDO -> Icons.Filled.Warning
        StatusValidade.ATENCAO -> Icons.Filled.WarningAmber
        StatusValidade.FRESCO -> Icons.Filled.CheckCircle
        StatusValidade.SEM_VALIDADE -> Icons.Filled.Info
    }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sombraFeira(escuro, RoundedCornerShape(24.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Item ${item.nome}, ${formatarQtdListar(item.quantidade)} ${item.unidade}. $textoAmigavel."
            },
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.nome,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${formatarQtdListar(item.quantidade)} ${item.unidade}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (escuro) AmbarEcom else LaranjaQueimada,
                    maxLines = 1
                )
            }
            Text(
                buildString {
                    append("${emojiLocal(item.local)} ${rotuloLocalListar(item.local)}")
                    val preco = item.precoRef
                    if (preco != null && preco > 0) {
                        append(" • R$ ${formatarReais(preco)}/${item.unidade}")
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = corFundoSelo(status, escuro),
                    contentColor = corTextoSelo(status, escuro),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            iconeSelo,
                            contentDescription = null,
                            tint = corTextoSelo(status, escuro),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            textoAmigavel,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                if (item.validade != null) {
                    Text(
                        epochParaBr(item.validade),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Button(
                onClick = onAdicionar,
                enabled = !jaNaLista,
                modifier = Modifier.fillMaxWidth().semantics {
                    onClick(
                        label = if (jaNaLista) "${item.nome} já está na lista" else "Adicionar ${item.nome} à Lista de Compras",
                        action = null
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TurquesaProfunda,
                    contentColor = Color.White,
                    disabledContainerColor = TurquesaProfunda.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                if (jaNaLista) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Na lista ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun formatarQtdListar(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

private fun rotuloLocalListar(local: br.com.despensa.data.local.entity.LocalEstoque): String =
    when (local) {
        br.com.despensa.data.local.entity.LocalEstoque.GELADEIRA -> "Geladeira"
        br.com.despensa.data.local.entity.LocalEstoque.FREEZER -> "Freezer"
        br.com.despensa.data.local.entity.LocalEstoque.DESPENSA -> "Despensa"
    }
