package br.com.despensa.presentation.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.R
import br.com.despensa.presentation.theme.LaranjaEcom

/**
 * Marca oficial (issue #24): `image/logo.png` (cesta com compras + check em
 * squircle teal-escuro) como selo em todos os headers (MainActivity + Sobre),
 * no lugar do tile vetorial antigo (cesta branca + dot âmbar).
 * Wordmark "Despensa / INTELIGENTE" mantido (paleta v4 intacta).
 */
@Composable
fun Logo(
    modifier: Modifier = Modifier,
    escuro: Boolean = false
) {
    // FIX header 360dp: bounds fixos, sem sobreposição. Selo 40dp (logo
    // oficial, cantos 12dp ecoando o squircle) + coluna com weight(1f) para
    // o título truncar com ellipsis. Altura travada em 48dp para caber no
    // TopAppBar de 64dp sem colidir com o divider.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 48.dp)
            .semantics {
                contentDescription = "Despensa Inteligente"
            }
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                "Despensa",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "INTELIGENTE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.8.sp,
                    fontSize = 9.sp
                ),
                color = LaranjaEcom,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
