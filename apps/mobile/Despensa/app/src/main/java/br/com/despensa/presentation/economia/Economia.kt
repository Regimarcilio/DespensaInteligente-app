package br.com.despensa.presentation.economia

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.despensa.data.local.dao.BaixaDao
import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.data.local.entity.MotivoBaixa
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.inicioDoMes
import br.com.despensa.domain.proporcaoConsumida
import br.com.despensa.presentation.theme.Nevoa
import br.com.despensa.presentation.theme.CartaoEcom
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.VermelhoNoite
import br.com.despensa.presentation.theme.VermelhoQueimado
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.escalaPress
import br.com.despensa.presentation.theme.lembrarMovimentoReduzido
import br.com.despensa.presentation.theme.sombraFeira
import br.com.despensa.presentation.theme.specCalma
import br.com.despensa.share.BackupJson
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Tag de log defensivo da tela Economia (sem PII — só o fato + exceção). */
private const val TAG_ECONOMIA = "EconomiaVM"

/** Tela R$ (US07): salvos (CONSUMIDO no mês) vs perdidos (DESCARTADO no mês). */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EconomiaViewModel @Inject constructor(
    private val baixaDao: BaixaDao,
    private val estoqueDao: EstoqueDao
) : ViewModel() {
    /** Marco fixo na criação do VM: 1º dia do mês 00h (não recalcula a cada coleta). */
    private val marco: Long = inicioDoMes(System.currentTimeMillis())

    private val salvosFluxo: Flow<Double> = baixaDao.consumidosNoMes(marco)
    private val perdidosFluxo: Flow<Double> = baixaDao.perdidosNoMes(marco)

    val salvos: StateFlow<Double> = salvosFluxo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val perdidos: StateFlow<Double> = perdidosFluxo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Paridade web v2.0 (EconomiaView Histórico Recente + "Total acumulado
     * salvo"): fluxos SOMENTE-leitura sobre queries novas do [BaixaDao]
     * (sem migração, sem mudar as somas do mês).
     */
    val historico: StateFlow<List<BaixaEntity>> = baixaDao.observarTodas()
        .map { it.take(15) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salvosAcumulado: StateFlow<Double> = baixaDao.salvosTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Expõe o marco p/ testes de UI/preview (default = mês corrente). */
    fun marcoMes(): Long = marco

    /**
     * Issue #27: apaga o histórico recente (últimas baixas) com confirmação
     * da UI. Suspenso + Boolean como o save da #21: a UI só fecha a
     * confirmação no `true`; `false` mantém o diálogo com msg (nunca
     * silêncio). Não toca nas queries de cálculo (mês/acumulado seguem as
     * mesmas fórmulas — os totais apenas refletem as baixas restantes).
     */
    suspend fun limparHistorico(): Boolean =
        runCatching { baixaDao.limparTudo() }
            .onFailure { e -> Log.e(TAG_ECONOMIA, "limpar historico falhou", e) }
            .isSuccess

    /**
     * Issue #2: exporta estoque + baixas em JSON (puro via [BackupJson]).
     * Chamado da UI com o `exportedAt` ISO — IO fica com quem chama.
     */
    suspend fun exportar(exportedAt: String): String =
        BackupJson.export(estoqueDao.listarTudo(), baixaDao.listarTodas(), exportedAt)

    /**
     * Issue #2: importa backup (upsert por id — REPLACE — sem apagar o atual).
     * Retorna o resultado p/ a UI narrar quantos entraram e quantos falharam.
     */
    suspend fun importar(texto: String): BackupJson.ImportResult {
        val res = withContext(Dispatchers.IO) { BackupJson.import(texto) }
        res.itens.forEach { estoqueDao.upsert(it) }
        res.baixas.forEach { baixaDao.registrar(it) }
        return res
    }

    /**
     * Paridade web v2.0 ("Restaurar exemplos", storage/db.ts): reinsere os
     * 6 itens + 3 baixas de exemplo com validades relativas a HOJE (upsert por
     * id seed-w*, idempotente). Só INSERTs — sem migração, sem mudar regras.
     */
    suspend fun restaurarExemplos() {
        val dia = 24L * 60 * 60 * 1000
        val agora = System.currentTimeMillis()
        listOf(
            EstoqueEntity("seed-w1", "Leite Integral 1L", 2.0, "cx", agora + 2 * dia, LocalEstoque.GELADEIRA, null, 4.89, agora - 3 * dia),
            EstoqueEntity("seed-w2", "Iogurte Natural", 3.0, "pote", agora - 1 * dia, LocalEstoque.GELADEIRA, null, 3.5, agora - 10 * dia),
            EstoqueEntity("seed-w3", "Filé de Frango 1kg", 2.0, "pct", agora + 60 * dia, LocalEstoque.FREEZER, null, 19.9, agora - 5 * dia),
            EstoqueEntity("seed-w4", "Arroz Branco 5kg", 1.0, "pct", agora + 180 * dia, LocalEstoque.DESPENSA, null, 28.5, agora - 12 * dia),
            EstoqueEntity("seed-w5", "Tomate Italiano", 6.0, "un", agora + 1 * dia, LocalEstoque.GELADEIRA, null, 1.2, agora - 4 * dia),
            EstoqueEntity("seed-w6", "Azeite Extra Virgem 500ml", 1.0, "un", agora + 240 * dia, LocalEstoque.DESPENSA, null, 34.9, agora - 20 * dia)
        ).forEach { estoqueDao.upsert(it) }
        listOf(
            BaixaEntity("seed-wb1", "Manteiga com Sal 200g", 1.0, "pote", 11.5, MotivoBaixa.CONSUMIDO, agora - 2 * dia),
            BaixaEntity("seed-wb2", "Queijo Muçarela 300g", 1.0, "pct", 16.8, MotivoBaixa.CONSUMIDO, agora - 4 * dia),
            BaixaEntity("seed-wb3", "Pão de Forma", 1.0, "pct", 7.9, MotivoBaixa.DESCARTADO, agora - 7 * dia)
        ).forEach { baixaDao.registrar(it) }
    }
}

@Composable
fun EconomiaScreen(vm: EconomiaViewModel = hiltViewModel(), escuro: Boolean = false) {
    val salvos by vm.salvos.collectAsState()
    val perdidos by vm.perdidos.collectAsState()
    val historico by vm.historico.collectAsState()
    val acumulado by vm.salvosAcumulado.collectAsState()
    val proporcao = proporcaoConsumida(salvos, perdidos)
    val pctConsumido = (proporcao * 100).toInt()
    val temMovimentoMes = salvos + perdidos > 0
    // Maré v4 (§07-paleta-v4): hero turquesa/menta, perda vermelho-queimado — sem neon.
    // CTA LaranjaEcom SEMPRE fill grande com onSecondary = TintaMaré/NoiteEcom
    // (branco 2,36 ❌ proibido); laranja nunca como texto pequeno.
    val corHero = if (escuro) MentaEcom else TurquesaProfunda
    val corPerda = if (escuro) VermelhoNoite else VermelhoQueimado
    val corTrilho = if (escuro) CartaoEcom else Nevoa
    val corKnob = LaranjaEcom
    val corCta = LaranjaEcom
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    var statusBackup by remember { mutableStateOf<String?>(null) }
    var confirmarExemplos by remember { mutableStateOf(false) }
    // Issue #27: confirmação + anti-duplo-toque + erro inline da limpeza.
    var confirmarLimpeza by remember { mutableStateOf(false) }
    var limpandoHistorico by remember { mutableStateOf(false) }
    var erroLimpeza by remember { mutableStateOf<String?>(null) }
    val calmo = lembrarMovimentoReduzido()

    // E03 — count-up do hero: inteiros + centavos, mesma spec 800ms.
    // Reduce-motion: valor final direto (sem count-up).
    val alvoInt = salvos.toInt()
    val num by animateIntAsState(
        targetValue = alvoInt,
        animationSpec = specCalma(tween(Dur.P800, easing = FastOutSlowInEasing), calmo),
        label = "hero-count"
    )
    val centAlvo = (((salvos - alvoInt).coerceIn(0.0, 0.999)) * 100).toInt().coerceIn(0, 99)
    val cent by animateFloatAsState(
        targetValue = centAlvo.toFloat(),
        animationSpec = specCalma(tween(Dur.P800, easing = FastOutSlowInEasing), calmo),
        label = "hero-cent"
    )
    val exibido = if (calmo) salvos else num.toDouble() + (cent / 100.0)

    // E03 — bump a cada mudança de valor: 1.0 → 1.06 → 1.0, spring(300, 0.55).
    var bumpOn by remember { mutableStateOf(false) }
    LaunchedEffect(salvos) {
        if (!calmo) {
            bumpOn = true
            kotlinx.coroutines.delay(175)
            bumpOn = false
        }
    }
    val bump by animateFloatAsState(
        targetValue = if (bumpOn && !calmo) 1.06f else 1f,
        animationSpec = specCalma(spring(stiffness = 300f, dampingRatio = 0.55f), calmo),
        label = "hero-bump"
    )

    fun avisar(msg: String) {
        statusBackup = msg
        Toast.makeText(contexto, msg, Toast.LENGTH_LONG).show()
    }

    // Issue #2: SAF — CreateDocument p/ exportar, OpenDocument p/ importar.
    val exportarDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch(Dispatchers.IO) {
            val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale("pt", "BR")).format(Date())
            val json = vm.exportar(iso)
            runCatching {
                contexto.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray())
                } ?: throw IllegalStateException("SAF não abriu o destino")
            }.onSuccess {
                withContext(Dispatchers.Main) { avisar("Backup exportado ✅") }
            }.onFailure {
                withContext(Dispatchers.Main) { avisar("Falha ao exportar: ${it.message}") }
            }
        }
    }
    val importarDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch(Dispatchers.IO) {
            val texto = runCatching {
                contexto.contentResolver.openInputStream(uri)?.use { inn ->
                    inn.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("SAF não abriu o arquivo")
            }.getOrElse {
                withContext(Dispatchers.Main) { avisar("Falha ao ler arquivo: ${it.message}") }
                return@launch
            }
            val res = vm.importar(texto)
            withContext(Dispatchers.Main) {
                if (res.itens.isEmpty() && res.baixas.isEmpty() && res.erros.isNotEmpty()) {
                    avisar("Backup inválido: ${res.erros.first()}")
                } else {
                    val base = "Backup restaurado: ${res.itens.size} itens + ${res.baixas.size} baixas ✅"
                    avisar(if (res.erros.isEmpty()) base else "$base (${res.erros.size} com erro)")
                }
            }
        }
    }

    // E06 — press scale nos CTAs (interactionSource próprio por botão).
    val interExport = remember { MutableInteractionSource() }
    val interImport = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // Paridade web v2.0 (hero "Economia deste Mês" + selo "% aproveitado" /
        // "Sem dados ainda"). Selo em pílula calma, mesma forma dos demais.
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Você salvou em ${mesAtual()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = if (temMovimentoMes) corHero.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (temMovimentoMes) corHero
                else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            ) {
                Text(
                    if (!temMovimentoMes) "Sem dados ainda"
                    else "$pctConsumido% aproveitado",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    maxLines = 1
                )
            }
        }
        // E03 — HERO maré: preço de banca escrito à mão — Fraunces Bold 52/56
        // em TurquesaProfunda/MentaEcom + count-up + bump. Sem glow, sem radial.
        Text(
            "R$ ${formatarReais(exibido)}",
            style = MaterialTheme.typography.displayLarge,
            color = corHero,
            modifier = Modifier.graphicsLayer(scaleX = bump, scaleY = bump)
        )
        Text(
            "R$ ${formatarReais(perdidos)} perdidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = corPerda
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EconomiaCard(
                titulo = "CONSUMIDO",
                valor = salvos,
                cor = corHero,
                modifier = Modifier.weight(1f),
                calmo = calmo,
                escuro = escuro
            )
            EconomiaCard(
                titulo = "DESCARTADO",
                valor = perdidos,
                cor = corPerda,
                modifier = Modifier.weight(1f),
                calmo = calmo,
                escuro = escuro
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Consumido × descartado",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        // E09 — barra custom com física + marco (troca o LinearProgressIndicator cru).
        BarraConsumo(
            proporcao = proporcao,
            preenchimento = corHero,
            trilho = corTrilho,
            knob = corKnob,
            calmo = calmo,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        // E09 — label com AnimatedContent 300ms; empty mantém texto atual.
        AnimatedContent(
            targetState = if (salvos + perdidos <= 0) "empty"
            else "$pctConsumido% consumido · ${100 - pctConsumido}% descartado",
            transitionSpec = {
                androidx.compose.animation.fadeIn(specCalma(tween(Dur.P300), calmo)) togetherWith
                    androidx.compose.animation.fadeOut(specCalma(tween(Dur.P300), calmo))
            },
            label = "pct-consumido"
        ) { rotulo ->
            Text(
                if (rotulo == "empty") "Nada lançado este mês — dê baixa nos itens do estoque."
                else rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Paridade web v2.0 ("Total acumulado salvo"): soma CONSUMIDO sem
        // filtro de mês, abaixo da barra como na grade de stats da web.
        Text(
            "Total acumulado salvo: R$ ${formatarReais(acumulado)}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = corHero,
            modifier = Modifier.padding(top = 6.dp)
        )
        // Paridade web v2.0 (Histórico Recente, 15 últimas baixas): ✅/🗑️ +
        // data + qtd + +R$/-R$. Card 24dp/borda 1dp, mesma forma dos demais.
        Spacer(Modifier.height(16.dp))
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .sombraFeira(escuro, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        "Histórico Recente",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    // Issue #27: limpar o histórico (últimas baixas) com
                    // confirmação — sem tocar no cálculo de economia/total.
                    if (historico.isNotEmpty()) {
                        TextButton(
                            onClick = { erroLimpeza = null; confirmarLimpeza = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Limpar histórico recente"
                                onClick(label = "Limpar histórico recente", action = null)
                            }
                        ) { Text("Limpar") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (historico.isEmpty()) {
                    Text(
                        "Nenhuma movimentação registrada ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    historico.forEach { b ->
                        val consumido = b.motivo == MotivoBaixa.CONSUMIDO
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(if (consumido) "✅" else "🗑️")
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(
                                    b.nome,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    "${formatarDataCurta(b.ts)} • ${formatarQtdEco(b.quantidade)} ${b.unidade}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                (if (consumido) "+ " else "- ") + "R$ ${formatarReais(b.valorTotal)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (consumido) corHero else corPerda,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        // Issue #2: seção Backup (export/import via SAF, padrão ListaCompras).
        Spacer(Modifier.height(24.dp))
        Text(
            "Backup",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Salve tudo em JSON e restaure após reinstalar.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val iso = SimpleDateFormat("yyyyMMdd-HHmm", Locale("pt", "BR")).format(Date())
                    exportarDoc.launch("backup-despensa-$iso.json")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = corCta,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                interactionSource = interExport,
                modifier = Modifier
                    .weight(1f)
                    .escalaPress(interExport, calmo)
                    .semantics { onClick(label = "Exportar backup em JSON", action = null) }
            ) { Text("Exportar", fontWeight = FontWeight.Bold) }
            OutlinedButton(
                onClick = { importarDoc.launch(arrayOf("application/json")) },
                interactionSource = interImport,
                border = BorderStroke(1.dp, corHero),
                modifier = Modifier
                    .weight(1f)
                    .escalaPress(interImport, calmo)
                    .semantics { onClick(label = "Importar backup em JSON", action = null) }
            ) { Text("Importar", fontWeight = FontWeight.Bold, color = corHero) }
        }
        statusBackup?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Paridade web v2.0 ("Restaurar exemplos", storage/db.ts): reinsere os
        // dados de exemplo com confirmação. Sem GitHub-Sync (OUT da issue #1).
        TextButton(
            onClick = { confirmarExemplos = true },
            modifier = Modifier.semantics { onClick(label = "Restaurar alimentos e dados de exemplo", action = null) }
        ) { Text("Restaurar exemplos") }
        Spacer(Modifier.height(24.dp))
    }

    // Issue #27: confirmação antes de apagar — Cancelar não apaga nada
    // (AC2); falha mantém o diálogo com msg (nunca silêncio, padrão #21).
    // Após limpar, o card cai no estado vazio amigável (AC1).
    if (confirmarLimpeza) {
        AlertDialog(
            onDismissRequest = { if (!limpandoHistorico) confirmarLimpeza = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Limpar histórico recente") },
            text = {
                Column {
                    Text("Apagar as baixas exibidas? Os totais passam a refletir o histórico restante.")
                    erroLimpeza?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (limpandoHistorico) return@TextButton
                        limpandoHistorico = true
                        erroLimpeza = null
                        escopo.launch {
                            val ok = vm.limparHistorico()
                            limpandoHistorico = false
                            if (ok) {
                                confirmarLimpeza = false
                                avisar("Histórico apagado.")
                            } else {
                                erroLimpeza = "Não foi possível apagar. Tente de novo."
                            }
                        }
                    },
                    enabled = !limpandoHistorico,
                    modifier = Modifier.semantics {
                        onClick(label = "Confirmar apagar histórico", action = null)
                    }
                ) { Text(if (limpandoHistorico) "Apagando…" else "Apagar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpeza = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarExemplos) {
        AlertDialog(
            onDismissRequest = { confirmarExemplos = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Restaurar exemplos") },
            text = { Text("Deseja recarregar os alimentos e dados de exemplo?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExemplos = false
                    escopo.launch(Dispatchers.IO) {
                        vm.restaurarExemplos()
                        withContext(Dispatchers.Main) { avisar("Dados de exemplo recarregados.") }
                    }
                }) { Text("Restaurar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExemplos = false }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * E09 — barra Consumido × Descartado maré (§07-paleta-v4): altura 16dp, trilho neutro
 * (Nevoa/CartaoEcom), preenchimento TurquesaProfunda/MentaEcom (animate 600ms), cantos 8dp,
 * knob LaranjaEcom 12dp sólido sem halo. A perda vira número ("R$ perdidos"),
 * não cor de trilho — menos alarmista. Com total zerado, knob à esquerda.
 */
@Composable
private fun BarraConsumo(
    proporcao: Float,
    preenchimento: Color,
    trilho: Color,
    knob: Color,
    calmo: Boolean,
    modifier: Modifier = Modifier
) {
    val anim by animateFloatAsState(
        targetValue = proporcao.coerceIn(0f, 1f),
        animationSpec = specCalma(tween(Dur.P600, easing = FastOutSlowInEasing), calmo),
        label = "barra-consumo"
    )
    val pct = (anim * 100).toInt()
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .semantics { contentDescription = "$pct por cento consumido no mês" }
    ) {
        val w = size.width
        val h = size.height
        val raio = 8.dp.toPx()
        drawRoundRect(
            color = trilho,
            cornerRadius = CornerRadius(raio, raio),
            size = Size(w, h)
        )
        val larg = (w * anim).coerceIn(0f, w)
        if (larg > 1f) {
            drawRoundRect(
                color = preenchimento,
                topLeft = Offset.Zero,
                size = Size(larg, h),
                cornerRadius = CornerRadius(raio, raio)
            )
        }
        val kx = if (anim <= 0.02f) 12.dp.toPx() / 2 + 2.dp.toPx() else w * anim
        drawCircle(color = knob, radius = 12.dp.toPx() / 2, center = Offset(kx, h / 2))
    }
}

@Composable
private fun EconomiaCard(
    titulo: String,
    valor: Double,
    cor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    calmo: Boolean = false,
    escuro: Boolean = false
) {
    // E05 — card 24dp, borda 1dp outline, sombra suave quente + expansão com
    // spring(300, 0.7). Título e valor na mesma cor (turquesa ou vermelho-queimado).
    OutlinedCard(
        modifier = modifier
            .sombraFeira(escuro, RoundedCornerShape(24.dp))
            .animateContentSize(
                animationSpec = specCalma(
                    spring(
                        stiffness = 300f,
                        dampingRatio = 0.7f
                    ),
                    calmo
                )
            ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelSmall,
                color = cor
            )
            Text(
                "R$ ${formatarReais(valor)}",
                style = MaterialTheme.typography.titleLarge,
                color = cor
            )
        }
    }
}

private fun mesAtual(): String {
    val nome = java.text.SimpleDateFormat("MMMM", java.util.Locale("pt", "BR"))
        .format(java.util.Date())
    return nome.replaceFirstChar { it.uppercase() }
}

/** "25/12/2026" p/ o Histórico Recente (port de formatarDataBr da web). */
private fun formatarDataCurta(epoch: Long): String =
    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR")).format(java.util.Date(epoch))

/** Inteiro sem casa decimal (mesmo padrão dos cards de estoque/lista). */
private fun formatarQtdEco(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()
