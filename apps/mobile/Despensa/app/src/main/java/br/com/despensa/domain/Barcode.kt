package br.com.despensa.domain

/**
 * Leitor de código de barras — camada de domínio pura (issues #3, #8, #9).
 *
 * Regras sem Android/Room, 100% testáveis em JVM:
 * - [normalizarCodigo]: tira espaços, hífens e prefixos; mantém só dígitos.
 * - [validarEan13]: EAN-13 com dígito verificador módulo-10 (GS1).
 * - [ehCodigoValido]: aceita o que o ML Kit entrega no varejo BR
 *   (EAN-13, EAN-8 e UPC-A com dígito válido).
 * - [sugerirNomeCatalogo]: lookup LOCAL offline-first (issue #9), prioridade:
 *   1) aprendidos (EAN digitado/confirmado antes, via DataStore);
 *   2) nomes já cadastrados em memória que mencionam o código (legado #3);
 *   3) [SEMENTE_CATALOGO_EAN] (seed curado);
 *   4) o próprio código (desconhecido → usuário digita e é lembrado).
 * - [deveMostrarDialogoCadastro]: issue #8 — o AlertDialog de cadastro tem
 *   z-order acima do preview; com o scanner aberto ele deve ser ocultado para
 *   o preview ficar em tela cheia com a mira.
 *
 * OUT explícito (NÃO implementar aqui): busca online de produtos,
 * comparação de preços, sync nuvem.
 */

/** Debounce do analyzer: 1 leitura aceita por janela (bem abaixo dos 2s do AC #9a). */
const val JANELA_DEBOUNCE_SCAN_MS = 1500L

/**
 * Seed curado EAN → nome (issue #9b, offline-first, NFR-001 sem conta/rede).
 * Chaves normalizadas (só dígitos). Nomes editáveis: o que o usuário digitar
 * sobrescreve via aprendidos (DataStore) — o seed é só o chute inicial.
 */
val SEMENTE_CATALOGO_EAN: Map<String, String> = mapOf(
    "7894900011517" to "Guaraná Antarctica Lata 350ml",
    "7891000315507" to "Leite Ninho Integral 400g",
    "7894321713021" to "Arroz Tio João 5kg"
)

/** Só dígitos do que o scanner/UI entregou (`"7894 9000-1151 7"` → `"7894900011517"`). */
fun normalizarCodigo(bruto: String): String =
    bruto.filter { it.isDigit() }

/** Dígito verificador EAN-13 (módulo-10 GS1) para os 12 primeiros dígitos. */
fun digitoEan13(dozeDigitos: String): Int {
    require(dozeDigitos.length == 12 && dozeDigitos.all { it.isDigit() }) {
        "EAN-13 exige 12 dígitos para o cálculo"
    }
    var soma = 0
    dozeDigitos.forEachIndexed { i, c ->
        val d = c.digitToInt()
        soma += if (i % 2 == 0) d else d * 3
    }
    return (10 - (soma % 10)) % 10
}

/** EAN-13 válido: 13 dígitos + verificador GS1 confere. */
fun validarEan13(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 13) return false
    return runCatching {
        digitoEan13(digitos.substring(0, 12)) == digitos[12].digitToInt()
    }.getOrDefault(false)
}

/** EAN-8 válido: 8 dígitos + verificador (mesma regra, pesos invertidos). */
fun validarEan8(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 8) return false
    var soma = 0
    digitos.substring(0, 7).forEachIndexed { i, c ->
        val d = c.digitToInt()
        soma += if (i % 2 == 0) d * 3 else d
    }
    val esperado = (10 - (soma % 10)) % 10
    return runCatching { esperado == digitos[7].digitToInt() }.getOrDefault(false)
}

/** UPC-A válido: 12 dígitos; equivale ao EAN-13 com `0` à esquerda. */
fun validarUpcA(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 12) return false
    return validarEan13("0$digitos")
}

/** Aceito pelo scanner no varejo BR: EAN-13, EAN-8 ou UPC-A com dígito válido. */
fun ehCodigoValido(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    return when (digitos.length) {
        13 -> validarEan13(digitos)
        8 -> validarEan8(digitos)
        12 -> validarUpcA(digitos)
        else -> false
    }
}

/**
 * Lookup local-first com catálogo (issue #9):
 * aprendidos → nomes em memória → seed → próprio código.
 *
 * @param codigo código bruto do scanner (normalizado internamente).
 * @param nomesExistentes nomes dos itens em memória (vindos do ViewModel).
 * @param aprendidos EAN→nome aprendidos (DataStore, chaves normalizadas).
 * @param seed seed curado EAN→nome (default [SEMENTE_CATALOGO_EAN]).
 */
fun sugerirNomeCatalogo(
    codigo: String,
    nomesExistentes: List<String> = emptyList(),
    aprendidos: Map<String, String> = emptyMap(),
    seed: Map<String, String> = SEMENTE_CATALOGO_EAN
): String {
    val alvo = normalizarCodigo(codigo)
    if (alvo.isEmpty()) return codigo
    aprendidos[alvo]?.takeIf { it.isNotBlank() }?.let { return it }
    nomesExistentes.firstOrNull {
        normalizarCodigo(it) == alvo
    }?.let { return it }
    nomesExistentes.firstOrNull {
        it.contains(codigo.trim(), ignoreCase = true) || it.contains(alvo, ignoreCase = true)
    }?.let { return it }
    seed[alvo]?.let { return it }
    return codigo.trim()
}

/**
 * Lookup local-first legado (issue #3): mantido por compatibilidade, delega
 * para [sugerirNomeCatalogo] sem aprendidos/seed explícito.
 */
fun nomeSugeridoPara(codigo: String, nomesExistentes: List<String>): String =
    sugerirNomeCatalogo(codigo, nomesExistentes)

/**
 * Issue #8: o diálogo de cadastro (AlertDialog = janela própria, z-order acima)
 * deve ser ocultado enquanto o scanner está aberto, senão o menu fica por cima
 * do preview e bloqueia a mira. Pura para teste JVM do AC.
 */
fun deveMostrarDialogoCadastro(dialogoAberto: Boolean, escaneando: Boolean): Boolean =
    dialogoAberto && !escaneando
