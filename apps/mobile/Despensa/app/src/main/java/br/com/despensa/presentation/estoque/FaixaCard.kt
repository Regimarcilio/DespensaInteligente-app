package br.com.despensa.presentation.estoque

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.sombraFeira

/**
 * Issue #16 (reaberta, spec visual final `docs/prototipos/inventario-26/T1-estoque.png`
 * — PREVALECE sobre descrições antigas): gramática única das faixas do Estoque
 * (hero Controle de Frescor + cards Listar/Inventário).
 *
 * - Mesmo raio 28dp e mesma faixa (gradiente teal TurquesaProfunda → 0xFF0E7C6B;
 *   borda MentaEcom 25% só no escuro).
 * - Blocos "+" idênticos 56dp/cantos 16dp ([VerdeBlocoMais] com "+" branco)
 *   sobrepondo a borda inferior (metade fora, no padrão do hero).
 * - O [Box] reserva 28dp abaixo da faixa: a metade de fora do "+" continua
 *   100% tocável (hit-test dentro dos bounds) e vira o respiro até o próximo
 *   card — o "+" nunca invade o card seguinte (image/6.jpg).
 * - Só UI: destinos/fluxos intactos (card e "+" chamam o mesmo [onAbrir]).
 */
val VerdeBlocoMais = Color(0xFF08493F)

/** Raio das faixas do Estoque (hero + cards). */
val RaioFaixaEstoque = RoundedCornerShape(28.dp)

/** Gradiente teal único das faixas (idêntico no light e no dark). */
val FundoFaixaEstoque = Brush.linearGradient(
    colors = listOf(TurquesaProfunda, Color(0xFF0E7C6B))
)

/** Quanto o bloco "+" desce sobre a borda inferior (metade dos 56dp). */
private val SaidaBlocoMais = 28.dp

/**
 * Bloco "+" sobreposto no padrão do hero/FAB: 56dp, cantos 16dp, fundo
 * [VerdeBlocoMais] com "+" branco, visível e tocável em light + dark.
 */
@Composable
fun BotaoMaisSobreposto(
    descricao: String,
    rotuloClique: String,
    onClick: () -> Unit,
    escuro: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = VerdeBlocoMais,
        contentColor = Color.White,
        modifier = modifier
            .size(56.dp)
            .sombraFeira(escuro, RoundedCornerShape(16.dp))
            .semantics {
                onClick(label = rotuloClique, action = null)
            }
            .clickable(onClickLabel = rotuloClique) { onClick() }
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = descricao,
            tint = Color.White,
            modifier = Modifier.padding(14.dp)
        )
    }
}

/**
 * Faixa verde do Estoque (cards Listar/Inventário, T1): à esquerda, ícone +
 * rótulo em linha única; à direita, [BotaoMaisSobreposto] sobre a borda
 * inferior. O conteúdo reserva 88dp à direita para o texto nunca correr por
 * baixo do "+". Utilidade/contagem vivem no TalkBack ([descricaoAcessivel]).
 */
@Composable
fun FaixaEstoque(
    icone: ImageVector,
    rotulo: String,
    escuro: Boolean,
    descricaoAcessivel: String,
    descricaoMais: String,
    rotuloAbrir: String,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = SaidaBlocoMais)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .background(FundoFaixaEstoque, RaioFaixaEstoque)
                .then(
                    if (escuro) Modifier.border(
                        1.dp,
                        MentaEcom.copy(alpha = 0.25f),
                        RaioFaixaEstoque
                    ) else Modifier
                )
                .clickable(onClickLabel = rotuloAbrir) { onAbrir() }
                .padding(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 88.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = descricaoAcessivel
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                rotulo,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        BotaoMaisSobreposto(
            descricao = descricaoMais,
            rotuloClique = rotuloAbrir,
            onClick = onAbrir,
            escuro = escuro,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
        )
    }
}
