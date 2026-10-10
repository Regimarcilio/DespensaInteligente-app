package br.com.despensa.presentation.header

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.NevoaTurquesa
import br.com.despensa.presentation.theme.RosaQueimadoFundo
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.VermelhoNoite
import br.com.despensa.presentation.theme.VermelhoQueimado

/**
 * Pílula de status do header (port de Navbar.tsx web 5d974e1, issue #6).
 *
 * Regra (idêntica à web): vencidos>0 → vermelha pulsante
 * "N vencido(s)"; senão atenção>0 → âmbar "N a vencer";
 * senão → turquesa "Estoque Seguro".
 *
 * Contagem reaproveita statusValidade (puro) no ViewModel; aqui só UI.
 * Pulso = animate alpha infinito 800ms (desligado com calmo / reduce-motion).
 * Claro+escuro; TalkBack via contentDescription.
 */
@Composable
fun PilulaStatus(
    vencidos: Int,
    atencao: Int,
    escuro: Boolean,
    calmo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val descricao = when {
        vencidos > 0 -> "$vencidos ${if (vencidos == 1) "item vencido" else "itens vencidos"} no estoque"
        atencao > 0 -> "$atencao ${if (atencao == 1) "item vence" else "itens vencem"} em breve"
        else -> "Estoque seguro, nenhum item vencido"
    }
    val texto = when {
        vencidos > 0 -> "$vencidos ${if (vencidos == 1) "vencido" else "vencidos"}"
        atencao > 0 -> "$atencao a vencer"
        else -> "Estoque Seguro"
    }
    val fundo: Color
    val tinta: Color
    val borda: Color
    val icone: ImageVector
    val pulsante: Boolean
    when {
        vencidos > 0 -> {
            fundo = if (escuro) VermelhoNoite.copy(alpha = 0.20f) else RosaQueimadoFundo
            tinta = if (escuro) VermelhoNoite else VermelhoQueimado
            borda = if (escuro) VermelhoNoite.copy(alpha = 0.40f) else VermelhoQueimado.copy(alpha = 0.35f)
            icone = Icons.Filled.Warning
            pulsante = true
        }
        atencao > 0 -> {
            fundo = if (escuro) AmbarEcom.copy(alpha = 0.20f) else Color(0xFFFFF3D6)
            tinta = if (escuro) AmbarEcom else Color(0xFF8A5A00)
            borda = if (escuro) AmbarEcom.copy(alpha = 0.40f) else AmbarEcom.copy(alpha = 0.55f)
            icone = Icons.Filled.WarningAmber
            pulsante = false
        }
        else -> {
            fundo = if (escuro) MentaEcom.copy(alpha = 0.15f) else NevoaTurquesa
            tinta = if (escuro) MentaEcom else TurquesaProfunda
            borda = Color.Transparent
            icone = Icons.Filled.CheckCircle
            pulsante = false
        }
    }

    var mod = modifier.semantics { contentDescription = descricao }
    if (pulsante && !calmo) {
        val transicao = rememberInfiniteTransition(label = "pilula-pulso")
        val a by transicao.animateFloat(
            initialValue = 1f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(Dur.P800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pilula-alpha"
        )
        mod = mod.alpha(a)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = mod
            .background(fundo, CircleShape)
            .border(1.dp, borda, CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = tinta,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            texto,
            color = tinta,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
