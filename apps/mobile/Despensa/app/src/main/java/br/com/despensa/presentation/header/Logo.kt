package br.com.despensa.presentation.header

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda

/**
 * Marca criativa (port de Logo.tsx web 5d974e1 p/ Compose, issue #6).
 *
 * Web: selo hexagonal turquesa em gradiente + folha gelo + spark âmbar
 * + wordmark "Despensa PRO / Inteligente".
 *
 * Nativo (paleta v4 MANTIDA, sem gradiente/glow — forma Feira):
 * selo arredondado 12dp TurquesaProfunda com ícone cesta branco +
 * wordmark "Despensa" (Fraunces Bold = titleLarge) + "INTELIGENTE"
 * em LaranjaEcom com tracking largo. Dot âmbar ecoa o badge web.
 */
@Composable
fun Logo(
    modifier: Modifier = Modifier,
    escuro: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = "Despensa Inteligente"
        }
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TurquesaProfunda, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ShoppingBasket,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Badge âmbar (eco do spark web + dot de override do tema).
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
                    .size(9.dp)
                    .background(LaranjaEcom, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                "Despensa",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                "INTELIGENTE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.2.sp,
                    fontSize = 9.sp
                ),
                color = LaranjaEcom,
                maxLines = 1
            )
        }
    }
}
