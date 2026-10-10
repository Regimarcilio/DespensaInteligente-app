package br.com.despensa.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.despensa.R

// Tokens docs/07-paleta-v4.md — "Maré + PlacaCerta" (troca SOMENTE de cor sobre a forma Feira).
// FORMA v2 intacta: cantos 24/28/16dp, Fraunces Bold + Inter, sombras suaves, motion E03–E10.
// Light: fundo BrancoMare + surface NeveEcom · Dark: fundo NoiteEcom (nunca uva, nunca vinho).
// VETO: #0E9F8A NUNCA como texto/fill de leitura (3,21:1 ❌) — só acento gráfico grande
// (anel 3dp, ilustração). Leitura usa TurquesaProfunda #0B6E5F (5,65 ✅).
// VETO: #4361EE azul royal (storefront legado) NUNCA — link usa TurquesaProfunda/CianoEcom.
// VETO: #9AA4C7 (MutedEcom) NUNCA como texto no light (2,39 ❌) — só onSurfaceVariant dark.
// VETO: #FF5E3A ponta do grad NUNCA como fill com texto claro (3,04 ❌).
// CTA LaranjaEcom SEMPRE com tinta escura (TintaMare 5,60 ✅ / NoiteEcom 8,15 ✅);
// branco sobre laranja (2,36 ❌) proibido. Gradiente/Glow adiados (só flat #FF8A00).
val BrancoMare = Color(0xFFFFFBF4)
val Nevoa = Color(0xFFEAF3F3)
val TintaMare = Color(0xFF14343C)
val PetroleoSec = Color(0xFF47626E)
val TurquesaProfunda = Color(0xFF0B6E5F)
val NevoaTurquesa = Color(0xFFE0F2EE)
val LinhaAzul = Color(0xFFD8E6E8)
val VermelhoQueimado = Color(0xFFB91C1C)
val LaranjaQueimada = Color(0xFFC2410C)
val RosaQueimadoFundo = Color(0xFFFDECEC)

// v4 — língua PlacaCerta (e-commerce): gelo/âmbar/cta/noite/menta/ciano/trilho.
val NeveEcom = Color(0xFFF2F5FF)
val LaranjaEcom = Color(0xFFFF8A00)
val AmbarEcom = Color(0xFFFFB224)
val NoiteEcom = Color(0xFF0A0E1A)
val CartaoEcom = Color(0xFF131B36)
val MutedEcom = Color(0xFF9AA4C7)
val MentaEcom = Color(0xFF6EE7B7)
val CianoEcom = Color(0xFF38E1FF)
val TrilhoEcom = Color(0xFF0B1128)

// Dark: outline mantido (equivale à --line sobre navy).
val BordaNoiteAzul = Color(0xFF26414B)
val VermelhoNoite = Color(0xFFFCA5A5)

// §(c) — display serifada (Fraunces Bold) + corpo grotesca (Inter).
// TTFs estáticos subsetados em res/font/ (~179KB no APK).
val DisplayFeira = FontFamily(Font(R.font.fraunces_bold, FontWeight.Bold))
val CorpoFeira = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val TipografiaFeira = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFeira, fontWeight = FontWeight.Bold,
        fontSize = 52.sp, lineHeight = 56.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFeira, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 44.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFeira, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFeira, fontWeight = FontWeight.Bold,
        fontSize = 18.sp, lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CorpoFeira, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)

/** §(b) — "cesto, não placa": cards 24dp, hero/dialog 28dp, inputs 16dp. */
val FormasFeira = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

// Light — "maré clara + gelo ecom" (§a.3: primary TurquesaProfunda 5,65 ✅,
// secondary LaranjaEcom + TintaMare 5,60 ✅, tertiary AmbarEcom 7,34 ✅,
// surface NeveEcom 12,16 ✅, onSurfaceVariant PetroleoSec 5,95 ✅).
private val CoresClaras = lightColorScheme(
    primary = TurquesaProfunda,
    onPrimary = Color.White,
    primaryContainer = NevoaTurquesa,
    onPrimaryContainer = TurquesaProfunda,
    secondary = LaranjaEcom,
    onSecondary = TintaMare,
    tertiary = AmbarEcom,
    onTertiary = TintaMare,
    background = BrancoMare,
    onBackground = TintaMare,
    surface = NeveEcom,
    onSurface = TintaMare,
    surfaceVariant = Nevoa,
    onSurfaceVariant = PetroleoSec,
    outline = LinhaAzul,
    error = VermelhoQueimado,
    onError = Color.White
)

// Dark — "noite e-commerce" (§a.3: primary MentaEcom 12,63 ✅,
// secondary LaranjaEcom 8,15 ✅, tertiary AmbarEcom 10,68 ✅,
// background/surface NoiteEcom 17,68 ✅, surfaceVariant CartaoEcom 15,58 ✅,
// onSurfaceVariant MutedEcom 6,87 ✅).
private val CoresEscuras = darkColorScheme(
    primary = MentaEcom,
    onPrimary = NoiteEcom,
    primaryContainer = CartaoEcom,
    onPrimaryContainer = NeveEcom,
    secondary = LaranjaEcom,
    onSecondary = NoiteEcom,
    tertiary = AmbarEcom,
    onTertiary = NoiteEcom,
    background = NoiteEcom,
    onBackground = NeveEcom,
    surface = NoiteEcom,
    onSurface = NeveEcom,
    surfaceVariant = CartaoEcom,
    onSurfaceVariant = MutedEcom,
    outline = BordaNoiteAzul,
    error = VermelhoNoite,
    onError = NoiteEcom
)

@Composable
fun DespensaTheme(escuro: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (escuro) CoresEscuras else CoresClaras,
        typography = TipografiaFeira,
        shapes = FormasFeira,
        content = content
    )
}
