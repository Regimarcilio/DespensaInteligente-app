package br.com.despensa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.despensa.data.local.prefs.ModoTema
import br.com.despensa.data.local.prefs.MODOS_TEMA_VISIVEIS
import br.com.despensa.data.local.prefs.TemaPrefs
import br.com.despensa.data.local.prefs.migrarModoTema
import br.com.despensa.domain.formatarReais
import br.com.despensa.presentation.economia.EconomiaScreen
import br.com.despensa.presentation.economia.EconomiaViewModel
import br.com.despensa.presentation.estoque.EstoqueScreen
import br.com.despensa.presentation.estoque.EstoqueViewModel
import br.com.despensa.presentation.header.Logo
import br.com.despensa.presentation.header.PilulaStatus
import br.com.despensa.presentation.lista.ListaScreen
import br.com.despensa.presentation.lista.ListaViewModel
import br.com.despensa.presentation.notif.PedidoPermissaoNotificacao
import br.com.despensa.presentation.sobre.DialogoSobre
import br.com.despensa.presentation.theme.DespensaTheme
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.fundoFeira
import br.com.despensa.presentation.theme.lembrarMovimentoReduzido
import br.com.despensa.presentation.theme.specCalma
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

private enum class Aba(val rotulo: String, val icone: androidx.compose.ui.graphics.vector.ImageVector) {
    ESTOQUE("Estoque", Icons.Filled.Inventory),
    // Paridade web v2.0 (BottomNav): "Lista Compras" e "Economia R$".
    LISTA("Lista Compras", Icons.Filled.ShoppingCart),
    ECONOMIA("Economia R$", Icons.Filled.Savings)
}

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var temaPrefs: TemaPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Paleta v4 §b: modo de tema como OPÇÃO (DataStore "modo_tema").
            // Issue #22: SOMENTE Claro/Noturno; o prefs já migra o legado
            // SISTEMA → ESCURO, e aqui normaliza por defesa (cintura dupla).
            // Telas recebem `escuro` por parâmetro.
            val modoBruto by temaPrefs.modoTema.collectAsState(initial = ModoTema.ESCURO)
            val modo = migrarModoTema(modoBruto)
            val escuro = modo == ModoTema.ESCURO
            DespensaTheme(escuro = escuro) {
                // Sprint 2 (RF-002): rationale próprio + pedido do sistema na 1ª abertura.
                PedidoPermissaoNotificacao()
                var aba by rememberSaveable { mutableStateOf(Aba.ESTOQUE) }
                var mostrarDialogoTema by rememberSaveable { mutableStateOf(false) }
                var mostrarSobre by rememberSaveable { mutableStateOf(false) }
                val vmEstoque: EstoqueViewModel = hiltViewModel()
                // Hoisted p/ o badge da aba Lista (mesmo owner = mesma instância
                // que o ListaScreen usa; sem mudar escopo ou regra).
                val vmLista: ListaViewModel = hiltViewModel()
                val itens by vmEstoque.itens.collectAsState()
                val contadores by vmEstoque.contadores.collectAsState()
                val sugestoesLista by vmLista.sugestoes.collectAsState()
                val calmo = lembrarMovimentoReduzido()
                val escopo = rememberCoroutineScope()

                // §(e) + issue #6 — header com marca Logo (port Logo.tsx web)
                // + pílula de status (port Navbar.tsx). Translúcido surface 95%
                // como aproximação do glass (sem blur real).
                // FIX header 360dp: subtítulo "N itens • R$ X" REMOVIDO da
                // TopAppBar — o corpo/hero já mostram contagem + valor.
                // Manter as 2 linhas no title estourava os 64dp do TopAppBar e
                // colidia com o divider ("Despe"/"INTELIGENT" cortados). Logo
                // compacto + pílula têm bounds fixos, sem AnimatedContent aqui.
                // UM título por tela: marca no header, contagem no corpo.
                val verdeNav = if (escuro) MentaEcom else TurquesaProfunda
                // Issue #22: rótulo curto do modo atual (só Claro/Escuro).
                val rotuloTema = if (modo == ModoTema.ESCURO) "Escuro" else "Claro"


                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    // FIX: só Logo compacto (bounds fixos 48dp,
                                    // maxLines=1 ellipsis). Sem subtítulo aqui.
                                    Logo(
                                        escuro = escuro,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                // §b.2 — ação de tema à direita do título.
                                actions = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PilulaStatus(
                                            vencidos = contadores.vencidos,
                                            atencao = contadores.atencao,
                                            escuro = escuro,
                                            calmo = calmo
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        // Paridade web v2.0 (exemplo issue #1): Sobre com
                                        // links repo + releases. PWA/GitHub-Sync OUT.
                                        // FIX 360dp: 40dp em vez de 48dp p/ sobrar
                                        // largura garantida ao título.
                                        IconButton(
                                            onClick = { mostrarSobre = true },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .semantics {
                                                    contentDescription =
                                                        "Sobre o app: versão e links do GitHub"
                                                }
                                        ) {
                                            Icon(
                                                Icons.Filled.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Box(contentAlignment = Alignment.Center) {
                                        IconButton(
                                            onClick = { mostrarDialogoTema = true },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .semantics {
                                                    contentDescription =
                                                        "Tema: $rotuloTema. Toque para mudar"
                                                }
                                        ) {
                                            Icon(
                                                Icons.Filled.Contrast,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    // issue #6: translúcido surface 95% como
                                    // aproximação do glass web (bg-[#FFFBF4]/95).
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            )
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            Aba.values().forEach { destino ->
                                val sel = aba == destino
                                NavigationBarItem(
                                    selected = sel,
                                    onClick = { aba = destino },
                                    icon = {
                                        // Paridade web v2.0 (badges BottomNav): vencidos
                                        // no Estoque (laranja) e pendentes na Lista.
                                        val badgeQtd = when (destino) {
                                            Aba.ESTOQUE -> contadores.vencidos
                                            Aba.LISTA -> sugestoesLista.size
                                            Aba.ECONOMIA -> 0
                                        }
                                        if (badgeQtd > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = if (destino == Aba.ESTOQUE) LaranjaEcom else verdeNav
                                                    ) { Text("$badgeQtd") }
                                                }
                                            ) {
                                                Icon(
                                                    destino.icone,
                                                    contentDescription = destino.rotulo
                                                )
                                            }
                                        } else {
                                            Icon(
                                                destino.icone,
                                                contentDescription = destino.rotulo
                                            )
                                        }
                                    },
                                    // E10: label acesa quando selecionada
                                    // (Bold 12sp turquesa-maré × Medium onSurfaceVariant).
                                    label = {
                                        Text(
                                            destino.rotulo,
                                            fontSize = 12.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (sel) verdeNav
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = verdeNav,
                                        // E10: pílula turquesa-maré 18%.
                                        indicatorColor = verdeNav.copy(alpha = 0.18f)
                                    )
                                )
                            }
                        }
                    }
                ) { pad ->
                    // §(d) — 1 fundo pontilhado por TELA (não por card), estático, custo ~0.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(pad)
                            .fundoFeira(escuro)
                    ) {
                        // E10 — troca de aba com crossfade (fade 200ms +
                        // slideHorizontal it/12, saída fade 150ms).
                        AnimatedContent(
                            targetState = aba,
                            transitionSpec = {
                                (fadeIn(tween(Dur.P200)) +
                                    slideInHorizontally(tween(Dur.P200)) { it / 12 }) togetherWith
                                    fadeOut(tween(Dur.P150))
                            },
                            label = "troca-aba"
                        ) { abaAtual ->
                            when (abaAtual) {
                                Aba.ESTOQUE -> EstoqueScreen(vmEstoque, escuro = escuro)
                                Aba.LISTA -> {
                                    ListaScreen(vmLista, escuro = escuro)
                                }
                                Aba.ECONOMIA -> {
                                    val vmEco: EconomiaViewModel = hiltViewModel()
                                    EconomiaScreen(vmEco, escuro = escuro)
                                }
                            }
                        }
                    }
                }

                // §b.2 — diálogo radio Sistema/Claro/Escuro (28dp, container surface).
                if (mostrarDialogoTema) {
                    DialogoTema(
                        modoAtual = modo,
                        calmo = calmo,
                        onEscolher = { escolhido ->
                            escopo.launch {
                                temaPrefs.definir(escolhido)
                                mostrarDialogoTema = false
                            }
                        },
                        onFechar = { mostrarDialogoTema = false }
                    )
                }

                // Paridade web v2.0 (diálogo Sobre, exemplo da issue #1).
                if (mostrarSobre) {
                    DialogoSobre(
                        escuro = escuro,
                        onFechar = { mostrarSobre = false }
                    )
                }
            }
        }
    }
}

/**
 * Seletor de tema (§07-paleta-v4 b.2, issue #22): AlertDialog 28dp, 2 linhas
 * RadioButton (Modo Claro + Modo Noturno, nomes mantidos) + texto. Toque
 * seleciona e fecha (sem OK); "Cancelar" ghost fecha sem mudar.
 * Título segue titleMedium Inter Bold da forma atual (forma congelada).
 */
@Composable
private fun DialogoTema(
    modoAtual: ModoTema,
    calmo: Boolean,
    onEscolher: (ModoTema) -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            // Paridade web v2.0 (ThemeDialog "Aparência do App" + X).
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Aparência do App",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFechar) {
                    Icon(Icons.Filled.Close, contentDescription = "Fechar")
                }
            }
        },
        text = {
            Column {
                // Issue #22: só as 2 opções atuais (SISTEMA fora da UI).
                MODOS_TEMA_VISIVEIS.forEach { opcao ->
                    val selecionado = opcao == modoAtual
                    // Seleção com animateColorAsState 250ms (forma congelada).
                    val corTexto by animateColorAsState(
                        targetValue = if (selecionado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        animationSpec = specCalma(tween(Dur.P250), calmo),
                        label = "tema-cor-${opcao.name}"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClickLabel = "Usar tema ${opcao.rotulo.lowercase()}"
                            ) { onEscolher(opcao) }
                            .semantics(mergeDescendants = true) {
                                stateDescription =
                                    if (selecionado) "Selecionado" else "Não selecionado"
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selecionado,
                            onClick = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                opcao.rotulo,
                                style = MaterialTheme.typography.bodyLarge
                                    .copy(fontWeight = FontWeight.Bold),
                                color = corTexto
                            )
                            Text(
                                opcao.descricao,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onFechar,
                modifier = Modifier.semantics {
                    contentDescription = "Cancelar, manter tema atual"
                }
            ) { Text("Cancelar") }
        }
    )
}
