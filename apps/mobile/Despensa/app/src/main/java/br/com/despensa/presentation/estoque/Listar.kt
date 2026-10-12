package br.com.despensa.presentation.estoque

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.domain.StatusValidade
import br.com.despensa.domain.chaveLista
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.statusValidade
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.BordaNoiteAzul
import br.com.despensa.presentation.theme.CartaoEcom
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.LaranjaQueimada
import br.com.despensa.presentation.theme.LinhaAzul
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.MutedEcom
import br.com.despensa.presentation.theme.NeveEcom
import br.com.despensa.presentation.theme.NevoaTurquesa
import br.com.despensa.presentation.theme.PetroleoSec
import br.com.despensa.presentation.theme.RosaQueimadoFundo
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.VermelhoNoite
import br.com.despensa.presentation.theme.VermelhoQueimado
import br.com.despensa.presentation.theme.corFundoSelo
import br.com.despensa.presentation.theme.corTextoSelo
import br.com.despensa.presentation.theme.escalaPress
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
 * Issue #16 (spec visual oficial 11/10, `image/5.png`): fundo do bloco "+"
 * da faixa Listar. Tom teal escuro para o "+" branco contrastar sobre o
 * gradiente do card — mesma dimensão do FAB/hero (56dp) e mesmos cantos
 * (16dp). Só UI, sem regra nova.
 */
private val VerdeBlocoMais = Color(0xFF08493F)

/**
 * Issue #10 (spec aceita 11/10) + Issue #16 (spec visual oficial 11/10,
 * `image/5.png` — PREVALECE): faixa verde "Listar" abaixo do Controle de
 * Frescor, no mesmo padrão do hero (gradiente + 28dp). À esquerda, ícone de
 * lista (☰) + "Listar" em linha única; à direita, bloco "+" verde-escuro de
 * 56dp/cantos 16dp idêntico ao "+" do hero (padrão do FAB). Tocar no card ou
 * no "+" abre a tela Listar (fluxo #10/#18) — destino/comportamento intactos.
 * Utilidade + contagem vivem no TalkBack via [descricaoCardListar].
 */
@Composable
fun CardListar(
    totalItens: Int,
    escuro: Boolean,
    onAbrir: () -> Unit
) {
    // Issue #16 (reabertura 12/10, image/6.jpg): altura mínima real 72dp
    // (56dp do "+" + 16dp de padding vertical) + padding vertical folgado —
    // mesmo sob medida apertada o texto nunca corta e o "+" nunca transborda.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
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
            .padding(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = descricaoCardListar(totalItens)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Issue #16 (5.png): ícone de lista ☰ branco à esquerda do rótulo
        // (nulo p/ TalkBack — o card já anuncia "Listar ..." via merge).
        Icon(
            Icons.Filled.FormatListBulleted,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Listar",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        // "+" no padrão do hero/FAB (56dp, cantos 16dp, sombra): bloco
        // verde-escuro com "+" branco, visível e tocável em light + dark.
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VerdeBlocoMais,
            contentColor = Color.White,
            modifier = Modifier
                .size(56.dp)
                .sombraFeira(escuro, RoundedCornerShape(16.dp))
                .semantics {
                    onClick(label = "Abrir Listar", action = null)
                }
                .clickable(onClickLabel = "Abrir Listar") { onAbrir() }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Abrir Listar",
                tint = Color.White,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

/**
 * Issue #18 (puro, testável em JVM): texto de confirmação da remoção no
 * Listar — sai do Estoque e da tela Listar (AC com confirmação).
 */
fun mensagemRemocaoListar(nome: String): String =
    "\"${nome.trim()}\" sai do Estoque e da tela Listar. Continuar?"

/**
 * Nova tela dedicada (não diálogo, não overlay): itens do Estoque em blocos
 * com dados expostos (nome, qtd, local • preço, pílula de validade + data).
 * Cada bloco tem "Adicionar" → entra na Lista de Compras (via MANUAL).
 *
 * Issue #25: busca + chips de status/locais vivem AQUI (port fiel do Estoque
 * — mesmas regras, contadores e semântica; o estado segue no
 * EstoqueViewModel, só mudou de lugar). Os itens exibidos já vêm filtrados;
 * o hero do Estoque abre esta tela já filtrada (toggle #15 mantido).
 */
@Composable
fun ListarScreen(
    itens: List<EstoqueEntity>,
    nomesNaLista: Set<String>,
    escuro: Boolean = false,
    onAdicionar: (EstoqueEntity) -> Unit,
    onVoltar: () -> Unit,
    // Issue #18: CRUD completo reaproveitando os fluxos do Estoque
    // (criar/editar via DialogoCadastro, baixa via darBaixa, remover no DAO).
    // Sem regra nova; destino segue sendo a Lista de Compras.
    onCriar: () -> Unit = {},
    onEditar: (EstoqueEntity) -> Unit = {},
    onConsumir: (EstoqueEntity) -> Unit = {},
    onDescartar: (EstoqueEntity) -> Unit = {},
    onRemover: (EstoqueEntity) -> Unit = {},
    // Issue #25: estado de filtro (hospedado no EstoqueViewModel).
    busca: String = "",
    filtroLocal: LocalEstoque? = null,
    filtroStatus: StatusValidade? = null,
    contadores: ContadoresValidade = ContadoresValidade(),
    totalBrutos: Int = itens.size,
    onBuscar: (String) -> Unit = {},
    onFiltrarLocal: (LocalEstoque?) -> Unit = {},
    onAlternarStatus: (StatusValidade?) -> Unit = {},
    onLimparStatus: () -> Unit = {},
    onLimparFiltros: () -> Unit = {},
    calmo: Boolean = false
) {
    // Issue #18: voltar físico/gesto volta ao Estoque sem perder estado
    // (mesmos ViewModels; só fecha a tela Listar).
    BackHandler(onBack = onVoltar)
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
            // Issue #18: criar reaproveita o DialogoCadastro do Estoque.
            IconButton(
                onClick = onCriar,
                modifier = Modifier.semantics {
                    contentDescription = "Criar item no estoque"
                    stateDescription = "Abre o cadastro do Estoque"
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Criar item no estoque")
            }
        }
        Spacer(Modifier.height(8.dp))
        // Issue #25: busca (port fiel do Estoque — mesma aparência e semântica).
        OutlinedTextField(
            value = busca,
            onValueChange = onBuscar,
            placeholder = { Text("Buscar no estoque...") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Buscar no estoque" },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (escuro) MentaEcom else TurquesaProfunda,
                unfocusedBorderColor = if (escuro) BordaNoiteAzul else LinhaAzul
            )
        )
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(LOCAIS_FILTRO) { (rotulo, local) ->
                // E06 — FilterChip pílula com press scale (interactionSource próprio).
                val interChip = remember(rotulo) { MutableInteractionSource() }
                val sel = filtroLocal == local
                val chipFundo = if (escuro) CartaoEcom else NeveEcom
                val chipBorda = if (escuro) BordaNoiteAzul else LinhaAzul
                FilterChip(
                    selected = sel,
                    onClick = { onFiltrarLocal(local) },
                    label = {
                        Text(
                            rotulo,
                            maxLines = 1,
                            color = if (sel) Color.White else if (escuro) MutedEcom else PetroleoSec
                        )
                    },
                    interactionSource = interChip,
                    modifier = Modifier
                        .escalaPress(interChip, calmo)
                        .semantics {
                            contentDescription = "Filtrar local: $rotulo"
                        },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurquesaProfunda,
                        selectedLabelColor = Color.White,
                        containerColor = chipFundo
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = sel,
                        borderColor = chipBorda,
                        selectedBorderColor = TurquesaProfunda,
                        disabledBorderColor = chipBorda,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    )
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // Filtro por status (port web 5d974e1): Todos/Frescos/Atenção/
        // Vencidos com contadores, combinando com busca + local.
        // Contadores vêm da lista observada (pós busca+local).
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FILTROS_STATUS) { (rotulo, status) ->
                val interChip = remember("status-$rotulo") { MutableInteractionSource() }
                val sel = filtroStatus == status
                val qtd = when (status) {
                    null -> totalBrutos
                    StatusValidade.FRESCO -> contadores.frescos
                    StatusValidade.ATENCAO -> contadores.atencao
                    StatusValidade.VENCIDO -> contadores.vencidos
                    // Issue #25: ramo morto herdado do Estoque (FILTROS_STATUS
                    // não tem SEM_VALIDADE) — 0 sem precisar da lista bruta.
                    StatusValidade.SEM_VALIDADE -> 0
                }
                val textoChip = "$rotulo ($qtd)"
                val chipFundo = if (escuro) CartaoEcom else NeveEcom
                val chipBorda = if (escuro) BordaNoiteAzul else LinhaAzul
                FilterChip(
                    selected = sel,
                    onClick = { onAlternarStatus(status) },
                    label = {
                        Text(
                            textoChip,
                            maxLines = 1,
                            color = if (sel) Color.White else if (escuro) MutedEcom else PetroleoSec
                        )
                    },
                    interactionSource = interChip,
                    modifier = Modifier
                        .escalaPress(interChip, calmo)
                        .semantics {
                            contentDescription = "Filtrar por status: $rotulo, $qtd itens"
                        },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurquesaProfunda,
                        selectedLabelColor = Color.White,
                        containerColor = chipFundo
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = sel,
                        borderColor = chipBorda,
                        selectedBorderColor = TurquesaProfunda,
                        disabledBorderColor = chipBorda,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    )
                )
            }
        }
        // Faixa do filtro ativo (eco do chip removível web em EstoqueView).
        if (filtroStatus != null) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${itens.size} ${if (itens.size == 1) "item" else "itens"} com filtro " +
                        when (filtroStatus) {
                            StatusValidade.FRESCO -> "FRESCOS"
                            StatusValidade.ATENCAO -> "ATENÇÃO"
                            StatusValidade.VENCIDO -> "VENCIDOS"
                            else -> ""
                        },
                    style = MaterialTheme.typography.labelMedium,
                    color = LaranjaEcom,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onLimparStatus,
                    modifier = Modifier.semantics {
                        contentDescription = "Limpar filtro de status"
                    }
                ) { Text("Limpar") }
            }
        }
        Spacer(Modifier.height(8.dp))
        val filtrando = busca.isNotBlank() || filtroLocal != null || filtroStatus != null
        if (itens.isEmpty()) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                if (filtrando) {
                    Text(
                        "Nenhum item encontrado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            busca.isNotBlank() || filtroLocal != null ->
                                "Tente ajustar os filtros de busca ou localização."
                            else ->
                                "Nenhum item neste status. Toque em Limpar."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = onLimparFiltros,
                        modifier = Modifier.semantics {
                            contentDescription = "Limpar todos os filtros"
                        }
                    ) { Text("Limpar filtros") }
                } else {
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
                        onAdicionar = { onAdicionar(item) },
                        onEditar = onEditar,
                        onConsumir = onConsumir,
                        onDescartar = onDescartar,
                        onRemover = onRemover
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
    onAdicionar: () -> Unit,
    onEditar: (EstoqueEntity) -> Unit = {},
    onConsumir: (EstoqueEntity) -> Unit = {},
    onDescartar: (EstoqueEntity) -> Unit = {},
    onRemover: (EstoqueEntity) -> Unit = {}
) {
    var confirmarRemover by remember { mutableStateOf(false) }
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
            // Issue #18: ações do bloco no mesmo padrão do EstoqueCard
            // (Consumir/Descartar + Editar + Remover com confirmação).
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val alturaAcao = Modifier.height(36.dp)
                Button(
                    onClick = { onConsumir(item) },
                    modifier = alturaAcao
                        .weight(1f)
                        .semantics {
                            onClick(label = "Consumir ${item.nome}", action = null)
                        },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (escuro) MentaEcom.copy(alpha = 0.15f)
                        else NevoaTurquesa,
                        contentColor = if (escuro) MentaEcom else TurquesaProfunda
                    )
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Consumir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { onDescartar(item) },
                    modifier = alturaAcao
                        .weight(1f)
                        .semantics {
                            onClick(label = "Descartar ${item.nome}", action = null)
                        },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (escuro) VermelhoNoite.copy(alpha = 0.20f)
                        else RosaQueimadoFundo,
                        contentColor = if (escuro) VermelhoNoite else VermelhoQueimado
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Descartar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = { onEditar(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Editar ${item.nome}", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar ${item.nome}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { confirmarRemover = true },
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Remover ${item.nome} do estoque", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remover ${item.nome} do estoque",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Issue #18 (AC): remover com confirmação — sai do Estoque e do Listar
    // (a lista observa os brutos do DAO; sem regra nova).
    if (confirmarRemover) {
        AlertDialog(
            onDismissRequest = { confirmarRemover = false },
            title = { Text("Remover \"${item.nome}\"?") },
            text = { Text(mensagemRemocaoListar(item.nome)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmarRemover = false; onRemover(item) },
                    modifier = Modifier.semantics {
                        onClick(label = "Confirmar remoção de ${item.nome}", action = null)
                    }
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRemover = false }) { Text("Cancelar") }
            }
        )
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
