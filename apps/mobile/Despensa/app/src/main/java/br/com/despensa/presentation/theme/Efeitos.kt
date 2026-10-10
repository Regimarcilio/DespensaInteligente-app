package br.com.despensa.presentation.theme

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Tokens de motion do Sistema Neon Vivo (docs/07-efeitos.md §3),
 * mantidos 100% na Feira (docs/07-estilo-v2.md §e) — a física continua.
 * Um único ponto de verdade: toda tela usa estes Dur — nada de ms mágicos.
 */
object Dur {
    const val P120 = 120
    const val P150 = 150
    const val P200 = 200
    const val P250 = 250
    const val P300 = 300
    const val P600 = 600
    const val P800 = 800
    const val P1200 = 1200
}

/**
 * E-contenção §2.3 — único ponto de verdade p/ reduce-motion.
 * Compose não expõe `prefers-reduced-motion`; o equivalente Android é duplo:
 * 1) "Remover animações" do sistema (ANIMATOR_DURATION_SCALE == 0) e
 * 2) TalkBack ativo (exploração por toque — animação concorre com foco sonoro).
 *
 * Uso: `val calmo = lembrarMovimentoReduzido()`
 * calmo == true → tweens/springs viram snap(), infinitos desligados,
 * shimmer vira skeleton estático, stagger some, pop some.
 */
@Composable
fun lembrarMovimentoReduzido(): Boolean {
    val ctx = LocalContext.current
    val semAnim = remember {
        runCatching {
            Settings.Global.getFloat(
                ctx.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
    val talkBack = remember {
        runCatching {
            val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            am.isTouchExplorationEnabled
        }.getOrDefault(false)
    }
    return semAnim || talkBack
}

/** E-contenção §2.3: calmo → snap imediato, senão a spec animada. */
fun <T> specCalma(spec: FiniteAnimationSpec<T>, calmo: Boolean): FiniteAnimationSpec<T> =
    if (calmo) snap() else spec

/**
 * E06 — press scale 0.97 universal (contenção: só graphicsLayer = caminho GPU).
 * O [inter] é o MESMO passado ao clickable/Button/Chip (interactionSource),
 * então o scale segue o pressionado real. Com calmo: sempre 1f (mantém ripple).
 */
@Composable
fun Modifier.escalaPress(inter: MutableInteractionSource, calmo: Boolean): Modifier {
    val pressionado by inter.collectIsPressedAsState()
    val s by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressionado && !calmo) 0.97f else 1f,
        animationSpec = specCalma(
            tween(Dur.P120, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            calmo
        ),
        label = "press-097"
    )
    return this.graphicsLayer(scaleX = s, scaleY = s)
}

/**
 * §(d) D1 — pontilhado de banca: grade de pontos TintaMaré 4%
 * (dark: NeveEcom 5%), raio 1dp, passo 24dp, só no fundo da tela
 * (1x por tela, nunca por card, estático, custo ~0).
 * Substitui os blobs radiais neon — mesma assinatura p/ diff mínimo.
 */
fun Modifier.fundoFeira(escuro: Boolean): Modifier = this.drawBehind {
    val ponto = if (escuro) NeveEcom.copy(alpha = 0.05f) else TintaMare.copy(alpha = 0.04f)
    val passo = 24.dp.toPx()
    val r = 1.dp.toPx()
    var y = 0f
    while (y < size.height) {
        var x = 0f
        while (x < size.width) {
            drawCircle(ponto, r, Offset(x, y))
            x += passo
        }
        y += passo
    }
}

/**
 * §(b) — sombra suave e quente (nunca dura deslocada).
 * Light: TintaMaré 8%/6% · Dark: Black 30%/20%. 1 shadow por card — permitido
 * porque é 1 camada estática, sem animação por frame.
 */
fun Modifier.sombraFeira(
    escuro: Boolean,
    forma: Shape = RoundedCornerShape(24.dp)
): Modifier = this.shadow(
    elevation = 16.dp,
    shape = forma,
    clip = false,
    ambientColor = if (escuro) Color.Black.copy(alpha = 0.30f) else TintaMare.copy(alpha = 0.08f),
    spotColor = if (escuro) Color.Black.copy(alpha = 0.20f) else TintaMare.copy(alpha = 0.06f)
)

/** E04 — atraso do stagger: 40ms por posição visível, teto 400ms. */
fun atrasoStagger(index: Int): Int = minOf(index * 40, 400)

/**
 * E08 — skeleton fantasma do card (24dp, thumb fantasma círculo 64dp).
 * Recolorido p/ maré v4 (§07-paleta-v4): base TintaMaré 6%/brilho 12% light
 * (dark: NeveEcom 8/18%). Shimmer só quando !calmo.
 * Contenção: o infinito vive NESTE escopo (skeleton), nunca no item da lista.
 */
@Composable
fun EsqueletoCard(
    modifier: Modifier = Modifier,
    calmo: Boolean = false,
    escuro: Boolean = false,
    descricao: String = "Carregando estoque"
) {
    val base = if (escuro) NeveEcom.copy(alpha = 0.08f) else TintaMare.copy(alpha = 0.06f)
    val brilho = if (escuro) NeveEcom.copy(alpha = 0.18f) else TintaMare.copy(alpha = 0.12f)

    val brush = if (calmo) {
        null
    } else {
        val fase by rememberInfiniteTransition(label = "shimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(Dur.P1200, easing = LinearEasing, delayMillis = 200),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer-fase"
        )
        // Faixa de 30% da largura varrendo o card (bytes escaneados = 0: é brush, não query).
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to base,
                (fase * 0.7f).coerceIn(0f, 1f) to base,
                ((fase * 0.7f) + 0.3f).coerceIn(0f, 1f) to brilho,
                1f to base
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush ?: Brush.linearGradient(listOf(base, base)),
                RoundedCornerShape(24.dp)
            )
            .semantics { contentDescription = descricao }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(base, CircleShape)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.fillMaxWidth(0.6f).height(8.dp).background(base, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth(0.85f).height(8.dp).background(base, RoundedCornerShape(4.dp)))
            Box(Modifier.fillMaxWidth(0.4f).height(8.dp).background(base, RoundedCornerShape(4.dp)))
        }
    }
}

/**
 * Anel de frescor do thumb 64dp (07-paleta-v4 §a.6): cor por status.
 * FRESCO TurquesaProfunda · ATENÇÃO AmbarEcom · VENCEU VermelhoQueimado · SEM PetroleoSec.
 * Dark: FRESCO MentaEcom · ATENÇÃO AmbarEcom · VENCEU VermelhoNoite · SEM MutedEcom.
 * (#0E9F8A VETADO como texto — permitido só como acento gráfico no anel 3dp;
 * aqui usamos os tokens de leitura TurquesaProfunda/MentaEcom.)
 */
@Composable
fun corAnelFrescor(status: br.com.despensa.domain.StatusValidade, escuro: Boolean): Color =
    when (status) {
        br.com.despensa.domain.StatusValidade.VENCIDO ->
            if (escuro) VermelhoNoite else VermelhoQueimado
        br.com.despensa.domain.StatusValidade.ATENCAO -> AmbarEcom
        br.com.despensa.domain.StatusValidade.FRESCO ->
            if (escuro) MentaEcom else TurquesaProfunda
        br.com.despensa.domain.StatusValidade.SEM_VALIDADE ->
            if (escuro) MutedEcom else PetroleoSec
    }

/** Cor de fundo do selo pílula por status (07-paleta-v4 §a.6). */
@Composable
fun corFundoSelo(status: br.com.despensa.domain.StatusValidade, escuro: Boolean): Color =
    when (status) {
        br.com.despensa.domain.StatusValidade.VENCIDO ->
            if (escuro) VermelhoNoite else VermelhoQueimado
        br.com.despensa.domain.StatusValidade.ATENCAO -> AmbarEcom
        br.com.despensa.domain.StatusValidade.FRESCO ->
            if (escuro) MentaEcom else NevoaTurquesa
        br.com.despensa.domain.StatusValidade.SEM_VALIDADE ->
            if (escuro) CartaoEcom else Nevoa
    }

/** Cor do texto do selo pílula por status (07-paleta-v4 §a.6). */
@Composable
fun corTextoSelo(status: br.com.despensa.domain.StatusValidade, escuro: Boolean): Color =
    when (status) {
        br.com.despensa.domain.StatusValidade.VENCIDO ->
            if (escuro) NoiteEcom else Color.White
        br.com.despensa.domain.StatusValidade.ATENCAO ->
            if (escuro) NoiteEcom else TintaMare
        br.com.despensa.domain.StatusValidade.FRESCO ->
            if (escuro) NoiteEcom else TurquesaProfunda
        br.com.despensa.domain.StatusValidade.SEM_VALIDADE ->
            if (escuro) MutedEcom else PetroleoSec
    }
